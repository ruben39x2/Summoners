package info.summoners.app.rest.lol.util;

import org.json.JSONObject;

// LoLResponse.java

// Java object containing the info of a request send to the Riot server.
// - The JSONObject (which is null if there was an error).
// - The HTTP Status of the response (or -1 if it was Java error).
// - The error (in case of Java errors).

public class LoLResponse {
    private JSONObject jsonObject;
    private int status;
    private String error;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructor.
    public LoLResponse(JSONObject jsonObject, int status, String error) {
        this.jsonObject = jsonObject;
        this.status = status;
        this.error = error;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Getters.
    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

    public int getStatus() {
        return this.status;
    }

    public String getError() {
        return this.error;
    }
}
