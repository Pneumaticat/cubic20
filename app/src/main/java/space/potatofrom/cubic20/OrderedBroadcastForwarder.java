package space.potatofrom.cubic20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kevin on 7/13/16.
 */
public class OrderedBroadcastForwarder extends BroadcastReceiver {
    public static final String ACTION_NAME = "action";

    // Unique request codes for each possible forwarded intent, to prevent
    // strange behavior as mentioned in
    // https://developer.android.com/reference/android/app/PendingIntent.html:
    //
    // "A common mistake people make is to create multiple PendingIntent
    // objects with Intents that only vary in their 'extra' contents,
    // expecting to get a different PendingIntent each time. This does not
    // happen. The parts of the Intent that are used for matching are the same
    // ones defined by Intent.filterEquals. If you use two Intent objects that
    // are equivalent as per Intent.filterEquals, then you will get the same
    // PendingIntent for both of them."
    //
    // Note to me in the future: DO NOT CHANGE THESE values, especially
    // HIT_REMINDER's. Why? Well, so let's say you have a reminder alarm set
    // for HIT_REMINDER, with a nice PendingIntent and requestCode 3. If you
    // change REQUEST_CODE_HIT_REMINDER to, say, 4, ReminderHelper.isAlarmSet
    // will check for a HIT_REMINDER PendingIntent with requestCode 4, find
    // none, and thus that previously-set alarm will be forgotten by the app.
    // This isn't that big of a deal (mainly because rebooting will easily
    // wipe the rogue alarm), but it's weird and sort of difficult to debug.
    // Just don't do it.
    //public static final int REQUEST_CODE_START_REMINDERS = 0; // Unused (for now...)
    public static final int REQUEST_CODE_STOP_REMINDERS = 1;
    public static final int REQUEST_CODE_POSTPONE_NEXT_REMINDER = 2;
    public static final int REQUEST_CODE_HIT_REMINDER = 3;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent forwardIntent = new Intent(intent.getStringExtra(ACTION_NAME));
        forwardIntent.putExtras(intent);
        forwardIntent.removeExtra(ACTION_NAME);

        context.sendOrderedBroadcast(forwardIntent, null);
    }
}
