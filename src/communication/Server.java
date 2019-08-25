package communication;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TooManyListenersException;

import serialport.utils.SettingUtils;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import serialport.manage.SerialPortManager;

public class Server {
	public ServerSocket serverSocket = null;
	public final int port = 8888;
	private static SerialPort serialPort;  //串口
	private String Illminition;            //光照
	private String Tem;                    //温度
	
	public Server(){
        //1. 实例化一个ServerSocket套接字对象，使用到端口号绑定本机IP地址作为唯一标识
        try {
            serverSocket = new ServerSocket(port,10,InetAddress.getByName(SettingUtils.localIp));
            System.out.println("-------------服务器初始化成功--------------------");
           
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void startService() {
		try {
		//2. 为 本地服务器套接字 绑定一个 远程套接字对象，并写入accept()方法，作为开始监听的标志
		//3. 建立面向长连接应用的TCP套接字，使用while(true)，作为长期监听的标志
		Socket socket = null;
		System.out.println("waiting....");
		while(true) {
			socket = serverSocket.accept();
			System.out.println("connect to " + socket.getInetAddress() + ":"+socket.getLocalPort());
			new ConnectThread(socket).start();
		}
		}catch (IOException e) {
			// TODO: handle exception
		}
	}
	
	
	class ConnectThread extends Thread{
		//4. 复写线程类的run()， 为保证服端务器多个口的监听应用，需要对一个服务套接字开一个线程
		//5. 跟据客户端请求，做出对应响应
			//5.1  控制位为"0",将最新传感器信息传送
			//5.2 控制位为"1",关闭此次连接
		Socket socket = null;  		
  		public ConnectThread(Socket socket) {
  			super();
  			this.socket=socket;
  		}  		
  		@Override
  		public void run() {
  			try {
  				DataInputStream dis = new DataInputStream(socket.getInputStream());
  				String control = dis.readUTF();
  				int i = 83;
  				if(control.equals("1")) {
	  				while(true) {
	  					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
	  					//InterAll interall = new DBAll();	// 取出数据库最新的一条数据
//	  					Temperill ti = new Temperill();
	  					//Temperill ti1 = interall.GetAllOne(i);
	  					i = i + 40;
//	  					ti.setIllumination(ti1.getIllumination());	//将数据封装进一个数据传输类对象
//	  					ti.setTemperture(ti1.getTemperture());
//	  					out.writeObject(ti);	//将封装好的数据发送出去
	  					Thread.sleep(2000);
	  				}
  				} else {
  					dis.close();
  				}
  			}catch (IOException e) {
  				System.out.println(socket.getInetAddress());
  			} catch (InterruptedException e) {
				e.printStackTrace();
			}
  		}
  	}

	  private static class SerialListener implements SerialPortEventListener {
	   /**
	     * 处理监控到的串口事件
	    */
	    public void serialEvent(SerialPortEvent serialPortEvent) {
	    	switch (serialPortEvent.getEventType()) {
	            case SerialPortEvent.BI: // 6 通讯中断
	                break;
                case SerialPortEvent.OE: // 5 溢位（溢出）错误
                	System.out.println("溢出");
                case SerialPortEvent.FE: // 4 帧错误
                	System.out.println("帧结构不完整");
                case SerialPortEvent.PE: // 3 奇偶校验错误
                	System.out.println("奇偶校验");
	            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
	                 break;
	            case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
	                 byte[] data = null;
	                 try {
	                     if (serialPort == null) {                    
	                    	 System.out.println("串口对象为空！监听失败！");
	                     } else {

							 // 读取串口数据
							 data = SerialPortManager.readMsg(serialPort);

	                     }
	                 } catch (Exception e) {
	                     // 发生读取错误时显示错误信息后退出系统
	                      System.exit(0);
	                   }
	                 break;
	            }
	        }
	    }
	public static void main(String[] args) throws IOException, TooManyListenersException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
		serialPort = SerialPortManager.openPort(SettingUtils.PortNum, SettingUtils.Bps);
	    SerialPortManager.addListener(serialPort, new SerialListener());
		new Server().startService();
	}
}
