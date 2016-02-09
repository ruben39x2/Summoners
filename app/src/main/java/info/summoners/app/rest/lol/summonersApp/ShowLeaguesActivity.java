package info.summoners.app.rest.lol.summonersApp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import info.summoners.app.rest.lol.util.LoLDataParser;
import info.summoners.app.rest.lol.util.LoLResponse;

// ShowLeaguesActivity.java

// Shows the current leagues of a summoner (everything is done by an asyncTask)

public class ShowLeaguesActivity extends ActionBarActivity {
    private final Context context = this;
    private String REGION;
    private String[] leagues = null;

    /*
    Methods from the superclass.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_leagues);

        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        this.REGION = prefs.getString("region", "EUW").toLowerCase();
        if (savedInstanceState == null) { // We didn't have saved Instance (activity first created)
            Intent intent = getIntent();
            Long summonerId = intent.getLongExtra("summonerId", 0);
            new LoadLeagues().execute(summonerId.toString());
        } else {

            String[] leagues = savedInstanceState.getStringArray("leagues");
            if (leagues != null) {// Activity being restored
                writeGlobalVariableLeagues(leagues);
                showLeagues();
            } else { // Not being restored.
                Intent intent = getIntent();
                Long summonerId = intent.getLongExtra("summonerId", 0);
                new LoadLeagues().execute(summonerId.toString());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (this.leagues != null) outState.putStringArray("leagues", this.leagues);
        super.onSaveInstanceState(outState);
    }

    /*
    Methods of the activity.
    */

    // Method called from the asyncTask.
    private void showLeagues() {
        // Initialize the setText
        TextView soloQ = (TextView) findViewById(R.id.textViewSoloQ);
        TextView soloQTier = (TextView) findViewById(R.id.textViewSoloQTier);
        TextView soloQLeagueName = (TextView) findViewById(R.id.textViewSoloQLeagueName);
        TextView soloQLeaguePoints = (TextView) findViewById(R.id.textViewSoloQLeaguePoints);
        TextView otherLeagues = (TextView) findViewById(R.id.textViewOtherLeagues);
        TextView otherLeaguesInfo = (TextView) findViewById(R.id.textViewOtherLeaguesInfo);

        // Set the texts (titles)
        soloQ.setText(getString(R.string.ranked_solo_leagues));

        // And now the other ones...
        // Exactly: string[0] tier info (Gold V)
        //          string[1] league name info (Akali's guild)
        //          string[2] LP

        soloQTier.setText(this.leagues[0]);
        soloQLeagueName.setText(this.leagues[1]);
        soloQLeaguePoints.setText(this.leagues[2]);

        if (!this.leagues[3].equals("")) {
            // Set the text of other leagues (they will not be visible yet - remember, they are
            // INVISIBLE until the button is clicked (onClick).
            otherLeagues.setText(R.string.other_leagues);
            otherLeaguesInfo.setText(this.leagues[3]);
            // Set the visibility of the button to visible.
            Button b = (Button) findViewById(R.id.buttonOtherLeagues);
            b.setVisibility(View.VISIBLE);
        } // We won't enable the button for "other leagues" if there are not other leagues, it is,
        // leagues[3] == null.
    }

    private void writeGlobalVariableLeagues(String[] leagues) {
        this.leagues = leagues;
    }

    public void onClickOtherLeagues(View v) {
        Button b = (Button) v;
        String buttonText = b.getText().toString();

        // Let's difference if we have to "show other leagues" or "occult them"
        if (buttonText.equals(getString(R.string.show_other_leagues))) {
            // SHOW OTHER LEAGUES PRESSED
            findViewById(R.id.textViewOtherLeagues).setVisibility(View.VISIBLE);
            findViewById(R.id.textViewOtherLeaguesInfo).setVisibility(View.VISIBLE);
            b.setText(getString(R.string.hide_other_leagues));
        } else if (buttonText.equals(getString(R.string.hide_other_leagues))) {
            // HIDE OTHER LEAGUES PRESSED
            findViewById(R.id.textViewOtherLeagues).setVisibility(View.INVISIBLE);
            findViewById(R.id.textViewOtherLeaguesInfo).setVisibility(View.INVISIBLE);
            b.setText(getString(R.string.show_other_leagues));
        } else
            Toast.makeText(getApplicationContext(),
                    getString(R.string.an_unknown_error_occured),
                    Toast.LENGTH_LONG).show();
    }

    /*
    Subclasses.
    */

    // Subclass that manages to take the data of the summoner's league.
    private class LoadLeagues extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPostExecute(String[] strings) {
            writeGlobalVariableLeagues(strings);
            showLeagues();
        }

        // IN: String containing the id of the summoner
        // OUT: String [] with -> string[0,1,2] SoloQ info ... and ... string[3] Team Leagues info
        // Exactly: string[0] tier info (Gold V)
        //          string[1] league name info (Akali's guild)
        //          string[2] LP
        //          string[3] other leagues info

        @Override
        protected String[] doInBackground(String... params) {
            String id = params[0];
            LoLResponse loLResponse;
            String request = "https://" + REGION + ".api.pvp.net/api/lol/" +
                    REGION + "/v2.5/league/by-summoner/" + id + "/entry?api_key=" + StaticKeyData.KEY;
            loLResponse = SendRequest.get(request);
            if (loLResponse.getStatus() == 200) {
                // Everything went OK.
                return parseLeagues(loLResponse.getJsonObject(), id);

            } else
                // Error control!
                if (loLResponse.getStatus() == -1) { // IO Exception.
                    return new String[]{getString(R.string.pay_your_internet_connection_dude), "", "", ""};
                } else if (loLResponse.getStatus() == -2) { // General exception.
                    return new String[]{getString(R.string.an_unknown_error_occured) + " - " + loLResponse.getError(), "", "", ""};
                } else if (loLResponse.getStatus() != 200) { // Not OK.
                    int statusCode = loLResponse.getStatus();
                    switch (statusCode) {
                        case 400:
                            return new String[]{getString(R.string.bad_request), "", "", ""};
                        case 401:
                            return new String[]{getString(R.string.unauthorized), "", "", ""};
                        case 404:
                            return new String[]{getString(R.string.the_current_summoner_has_no_leagues), "", "", ""};
                        case 429:
                            return new String[]{getString(R.string.rate_limit_exceeded), "", "", ""};
                        case 500:
                            return new String[]{getString(R.string.internal_server_error), "", "", ""};
                        case 503:
                            return new String[]{getString(R.string.service_unavailable), "", "", ""};
                        default:
                            return new String[]{getString(R.string.wtf_this_should_never_happen), "", "", ""};
                    }
                } else {
                    return new String[]{getString(R.string.wtf_this_should_never_happen), "", "", ""};
                }
        }

        private String[] parseLeagues(JSONObject jsonLeagues, String summId) {
            String soloQTier = "", soloQLeagueName = "", soloQLeaguePoints = "";
            String otherLeagues = "";
            try {
                JSONArray array = jsonLeagues.getJSONArray(summId);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject currentLeague = array.getJSONObject(i);
                    if (currentLeague.getString("queue").equals("RANKED_SOLO_5x5")) {
                        // SOLO Q LEAGUE
                        soloQTier = LoLDataParser.getLeagueTier(currentLeague, context);
                        soloQLeaguePoints = LoLDataParser.getLeaguePointsOrMiniSeries(currentLeague, context);
                        soloQLeagueName = currentLeague.getString("name");
                    } else {

                        // IT'S ANOTHER LEAGUE (TEAM LEAGUE)
                        otherLeagues += currentLeague.getJSONArray("entries").getJSONObject(0).getString("playerOrTeamName");
                        otherLeagues += ":\n";
                        otherLeagues += LoLDataParser.getLeagueTier(currentLeague, context);
                        otherLeagues += " (";
                        Integer leaguePoints = currentLeague.getJSONArray("entries").getJSONObject(0).getInt("leaguePoints");
                        otherLeagues += leaguePoints.toString() + " " + getString(R.string.lp);
                        otherLeagues += ") ";
                        otherLeagues += currentLeague.getString("name");
                        otherLeagues += "\n";
                        otherLeagues += LoLDataParser.getLeagueQueue(currentLeague, context);
                        otherLeagues += "\n\n";

                    }
                }
            } catch (JSONException e) {
                LogSystem.appendLog("FATAL - Code 13 (ShowLeagues): " + e.toString(),  "SummonersErrors");
                Log.d("SummonersApp", "An error occured: " + e.toString());
                return new String[]{getString(R.string.wtf_this_should_never_happen) + ": " + e.toString(), "", "", ""};

            }
            return new String[]{soloQTier, soloQLeagueName, soloQLeaguePoints, otherLeagues};
        }
    }
}




