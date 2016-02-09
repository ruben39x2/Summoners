package info.summoners.app.rest.lol.summonersApp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

// MySummonersActivity.java

// Launcher activity of the app. It shows a List of the summoners saved by the user.

public class MySummonersActivity extends ActionBarActivity {
    private final Context context = this;
    private final int MAX_SUMMONERS = 40;
    private ArrayList<String> summonersList;
    private ListView myList;
    private ArrayAdapter <String> adapter;

    // The following things are used to log the uncaught exceptions in a file. They are not
    // really relevant for the app functionality, but also important.
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    String error = "-----------------------------------------\nFATAL - Black Code: An unhandled exception has occured!\n";
                    error += "Reason of the error: " + ex.getMessage() + "\nTRACE:\n";
                    StackTraceElement[] trace = ex.getStackTrace();
                    for (StackTraceElement aTrace : trace) error += aTrace.toString() + "\n";
                    error += "CAUSE:\n";
                    StackTraceElement[] cause = ex.getCause().getStackTrace();
                    for (StackTraceElement aCause : cause) error += aCause.toString() + "\n";
                    error += "-----------------------------------------\n";
                    LogSystem.appendLog(error, "SummonersErrors");
                    // Re-throw critical exception further to the os (important)
                    defaultUEH.uncaughtException(thread, ex);
                }
            };

    /*
    Methods from the superclass.
     */

    // Launched when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the view.
        setContentView(R.layout.activity_my_summoners);
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        // Setup handler for uncaught exception.
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
        // Initialize the variable of the summoners.
        this.summonersList = new ArrayList<>(MAX_SUMMONERS);

        // Get the summoners data from preferences.
        // NOTE: Summoners list data is stored as one string. Different summoners are separated
        // by white spaces.
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        String friends = prefs.getString("friendList", "");
        if (!friends.equals("")) {
            String[] friendsArray = friends.split(" ");
            // Store the summoners on an Array type variable.
            for (int i = 0; i < friendsArray.length; i++)
                this.summonersList.add(friendsArray[i]);
        }

        // Initialize the list.
        this.myList = (ListView) findViewById(R.id.listViewSummoners);

        this.adapter = new ArrayAdapter<>(context,
                R.layout.summoner_item, R.id.textViewSummonerListName, summonersList);
        this.myList.setAdapter(adapter);

        // Initialize the onClickListeners for the list.
        this.myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showSummoner(summonersList.get(position));
            }
        });

        this.myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteSummoner(i);
                return true;
            }
        });

        // If there are no summoners, set this text to visible.
        if (this.summonersList.isEmpty()) {
            findViewById(R.id.textViewYouHaveNoSummoners).setVisibility(View.VISIBLE);
        }

        boolean isServiceRunning = prefs.getBoolean("isServiceRunning", false);
        if (!isServiceRunning) {
            Calendar cal = Calendar.getInstance();
            Intent i = new Intent(this, SummonersNotify.class);
            PendingIntent pintent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60*60*2*1000, pintent);
            SharedPreferences.Editor editor =
                    getSharedPreferences("Summoners", MODE_PRIVATE).edit();
            editor.putBoolean("isServiceRunning", true);
            editor.commit();
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_summoners, menu);
      /*  final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        View v = findViewById(R.id.busqueda);
        KeyEvent keyEvent = new KeyEvent(1, );
        final SearchView searchView = (SearchView) v; //menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        switch (id) {
            // Four posibilities of the menu:
            // - Add a new summoner.
            // - Change the Region in preferences (EUW, NA...)
            // - Change patch version.
            // - Show About.

            case R.id.action_add: {
                addSummoner();
                return true;
            }
            case R.id.action_preferences: {
                changePreferences();
                return true;
            }
            case R.id.action_about: {
                showAbout();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Methods of the activity
    */

    // Starts the activity "show info about a summoner".
    private void showSummoner(String summonerName) {
        final Intent intent = new Intent(this, ShowSummonerActivity.class);
        intent.putExtra("name", summonerName);
        startActivity(intent);
    }

    // Deletes an item from the summonersList (also from the preferences).
    private void deleteSummoner(int i) {
        final int position = i;
        // Build a dialog.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false)
                .setTitle(getString(R.string.delete_summoner))
                .setMessage(getString(R.string.are_you_sure_want_to_delete_summoner) + " \""
                        + this.summonersList.get(i) + "\" " + getString(R.string.from_the_list) + "?")
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Remove the summoner from the list:
                                summonersList.remove(position);
                                // Notify to the adapter of the list that one item was removed.
                                adapter.notifyDataSetChanged();
                                // Now we will delete it from the preferences.
                                SharedPreferences.Editor editor =
                                        getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                                // Prepare a new string with the summoner's names.
                                String newPreferences = "";
                                for (String name : summonersList) newPreferences += (name + " ");
                                // Save it.
                                editor.putString("friendList", newPreferences.trim());
                                editor.commit();

                                // If summoners's list is empty, set this text to "visible".
                                if (newPreferences.trim().length() == 0)
                                    findViewById(R.id.textViewYouHaveNoSummoners).setVisibility(View.VISIBLE);

                            }
                        })
                .setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // Create alert dialog and show it.
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // A method used to add a summoner to the list (also to the preferences).
    public void addSummoner() {
        // If summonersList is full.
        if (summonersList.size() == MAX_SUMMONERS)
            Toast.makeText(getApplicationContext(),
                    getString(R.string.you_cannot_add_more),
                    Toast.LENGTH_LONG).show();
        else {
            // Build the dialog.
            LayoutInflater li = LayoutInflater.from(this);
            View addVideoView = li.inflate(R.layout.add_summoner, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            final EditText userInput = (EditText) addVideoView
                    .findViewById(R.id.inputName);
            alertDialogBuilder
                    .setCancelable(false)
                    .setTitle(getString(R.string.add))
                    .setView(addVideoView)
                    .setPositiveButton(getString(R.string.accept),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // When we click on "Accept" after writing the name.
                                    String name = String.valueOf(userInput.getText()).replaceAll(" ", "");
                                    // If the name (without intermediate spaces) is valid...
                                    if ((name.length()) != 0) {
                                        // Then...
                                        // We warn the user that names are not stored with spaces.
                                        // (In case the name has a white space).
                                        if (String.valueOf(userInput.getText()).contains(" ")) {
                                            name = String.valueOf(userInput.getText()).replaceAll(" ", "");
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.summoner_names_are_not_stored_with_white_spaces),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        // Notify to the adapter of the list that one item was removed.
                                        adapter.notifyDataSetChanged();
                                        // We add it to the list.
                                        summonersList.add(name);
                                        // And to preferences.
                                        SharedPreferences prefs =
                                                getSharedPreferences("Summoners", MODE_PRIVATE);
                                        SharedPreferences.Editor editor =
                                                getSharedPreferences("Summoners", MODE_PRIVATE).edit();

                                        // ...by getting again the preferences String and adding it.
                                        String friends = prefs.getString("friendList", "");
                                        friends += (" " + name);
                                        editor.putString("friendList", friends.trim());
                                        editor.commit();

                                        // And we make sure that this text is not visible.
                                        findViewById(R.id.textViewYouHaveNoSummoners).setVisibility(View.INVISIBLE);

                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.please_enter_a_name),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            // Create alert dialog and show it.
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    // A method used to change the version (5.2.1, for instance) of LoL.
    /*public void changePatch() {
        SharedPreferences prefs = getSharedPreferences("Summoners", MODE_PRIVATE);
        String ver = prefs.getString("version", "5.6.1");
                LayoutInflater li = LayoutInflater.from(this);
        View addVideoView = li.inflate(R.layout.set_patch, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        EditText userInput = (EditText) addVideoView
                .findViewById(R.id.inputPatch);
        userInput.setText(ver);
        final EditText usInput = userInput;
        alertDialogBuilder
                .setCancelable(false)
                .setTitle(getString(R.string.current_patch))
                .setView(addVideoView)
                .setPositiveButton(getString(R.string.accept),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // When we click on "Accept" after writing.
                                String version = String.valueOf(usInput.getText());
                                // If the name (without intermediate spaces) is valid...
                                if ((version.length()) != 0) {
                                    // Add to preferences.
                                    SharedPreferences.Editor editor =
                                            getSharedPreferences("Summoners", MODE_PRIVATE).edit();
                                    editor.putString("version", version);
                                    editor.commit();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.please_enter_a_version),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        // Create alert dialog and show it.
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }*/

    private void changePreferences(){
        final Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }

    // Shows a dialog with "about the app" info.
    private void showAbout() {
        AlertDialog.Builder aboutDialog = new AlertDialog.Builder(context);
        aboutDialog.setTitle(getString(R.string.about_the_app))
                .setIcon(R.drawable.ic_launcher)
                .setMessage(getString(R.string.about_text))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok_cool), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        // Create the alert dialog and show it.
        AlertDialog alertDialog = aboutDialog.create();
        alertDialog.show();
    }


}
