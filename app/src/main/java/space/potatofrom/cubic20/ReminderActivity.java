package space.potatofrom.cubic20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ReminderActivity extends AppCompatActivity {
    // Used to stop countdown after activity closes
    private boolean isOpen = true;

    /**
     * Listen for when a reminder is hit, and start ReminderActivity
     */
    public static class HitReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(context.getString(R.string.intent_hit_reminder))) {
                if (!ReminderManager.isDnDActive(context)) {
                    // Start ReminderActivity
                    Intent reminderIntent = new Intent(context, ReminderActivity.class);
                    reminderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(reminderIntent);
                }
            } else {
                    throw new UnsupportedOperationException(
                            "This broadcast receiver does not implement action " + action);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow activity to be shown when locked, and turn on the screen
        Window window = getWindow();
        window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON   |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_reminder);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
        }

        // Prepare for countdown
        final int reminderLength = ReminderManager.getReminderLength(this);
        final Handler handler = new Handler();
        final TextView counterDown = (TextView) findViewById(R.id.notification_counterdown);
        final TextView countdownDesc = (TextView) findViewById(R.id.notification_countdown_desc);
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final long COUNTDOWN_INTERVAL_MILLIS = 1000;

        // Set initial countdown value to reminder length specified in settings
        counterDown.setText(String.valueOf(reminderLength));
        countdownDesc.setText(getResources().getQuantityString(
                R.plurals.reminder_look_away_desc, reminderLength, reminderLength));

        vibrator.vibrate(500);

        // Countdown for 20s
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isOpen) {
                            int previousNo = Integer.parseInt(counterDown.getText().toString());
                            int currentNo = previousNo - 1;
                            counterDown.setText(String.valueOf(currentNo));
                            if (currentNo > 0) {
                                // Wait for it, wait for it
                                handler.postDelayed(this, COUNTDOWN_INTERVAL_MILLIS);
                            } else {
                                // Countdown has reached 0
                                // Vibrate as feedback that the countdown has
                                // completed.

                                vibrator.vibrate(500);

                                // Then wait 3 seconds and close the activity.
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        close();
                                    }
                                }, 3000);
                            }
                        }
                    }
                });
            }
        }, COUNTDOWN_INTERVAL_MILLIS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOpen = false;
    }

    public void stopReminders(View button) {
        ReminderManager.sendStopRemindersBroadcast(this);
        close();
    }

    private void close() {
        finish();
        isOpen = false;
    }
}
