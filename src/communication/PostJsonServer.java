package communication;


import communication.jsonObject.HummTempJsonObject;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;

public class PostJsonServer {

    public PostJsonServer(HummTempJsonObject hummtemp){

        String url = "http://10.0.116.47:8080/fruitBake_war_exploded/hardware/put/";
        String json = hummtemp.getJsonObject().toString();

        HttpPostWithJson(url, json);
    }

    private static String HttpPostWithJson(String url, String json) {
        String returnValue = "这是默认返回值，接口调用失败";
        try{
            //第一步：创建HttpClient对象
            CloseableHttpClient httpClient = HttpClients.createDefault();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            //第二步：创建httpPost对象
            HttpPost httpPost = new HttpPost(url);

            //第三步：给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json,"utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);

            //第四步：发送HttpPost请求，获取返回值
            returnValue = httpClient.execute(httpPost, responseHandler); //调接口获取返回值时，必须用此方法
            httpClient.close();
        }
        catch(Exception e)
        {
            System.out.println("发送异常");
            e.printStackTrace();
        }

        //第五步：处理返回值
        return returnValue;
    }

}

