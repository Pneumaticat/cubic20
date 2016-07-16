package space.potatofrom.cubic20;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by kevin on 7/11/16.
 */
public class ReminderHelper {
    private static final int EYE_TIMER_CODE = 1;
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private ReminderHelper() { }

    /**
     * Updates alarm when it fires
     *
     * When the reminder alarm fires, this sets another alarm in its place
     * [reminder interval] in the future.
     */
    public class ReminderBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "space.potatofrom.cubic20.HIT_REMINDER":
                    // Set next alarm
                    createAlarm(context);
                    updateNextAlarmTimePref(context);
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "This broadcast receiver does not implement action " + action);
            }
        }
    }

    private static Intent getReminderBroadcastIntent(Context context) {
        return new Intent("space.potatofrom.cubic20.HIT_REMINDER");
    }

    /**
     * Gets the reminder interval (minutes) from shared preferences
     */
    public static int getReminderInterval(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString(
                context.getString(R.string.pref_key_reminder_interval_min), null));
    }

    public static long getReminderIntervalMillis(Context context) {
        return getReminderInterval(context) * 60L * 1000;
    }

    /**
     * Gets the reminder length (seconds) from shared preferences
     */
    public static int getReminderLength(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString(
                context.getString(R.string.pref_key_reminder_length_sec), null));
    }

    /**
     * Returns the system time, in milliseconds, of the next scheduled alarm
     */
    public static long getSystemTimeAtNextAlarm(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final long errorValue = Long.MIN_VALUE;
        long nextAlarmTime = prefs.getLong(
                context.getString(R.string.pref_key_next_alarm_time), errorValue);
        if (nextAlarmTime == errorValue) {
            throw new IllegalStateException(
                    "Attempted to get system time at next alarm when the next alarm time was not set.");
        } else {
            return nextAlarmTime;
        }
    }

    /**
     * Returns the time, in milliseconds, until the next scheduled alarm
     */
    public static long getTimeUntilAlarmMillis(Context context) {
        return getSystemTimeAtNextAlarm(context) - System.currentTimeMillis();
    }

    private static boolean isAlarmSet(Context context) {
        return PendingIntent.getBroadcast(
                context,
                EYE_TIMER_CODE,
                getReminderBroadcastIntent(context),
                PendingIntent.FLAG_NO_CREATE) != null;
    }

    private static boolean isNextAlarmTimePrefSet(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(context.getString(
                R.string.pref_key_next_alarm_time), Long.MIN_VALUE) != Long.MIN_VALUE;
    }

    /**
     * Determines whether or not tracking is currently active
     *
     * Checks the saved next-alarm-time as well as whether or not an alarm is
     * actually set, and after resolving inconsistencies between the two,
     * returns an answer.
     *
     * @return Whether or not we're currently tracking
     */
    public static boolean currentlyTracking(Context context) {
        boolean alarmTimeWritten = isNextAlarmTimePrefSet(context);
        boolean alarmSet = isAlarmSet(context);

        // Handle inconsistencies that might arise from, for example, if the
        // device is restarted while the alarm is running.
        if (!alarmSet && alarmTimeWritten) {
            // We're in a weird state, remove the alarm time.
            removeNextAlarmTimePref(context);
            return false;
        } else if (alarmSet && !alarmTimeWritten) {
            // We're in a weird state, remove the alarm.
            removeAlarm(context);
            return false;
        } else {
            return alarmSet; // If we're here, both values should be identical
        }
    }

    public static void beginTracking(Context context, boolean displayUi) {
        if (currentlyTracking(context))
            throw new IllegalStateException(
                    "Attempted to enable tracking when tracking is already enabled.");

        createAlarm(context);

        updateNextAlarmTimePref(context);

        if (displayUi) {
            Toast.makeText(
                    context,
                    context.getResources().getQuantityString(
                            R.plurals.toast_created_alarm,
                            getReminderInterval(context),
                            getReminderInterval(context)),
                    Toast.LENGTH_SHORT).show();

            // Create persistent notification
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean(
                    context.getString(R.string.pref_key_show_persistent_notification), false)) {
                NotificationManager notMan = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                notMan.notify(ONGOING_NOTIFICATION_ID, getOngoingNotification(context));
            }
        }
    }

    private static void setNextAlarmTimePref(Context context, long systemTimeMillis) {
        SharedPreferences.Editor prefEditor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.putLong(
                context.getString(R.string.pref_key_next_alarm_time),
                systemTimeMillis);
        prefEditor.apply();
    }

    /**
     * Updates the next alarm time to be current time + [reminder interval].
     */
    public static void updateNextAlarmTimePref(Context context) {
        setNextAlarmTimePref(context, System.currentTimeMillis() + getReminderIntervalMillis(context));
    }

    private static void removeNextAlarmTimePref(Context context) {
        SharedPreferences.Editor prefEditor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.remove(context.getString(R.string.pref_key_next_alarm_time));
        prefEditor.apply();
    }

    private static void createAlarm(Context context) {
        long reminderIntervalMillis = getReminderIntervalMillis(context);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + reminderIntervalMillis, // run at:
                reminderIntervalMillis, // interval
                // Launch notification activity
                PendingIntent.getBroadcast(
                        context,
                        EYE_TIMER_CODE,
                        getReminderBroadcastIntent(context),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private static void removeAlarm(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(PendingIntent.getBroadcast(
                context,
                EYE_TIMER_CODE,
                getReminderBroadcastIntent(context),
                PendingIntent.FLAG_NO_CREATE));
    }

    /**
     * Update the underlying alarm's time to be [reminder-interval] in the future.
     */
    private static void updateAlarm(Context context) {
        if (!isAlarmSet(context)) {
            throw new IllegalStateException(
                    "Attempted to postpone alarm when alarm isn't set.");
        } else {
            // Alarm is set
            removeAlarm(context);
            createAlarm(context);
        }
    }

    public static void updateNextReminderTime(Context context, boolean displayUi) {
        updateAlarm(context);
        updateNextAlarmTimePref(context);

        if (displayUi) {
            long millisUntilAlarm = ReminderHelper.getTimeUntilAlarmMillis(context);
            long seconds = (millisUntilAlarm / 1000) % 60;
            long minutes = (millisUntilAlarm / (1000 * 60));
            String nextAlarmTime = context.getString(
                    R.string.time_until_alarm_format,
                    minutes,
                    seconds);
            Toast.makeText(
                    context,
                    context.getString(R.string.toast_updated_alarm, nextAlarmTime),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void endTracking(Context context, boolean displayUi) {
        if (!currentlyTracking(context))
            throw new IllegalStateException(
                    "Attempted to disable tracking when tracking is already inactive.");

        removeAlarm(context);

        removeNextAlarmTimePref(context);

        if (displayUi) {
            Toast.makeText(
                    context,
                    R.string.toast_removed_alarm,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private static Notification getOngoingNotification(Context context) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_brightness_medium_black_24dp)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(
                        context.getResources().getQuantityString(
                                R.plurals.notification_text,
                                getReminderInterval(context),
                                getReminderInterval(context)))
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
                                getReminderInterval(context),
                                getReminderInterval(context)),
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
                                                "space.potatofrom.cubic20.STOP_REMINDERS")
                                        .putExtra(
                                                NotificationBroadcastReceiver.EXTRA_NOTIFICATION_ID,
                                                ONGOING_NOTIFICATION_ID),
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
}
