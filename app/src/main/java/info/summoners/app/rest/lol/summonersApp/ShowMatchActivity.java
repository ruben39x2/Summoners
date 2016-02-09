package info.summoners.app.rest.lol.summonersApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import info.summoners.app.rest.lol.util.LoLDataParser;
import info.summoners.app.rest.lol.util.LoLResponse;

// ShowMatchActivity.java

// Shows info about a match. This activity gets an intent with a JSON (and more) and uses that data,
// after that, it starts a thread (asyncTask) to load the images for the items, team names...
// NOTE: The execution flow should be the following.
// 1- The activity gets the intent and starts the procedure "loadChampsAndItems"
// 2- That procedure creates a new AsyncTask "loadFellowsAndSpellsTask"
// 3- That task loads the names of the fellow players and onPostExecute, starts "DownloadItemsTask"
// 4- DownloadItemsTask load asynchronously the images of the items (also summoner spells).

public class ShowMatchActivity extends ActionBarActivity {
    private String REGION;
    private String VERSION;

    /*
    Methods from the superclass.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_match);

        // Get the intent and take the data.
        Intent intent = getIntent();
        Long summonerId = intent.getLongExtra("summonerId", 0);
        Bitmap championImage = null;
        // Only decode the byte array if it exists in the intent (Remember: if the matches were
        // not correctly loaded in the showSummonerActivity because the load was interrupted...
        // ...then this'll be null!)
        byte[] byteArray = getIntent().getByteArrayExtra("champIcon");
        if (byteArray != null)
            championImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // Get region from preferences.
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        this.VERSION = prefs.getString("version", "5.6.1");
        this.REGION = prefs.getString("region", "EUW").toLowerCase();

        // Initialize the JSON.
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getStringExtra("gameInfo"));
        } catch (JSONException e) {
            LogSystem.appendLog("FATAL - Code 14 (ShowMatch): " + e.toString(), "SummonersErrors");
            Log.d("SummonersApp", "An error occured: " + e.toString());
            Toast.makeText(getApplicationContext(),
                    getString(R.string.wtf_this_should_never_happen),
                    Toast.LENGTH_LONG).show();
        }

        // Set the data.
        if (championImage != null) setChampionIcon(championImage);
        setMainTexts(data);
        loadChampsAndItems(data, summonerId); // this will be threaded
    }

    /*
    Methods of the activity.
    */

    private String getVersion() {
        return this.VERSION;
    }

    // Just set the champion icon.
    private void setChampionIcon(Bitmap icon) {
        ImageView im1 = (ImageView) findViewById(R.id.imageViewMatchPlayedChamp);
        im1.setImageBitmap(icon);
    }

    // Set the main texts.
    private void setMainTexts(JSONObject data) {
        // Initialize the variables.
        TextView tVmatchTitle, tVmatchSubtitle1, tVmatchSubtitle2,
                tVkills, tVdeaths, tVassists, tVmultikill, tVkillingspree, tVturrets, tVinhibitors,
                tVminions, tVneutralMinions, tVgoldEarned, tVlevel, tVtimePlayed, tVIPEarned;
        tVmatchTitle = (TextView) findViewById(R.id.textViewMatchTitle);
        tVmatchSubtitle1 = (TextView) findViewById(R.id.textViewMatchSubtitle);
        tVmatchSubtitle2 = (TextView) findViewById(R.id.textViewMatchSubtitle2);
        tVkills = (TextView) findViewById(R.id.textViewKills);
        tVdeaths = (TextView) findViewById(R.id.textViewDeaths);
        tVassists = (TextView) findViewById(R.id.textViewAssists);
        tVmultikill = (TextView) findViewById(R.id.textViewLargestMultikill);
        tVkillingspree = (TextView) findViewById(R.id.textViewLargestKillingspree);
        tVturrets = (TextView) findViewById(R.id.textViewTurretsKilled);
        tVinhibitors = (TextView) findViewById(R.id.textViewBarracksKilled);
        tVminions = (TextView) findViewById(R.id.textViewMinionsKilled);
        tVneutralMinions = (TextView) findViewById(R.id.textViewNeutralMinionsKilled);
        tVgoldEarned = (TextView) findViewById(R.id.textViewGoldEarned);
        tVlevel = (TextView) findViewById(R.id.textViewLevel);
        tVtimePlayed = (TextView) findViewById(R.id.textViewTimePlayed);
        tVIPEarned = (TextView) findViewById(R.id.textViewIPEarned);

        // And now... set them.
        tVmatchTitle.setText(LoLDataParser.getGameMode(this, data) + " (" +
                LoLDataParser.getGameSubtype(this, data) + ")" +
                LoLDataParser.getGameType(this, data));
        tVmatchSubtitle1.setText(LoLDataParser.getVictory(this, data));
        tVmatchSubtitle2.setText(LoLDataParser.getCreateDate(this, data));
        tVkills.setText(getString(R.string.kills) + " " + LoLDataParser.getKills(data));
        tVdeaths.setText(getString(R.string.deaths) + " " + LoLDataParser.getDeaths(data));
        tVassists.setText(getString(R.string.assists) + " " + LoLDataParser.getAssists(data));
        tVmultikill.setText(getString(R.string.largest_multikill) + " " + LoLDataParser.getLargestMultikillSimpleFormat(data));
        tVkillingspree.setText(getString(R.string.largest_killingspree) + " " + LoLDataParser.getLargestKillingspree(data));
        tVturrets.setText(getString(R.string.turrets_destroyed) + " " + LoLDataParser.getTurretsDestroyed(data));
        tVinhibitors.setText(getString(R.string.inhibitors_destroyed) + " " + LoLDataParser.getInhibitorsDestroyed(data));
        tVminions.setText(getString(R.string.minions_killed) + " " + LoLDataParser.getMinionsKilled(data));
        tVneutralMinions.setText(getString(R.string.neutral_minions_killed) + " " + LoLDataParser.getNeutralMinionsKilled(data));
        tVgoldEarned.setText(getString(R.string.gold_earned) + " " + LoLDataParser.getGoldEarned(data));
        tVlevel.setText(getString(R.string.level) + " " + LoLDataParser.getChampionLevel(this, data));
        tVtimePlayed.setText(getString(R.string.time_played) + " " + LoLDataParser.getTimePlayed(this, data));
        tVIPEarned.setText(getString(R.string.ip_earned) + " " + LoLDataParser.getIPEarned(this, data));
    }

    // Starts an asyncTask to load the name of the fellow participants and the name of the spells;
    // at the end of that asyncTask, another asyncTask will be called so that the items are loaded.
    private void loadChampsAndItems(JSONObject data, Long summId) {
        List<String> team100 = LoLDataParser.getTeam100(data, summId);
        List<String> team200 = LoLDataParser.getTeam200(data, summId);

        new LoadFellowsAndSpellsTask(team100, team200,
                LoLDataParser.getSpell1(data), // May return null...
                LoLDataParser.getSpell2(data),
                data).execute();
    }

    private void setItemIcons(JSONObject data, String spellKey1, String spellKey2) {
        ImageView[] img = new ImageView[9];
        img[0] = (ImageView) findViewById(R.id.imageViewItem1);
        img[1] = (ImageView) findViewById(R.id.imageViewItem2);
        img[2] = (ImageView) findViewById(R.id.imageViewItem3);
        img[3] = (ImageView) findViewById(R.id.imageViewItem4);
        img[4] = (ImageView) findViewById(R.id.imageViewItem5);
        img[5] = (ImageView) findViewById(R.id.imageViewItem6);
        img[6] = (ImageView) findViewById(R.id.imageViewItem7);
        img[7] = (ImageView) findViewById(R.id.imageViewSummonerSpell1);
        img[8] = (ImageView) findViewById(R.id.imageViewSummonerSpell2);

        String[] items = new String[9];
        items[0] = LoLDataParser.getItem0(data); // May return -1.
        items[1] = LoLDataParser.getItem1(data);
        items[2] = LoLDataParser.getItem2(data);
        items[3] = LoLDataParser.getItem3(data);
        items[4] = LoLDataParser.getItem4(data);
        items[5] = LoLDataParser.getItem5(data);
        items[6] = LoLDataParser.getItem6(data);
        items[7] = spellKey1;
        items[8] = spellKey2;

        new DownloadItemsTask(img, items).execute();
    }

    private void setTeams(List<String> team1, List<String> team2) {
        TextView s101, s102, s103, s104, s105, s106, s201, s202, s203, s204, s205, s206;
        s101 = (TextView) findViewById(R.id.textViewSummoner101);
        s102 = (TextView) findViewById(R.id.textViewSummoner102);
        s103 = (TextView) findViewById(R.id.textViewSummoner103);
        s104 = (TextView) findViewById(R.id.textViewSummoner104);
        s105 = (TextView) findViewById(R.id.textViewSummoner105);
        s106 = (TextView) findViewById(R.id.textViewSummoner106);
        s201 = (TextView) findViewById(R.id.textViewSummoner201);
        s202 = (TextView) findViewById(R.id.textViewSummoner202);
        s203 = (TextView) findViewById(R.id.textViewSummoner203);
        s204 = (TextView) findViewById(R.id.textViewSummoner204);
        s205 = (TextView) findViewById(R.id.textViewSummoner205);
        s206 = (TextView) findViewById(R.id.textViewSummoner206);

        if (0 < team1.size()) s101.setText(team1.get(0));
        else s101.setText("");
        if (1 < team1.size()) s102.setText(team1.get(1));
        else s102.setText("");
        if (2 < team1.size()) s103.setText(team1.get(2));
        else s103.setText("");
        if (3 < team1.size()) s104.setText(team1.get(3));
        else s104.setText("");
        if (4 < team1.size()) s105.setText(team1.get(4));
        else s105.setText("");
        if (5 < team1.size()) s106.setText(team1.get(5));
        else s106.setText("");

        if (0 < team2.size()) s201.setText(team2.get(0));
        else s201.setText("");
        if (1 < team2.size()) s202.setText(team2.get(1));
        else s202.setText("");
        if (2 < team2.size()) s203.setText(team2.get(2));
        else s203.setText("");
        if (3 < team2.size()) s204.setText(team2.get(3));
        else s204.setText("");
        if (4 < team2.size()) s205.setText(team2.get(4));
        else s205.setText("");
        if (5 < team2.size()) s206.setText(team2.get(5));
        else s206.setText("");
    }

    /*
    Subclasses.
    */

    // Subclass LoadFellowsAndSpellsTask. That's what it does. OnPostExecute, creates a new
    // DownloadItemsTask and gets the items.
    private class LoadFellowsAndSpellsTask extends AsyncTask<Void, Void, Integer> {
        private JSONObject gameData;
        private String team100commas = "", team200commas = "";
        private String spellId1, spellId2;
        private String spellKey1, spellKey2;
        private List<String> team100list, team200list, team100names, team200names;

        public LoadFellowsAndSpellsTask(List<String> teamId100, List<String> teamId200,
                                        String summonerSpell1Id, String summonerSpell2Id,
                                        JSONObject gameData) {
            this.gameData = gameData;
            this.team100commas = "";
            this.team200commas = "";
            if (teamId100 != null)
                for (String s : teamId100) this.team100commas += (s + ",");
            if (teamId200 != null)
                for (String s : teamId200) this.team200commas += (s + ",");

            this.spellId1 = summonerSpell1Id;
            this.spellId2 = summonerSpell2Id;

            this.team100list = teamId100;
            this.team200list = teamId200;

            this.team100names = new LinkedList<>();
            this.team200names = new LinkedList<>();
        }

        @Override
        protected void onPreExecute() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarLoadingItems);
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... nothing) {
            LoLResponse loLResponse1, loLResponse2, loLResponse3, loLResponse4;
            SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);

            // First: Get the names of the spells (from preferences; if they are not saved yet,
            // get them and save them)

            String spell1 = prefs.getString("spell" + spellId1, null);
            String spell2 = prefs.getString("spell" + spellId2, null);

            // Not loaded yet.
            if (spell1 == null) {
                loLResponse1 = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" +
                        REGION + "/v1.2/summoner-spell/" + spellId1 + "?api_key=" + StaticKeyData.KEY);
                try {
                    SharedPreferences.Editor editor =
                            getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                    String key = loLResponse1.getJsonObject().getString("key");
                    editor.putString("spell" + spellId1, key);
                    editor.commit();
                } catch (Exception e) {
                    LogSystem.appendLog("FATAL - Code 16 (ShowMatch): " + e.toString(), "SummonersErrors");
                    Log.d("SummonersApp", "An error occured: " + e.toString());
                    return -1;
                }
            }

            if (spell2 == null) {
                loLResponse2 = SendRequest.get("https://global.api.pvp.net/api/lol/static-data/" +
                        REGION + "/v1.2/summoner-spell/" + spellId2 + "?api_key=" + StaticKeyData.KEY);
                try {
                    SharedPreferences.Editor editor =
                            getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                    String key = loLResponse2.getJsonObject().getString("key");
                    editor.putString("spell" + spellId2, key);
                    editor.commit();
                } catch (Exception e) {
                    LogSystem.appendLog("FATAL - Code 17 (ShowMatch): " + e.toString(), "SummonersErrors");
                    Log.d("SummonersApp", "An error occured: " + e.toString());
                    return -1;
                }
            }

            this.spellKey1 = prefs.getString("spell" + spellId1, "");
            this.spellKey2 = prefs.getString("spell" + spellId2, "");


            // Second: About the team participants.

            if (!team100commas.equals(""))  //if team 100 is composed by at least one human player
                loLResponse3 = SendRequest.get("https://" + REGION + ".api.pvp.net/api/lol/" +
                        REGION + "/v1.4/summoner/" + this.team100commas + "/name?api_key=" + StaticKeyData.KEY);
            else
                loLResponse3 = null;
            if (!team200commas.equals("")) //if team 200 is composed by at least one human player
                loLResponse4 = SendRequest.get("https://" + REGION + ".api.pvp.net/api/lol/" +
                        REGION + "/v1.4/summoner/" + this.team200commas + "/name?api_key=" + StaticKeyData.KEY);
            else
                loLResponse4 = null;


            // Team 100.

            if (loLResponse3 == null) {
                // We have a null! Dammit!
                this.team100names.add(getString(R.string.unknown));
            } else {
                // Everything went ok!
                if (loLResponse3.getStatus() == 200) {
                    if (!team100commas.equals(""))  //if team 100 is composed by at least one human player
                        for (String s : team100list) {
                            // Fill the names list with the response from the web
                            try {
                                this.team100names.add(loLResponse3.getJsonObject().getString(s));
                            } catch (JSONException e) {
                                LogSystem.appendLog("FATAL - Code 18 (ShowMatch): " + e.toString(), "SummonersErrors");
                                Log.d("SummonersApp", "An error occured: " + e.toString());
                                this.team100names.add(getString(R.string.unknown));
                                this.team200names.add(getString(R.string.unknown));
                                return -1;
                            }
                        }
                    else {
                        // Possibly bots
                        this.team100names.add(getString(R.string.bots));
                    }
                } else
                    // Not 200 OK... fuck...
                    this.team100names.add(getString(R.string.unknown));
            }

            // Team 200.

            if (loLResponse4 == null) {
                // We have a null! Dammit!
                this.team200names.add(getString(R.string.unknown));
            } else {
                // Everything went ok!
                if (loLResponse4.getStatus() == 200) {
                    if (!team200commas.equals(""))  //if team 200 is composed by at least one human player
                        for (String s : team200list) {
                            // Fill the names list with the response from the web
                            try {
                                this.team200names.add(loLResponse4.getJsonObject().getString(s));
                            } catch (JSONException e) {
                                LogSystem.appendLog("FATAL - Code 19 (ShowMatch): " + e.toString(), "SummonersErrors");
                                Log.d("SummonersApp", "An error occured: " + e.toString());
                                this.team100names.add(getString(R.string.unknown));
                                this.team200names.add(getString(R.string.unknown));
                                return -1;
                            }
                        }
                    else {
                        // Possibly bots
                        this.team200names.add(getString(R.string.bots));
                    }
                } else
                    // Not 200 OK...
                    this.team200names.add(getString(R.string.unknown));
            }
            return 0;

        }

        @Override
        protected void onPostExecute(Integer a) {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarLoadingItems);
            bar.setVisibility(View.INVISIBLE);
            if (a != -1) {
                setTeams(this.team100names, this.team200names);
                setItemIcons(this.gameData, spellKey1, spellKey2);
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.some_data_could_not_be_loaded),
                        Toast.LENGTH_LONG).show();
            }

        }
    }

    // Subclass DownloadItemsTask. It is used to download asynchronously the data of the items, but
    // also of the summoner's spells.
    private class DownloadItemsTask extends AsyncTask<Void, Void, Bitmap[]> {
        private ImageView[] bmItems;
        private String[] items;

        public DownloadItemsTask(ImageView[] bmItems, String[] items) {
            this.bmItems = bmItems;
            this.items = items;
        }

        @Override
        protected void onPreExecute() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarLoadingItems);
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap[] doInBackground(Void... nothing) {
            Bitmap images[] = new Bitmap[9];
            // Take the icon items.
            for (int i = 0; i < 7; i++) {
                if (!items[i].equals("-1")) {
                    try {
                        InputStream in = new java.net.URL("http://ddragon.leagueoflegends.com/cdn/" + getVersion() + "/img/item/" +
                                items[i] + ".png").openStream();
                        Bitmap image = BitmapFactory.decodeStream(in);
                        images[i] = Bitmap.createScaledBitmap(image, 64, 64, false);
                    } catch (Exception e) {
                        LogSystem.appendLog("Code 15 (ShowMatch): " + e.toString(), "SummonersErrors");
                        Log.d("SummonersApp", "An error occured: " + e.toString());
                        this.items[i] = "-1";
                    }
                }
            }
            // And now the summoner spells.
            if (items[7] != null)
                try {
                    InputStream in = new java.net.URL("http://ddragon.leagueoflegends.com/cdn/" + getVersion() + "/img/spell/" +
                            items[7] + ".png").openStream();
                    Bitmap image = BitmapFactory.decodeStream(in);
                    images[7] = Bitmap.createScaledBitmap(image, 64, 64, false);
                } catch (Exception e) {
                    this.items[7] = "-1";
                }
            if (items[8] != null)
                try {
                    InputStream in = new java.net.URL("http://ddragon.leagueoflegends.com/cdn/" + getVersion() + "/img/spell/" +
                            items[8] + ".png").openStream();
                    Bitmap image = BitmapFactory.decodeStream(in);
                    images[8] = Bitmap.createScaledBitmap(image, 64, 64, false);
                } catch (Exception e) {
                    this.items[8] = "-1";
                }
            return images;
        }

        @Override
        protected void onPostExecute(Bitmap[] result) {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarLoadingItems);
            bar.setVisibility(View.INVISIBLE);
            for (int i = 0; i < 9; i++)
                if (!items[i].equals("-1"))
                    bmItems[i].setImageBitmap(result[i]);
                else {
                    Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.no_item);
                    bmItems[i].setImageBitmap(Bitmap.createScaledBitmap(image, 64, 64, false));
                }
        }
    }
}
