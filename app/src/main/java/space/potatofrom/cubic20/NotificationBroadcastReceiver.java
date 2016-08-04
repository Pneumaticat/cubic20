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
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(
                        context.getResources().getQuantityString(
                                R.plurals.notification_text,
                                ReminderManager.getReminderInterval(context),
                                ReminderManager.getReminderInterval(context)))
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        new Intent(context, MainActivity.class),
                        0))
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_update_black_24dp,
                        context.getResources().getQuantityString(
                                R.plurals.notification_postpone,
                                ReminderManager.getReminderInterval(context),
                                ReminderManager.getReminderInterval(context)),
                        PendingIntent.getBroadcast(
                                context,
                                OrderedBroadcastForwarder.REQUEST_CODE_POSTPONE_NEXT_REMINDER,
                                new Intent(OrderedBroadcastForwarder.ACTION_FORWARD)
                                        .putExtra(
                                                OrderedBroadcastForwarder.EXTRA_ACTION,
                                                "space.potatofrom.cubic20.POSTPONE_NEXT_REMINDER"),
                                0)).build())
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_alarm_off_black_24dp,
                        context.getString(R.string.notification_stop),
                        PendingIntent.getBroadcast(
                                context,
                                OrderedBroadcastForwarder.REQUEST_CODE_STOP_REMINDERS,
                                new Intent(OrderedBroadcastForwarder.ACTION_FORWARD)
                                        .putExtra(
                                                OrderedBroadcastForwarder.EXTRA_ACTION,
                                                "space.potatofrom.cubic20.STOP_REMINDERS"),
                                0)).build())
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_menu_settings,
                        context.getString(R.string.notification_options),
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, SettingsActivity.class),
                                0)).build())
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

        if (action.equals(context.getString(R.string.intent_start_reminders))) {
            // Only create ongoing notification if allowed by preferences
            if (prefs.getBoolean(
                    context.getString(R.string.pref_key_show_persistent_notification),
                    false)) {
                notifs.notify(ONGOING_NOTIFICATION_ID, getOngoingNotification(context));
            }
        } else if (action.equals(context.getString(R.string.intent_stop_reminders))) {
            // Cancel ongoing notification
            notifs.cancel(ONGOING_NOTIFICATION_ID);
        } else if (action.equals(context.getString(R.string.intent_hit_reminder))) {
            // Renotify notification, because a new alarm has been created
            // with a potentially new reminder interval, etc.
            if (prefs.getBoolean(
                    context.getString(R.string.pref_key_show_persistent_notification),
                    false)) {
                notifs.notify(ONGOING_NOTIFICATION_ID, getOngoingNotification(context));
            }
        } else {
            throw new UnsupportedOperationException("Unimplemented action " + action);
        }
    }
}
