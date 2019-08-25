package serialport.manage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TooManyListenersException;

import communication.PostJsonServer;
import communication.jsonObject.HummTempJsonObject;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import javafx.geometry.Pos;
import serialport.exception.*;
import serialport.utils.MyUtils;


public class SerialPortManager {
	private static float illuminationValue;
	private static boolean ifrared;
	private static float tempValue;
	private static float humnValue;
	

	//打开串口
	public static final SerialPort openPort(String portName, int baudrate)
			throws IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
			// 通过端口名识别端口
			CommPortIdentifier portIdentifier = CommPortIdentifier
					.getPortIdentifier(portName);
			// 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
			CommPort commPort = portIdentifier.open(portName, 2000);
			// 判断是不是串口
				SerialPort serialPort = (SerialPort) commPort;
					// 设置一下串口的波特率等参数
					serialPort.setSerialPortParams(baudrate,
							SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);

				return serialPort;
		 
	}

	//关闭串口
	public static void closePort(SerialPort serialPort) {
		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}

	//往串口发送数据
	public static void sendToPort(SerialPort serialPort, byte[] order)
			throws IOException {
		OutputStream out = null;
		out = serialPort.getOutputStream();
		out.write(order);
		out.flush();

		if (out != null) {
			out.close();
			out = null;
		}
	}

	//从串口读取数据  
	public static byte[] readMsg(SerialPort serialPort) {  
	    InputStream in = null;  
	    byte[] bytes = {};  
	    try {  
	        in = serialPort.getInputStream();  
	        // 缓冲区大小为一个字节  
	        byte[] readBuffer = new byte[1];  
	        int bytesNum = in.read(readBuffer);  
	        while (bytesNum > 0) {  
	            bytes = MyUtils.concat(bytes, readBuffer);  
	            bytesNum = in.read(readBuffer);  
	        }  
	    } catch (IOException e) {  
	        new ReadDataFromSerialPortFailure().printStackTrace();  
	    } finally { 
	        try {  
	            if (in != null) {  
	                in.close();  
	                in = null;  
	            }  
	        } catch (IOException e) {  
	            new SerialPortInputStreamCloseFailure().printStackTrace();  
	        }  
	    }
	    String str = MyUtils.byteArray2HexString(bytes, bytes.length, true);
	    String source = MyUtils.DistinguishSource(str);
        if (source.equals("illumination_01")) {
        	illuminationValue = MyUtils.HexStringToDataForIllum(str);
            System.out.println("光照：" + illuminationValue);

        } else if (source.equals("infrared_01")) {
        	ifrared = MyUtils.HexStringToDataForInfrared(str);
            System.out.println("人体红外：" + ifrared);

        } else if (source.equals("executor_01")){
            System.out.println("执行器：");

        } else if (source.equals("router_01")){
        	System.out.println("路由结点：");

		} else if (source.equals("tempHumm_01")){
			tempValue = MyUtils.HexStringToDataForTemp(str);
			humnValue = MyUtils.HexStringToDataForHumn(str);
			System.out.println("湿温度传感器01：");
			System.out.println("温度：" + tempValue + "; ");
			sendMsg(serialPort,"0");
			System.out.println("湿度：" + humnValue);

			//向服务器发送数据
			HummTempJsonObject hummtemp_01 = new HummTempJsonObject();
			hummtemp_01.Temp = tempValue;
			hummtemp_01.Humm = humnValue;
			hummtemp_01.date = hummtemp_01.simpleDateFormat.format(new Date());
			new PostJsonServer(hummtemp_01);

		}else if(source.equals("tempHumm_02")) {
			tempValue = MyUtils.HexStringToDataForTemp(str);
			humnValue = MyUtils.HexStringToDataForHumn(str);
			System.out.println("湿温度传感器02：");
			System.out.println("温度：" + tempValue + "; ");
			sendMsg(serialPort, "0");
			System.out.println("湿度：" + humnValue);

			//向服务器发送数据
			HummTempJsonObject hummtemp_02 = new HummTempJsonObject();
			hummtemp_02.Temp = tempValue;
			hummtemp_02.Humm = humnValue;
			hummtemp_02.date = hummtemp_02.simpleDateFormat.format(new Date());
			new PostJsonServer(hummtemp_02);
		}
        else {
        	System.out.println("无传感器数据读取");
		}
                 
        if(tempValue<=25.0&&illuminationValue>=400.0){
			sendMsg(serialPort,"0");                 //光照温度正常
		}else if(tempValue>25.0&&illuminationValue>=400.0){
			sendMsg(serialPort,"2");                 //温度过高 光照正常 开风扇
		}else if(illuminationValue<400.0&&tempValue<=25.0){
			sendMsg(serialPort,"4");                 //温度正常 光照过低 开灯
		}
		else {
			sendMsg(serialPort,"6");                 //温度过低 光照过低 开灯开风扇
		}
	    return bytes;
	}

	public static void sendMsg(SerialPort serialPort,String com){
		OutputStream out = null;
		String info="";
		String msg = "071800F1A3F501F"+com;//要发送的命令
		info="02"+msg+checkcode(msg);
		try {
			out = serialPort.getOutputStream();
			out.write(hex2byte(info));
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String Xor(String strHex_X,String strHex_Y){     //异或
		//将x、y转成二进制形式
		String anotherBinary=Integer.toBinaryString(Integer.valueOf(strHex_X,16));
		String thisBinary=Integer.toBinaryString(Integer.valueOf(strHex_Y,16));
		String result = "";
		//判断是否为8位二进制，否则左补零
		if(anotherBinary.length() != 8){
			for (int i = anotherBinary.length(); i <8; i++) {
				anotherBinary = "0"+anotherBinary;
			}
		}
		if(thisBinary.length() != 8){
			for (int i = thisBinary.length(); i <8; i++) {
				thisBinary = "0"+thisBinary;
			}
		}

		//异或运算
		for(int i=0;i<anotherBinary.length();i++){
			//如果相同位置数相同，则补0，否则补1
			if(thisBinary.charAt(i)==anotherBinary.charAt(i))
				result+="0";
			else{
				result+="1";
			}
		}
		return Integer.toHexString(Integer.parseInt(result, 2));
	}
	public static String checkcode(String para) {        //生成校验码
		int length = para.length() / 2;
		String[] dateArr = new String[length];
		for (int i = 0; i < length; i++) {
			dateArr[i] = para.substring(i * 2, i * 2 + 2);
		}
		String code = "00";
		for (int i = 0; i < dateArr.length; i++) {
			code = Xor(code, dateArr[i]);
		}
		return code.toUpperCase();                       //转换成大写
	}

	private static byte[] hex2byte(String hex1) {

		String digital = "0123456789ABCDEF";
//		String hex1 = hex.replace(" ", "");
		char[] hex2char = hex1.toCharArray();
		byte[] bytes = new byte[hex1.length() / 2];
		byte temp;
		for (int p = 0; p < bytes.length; p++) {
			temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
			temp += digital.indexOf(hex2char[2 * p + 1]);
			bytes[p] = (byte) (temp & 0xff);
		}
		return bytes;
	}


	public static void addListener(SerialPort port,
			SerialPortEventListener listener) throws IOException, TooManyListenersException {
		// 给串口添加监听器
		port.addEventListener(listener);
		// 设置当有数据到达时唤醒监听接收线程
		port.notifyOnDataAvailable(true);
		// 设置当通信中断时唤醒中断线程
		port.notifyOnBreakInterrupt(true);

	}
}
