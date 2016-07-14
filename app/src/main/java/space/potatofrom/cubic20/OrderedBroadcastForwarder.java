package space.potatofrom.cubic20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kevin on 7/13/16.
 */
public class OrderedBroadcastForwarder extends BroadcastReceiver {
    public static final String ACTION_NAME = "action";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent forwardIntent = new Intent(intent.getStringExtra(ACTION_NAME));
        forwardIntent.putExtras(intent);
        forwardIntent.removeExtra(ACTION_NAME);

        context.sendOrderedBroadcast(forwardIntent, null);
    }
}
