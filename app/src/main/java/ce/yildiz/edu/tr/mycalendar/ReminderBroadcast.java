package ce.yildiz.edu.tr.mycalendar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

// A Broadcast Receiver for remind events to user
public class ReminderBroadcast extends BroadcastReceiver {
    private final static int NOTIFICATION_ID = 101;
    CalendarHelper calendarHelper;
    Event event;

    @Override
    public void onReceive(Context context, Intent intent) {
        String channelID = createNotificationChannel(context);
        calendarHelper = new CalendarHelper(context);
        long eventID = intent.getLongExtra("eventID", 0);
        event = calendarHelper.getEventsByID(eventID);

        Intent showIntent = new Intent(context, ShowEventActivity.class);
        showIntent.putExtra("selectedEvent", event);
        showIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent showPendingIntent = PendingIntent.getActivity(context, (int) eventID, showIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_logo_calendar)
                .setContentTitle(event.getEventName())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setNumber(1)
                .setContentIntent(showPendingIntent)
                .setSound(getDefaultSoundUri(context));

        switch (event.getEventType()) {
            case "birthday":
                builder.setContentText(CalendarEventAdapter.getTimeAsStringBirthday(context, event))
                        .addAction(R.drawable.ic_menu_birthday_black, context.getString(R.string.show_birthday), showPendingIntent).setAutoCancel(true);
                break;

            case "task":
                builder.setContentText(CalendarEventAdapter.getTimeAsString(context, event))
                        .addAction(R.drawable.ic_menu_event_black, context.getString(R.string.show_task), showPendingIntent).setAutoCancel(true);
                break;

            case "meeting":
                builder.setContentText(CalendarEventAdapter.getTimeAsString(context, event))
                        .addAction(R.drawable.ic_meeting_black_24dp, context.getString(R.string.show_meeting), showPendingIntent).setAutoCancel(true);
                break;
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    // A function which create a notification channel
    public String createNotificationChannel(Context context) {
        int id = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Calendar Channel";
            String description = "Channel for calendar";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            String channelID = sharedPreferences.getString("channelID", "0");
            deleteNotificationChannel(context, channelID);
            if (channelID != null) {
                id = Integer.parseInt(channelID);
            }
            id = id + 1;

            NotificationChannel channel = new NotificationChannel(String.valueOf(id), name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);

            boolean vibration = sharedPreferences.getBoolean("vibration", false);
            channel.enableVibration(vibration);

            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.setSound(getDefaultSoundUri(context), att);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("channelID", String.valueOf(id)).apply();
        }
        return String.valueOf(id);
    }


    // Get default sound uri which user choose, with SharedPreferences
    public Uri getDefaultSoundUri(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        Uri defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String defaultSound = sharedPreferences.getString("defaultSoundUri", defaultNotificationSound.toString());

        return Uri.parse(defaultSound);
    }


    // Deleting notification channel for update it because of user can change default sound
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void deleteNotificationChannel(Context context, String id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.deleteNotificationChannel(id);
    }

}
