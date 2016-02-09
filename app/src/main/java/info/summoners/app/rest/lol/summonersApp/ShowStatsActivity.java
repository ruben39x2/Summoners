package info.summoners.app.rest.lol.summonersApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import info.summoners.app.rest.lol.util.LoLDataParser;
import info.summoners.app.rest.lol.util.LoLResponse;

public class ShowStatsActivity extends ActionBarActivity {
    private String REGION;
    private String stats = null;

    /*
    Methods from the superclass.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stats);

        // Get the region from preferences.
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        this.REGION = prefs.getString("region", "EUW").toLowerCase();

        Intent intent = getIntent();
        String name = intent.getStringExtra("summonerName"); // Should never be null.
        setMainTexts(name);

        if (savedInstanceState == null) { // We didn't have saved Instance (activity first created)
            Long id = intent.getLongExtra("summonerId", 0);
            new LoadStatsTask().execute(id);
        } else {
            String stats = savedInstanceState.getString("stats");
            if (stats != null) {// Activity restored.
                ProgressBar pB = (ProgressBar) findViewById(R.id.progressBarStats);
                pB.setVisibility(View.INVISIBLE);
                writeGlobalVariableStats(stats);
                setStatsText();
            } else { // Not being restored.
                Long id = intent.getLongExtra("summonerId", 0);
                new LoadStatsTask().execute(id);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (this.stats != null) outState.putString("stats", this.stats);
        super.onSaveInstanceState(outState);
    }

    /*
    Methods of the activity.
    */

    private void setMainTexts(String name) {
        TextView tVName, tVRegion;
        tVName = (TextView) findViewById(R.id.textNameStats);
        tVRegion = (TextView) findViewById(R.id.textRegion);
        tVName.setText(name);
        tVRegion.setText(this.REGION.toUpperCase());
    }

    private void parseStatsTexts(String JSONStatsString) {
        JSONArray stats;
        try {
            stats = new JSONObject(JSONStatsString).getJSONArray("playerStatSummaries");
        } catch (JSONException e) {
            LogSystem.appendLog("FATAL - Code 20 (ShowStats): " + e.toString(), "SummonersErrors");
            Log.d("SummonersApp", "An error occured: " + e.toString());
            return;
        }
        String statsStr = LoLDataParser.getStats(this, stats);

        // And...
        writeGlobalVariableStats(statsStr);
        setStatsText();
    }

    private void writeGlobalVariableStats(String stats) {
        this.stats = stats;
    }

    private void setStatsText() {
        TextView tVstats;
        tVstats = (TextView) findViewById(R.id.textStats);
        tVstats.setText(this.stats);
    }

    /*
    Subclasses.
    */

    private class LoadStatsTask extends AsyncTask<Long, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            ProgressBar pB = (ProgressBar) findViewById(R.id.progressBarStats);
            pB.setVisibility(View.INVISIBLE);
            if (s != null)
                parseStatsTexts(s);
            else
                Toast.makeText(getApplicationContext(), getString(R.string.some_data_could_not_be_loaded), Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Long... params) {
            LoLResponse loLResponse = SendRequest.get("https://" + REGION + ".api.pvp.net/api/lol/" +
                    REGION + "/v1.3/stats/by-summoner/" + params[0] + "/summary?api_key=" + StaticKeyData.KEY);
            if (loLResponse.getStatus() == 200) {
                // Everything went OK.
                return loLResponse.getJsonObject().toString();

            } else
                return null;
        }
    }
}
