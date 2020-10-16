package com.zwir.bigpen.broadCastReceiver;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.RemoteViews;

import com.zwir.bigpen.R;
import com.zwir.bigpen.activitys.MainBigPen;
import com.zwir.bigpen.util.Constants;

import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

public class BigPenAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent=new Intent(context, MainBigPen.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(context, Constants.NOTIFICATION_REQUEST_CODE,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        RemoteViews notificationLayout=new RemoteViews(context.getPackageName(),
                R.layout.big_pen_notification);
        long[] VIBRATE_PATTERN    = {0, 500};
        NotificationCompat.Builder builder= new NotificationCompat.Builder(context,"Big_Pen_Reminder")
                .setSmallIcon(R.drawable.big_pen_small_notif)
                .setCustomContentView(notificationLayout)
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(VIBRATE_PATTERN)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //Generate bitmap from text
        String notificationTitle=context.getResources().getString(R.string.app_name);
        String notificationMsg=context.getResources().getString(R.string.notification_text);
        Bitmap bitmapTitle = textAsBitmap(context, notificationTitle, 38);
        Bitmap bitmapMsg = textAsBitmap(context, notificationMsg, 28);
        //Set bitmap as Title and Msg
        notificationLayout.setImageViewBitmap(R.id.notification_title,bitmapTitle);
        notificationLayout.setImageViewBitmap(R.id.notification_msg,bitmapMsg);
        notificationManager.notify(100,builder.build());
    }
    public static Bitmap textAsBitmap(Context context, String messageText, float textSize){
        Typeface font= ResourcesCompat.getFont(context,R.font.big_pen_font);
        Paint paint=new Paint();
        paint.setTextSize(textSize);
        paint.setTypeface(font);

        //paint.setColor(Color.WHITE);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline=-paint.ascent(); // ascent() is negative
        int width=(int)(paint.measureText(messageText)+0.5f); // round
        int height=(int)(baseline+paint.descent()+0.5f);
        Bitmap image=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(image);
        canvas.drawText(messageText,0,baseline,paint);
        return image;
    }
}
