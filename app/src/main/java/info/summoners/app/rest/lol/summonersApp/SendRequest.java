package info.summoners.app.rest.lol.summonersApp;

import android.net.http.AndroidHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

import info.summoners.app.rest.lol.util.LoLResponse;

// SendRequest.java

// A simple class with one static method, used to send a petition to the server.

public class SendRequest {

    public static LoLResponse get(String url) {
        HttpGet request = new HttpGet(url);
        HttpResponse response;
        Integer status;
        LoLResponse loLResponse;
        // New AndroidHttpClient.
        AndroidHttpClient client = AndroidHttpClient.newInstance("SummonersApp Agent SoloTop");
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 2000);
        HttpConnectionParams.setSoTimeout(client.getParams(), 2000);
        HttpConnectionParams.setTcpNoDelay(client.getParams(), true);
        LogSystem.appendLog("Sending request: " + url, "SummonersRequestsLogs");
        try {
            response = client.execute(request);
            status = response.getStatusLine().getStatusCode();
            if (status == 200)
                loLResponse = new LoLResponse(
                        new JSONObject(EntityUtils.toString(response.getEntity())),
                        200,
                        null);
            else
                loLResponse = new LoLResponse(null, status, null);
        } catch (IOException e) {
            LogSystem.appendLog("Code 02 - SendRequest:" + e.toString(), "SummonersRequestsLogs");
            loLResponse = new LoLResponse(null, -2, e.toString());
        } catch (Exception e) {
            LogSystem.appendLog("Code 03 - SendRequest:" + e.toString(), "SummonersRequestsLogs");
            loLResponse = new LoLResponse(null, -1, e.toString());
        }
        client.close();
        return loLResponse;
    }
}
