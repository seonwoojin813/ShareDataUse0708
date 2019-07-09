package com.tjoeun.smartphonemodule;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class SMSReceiver extends BroadcastReceiver {

    //이벤트가 발생했을 때 호출되는 메소드
    @Override
    public void onReceive(Context context, Intent intent) {
        //문자메시지 데이터를 가져오기
        Bundle bundle = intent.getExtras();
        Object [] pdus = (Object []) bundle.get("pdus");
        SmsMessage [] messages = new SmsMessage[pdus.length];
        for(int i=0; i<pdus.length; i=i+1){
            messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            try{
                String message = new String(messages[i].getMessageBody());
                String phoneNumber = messages[i].getOriginatingAddress();
                //Notification 을 생성
                NotificationManager manager =
                        (NotificationManager) context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );
                NotificationCompat.Builder builder = null;
                //운영체제 별로 다르게 Builder를 생성
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    String channelId = "one-channel";
                    String channelName = "My Channel One";
                    String channelDescription = "My Channel One Description";

                    NotificationChannel channel = new NotificationChannel(
                            channelId, channelName,
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    channel.setDescription(channelDescription);
                    manager.createNotificationChannel(channel);
                    builder = new NotificationCompat.Builder(context, channelId);
                }else{
                    builder = new NotificationCompat.Builder(context);
                }
                Notification noti =
                        builder.setSmallIcon(android.R.drawable.ic_notification_overlay)
                                //builder.setContentTitle("새로운 메시지");
                                .setContentTitle("새로운 메시지")
                                .setContentText(message)
                                .setAutoCancel(true)
                                .build();
                manager.notify(111,noti);

            }catch (Exception e){
                Log.e("메시지 수신 에러", e.getMessage());
            }
        }
    }
}
