package space.potatofrom.cubic20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Records stats on various broadcasts
 */
public class StatsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        String action = intent.getAction();

        if (action.equals(context.getString(R.string.intent_start_reminders))) {
            String remindersStartedPref =
                    context.getString(R.string.pref_key_stats_reminders_started);

            prefEditor.putInt(remindersStartedPref, prefs.getInt(remindersStartedPref, 0) + 1);
        } else if (action.equals(context.getString(R.string.intent_stop_reminders))) {
            String remindersStoppedPref =
                    context.getString(R.string.pref_key_stats_reminders_stopped);

            prefEditor.putInt(remindersStoppedPref, prefs.getInt(remindersStoppedPref, 0) + 1);
        } else if (action.equals(context.getString(R.string.intent_postpone_next_reminder))) {
            String remindersPostponedPref =
                    context.getString(R.string.pref_key_stats_reminders_postponed);
            String timePostponedMinPref =
                    context.getString(R.string.pref_key_stats_time_postponed_min);

            prefEditor.putInt(remindersPostponedPref, prefs.getInt(remindersPostponedPref, 0) + 1);
            prefEditor.putInt(timePostponedMinPref, prefs.getInt(timePostponedMinPref, 0) +
                            ReminderHelper.getReminderInterval(context));
        } else if (action.equals(context.getString(R.string.intent_hit_reminder))) {
            String remindersHitPref =
                    context.getString(R.string.pref_key_stats_reminders_hit);
            String timeRestedSecPref =
                    context.getString(R.string.pref_key_stats_time_rested_sec);

            prefEditor.putInt(remindersHitPref, prefs.getInt(remindersHitPref, 0) + 1);
            prefEditor.putInt(timeRestedSecPref, prefs.getInt(timeRestedSecPref, 0) +
                    ReminderHelper.getReminderLength(context));
        } else {
            throw new UnsupportedOperationException(
                    "This broadcast receiver does not implement action " + action);
        }

        prefEditor.apply();
    }
}
