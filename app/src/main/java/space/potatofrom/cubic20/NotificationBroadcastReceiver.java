package space.potatofrom.cubic20;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * Created by kevin on 7/12/16.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                if (PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(context.getString(R.string.pref_key_start_on_boot), false)) {
                    ReminderHelper.beginTracking(context, true);
                }
                break;
            case "space.potatofrom.cubic20.POSTPONE_NEXT_REMINDER":
                ReminderHelper.updateNextReminderTime(context, true);
                break;
            case "space.potatofrom.cubic20.STOP_REMINDERS":
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
                ReminderHelper.endTracking(context, true);

                // Cancel ongoing notification
                NotificationManager manager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notificationId);
                break;
            default:
                throw new UnsupportedOperationException("Unimplemented action " + action);
        }
    }
}
