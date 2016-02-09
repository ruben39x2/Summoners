package info.summoners.app.rest.lol.summonersApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

import info.summoners.app.rest.lol.util.LoLResponse;

public class PreferencesActivity extends ActionBarActivity {
    private String VERSION;
    private String REGION;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        this.VERSION = prefs.getString("version", "5.6.1");
        this.REGION = prefs.getString("region", "EUW").toLowerCase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        boolean loadDone = prefs.getBoolean("preloadDone", false);
        if (loadDone) {
            Button b = (Button) findViewById(R.id.buttonPreload);
            b.setText(getString(R.string.data_already_loaded));
            b.setEnabled(false);
        }
        TextView version = (TextView) findViewById(R.id.currentVersionTextPrefs);
        version.setText(getString(R.string.current_version_2_points) + " " + VERSION);

    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }*/

    public void onClickRegion(View v) {
        // Get current region.
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        String region = prefs.getString("region", "EUW");
        Integer regionInt = 0;
        if (region.equals("BR")) regionInt = 0;
        if (region.equals("EUNE")) regionInt = 1;
        if (region.equals("EUW")) regionInt = 2;
        if (region.equals("KR")) regionInt = 3;
        if (region.equals("LAN")) regionInt = 4;
        if (region.equals("LAS")) regionInt = 5;
        if (region.equals("NA")) regionInt = 6;
        if (region.equals("OCE")) regionInt = 7;
        if (region.equals("RU")) regionInt = 8;
        if (region.equals("TR")) regionInt = 9;

        final String[] items =
                {"BR", "EUNE", "EUW", "KR", "LAN", "LAS", "NA", "OCE", "RU", "TR"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_your_region))
                .setSingleChoiceItems(items, regionInt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int item) {
                        // Every time you click on a singleChoice Option, this is executed.
                        SharedPreferences.Editor editor =
                                getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                        editor.putString("region", items[item]);
                        editor.commit();
                    }
                });

        builder.create().show();
    }

    public void onClickPreload(View v){
        new preloadTask().execute();
    }

    public void onClickVersion(View v){
        new versionTask().execute();
    }

    private class versionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            Button b = (Button) findViewById(R.id.buttonVersion);
            b.setEnabled(false);
            b.setText(getText(R.string.loading));
            ProgressBar pB = (ProgressBar) findViewById(R.id.progressVersion);
            pB.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            Button b = (Button) findViewById(R.id.buttonVersion);
            b.setEnabled(true);
            b.setText(getText(R.string.look_for_changes));
            ProgressBar pB = (ProgressBar) findViewById(R.id.progressVersion);
            pB.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),
                    result,
                    Toast.LENGTH_LONG).show();
            TextView version = (TextView) findViewById(R.id.currentVersionTextPrefs);
            version.setText(getString(R.string.current_version_2_points) + " " + VERSION);
            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(Void... voids) {
            LoLResponse loLResponse;
            loLResponse = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/"
                    + REGION + "/v1.2/realm?api_key=" + StaticKeyData.KEY);
            if (loLResponse.getStatus()==200) {
                try {
                    String newdd = loLResponse.getJsonObject().getString("dd");
                    if (newdd.equals(VERSION))
                        return getString(R.string.version_uptodate);
                    else {
                        SharedPreferences.Editor editor =
                                getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                        editor.putString("version", newdd);
                        editor.commit();
                        VERSION=newdd;
                        return getString(R.string.version_actualized);
                    }

                } catch (JSONException e) {
                    LogSystem.appendLog("Error - Code 97: " + e.getCause(), "SummonersErrors");
                    return getString(R.string.an_unknown_error_occured);
                }
            } else
                return getString(R.string.an_unknown_error_occured);
        }
    }




    private class preloadTask extends AsyncTask<Void, Void, Boolean> {
        SharedPreferences.Editor editor = getSharedPreferences("Summoners", MODE_PRIVATE).edit();

        @Override
        protected Boolean doInBackground(Void... voids) {

            LoLResponse loLResponse, loLResponse1;
            loLResponse = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" + REGION +"/v1.2/champion?dataById=true&api_key=" + StaticKeyData.KEY);
            if (loLResponse.getStatus()==200) {
                try {
                    JSONObject data = loLResponse.getJsonObject().getJSONObject("data");
                    Iterator<String> keys = data.keys();
                    while( keys.hasNext() ) {
                        String key = keys.next();
                        if (data.get(key) instanceof JSONObject) {
                            editor.putString("champ" + key, data.getJSONObject(key).getString("key"));
                        }
                    }
                    editor.apply();
                } catch (JSONException e) {
                    LogSystem.appendLog("Error - Code 99 - " + e.getCause(), "SummonersErrors");
                    return false;
                }

                // Now we loaded the names of the champions, let's load the runes.
                String locale = Locale.getDefault().toString();
                loLResponse1 = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/"
                        + REGION + "/v1.2/rune?locale=" + locale + "&api_key=" + StaticKeyData.KEY);
                if (loLResponse1.getStatus()==200) {
                    return saveRunes(loLResponse1.getJsonObject(), locale);

                } else { // That locale is not supported by Riot (or there was an error)

                    if (loLResponse1.getStatus()==400) { // Locale not supported
                        locale = "en_US"; // We will try with en_US locale
                        loLResponse1 = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/"
                                + REGION + "/v1.2/rune?locale=" + locale + "&api_key=" + StaticKeyData.KEY);
                        if (loLResponse1.getStatus() == 200) {
                            return saveRunes(loLResponse1.getJsonObject(), locale);
                        } else
                            return false;

                    } else // Other error
                    return false;
                }

            } else
                return false;
        }

        private boolean saveRunes(JSONObject response, String locale){
            JSONObject data;
            try {
                data = response.getJSONObject("data");
                Iterator<String> keys = data.keys();
                while( keys.hasNext() ) {
                    String key = keys.next();
                    if (data.get(key) instanceof JSONObject) {
                        editor.putString("rune" + key + locale, data.getJSONObject(key).getString("name"));
                    }
                }
                editor.apply();
                return true;
            } catch (JSONException e) {
                LogSystem.appendLog("Error - Code 98 - " + e.getCause(), "SummonersErrors");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                editor.putBoolean("preloadDone", true);
                editor.commit();
                Button b = (Button) findViewById(R.id.buttonPreload);
                b.setText(getString(R.string.data_already_loaded));
                ProgressBar pB = (ProgressBar) findViewById(R.id.progressBarPreload);
                pB.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.data_loaded_successfuly),
                        Toast.LENGTH_LONG).show();
            } else {
                Button b = (Button) findViewById(R.id.buttonPreload);
                b.setText(getText(R.string.an_unknown_error_occured));
                ProgressBar pB = (ProgressBar) findViewById(R.id.progressBarPreload);
                pB.setVisibility(View.INVISIBLE);
            }
            super.onPostExecute(success);
        }

        @Override
        protected void onPreExecute() {
            Button b = (Button) findViewById(R.id.buttonPreload);
            b.setEnabled(false);
            b.setText(getText(R.string.loading));
            ProgressBar pB = (ProgressBar) findViewById(R.id.progressBarPreload);
            pB.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }
    }
}
