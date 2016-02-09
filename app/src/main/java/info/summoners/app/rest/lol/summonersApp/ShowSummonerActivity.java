package info.summoners.app.rest.lol.summonersApp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import info.summoners.app.rest.lol.util.LoLDataParser;
import info.summoners.app.rest.lol.util.LoLRecentGamesData;
import info.summoners.app.rest.lol.util.LoLResponse;

// ShowSummonerActivity.java

// Shows the data of ONE summoner. Firstly, name and image are loaded. Later, the recent match
// history.

public class ShowSummonerActivity extends ActionBarActivity {
    private String REGION;
    private String PLATFORM;
    private String VERSION;
    private String[] basicInfo = null; //[iconId, name, level, summonerId]
    private String inGame = null;
    private JSONArray games = null;
    private LoLRecentGamesData recentGamesData = null;
    private Bundle savedState = null;
    private boolean menuCallable = false;

    /*
    Methods from the superclass.
    */

    // Launched when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the things.
        setContentView(R.layout.activity_show_summoner);

        // Get the summoner name.
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");

        // Get the region from preferences.
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        this.VERSION = prefs.getString("version", "5.6.1");
        this.REGION = prefs.getString("region", "EUW");
        this.PLATFORM = this.REGION + "1";
        // NOTE: Ru and Kr have a different platform name (which doesn't end with "1")
        if (this.PLATFORM.equals("RU1")) this.PLATFORM = "RU";
        if (this.PLATFORM.equals("KR1")) this.PLATFORM = "KR";

        this.REGION = this.REGION.toLowerCase();

        this.savedState = savedInstanceState;
        if (this.savedState == null) {
            // Activity created for the first time.
            new InitialLoadTask().execute(name);
        } else {
            //Activity restored.
            if (this.savedState.getStringArray("basicInfo") == null) {
                // Basic info was not even loaded.
                new InitialLoadTask().execute(name);
            } else {
                String[] basicInfo = this.savedState.getStringArray("basicInfo");
                writeGlobalVariableBasicInfo(basicInfo);
                setBasicInfo();
                restoreData(); // This method will set the game of the match history if it was
                // previously loaded or request the info for the first time.
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // The JSONObject with the info of the recent games data (we need to conserve this because
        // it is passed as an argument to the ShowMatchActivity)
        if (this.recentGamesData != null)
            outState.putSerializable("gamesData", this.recentGamesData);
        // The specific data of the recent games.
        if (this.games != null) outState.putString("JSONGames", this.games.toString());
        // The "in-game" string.
        if (this.inGame != null) outState.putString("inGame", this.inGame);

        // basic info yeah
        if (this.basicInfo != null) outState.putStringArray("basicInfo", this.basicInfo);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_summoner, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (checkNotificationsActivated()) {
            menu.findItem(R.id.action_notify).setVisible(false);
            menu.findItem(R.id.action_unnotify).setVisible(true);
        } else {
            menu.findItem(R.id.action_notify).setVisible(true);
            menu.findItem(R.id.action_unnotify).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (!menuCallable) {
            displayPartialError(getString(R.string.wait_for_basic_info));
        } else {
            int id = item.getItemId();
            if (id == R.id.action_leagues) {
                final Intent intent = new Intent(this, ShowLeaguesActivity.class);
                intent.putExtra("summonerId", getSummonerId());
                startActivity(intent);
            }

            if (id == R.id.action_stats) {
                final Intent intent = new Intent(this, ShowStatsActivity.class);
                intent.putExtra("summonerId", getSummonerId());
                intent.putExtra("summonerName", getSummonerName());
                startActivity(intent);
            }
            if (id == R.id.action_unnotify) {
                Intent intent = getIntent();
                String name = intent.getStringExtra("name");
                //  Then we have to DISABLE notifications
                SharedPreferences.Editor editor =
                        getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                editor.remove(name);
                editor.commit();
            }
            if (id == R.id.action_notify) {
                Intent intent = getIntent();
                String name = intent.getStringExtra("name");
                // Then we ENABLE notifications
                SharedPreferences.Editor editor =
                        getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                editor.putString(name, "0");
                editor.commit();
            }


        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Methods of the activity
    */

    private String getVersion() {
        return this.VERSION;
    }


    // Send a request to get the "basic info" of a summoner.
    // That info will be returned as a array of String containing [iconId, name, level, summonerId]
    // or [null, null, errorCode]
    private String[] downloadBasicInformation(String name) {
        LoLResponse loLResponse;
        String encodedName;

        // Encode the name (replace " " to %20 and those things).
        try {
            encodedName = name.toLowerCase();
            URLEncoder.encode(encodedName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LogSystem.appendLog("FATAL - Code 21 (ShowSummoner): " + e.toString(), "SummonersErrors");
            return new String[]{null, null, getString(R.string.an_unknown_error_occured) + " - " + e.toString(), null};
        }

        String request = "https://" + REGION + ".api.pvp.net/api/lol/" +
                REGION + "/v1.4/summoner/by-name/" + encodedName + "?api_key=" + StaticKeyData.KEY;
        // Create this to get the result of the petition (of basic info about a summoner).
        loLResponse = SendRequest.get(request);
        if (loLResponse.getStatus() == 200) {
            // Everything went OK.
            return parseBasicInfo(loLResponse.getJsonObject(), encodedName);

        } else
            // Error control!
            if (loLResponse.getStatus() == -1) { // IO Exception.
                return new String[]{null, null, getString(R.string.pay_your_internet_connection_dude), null};
            } else if (loLResponse.getStatus() == -2) { // General exception.
                return new String[]{null, null, getString(R.string.an_unknown_error_occured) + " - " + loLResponse.getError(), null};
            } else if (loLResponse.getStatus() != 200) { // Not OK.
                int statusCode = loLResponse.getStatus();
                switch (statusCode) {
                    case 400:
                        return new String[]{null, null, getString(R.string.bad_request), null};
                    case 401:
                        return new String[]{null, null, getString(R.string.unauthorized)};
                    case 404:
                        return new String[]{null, null, getString(R.string.no_summoner_data_for_the_given_name_please_erase_it), null};
                    case 429:
                        return new String[]{null, null, getString(R.string.rate_limit_exceeded), null};
                    case 500:
                        return new String[]{null, null, getString(R.string.internal_server_error), null};
                    case 503:
                        return new String[]{null, null, getString(R.string.service_unavailable), null};
                    default:
                        return new String[]{null, null, getString(R.string.wtf_this_should_never_happen), null};
                }
            } else {
                return new String[]{null, null, getString(R.string.wtf_this_should_never_happen), null};
            }
    }

    // Gets a JSONObject with the basic info (name, icon, level) and parses it. Then, returns it
    // as an array of String containing [iconId, name, level] or [null, null, errorCode].
    private String[] parseBasicInfo(JSONObject obj, String searchedName) {
        String summonerName;
        String summonerLevel;
        String summonerIconId;
        Long summonerId;
        // Parse the JSON.
        try {
            JSONObject info = obj.getJSONObject(searchedName);
            summonerName = info.getString("name");
            Long level = info.getLong("summonerLevel");
            summonerId = info.getLong("id");
            summonerLevel = getString(R.string.level_of_summoner) + " " + level.toString();
            Integer iconId = info.getInt("profileIconId");
            summonerIconId = iconId.toString();
        } catch (JSONException e) {
            LogSystem.appendLog("FATAL - Code 22 (ShowSummoner): " + e.toString(), "SummonersErrors");
            return new String[]{null, null, getString(R.string.an_unknown_error_occured) + " - " + e.toString(), null};
        }

        // Return the data.
        return new String[]{summonerIconId, summonerName, summonerLevel, summonerId.toString()};
    }

    // Called at the end of InitialLoadTask().
    private void restoreData() {
        this.menuCallable = true;
        if (this.savedState == null) { // activity created for the first time
            loadGames();
            findViewById(R.id.buttonInGame).setVisibility(View.VISIBLE);
        } else {
            try {
                if (this.savedState.getString("JSONGames") != null)
                    this.games = new JSONArray(this.savedState.getString("JSONGames"));
                else { // Load the whole recent match history (there is no data for it).
                    loadGames();
                    findViewById(R.id.buttonInGame).setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                LogSystem.appendLog("FATAL - Code 23 (ShowSummoner): " + e.toString(), "SummonersErrors");
                displayError(getString(R.string.wtf_this_should_never_happen));
            }

            this.recentGamesData = (LoLRecentGamesData) savedState.getSerializable("gamesData");
            this.inGame = this.savedState.getString("inGame");

            // Match history was already loaded.
            if (this.recentGamesData != null) {

                // In-game status was already checked, so we'll show it.
                if (this.inGame != null) {
                    TextView tV = (TextView) findViewById(R.id.textInGame);
                    tV.setVisibility(View.VISIBLE);
                    tV.setText(this.inGame);
                    initializeGamesList();

                } else { // Status not checked yet.
                    Button buttonInGame = (Button) findViewById(R.id.buttonInGame);
                    buttonInGame.setVisibility(View.VISIBLE);
                    initializeGamesList();
                }
            } else { // Match history was not loaded.
                loadGames();
                findViewById(R.id.buttonInGame).setVisibility(View.VISIBLE);
            }
        }

    }

    // The name says everything about this method.
    private void initializeGamesList() {
        ListView list;
        list = (ListView) findViewById(R.id.listViewMatches);
        list.setAdapter(new MatchesListAdapter(this.recentGamesData.getGameMode(),
                this.recentGamesData.getKDA(),
                this.recentGamesData.getCreateDate(),
                this.recentGamesData.getWin(),
                this.recentGamesData.getIcon()));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showMatch(position);
            }
        });
    }

    // This is called whenever the user clicks on a item of the recent matches list.
    // It starts the activity ShowMatchActivity.
    private void showMatch(int pos) {
        JSONObject info = null;
        try {
            info = this.games.getJSONObject(pos);
        } catch (JSONException e) {
            LogSystem.appendLog("FATAL - Code 24 (ShowSummoner): " + e.toString(), "SummonersErrors");
            displayError(getString(R.string.wtf_this_should_never_happen));
        }

        // Create an intent.
        final Intent intent = new Intent(this, ShowMatchActivity.class);

        // Code the icon of the champion in a byte array.
        Bitmap icon = this.recentGamesData.getIcon()[pos];
        if (icon != null) { // Icon may be null if the recent games load was interrupted.
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("champIcon", byteArray);
        }

        // Fill the intent and start it.
        intent.putExtra("gameInfo", info.toString());
        intent.putExtra("summonerId", getSummonerId());
        startActivity(intent);
    }

    // Used when the data is being loaded in the recent games list.
    private void updateProgressPercentage(Integer progress) {
        TextView percentage = (TextView) findViewById(R.id.textViewPercentage);
        percentage.setText(progress.toString() + " %");
    }

    // Sets the main text to "unable to load" and shows a Toast.
    private void displayError(String text) {
        TextView name = (TextView) findViewById(R.id.textName);
        name.setText(getString(R.string.unable_to_load));
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    // Shows a Toast with error info, but preserves everything else.
    private void displayPartialError(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    public void loadGames() {
        // The load of the recent games implies a considerable number of web requests.
        // It is an operation a bit costly - hell yeah!

        // Async task, babe.
        new LoadMatchesTask().execute();
    }

    private void saveRecentGamesData(LoLRecentGamesData data) {
        this.recentGamesData = data;
    }

    private void writeGlobalVariableGames(JSONArray games) {
        this.games = games;
    }

    private void writeGlobalVariableInGame(String s) {
        this.inGame = s;
    }

    private Context getContext() {
        return this;
    }

    // onClick for the button "check in game status".
    public void onClickInGame(View v) {
        new CheckInGameInfoTask().execute();
    }

    // Sets the basic info (that is: name of the summoner, level and icon).
    private void setBasicInfo() {

        TextView name = (TextView) findViewById(R.id.textName);
        TextView level = (TextView) findViewById(R.id.textLevel);
        ImageView icon = (ImageView) findViewById(R.id.imageViewIcon);
        name.setText(this.basicInfo[1]);
        level.setText(this.basicInfo[2]);
        new DownloadImageTask(getContext(), icon, true, true)
                .execute("http://ddragon.leagueoflegends.com/cdn/" + this.VERSION + "/img/profileicon/" +
                        this.basicInfo[0] + ".png");
    }

    private Long getSummonerId() {
        return Long.parseLong(this.basicInfo[3]);
    }

    private String getSummonerName() {
        return this.basicInfo[1];
    }

    private void writeGlobalVariableBasicInfo(String[] basicInfo) {
        this.basicInfo = basicInfo;
    }

    // Called by the asyncTask that must load the in-game data.
    // This will return a String that will be put on a TextView.
    private String loadInGameData() {
        LoLResponse response1;
        response1 = SendRequest.get("https://" + REGION + ".api.pvp.net/observer-mode/rest/consumer/getSpectatorGameInfo/" +
                PLATFORM + "/" + getSummonerId() + "?api_key=" + StaticKeyData.KEY);
        switch (response1.getStatus()) {
            case 404: {
                return (getSummonerName() + " " + getString(R.string.is_not_currently_in_a_game));
            }
            case 429: {
                return getString(R.string.rate_limit_exceeded);
            }
            case 200: {
                final Intent intent = new Intent(this, ShowInGameInfoActivity.class);
                intent.putExtra("info", response1.getJsonObject().toString());
                startActivity(intent);
                return getSummonerName() + " " + getString(R.string.is_playing) + " " +
                        LoLDataParser.getGameMode(this, response1.getJsonObject()) +
                        (LoLDataParser.getGameType(this, response1.getJsonObject())) +
                        " (" + LoLDataParser.getGameQueueConfigId(this, response1.getJsonObject()) + ")";
            }
            default: {
                return (getString(R.string.some_data_could_not_be_loaded));
            }
        }
    }

    private boolean checkNotificationsActivated() {
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String latestMatchId = prefs.getString(name, null);

        if (latestMatchId == null) {
            Log.d("NOTIFICATIONS", "latestMatchId es NULL (devolviendo notifications are off)");
            return false;
        } else {
            Log.d("NOTIFICATIONS", "latestMatchId = " + latestMatchId);
            return true;
        }

    }

    /*
    Subclasses
    */

    // Subclass LoadMatchesTask. AsyncTask for loading the matches.
    private class LoadMatchesTask extends AsyncTask<Void, Integer, LoLRecentGamesData> {

        @Override
        protected void onPreExecute() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarLoadingGames);
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected LoLRecentGamesData doInBackground(Void... voids) {

            // BE AWARE: this uses the Global Variable summonerId, and it is not set until the end
            // basic information has been loaded.
            LoLResponse loLResponse =
                    SendRequest.get("https://" + REGION + ".api.pvp.net/api/lol/" +
                            REGION + "/v1.3/game/by-summoner/" + getSummonerId() + "/recent?api_key=" + StaticKeyData.KEY);
            if (loLResponse.getStatus() == 200) {

                LoLRecentGamesData gamesData = null;
                String[] championId = null;
                String[] championKey = null;
                Integer numberOfGames = 0;
                try {
                    writeGlobalVariableGames(loLResponse.getJsonObject().getJSONArray("games"));

                    numberOfGames = games.length();
                    // Initialize the variables.
                    gamesData = new LoLRecentGamesData(numberOfGames);
                    championId = new String[numberOfGames];
                    championKey = new String[numberOfGames];

                    // Get the data for each game.
                    for (int i = 0; i < numberOfGames; i++) {
                        gamesData.setGameMode(LoLDataParser.getGameMode(getContext(), games.getJSONObject(i)) + " (" +
                                LoLDataParser.getGameSubtype(getContext(), games.getJSONObject(i)) + ")" +
                                LoLDataParser.getGameType(getContext(), games.getJSONObject(i)), i);
                        gamesData.setKDA("KDA: " + LoLDataParser.getKDA(games.getJSONObject(i)) +
                                LoLDataParser.getLargestMultikill(getContext(), games.getJSONObject(i)), i);
                        gamesData.setCreateDate(LoLDataParser.getCreateDate(getContext(), games.getJSONObject(i)), i);
                        gamesData.setWin(LoLDataParser.getVictory(getContext(), games.getJSONObject(i)), i);
                        championId[i] = LoLDataParser.getChampionId(getContext(), games.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    LogSystem.appendLog("FATAL - Code 25 (ShowSummoner): " + e.toString(), "SummonersErrors");
                    Log.e("SummonersApp", "JSON Error" + e.toString());
                }


                boolean alreadyFound;
                // And now take the data and put it clean and ordered in an object gamesData.
                // First step: get the names of the championIds we have (in order to get their icon image).
                for (int i = 0; i < numberOfGames; i++) { // 10 requests (maximun)! this is hard!
                    publishProgress(i * 5);

                    // This first complicated shit is used to optimize the load. OK, i'll explain it...
                    // "if the name wasn't already loaded (it's not in preferences) load it and
                    // save it to the preferences. Else, do nothing. The names will be loaded from
                    // preferences. Okay, this was not so complicated...
                    SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
                    String name = prefs.getString("champ" + championId[i], null);

                    // Not loaded yet.
                    if (name == null) {
                        loLResponse = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" +
                                REGION + "/v1.2/champion/" + championId[i] + "?api_key=" + StaticKeyData.KEY);
                        try {
                            if (loLResponse.getStatus() == 200) {
                                String theKeyName = loLResponse.getJsonObject().getString("key");
                                SharedPreferences.Editor editor =
                                        getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                                editor.putString("champ" + championId[i], theKeyName);
                                editor.commit();
                            }
                        } catch (JSONException e) {
                            LogSystem.appendLog("FATAL - Code 26 (ShowSummoner): " + e.toString(), "SummonersErrors");
                            Log.d("SummonersApp", "An error occured: " + e.toString());
                        }
                    }

                    // And now load it from the preferences.
                    championKey[i] = prefs.getString("champ" + championId[i], "");
                }

                // Second step: download the images (of the champ's icons).
                for (int i = 0; i < numberOfGames; i++) { // 10 requests! this is hard!
                    alreadyFound = false;
                    // Optimizer ("dont load something if it has been loaded before!") - easy.
                    for (int subIndex = 0; subIndex < i; subIndex++) {
                        if (championId[i].equals(championId[subIndex])) {
                            gamesData.setIcon(gamesData.getIcon()[subIndex], i);
                            alreadyFound = true;
                            break;
                        }
                    }
                    publishProgress(50 + i * 5);
                    // And now, do the normal requests.
                    if (!alreadyFound)
                        try {
                            InputStream in = new java.net.URL("http://ddragon.leagueoflegends.com/cdn/" + getVersion() + "/img/champion/" +
                                    championKey[i] + ".png").openStream();
                            Bitmap image = BitmapFactory.decodeStream(in);
                            gamesData.setIcon(image, i);
                        } catch (Exception e) {
                            LogSystem.appendLog("FATAL - Code 27 (ShowSummoner): " + e.toString(), "SummonersErrors");
                            Log.d("SummonersApp", "An error occured: " + e.toString());
                        }
                }
                return gamesData;
            } else
                return null;
        }

        @Override
        protected void onPostExecute(LoLRecentGamesData gamesData) {
            TextView percentage = (TextView) findViewById(R.id.textViewPercentage);
            percentage.setText("");

            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarLoadingGames);
            bar.setVisibility(View.INVISIBLE);

            if (gamesData == null)
                displayPartialError(getString(R.string.something_went_wrong_loading_the_recent_matches));
            else {
                saveRecentGamesData(gamesData);
                // Put the adapter and everything >.<
                initializeGamesList();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateProgressPercentage(values[0]);
        }
    }

    // Subclass InitialLoadTask. AsyncTask for loading the basic info.
    private class InitialLoadTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            String name = strings[0];
            return downloadBasicInformation(name);
        }

        @Override
        protected void onPostExecute(String[] basicInfo) {
            // This means that there was no error.
            if (basicInfo[0] != null) {
                writeGlobalVariableBasicInfo(basicInfo);
                setBasicInfo();
                // This will restore the recent matches data, or set the visibility of the button
                // "load recent matches" to TRUE, so that user can load it.
                restoreData();
            } else {
                // The error code is passed in basicInfo[2]. Why? Because YOLO.
                displayError(basicInfo[2]);
            }
        }
    }

    // Subclass CheckInGameInfoTask. AsyncTask for loading the in-game data.
    private class CheckInGameInfoTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            Button b = (Button) findViewById(R.id.buttonInGame);
            b.setVisibility(View.INVISIBLE);
            TextView tV = (TextView) findViewById(R.id.textInGame);
            tV.setVisibility(View.VISIBLE);
            tV.setText(getString(R.string.loading_game_data));
        }

        @Override
        protected String doInBackground(Void... voids) {
            return loadInGameData();
        }

        @Override
        protected void onPostExecute(String s) {

            // If the result is null, then the summoner was in-game and another activity is being created.
            // Else, he/she was not in game or an error occured.
            TextView tV = (TextView) findViewById(R.id.textInGame);

            writeGlobalVariableInGame(s);
            tV.setText(s);
        }
    }

    // Subclass MatchesListTask. Extends BaseAdapter.
    class MatchesListAdapter extends BaseAdapter {
        String[] mode, kda, date, win;
        Bitmap[] championImage;

        public MatchesListAdapter(String[] text, String[] text1, String[] text2, String[] text3, Bitmap[] icon) {
            mode = text;
            kda = text1;
            date = text2;
            win = text3;
            championImage = icon;
        }

        public int getCount() {
            return mode.length;
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Inflate things the normal way.
            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.match_item, parent, false);
            // Initialize the variables.
            TextView tV_title, tV_subtitle1, tV_subtitle2, tV_titleRight;
            ImageView imageChamp;
            tV_title = (TextView) row.findViewById(R.id.textViewTitle);
            tV_subtitle1 = (TextView) row.findViewById(R.id.textViewSubtitle1);
            tV_subtitle2 = (TextView) row.findViewById(R.id.textViewSubtitle2);
            tV_titleRight = (TextView) row.findViewById(R.id.textViewTitleRight);
            imageChamp = (ImageView) row.findViewById(R.id.championIcon);
            // Set the data.
            tV_title.setText(mode[position]);
            tV_subtitle1.setText(kda[position]);
            tV_subtitle2.setText(date[position]);
            tV_titleRight.setText(win[position]);
            if (win[position].equals(getString(R.string.victory)))
                tV_titleRight.setTextColor(Color.rgb(10, 200, 20));
            else
                tV_titleRight.setTextColor(Color.rgb(210, 15, 5));
            imageChamp.setImageBitmap(championImage[position]);
            return (row);
        }
    }
}
