package space.potatofrom.cubic20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Sends start reminders broadcast on boot, if enabled in manifest
 */
public class ReminderBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                ReminderManager.sendStartRemindersBroadcast(context);
                break;
            default:
                throw new UnsupportedOperationException(
                        "ReminderBootReceiver does not support action " + intent.getAction());
        }
    }
}
