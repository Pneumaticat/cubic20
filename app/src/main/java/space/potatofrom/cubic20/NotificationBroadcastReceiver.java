package space.potatofrom.cubic20;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

/**
 * Manages ongoing notification in response to various intents
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private static Notification getOngoingNotification(Context context) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_brightness_medium_black_24dp)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(
                        context.getResources().getQuantityString(
                                R.plurals.notification_text,
                                ReminderHelper.getReminderInterval(context),
                                ReminderHelper.getReminderInterval(context)))
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        new Intent(context, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_update_black_24dp,
                        context.getResources().getQuantityString(
                                R.plurals.notification_postpone,
                                ReminderHelper.getReminderInterval(context),
                                ReminderHelper.getReminderInterval(context)),
                        PendingIntent.getBroadcast(
                                context,
                                0,
                                new Intent("space.potatofrom.cubic20.FORWARD_AS_ORDERED_BROADCAST")
                                        .putExtra(
                                                OrderedBroadcastForwarder.ACTION_NAME,
                                                "space.potatofrom.cubic20.POSTPONE_NEXT_REMINDER"),
                                PendingIntent.FLAG_UPDATE_CURRENT)).build())
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_alarm_off_black_24dp,
                        context.getString(R.string.notification_stop),
                        PendingIntent.getBroadcast(
                                context,
                                // Different request code to differentiate
                                // from postpone intent above
                                // see: https://developer.android.com/reference/android/app/PendingIntent.html
                                1,
                                new Intent("space.potatofrom.cubic20.FORWARD_AS_ORDERED_BROADCAST")
                                        .putExtra(
                                                OrderedBroadcastForwarder.ACTION_NAME,
                                                "space.potatofrom.cubic20.STOP_REMINDERS"),
                                PendingIntent.FLAG_UPDATE_CURRENT)).build())
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_settings_black_24dp,
                        context.getString(R.string.notification_options),
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, SettingsActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT)).build())
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationManager notifs = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                if (prefs.getBoolean(
                        context.getString(R.string.pref_key_start_on_boot),
                        false)) {
                    notifs.notify(ONGOING_NOTIFICATION_ID, getOngoingNotification(context));
                }
                break;
            case "space.potatofrom.cubic20.START_REMINDERS":
                // Only create ongoing notification if allowed by preferences
                if (prefs.getBoolean(
                        context.getString(R.string.pref_key_show_persistent_notification),
                        false)) {
                    notifs.notify(ONGOING_NOTIFICATION_ID, getOngoingNotification(context));
                }
                break;
            case "space.potatofrom.cubic20.STOP_REMINDERS":
                // Cancel ongoing notification
                notifs.cancel(ONGOING_NOTIFICATION_ID);
                break;
            case "space.potatofrom.cubic20.HIT_REMINDER":
                // Renotify notification, because a new alarm has been created
                // with a potentially new reminder interval, etc.
                if (prefs.getBoolean(
                        context.getString(R.string.pref_key_show_persistent_notification),
                        false)) {
                    notifs.notify(ONGOING_NOTIFICATION_ID, getOngoingNotification(context));
                }
                break;
            default:
                throw new UnsupportedOperationException("Unimplemented action " + action);
        }
    }
}
