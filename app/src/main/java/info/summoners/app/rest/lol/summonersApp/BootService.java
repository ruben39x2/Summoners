package info.summoners.app.rest.lol.summonersApp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Atreyu on 06/06/2015.
 */
public class BootService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 30);
        Intent i = new Intent(context, SummonersNotify.class);
        PendingIntent pintent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60*60*2*1000, pintent);
    }
}

