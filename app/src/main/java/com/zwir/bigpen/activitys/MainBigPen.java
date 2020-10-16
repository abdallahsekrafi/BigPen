package com.zwir.bigpen.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.zwir.bigpen.R;
import com.zwir.bigpen.broadCastReceiver.BigPenAlarmReceiver;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.fragments.ExitFragment;


public class MainBigPen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_pen);
        cancelAlarm();
    }

    public void registerAlarm() {
        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        long futureInMillis=System.currentTimeMillis()+ Constants.hourToRemind;
        Intent intent=new Intent(MainBigPen.this, BigPenAlarmReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, Constants.NOTIFICATION_REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP,futureInMillis,pendingIntent);
    }

    void cancelAlarm(){
        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent=new Intent(MainBigPen.this, BigPenAlarmReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,Constants.NOTIFICATION_REQUEST_CODE,intent,PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent!=null){
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        ExitFragment exitFragment=new ExitFragment();
        exitFragment.show(getSupportFragmentManager(),"exit_app");
    }
}
