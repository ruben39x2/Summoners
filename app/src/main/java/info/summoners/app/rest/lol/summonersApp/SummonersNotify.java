package info.summoners.app.rest.lol.summonersApp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import info.summoners.app.rest.lol.util.LoLResponse;

public class SummonersNotify extends BroadcastReceiver {
    private NotificationManager notificationManager;
    private int notificationID = 987;


    @Override
    public void onReceive(Context context, Intent intent) {
        new checkMatches(context).execute();
    }



    private class checkMatches extends AsyncTask<Void, Void, String> {
        Context context;
        String REGION;

        checkMatches(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            LogSystem.appendLog("Running service of notifications...", "SummonersNotify");

            SharedPreferences prefs = context.getSharedPreferences("Summoners", context.MODE_PRIVATE);
            this.REGION = prefs.getString("region", "EUW");

            notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            List<String> names = new ArrayList<>();
            String friends = prefs.getString("friendList", "");

            if (!friends.equals("")) {
                String[] friendsArray = friends.split(" ");
                for (String aFriendsArray : friendsArray) {
                    String latestMatchId = prefs.getString(aFriendsArray, null);
                    // If notifications are activated for that summoner
                    if (latestMatchId != null) {
                        LogSystem.appendLog("Getting ID and matches for: " + aFriendsArray, "SummonersNotify");
                        String id = getSummonerId(aFriendsArray);
                        if (id == null) {
                            LogSystem.appendLog("Error - couldn't get summoner ID while running a service: " + aFriendsArray, "SummonersNotify");
                            return null;
                        }
                        LoLResponse loLResponse =
                                SendRequest.get("https://" + REGION + ".api.pvp.net/api/lol/" +
                                        REGION + "/v1.3/game/by-summoner/" + id + "/recent?api_key=" + StaticKeyData.KEY);
                        if (loLResponse.getStatus() == 200) {
                            try {
                                JSONArray info = loLResponse.getJsonObject().getJSONArray("games");
                                Long trueLatestMatchId = info.getJSONObject(0).getLong("gameId");

                                // NOW ...


                                if (!latestMatchId.equals(trueLatestMatchId.toString())) {
                                    // If there's a recent game...

                                    names.add(aFriendsArray);

                                    // And actualize the latest match id
                                    SharedPreferences.Editor editor =
                                            context.getSharedPreferences("Summoners", context.MODE_PRIVATE).edit();
                                    editor.putString(aFriendsArray, trueLatestMatchId.toString());
                                    editor.commit();
                                }
                            } catch (JSONException e) {
                                return null;
                            }
                        } else {
                            LogSystem.appendLog("Error - couldn't get last match ID while running a service: " + aFriendsArray, "SummonersNotify");
                            return null;
                        }
                    } else {
                        LogSystem.appendLog("Not necessary to send a request for: " + aFriendsArray, "SummonersNotify");
                    }

                    SystemClock.sleep(2000); // Let's avoid Ratio Limit Exceeded ! (too much petitions)
                } // after the for-each

                if (names.size() > 0) {
                    String text = getNotificationText(names, context);
                    return text;

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(context.getString(R.string.app_name))
                                .setContentText(s);

                Intent targetIntent = new Intent(context, MySummonersActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                Notification nc = builder.build();
                nc.flags |= NotificationCompat.FLAG_AUTO_CANCEL;
                notificationManager.notify(notificationID, nc);
                notificationID++;
                super.onPostExecute(s);
            }
        }


        private String getNotificationText(List<String> names, Context context) {
            if (names.size() == 1) {
                return names.get(0) + " " + context.getString(R.string.played_recently_lol);
            } else if (names.size() == 2) {
                return names.get(0) + " " + context.getString(R.string.and) + " " + names.get(1) + " " + context.getString(R.string.played_recently_lol_they);
            } else {
                String theNames = "";
                for (int i = 0; i < names.size(); i++) {
                    if (i != names.size() - 1)
                        theNames += names.get(i) + ", ";
                    else
                        theNames = theNames.substring(0, theNames.length() - 2) + " " + context.getString(R.string.and) + " " + names.get(i) + " " + context.getString(R.string.played_recently_lol_they);
                }
                return theNames;
            }
        }

        private String getSummonerId(String name) {
            LoLResponse loLResponse;
            String encodedName;

            // Encode the name (replace " " to %20 and those things).
            try {
                encodedName = name.toLowerCase();
                URLEncoder.encode(encodedName, "utf-8");
            } catch (UnsupportedEncodingException e) {
                LogSystem.appendLog("FATAL - Code 21 (ShowSummoner): " + e.toString(), "SummonersNotify");
                return null;
            }

            String request = "https://" + REGION + ".api.pvp.net/api/lol/" +
                    REGION + "/v1.4/summoner/by-name/" + encodedName + "?api_key=" + StaticKeyData.KEY;
            // Create this to get the result of the petition (of basic info about a summoner).
            loLResponse = SendRequest.get(request);
            if (loLResponse.getStatus() == 200) {
                // Everything went OK.
                try {
                    JSONObject info = loLResponse.getJsonObject().getJSONObject(encodedName);
                    Long summonerId = info.getLong("id");
                    return summonerId.toString();
                } catch (JSONException e) {
                    Log.d("Aqui", "la estamos cagando - JSON Error");
                    return null;
                }

            } else {
                Integer a = loLResponse.getStatus();
                Log.d("Aqui", "la estamos cagando" + a.toString() + loLResponse.getError());
                return null;
            }
        }
    }
}
