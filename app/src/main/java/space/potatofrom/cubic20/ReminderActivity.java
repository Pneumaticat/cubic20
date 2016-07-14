package space.potatofrom.cubic20;

import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ReminderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow activity to be shown when locked, and turn on the screen
        Window window = getWindow();
        window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_reminder);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        int reminderLength = ReminderHelper.getReminderLength(this);

        ReminderHelper.updateNextAlarmTime(this);

        final Handler handler = new Handler();
        final TextView counterDown = (TextView) findViewById(R.id.notification_counterdown);
        final TextView countdownDesc = (TextView) findViewById(R.id.notification_countdown_desc);
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final long COUNTDOWN_INTERVAL_MILLIS = 1000;

        // Set initial countdown value to reminder length specified in settings
        counterDown.setText(String.valueOf(reminderLength));
        countdownDesc.setText(getResources().getQuantityString(
                R.plurals.look_away_desc, reminderLength, reminderLength));

        vibrator.vibrate(500);

        // Countdown for 20s
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int previous = Integer.parseInt(counterDown.getText().toString());
                        int current = previous - 1;
                        counterDown.setText(String.valueOf(current));
                        if (current > 0) {
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
                                    murderThis();
                                }
                            }, 3000);
                        }
                    }
                });
            }
        }, COUNTDOWN_INTERVAL_MILLIS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void stopNotifications(View button) {
        ReminderHelper.endTracking(this, true);
        murderThis();
    }

    /**
     * Probably not good practice. Oh well.
     */
    private void murderThis() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
