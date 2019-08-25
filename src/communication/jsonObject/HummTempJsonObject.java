package communication.jsonObject;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HummTempJsonObject{

    //id
    public int Id = 0;
    public int Level = 0;

    //温度
    public float Temp = 0;

    //湿度
    public float Humm = 0;

    public String date;
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public JSONObject getJsonObject(){

        JSONObject obj = new JSONObject();
        obj.append("id", Id);
        obj.append("date", date);
        obj.append("temp", Temp);
        obj.append("humm", Humm);
        obj.append("level", Level);
        return obj;
    }
}
