package info.summoners.app.rest.lol.summonersApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import info.summoners.app.rest.lol.util.LoLDataParser;
import info.summoners.app.rest.lol.util.LoLInGameData;
import info.summoners.app.rest.lol.util.LoLParticipantData;
import info.summoners.app.rest.lol.util.LoLResponse;

// ShowInGameInfoActivity.java

// The name is self-explicative, bro.

public class ShowInGameInfoActivity extends ActionBarActivity {
    private final Context context = this;
    private final Integer SIZE = 64;
    Timer timer = new Timer();
    Long gameLength;
    private String REGION;
    private String VERSION;
    private LoLInGameData gameData = null;
    private boolean allLoaded = false;

    /*
    Methods from the superclass.
    */

    // Launched when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_in_game_info);

        // Get the region from preferences.
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        this.REGION = prefs.getString("region", "EUW").toLowerCase();
        this.VERSION = prefs.getString("version", "5.6.1");
        // Get the JSON that was requested on the previous activity (contains in-game info)
        Intent intent = getIntent();
        JSONObject info = null;
        try {
            info = new JSONObject(intent.getStringExtra("info"));
        } catch (JSONException e) {
            LogSystem.appendLog("FATAL - Code 04 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
            e.printStackTrace();
        }
        setMainTexts(info);

        // Timer.
        TimerTask timerTextTask = new TimerText();
        timer.scheduleAtFixedRate(timerTextTask, 0, 1000);

        // Load the data.
        if (savedInstanceState == null)
            new LoadInGameDataTask(info).execute();
        else {
            this.allLoaded = savedInstanceState.getBoolean("allLoaded");
            if (this.allLoaded) {
                this.gameLength = savedInstanceState.getLong("gameLength");
                this.gameData = (LoLInGameData) savedInstanceState.getSerializable("gameData");
                setInGameData();
            } else {
                new LoadInGameDataTask(info).execute();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if ((this.allLoaded) && (this.gameData != null))
            outState.putSerializable("gameData", this.gameData);
        if (this.gameLength != null) outState.putLong("gameLength", this.gameLength);
        outState.putBoolean("allLoaded", this.allLoaded);
    }

    @Override
    protected void onStop() {
        this.timer.cancel();
        this.timer.purge();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        this.timer.cancel();
        this.timer.purge();
        super.onDestroy();
    }

    /*
    Methods of the activity.
    */

    private String getVersion() {
        return this.VERSION;
    }

    private void setMainTexts(JSONObject info) {
        TextView tVtitle;
        tVtitle = (TextView) findViewById(R.id.textViewInGameTitle);
        String mode = LoLDataParser.getGameMode(this, info) +
                (LoLDataParser.getGameType(this, info));
        String queueType = LoLDataParser.getGameQueueConfigId(this, info);
        tVtitle.setText(mode + " (" + queueType + ")");
        this.gameLength = (LoLDataParser.getCurrentGameLength(info));
    }

    private void updateLoadingText(String a) {
        TextView tV = (TextView) findViewById(R.id.textViewInGameLoading);
        tV.setText(a);
    }

    private void writeGlobalVariableInGameData(LoLInGameData inGameData) {
        this.gameData = inGameData;
    }

    private void writeGlobalVariableAllLoaded(boolean allLoaded) {
        this.allLoaded = allLoaded;
    }

    private void setInGameData() {
        Integer team100size, team200size;

        ImageView[] imageViews100 = new ImageView[]{
                (ImageView) findViewById(R.id.imageViewSummoner101),
                (ImageView) findViewById(R.id.imageViewSummoner102),
                (ImageView) findViewById(R.id.imageViewSummoner103),
                (ImageView) findViewById(R.id.imageViewSummoner104),
                (ImageView) findViewById(R.id.imageViewSummoner105),
                (ImageView) findViewById(R.id.imageViewSummoner106),};

        TextView[] textViewsNames100 = new TextView[]{
                (TextView) findViewById(R.id.textViewSummoner101Name),
                (TextView) findViewById(R.id.textViewSummoner102Name),
                (TextView) findViewById(R.id.textViewSummoner103Name),
                (TextView) findViewById(R.id.textViewSummoner104Name),
                (TextView) findViewById(R.id.textViewSummoner105Name),
                (TextView) findViewById(R.id.textViewSummoner106Name),
        };

        TextView[] textViewsLeagues100 = new TextView[]{
                (TextView) findViewById(R.id.textViewSummoner101League),
                (TextView) findViewById(R.id.textViewSummoner102League),
                (TextView) findViewById(R.id.textViewSummoner103League),
                (TextView) findViewById(R.id.textViewSummoner104League),
                (TextView) findViewById(R.id.textViewSummoner105League),
                (TextView) findViewById(R.id.textViewSummoner106League),
        };

        Button[] buttons100 = new Button[]{
                (Button) findViewById(R.id.buttonDetailsSummoner101),
                (Button) findViewById(R.id.buttonDetailsSummoner102),
                (Button) findViewById(R.id.buttonDetailsSummoner103),
                (Button) findViewById(R.id.buttonDetailsSummoner104),
                (Button) findViewById(R.id.buttonDetailsSummoner105),
                (Button) findViewById(R.id.buttonDetailsSummoner106)
        };


        ImageView[] imageViews200 = new ImageView[]{
                (ImageView) findViewById(R.id.imageViewSummoner201),
                (ImageView) findViewById(R.id.imageViewSummoner202),
                (ImageView) findViewById(R.id.imageViewSummoner203),
                (ImageView) findViewById(R.id.imageViewSummoner204),
                (ImageView) findViewById(R.id.imageViewSummoner205),
                (ImageView) findViewById(R.id.imageViewSummoner206),};

        TextView[] textViewsNames200 = new TextView[]{
                (TextView) findViewById(R.id.textViewSummoner201Name),
                (TextView) findViewById(R.id.textViewSummoner202Name),
                (TextView) findViewById(R.id.textViewSummoner203Name),
                (TextView) findViewById(R.id.textViewSummoner204Name),
                (TextView) findViewById(R.id.textViewSummoner205Name),
                (TextView) findViewById(R.id.textViewSummoner206Name),
        };

        TextView[] textViewsLeagues200 = new TextView[]{
                (TextView) findViewById(R.id.textViewSummoner201League),
                (TextView) findViewById(R.id.textViewSummoner202League),
                (TextView) findViewById(R.id.textViewSummoner203League),
                (TextView) findViewById(R.id.textViewSummoner204League),
                (TextView) findViewById(R.id.textViewSummoner205League),
                (TextView) findViewById(R.id.textViewSummoner206League),
        };

        Button[] buttons200 = new Button[]{
                (Button) findViewById(R.id.buttonDetailsSummoner201),
                (Button) findViewById(R.id.buttonDetailsSummoner202),
                (Button) findViewById(R.id.buttonDetailsSummoner203),
                (Button) findViewById(R.id.buttonDetailsSummoner204),
                (Button) findViewById(R.id.buttonDetailsSummoner205),
                (Button) findViewById(R.id.buttonDetailsSummoner206)
        };

        team100size = this.gameData.getTeam100names().size();
        team200size = this.gameData.getTeam200names().size();

        // Fill the things of the team 100.
        for (int i = 0; i < team100size; i++) {
            imageViews100[i].setImageBitmap(this.gameData.getTeam100champImages().get(i));
            textViewsNames100[i].setText(this.gameData.getTeam100names().get(i));
            textViewsLeagues100[i].setText(this.gameData.getTeam100leagues().get(i));
            buttons100[i].setVisibility(View.VISIBLE);
        }
        // Team 200:
        for (int i = 0; i < team200size; i++) {
            imageViews200[i].setImageBitmap(this.gameData.getTeam200champImages().get(i));
            textViewsNames200[i].setText(this.gameData.getTeam200names().get(i));
            textViewsLeagues200[i].setText(this.gameData.getTeam200leagues().get(i));
            buttons200[i].setVisibility(View.VISIBLE);
        }
        // Set text "V.S."
        TextView tVVs;
        tVVs = (TextView) findViewById(R.id.textViewVS);
        tVVs.setText("VS");
    }

    private void actualizeTimer() {
        TextView currentLength = (TextView) findViewById(R.id.textViewCurrentLength);
        this.gameLength++;
        Long minutes = this.gameLength / 60;
        Long seconds = this.gameLength % 60;
        String minutesStr = minutes.toString();
        String secondsStr;
        if (seconds.toString().length() == 1)
            secondsStr = "0" + seconds.toString();
        else
            secondsStr = seconds.toString();
        currentLength.setText(minutesStr + ":" + secondsStr);
    }

    // Called by the asyncTask "LoadDetails"
    private AlertDialog createDialog(String bodyText) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setCancelable(true)
                .setTitle(getString(R.string.details))
                .setMessage(bodyText)
        ;
        // Create alert dialog and show it.
        return alertDialogBuilder.create();

    }

    // onClick for the button "details" (of a summoner playing the match).
    // This will load the runes and masteries info and show a dialog.
    public void onClickDetails(View v) {
        // Get the JSON that was requested on the previous activity (contains in-game info)
        Intent intent = getIntent();
        JSONObject info = null;
        try {
            info = new JSONObject(intent.getStringExtra("info"));
        } catch (JSONException e) {
            LogSystem.appendLog("FATAL - Code 12 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
            e.printStackTrace();
        }
        if (v.getId() == R.id.buttonDetailsSummoner101)
            new LoadDetailsTask(info, this.gameData.getTeam100names().get(0)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner102)
            new LoadDetailsTask(info, this.gameData.getTeam100names().get(1)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner103)
            new LoadDetailsTask(info, this.gameData.getTeam100names().get(2)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner104)
            new LoadDetailsTask(info, this.gameData.getTeam100names().get(3)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner105)
            new LoadDetailsTask(info, this.gameData.getTeam100names().get(4)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner106)
            new LoadDetailsTask(info, this.gameData.getTeam100names().get(5)).execute();

        if (v.getId() == R.id.buttonDetailsSummoner201)
            new LoadDetailsTask(info, this.gameData.getTeam200names().get(0)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner202)
            new LoadDetailsTask(info, this.gameData.getTeam200names().get(1)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner203)
            new LoadDetailsTask(info, this.gameData.getTeam200names().get(2)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner204)
            new LoadDetailsTask(info, this.gameData.getTeam200names().get(3)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner205)
            new LoadDetailsTask(info, this.gameData.getTeam200names().get(4)).execute();
        if (v.getId() == R.id.buttonDetailsSummoner206)
            new LoadDetailsTask(info, this.gameData.getTeam200names().get(5)).execute();
    }

    /*
    Subclasses
    */

    // Subclass LoadInGameDataTask.
    private class LoadInGameDataTask extends AsyncTask<Void, String, LoLInGameData> {
        private JSONObject inGameInfo;
        private List<LoLParticipantData> team100;
        private List<LoLParticipantData> team200;
        private LoLInGameData inGameData;

        // Constructor.
        LoadInGameDataTask(JSONObject inGameInfo) {
            this.inGameInfo = inGameInfo;
        }

        @Override
        protected LoLInGameData doInBackground(Void... params) {
            inGameData = new LoLInGameData();
            team100 = LoLDataParser.getTeam100Participants(inGameInfo);
            team200 = LoLDataParser.getTeam200Participants(inGameInfo);

            // We create a string containing the name of all human summoners separated by commas:
            // i.e. sum1,sum2 ...
            // Also, we add the names of the participants to the final object.
            String summonerNamesCommas = "";
            for (LoLParticipantData p : team100) {
                if (!p.getBot()) summonerNamesCommas += p.getSummonerId().toString() + ",";
                inGameData.addTeam100name(p.getSummonerName());
            }
            for (LoLParticipantData p : team200) {
                if (!p.getBot()) summonerNamesCommas += p.getSummonerId().toString() + ",";
                inGameData.addTeam200name(p.getSummonerName());
            }

            // Now, we send a petition in order to get their leagues.
            publishProgress(getString(R.string.loading_leagues));
            String petition = "https://" + REGION + ".api.pvp.net/api/lol/" +
                    REGION + "/v2.5/league/by-summoner/" + summonerNamesCommas + "/entry?api_key=" + StaticKeyData.KEY;
            LoLResponse loLResponse = SendRequest.get(petition);

            if (loLResponse.getStatus() == 200) {
                // The fun continues here...
                // Everything went OK.
                parseSummonerLeagues(loLResponse.getJsonObject());
                loadChampKeys();
                loadChampImages();
                return inGameData;

            } else
                // Error control!
                if (loLResponse.getStatus() == -1) { // IO Exception.
                    publishProgress(getString(R.string.pay_your_internet_connection_dude));
                    return null;
                } else if (loLResponse.getStatus() == -2) { // General exception.
                    publishProgress(getString(R.string.an_unknown_error_occured) + " - " + loLResponse.getError());
                    return null;
                } else if (loLResponse.getStatus() != 200) { // Not OK.
                    int statusCode = loLResponse.getStatus();
                    switch (statusCode) {
                        case 400: {
                            publishProgress(getString(R.string.bad_request));
                            return null;
                        }
                        case 401: {
                            publishProgress(getString(R.string.unauthorized));
                            return null;
                        }
                        case 404: {
                            allAreUnrankedOMG();
                            loadChampKeys();
                            loadChampImages();
                            return inGameData;
                        } // LOOK AT THIS ! :D
                        case 429: {
                            publishProgress(getString(R.string.rate_limit_exceeded));
                            return null;
                        }
                        case 500: {
                            publishProgress(getString(R.string.internal_server_error));
                            return null;
                        }
                        case 503: {
                            publishProgress(getString(R.string.service_unavailable));
                            return null;
                        }
                        default: {
                            publishProgress(getString(R.string.wtf_this_should_never_happen));
                            return null;
                        }
                    }
                } else {
                    publishProgress(getString(R.string.wtf_this_should_never_happen));
                    return null;
                }

        }

        private void parseSummonerLeagues(JSONObject leaguesInfo) {

            // About the team 100...
            for (LoLParticipantData p : team100) {
                // For every participant, check if in the response exist a entry with his/her Id
                // and he/she has RANKED_SOLO_5x5 queue.
                try {
                    boolean leagueAdded = false; // What if he belongs to a team but has no soloQ rankeds?
                    JSONArray currentSummonerLeagues = leaguesInfo.getJSONArray(p.getSummonerId().toString());
                    // Check the leagues of this exact summoner, looking for ranked solo.
                    for (int i = 0; i < currentSummonerLeagues.length(); i++) {
                        JSONObject currentLeague = currentSummonerLeagues.getJSONObject(i);
                        if (currentLeague.getString("queue").equals("RANKED_SOLO_5x5")) {
                            String soloQTier = LoLDataParser.getLeagueTier(currentLeague, context);
                            String soloQLeaguePoints = LoLDataParser.getLeaguePointsOrMiniSeries(currentLeague, context);
                            inGameData.addTeam100League(soloQTier + " (" + soloQLeaguePoints + ")");
                            leagueAdded = true;
                        }
                    }
                    if (!leagueAdded) inGameData.addTeam100League(" - ");
                } catch (JSONException e) {
                    LogSystem.appendLog("Code 05 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                    inGameData.addTeam100League(" - ");
                }
            }

            // About the team 200...
            for (LoLParticipantData p : team200) {
                // For every participant, check if in the response exist a entry with his/her Id
                // and he/she has RANKED_SOLO_5x5 queue.
                try {
                    boolean leagueAdded = false;
                    JSONArray currentSummonerLeagues = leaguesInfo.getJSONArray(p.getSummonerId().toString());
                    // Check the leagues of this exact summoner, looking for ranked solo.
                    for (int i = 0; i < currentSummonerLeagues.length(); i++) {
                        JSONObject currentLeague = currentSummonerLeagues.getJSONObject(i);
                        if (currentLeague.getString("queue").equals("RANKED_SOLO_5x5")) {
                            String soloQTier = LoLDataParser.getLeagueTier(currentLeague, context);
                            Integer leaguePoints = currentLeague.getJSONArray("entries").getJSONObject(0).getInt("leaguePoints");
                            String soloQLeaguePoints = leaguePoints.toString() + " " + getString(R.string.lp);
                            inGameData.addTeam200League(soloQTier + " (" + soloQLeaguePoints + ")");
                            leagueAdded = true;
                        }
                    }
                    if (!leagueAdded) inGameData.addTeam200League(" - ");
                } catch (JSONException e) {
                    LogSystem.appendLog("Code 06 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                    inGameData.addTeam200League(" - ");
                }
            }

        }

        private void allAreUnrankedOMG() {
            for (int i = 0; i < 6; i++) this.inGameData.addTeam100League(" - ");
            for (int i = 0; i < 6; i++) this.inGameData.addTeam200League(" - ");
        }

        // Okay, everything until here was loaded right... Our "inGameData" has:
        // - Summoner names
        // - Summoner leagues
        // The only thing left is the champImages. Now, let's take the champion keys in order to
        // load their images. (From preferences of from the web)
        private void loadChampKeys() {
            LoLResponse loLResponse;

            // For the publishProgress...
            Integer totalSummoners = team100.size() + team200.size();
            Integer i = 0;

            // ..and now, DO THINGS !
            // TEAM 100.
            for (LoLParticipantData p : team100) {

                i++;
                publishProgress(getString(R.string.loading_champion_names) + " " + i.toString() + "/" + totalSummoners);

                SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
                String name = prefs.getString("champ" + p.getChampId(), null);

                // Not loaded yet.
                if (name == null) {
                    loLResponse = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" +
                            REGION + "/v1.2/champion/" + p.getChampId() + "?api_key=" + StaticKeyData.KEY);
                    try {
                        if (loLResponse.getStatus() == 200) {
                            String theKeyName = loLResponse.getJsonObject().getString("key");
                            SharedPreferences.Editor editor =
                                    getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                            editor.putString("champ" + p.getChampId(), theKeyName);
                            editor.commit();
                        }
                    } catch (JSONException e) {
                        LogSystem.appendLog("FATAL - Code 07 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                        Log.e("SummonersApp", e.toString());
                    }
                }

                // And now load it from the preferences.
                p.setChampKey(prefs.getString("champ" + p.getChampId(), ""));

            }

            // TEAM 200.
            for (LoLParticipantData p : team200) {

                i++;
                publishProgress(getString(R.string.loading_champion_names) + " " + i.toString() + "/" + totalSummoners);

                SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
                String name = prefs.getString("champ" + p.getChampId(), null);

                // Not loaded yet.
                if (name == null) {
                    loLResponse = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" +
                            REGION + "/v1.2/champion/" + p.getChampId() + "?api_key=" + StaticKeyData.KEY);
                    try {
                        if (loLResponse.getStatus() == 200) {
                            String theKeyName = loLResponse.getJsonObject().getString("key");
                            SharedPreferences.Editor editor =
                                    getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                            editor.putString("champ" + p.getChampId(), theKeyName);
                            editor.commit();
                        }
                    } catch (JSONException e) {
                        LogSystem.appendLog("FATAL - Code 08 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                        Log.e("SummonersApp", e.toString());
                    }
                }

                // And now load it from the preferences.
                p.setChampKey(prefs.getString("champ" + p.getChampId(), ""));
            }
        }

        private void loadChampImages() {

            // For the publishProgress...
            Integer totalSummoners = team100.size() + team200.size();
            Integer i = 0;

            for (LoLParticipantData p : team100) {

                i++;
                publishProgress(getString(R.string.loading_champion_icons) + " " + i.toString() + "/" + totalSummoners);
                Bitmap result;

                InputStream in;
                try {
                    in = new java.net.URL("http://ddragon.leagueoflegends.com/cdn/" + getVersion() + "/img/champion/" +
                            p.getChampKey() + ".png").openStream();
                    result = BitmapFactory.decodeStream(in);
                } catch (IOException e) {
                    LogSystem.appendLog("Code 08 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                    result = BitmapFactory.decodeResource(getResources(), R.drawable.no_item);
                }
                inGameData.addTeam100champImage(Bitmap.createScaledBitmap(result, SIZE, SIZE, false));
            }

            for (LoLParticipantData p : team200) {

                i++;
                publishProgress(getString(R.string.loading_champion_icons) + " " + i.toString() + "/" + totalSummoners);
                Bitmap result;

                InputStream in;
                try {
                    in = new java.net.URL("http://ddragon.leagueoflegends.com/cdn/" + getVersion() + "/img/champion/" +
                            p.getChampKey() + ".png").openStream();
                    result = BitmapFactory.decodeStream(in);
                } catch (IOException e) {
                    LogSystem.appendLog("Code 09 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                    result = BitmapFactory.decodeResource(getResources(), R.drawable.no_item);
                }
                inGameData.addTeam200champImage(Bitmap.createScaledBitmap(result, SIZE, SIZE, false));
            }
        }

        @Override
        protected void onPreExecute() {
            updateLoadingText(getString(R.string.starting_load));
            ProgressBar pB = (ProgressBar) findViewById(R.id.progressBarInGameLoading);
            pB.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(LoLInGameData gameData) {
            updateLoadingText("");
            ProgressBar pB = (ProgressBar) findViewById(R.id.progressBarInGameLoading);
            pB.setVisibility(View.INVISIBLE);

            // Methods from the superclass.
            writeGlobalVariableInGameData(gameData);
            writeGlobalVariableAllLoaded(true);
            setInGameData();

            // In case that (maybe) there are bots... (DISCOVERED ISSUE: RIOT DOES NOT RETURN THE
            // INFO OF BOTS, EVEN TOUGH A FLAG "BOT" EXISTS.)
            if (this.inGameData.getTeam100names().size() != this.inGameData.getTeam200names().size())
                Toast.makeText(getApplicationContext(), getString(R.string.bot_players_are_not_displayed), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            updateLoadingText(values[0]);
        }
    }

    // Subclass LoadDetailsTask. Called by onClickDetails.
    private class LoadDetailsTask extends AsyncTask<Void, Void, String> {
        AlertDialog loadingDialog;
        JSONArray runes;
        JSONArray masteries;

        LoadDetailsTask(JSONObject info, String name) {
            JSONArray participants;
            try {
                participants = info.getJSONArray("participants");
                for (int i = 0; i < participants.length(); i++) {
                    if (participants.getJSONObject(i).getString("summonerName").equals(name)) {
                        this.runes = participants.getJSONObject(i).getJSONArray("runes");
                        this.masteries = participants.getJSONObject(i).getJSONArray("masteries");
                    }
                }
            } catch (JSONException e) {
                LogSystem.appendLog("FATAL - Code 10 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog = createDialog(getString(R.string.loading));
            this.loadingDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String masteriesResume = LoLDataParser.getMasteriesResume(masteries);
            String runesResume = parseRunes(); // private method
            //return getString(R.string.masteries) + masteriesResume + getString(R.string.runes) + runesResume;
            return getString(R.string.runes) + runesResume;
        }

        @Override
        protected void onPostExecute(String s) {
            this.loadingDialog.dismiss();
            createDialog(s).show();
        }

        /*@Override
        protected void onCancelled() {
            this.loadingDialog.dismiss();
            super.onCancelled();
        }*/

        // Maybe this method is a bit complex.
        // Okay...
        // 1st: Load the name of the rune from the preferences. If it has not been loaded and saved
        // to preferences yet (runeName == null)... let's load it! (go to 2nd)
        // 2nd: Load the rune name (by sending a request) with the current Locale. If we got the rune
        // name - everything it's ok. If not... (go to 3rd)
        // 3rd: The locale is not supported by Riot. We will ask for the name rune in english Locale
        // and use that locale for future requests.
        private String parseRunes() {
            String result = "";
            String locale = Locale.getDefault().toString();
            for (int i = 0; i < this.runes.length(); i++) {
                try {
                    Integer count = this.runes.getJSONObject(i).getInt("count");
                    Long runeId = this.runes.getJSONObject(i).getLong("runeId");

                    SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
                    String runeName = prefs.getString("rune" + runeId + locale, null);

                    if (runeName == null) { // 1st.
                        LoLResponse loLResponse = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" +
                                REGION + "/v1.2/rune/" + runeId + "?locale=" + locale + "&api_key=" + StaticKeyData.KEY);
                        // 2nd.
                        if (loLResponse.getStatus() == 200) { // We got the rune name
                            String theRuneName = loLResponse.getJsonObject().getString("name");
                            SharedPreferences.Editor editor =
                                    getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                            editor.putString("rune" + runeId + locale, theRuneName);
                            editor.commit();
                            result += "(x" + count + ") " + theRuneName + "\n";

                            // 3rd.
                        } else if (loLResponse.getStatus() == 400) { // That locale is not supported by riot.
                            LoLResponse loLResponse2 = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" +
                                    REGION + "/v1.2/rune/" + runeId + "?locale=en_US&api_key=" + StaticKeyData.KEY);
                            if (loLResponse2.getStatus() == 200) { // We got the rune name

                                String theRuneName = loLResponse2.getJsonObject().getString("name");
                                SharedPreferences.Editor editor =
                                        getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                                editor.putString("rune" + runeId + locale, theRuneName);
                                editor.commit();
                                result += "(x" + count + ") " + theRuneName + "\n";
                                // At this point: We failed to load a rune with the locale "locale" but
                                // it worked with locale default (en_US) instead... Let's never use the
                                // locale "locale" again:
                                locale = "en_US";

                            } else // 3rd.
                                result += "(x" + count + ") " + getString(R.string.unknown) + "\n";
                        } else
                            result += "(x" + count + ") " + getString(R.string.unknown) + "\n";
                    } else {
                        result += "(x" + count + ") " + runeName + "\n";
                    }
                } catch (JSONException e) {
                    LogSystem.appendLog("FATAL - Code 11 (ShowInGameInfo): " + e.toString(), "SummonersErrors");
                    e.printStackTrace();
                }
            }
            return result;
        }
    }

    // Subclass for the timer (the textView that updates every second).
    private class TimerText extends TimerTask {
        private Handler update = new InGameTimerHandler(this);

        private void actualize() {
            actualizeTimer();
        }

        public void run() {
            update.sendEmptyMessage(0);
        }
    }

    // Subclass for the Handler. (It actualizes the textView by calling the method of the superclass
    // via WeakReference of it, so as to prevent memory leaks).
    private static class InGameTimerHandler extends Handler {
        private final WeakReference<TimerText> timerTextClassReference;

        private InGameTimerHandler(TimerText timerText) {
            this.timerTextClassReference = new WeakReference<>(timerText);
        }

        @Override
        public void dispatchMessage(Message msg) {
            TimerText timerTextObject = timerTextClassReference.get();
            timerTextObject.actualize();
        }
    }
}