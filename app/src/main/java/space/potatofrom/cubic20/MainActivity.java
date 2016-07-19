package space.potatofrom.cubic20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private MenuItem beginTrackingMenuItem;
    private MenuItem endTrackingMenuItem;
    private TextView alarmStatus;
    private LinearLayout countdownDisplayContainer;
    private TextView countdownDisplay;
    private Timer counterDown;

    private class CountdownTimerTask extends TimerTask {
        private long elapsed = 0;
        private final long TIMER_INTERVAL;
        private final long TIME_UNTIL_ALARM;
        private final String COUNTDOWN_FORMAT;

        public CountdownTimerTask(long timerInterval) {
            TIMER_INTERVAL = timerInterval;
            TIME_UNTIL_ALARM = ReminderHelper.getTimeUntilAlarmPrefMillis(getBaseContext());
            COUNTDOWN_FORMAT = getString(R.string.time_until_alarm_format);
        }

        @Override
        public void run() {
            elapsed += TIMER_INTERVAL;
            if (elapsed >= TIME_UNTIL_ALARM) {
                // Timer's done
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countdownDisplay.setText(R.string.countdown_display_complete);
                    }
                });
                this.cancel();
            } else {
                final long millisUntilFinished = TIME_UNTIL_ALARM - elapsed;
                final long seconds = (millisUntilFinished / 1000) % 60;
                final long minutes = (millisUntilFinished / (1000 * 60));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countdownDisplay.setText(
                                String.format(COUNTDOWN_FORMAT, minutes, seconds));
                    }
                });
            }
        }
    }

    private BroadcastReceiver updateUiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "space.potatofrom.cubic20.START_REMINDERS":
                case "space.potatofrom.cubic20.HIT_REMINDER":
                    updateTrackingStatus(true);
                    break;
                case "space.potatofrom.cubic20.STOP_REMINDERS":
                case "space.potatofrom.cubic20.POSTPONE_NEXT_REMINDER":
                    updateTrackingStatus(false);
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "The update ui broadcast receiver does not support action " + action);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize shared preferences with default values from XML
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Initialize variables that correspond to view elements
        beginTrackingMenuItem = navigationView.getMenu().findItem(R.id.start_reminders);
        endTrackingMenuItem = navigationView.getMenu().findItem(R.id.stop_reminders);
        alarmStatus = (TextView) findViewById(R.id.alarm_status);
        countdownDisplayContainer = (LinearLayout) findViewById(R.id.countdown_display_container);
        countdownDisplay = (TextView) findViewById(R.id.countdown_display_text);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.setPriority(1); // Less than 2, which is the priority of ReminderHelper's, to run after it
        filter.addAction("space.potatofrom.cubic20.START_REMINDERS");
        filter.addAction("space.potatofrom.cubic20.STOP_REMINDERS");
        filter.addAction("space.potatofrom.cubic20.POSTPONE_NEXT_REMINDER");
        registerReceiver(updateUiBroadcastReceiver, filter);

        updateTrackingStatus(ReminderHelper.areRemindersActive(this));
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(updateUiBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            default:
                throw new UnsupportedOperationException("Unimplemented menu item " + id);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.start_reminders:
                ReminderHelper.sendStartRemindersBroadcast(this);
                break;
            case R.id.stop_reminders:
                ReminderHelper.sendStopRemindersBroadcast(this);
                break;
            default:
                throw new UnsupportedOperationException("Unimplemented menu item " + id);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Update UI tracking, but without actually creating the alarm
     * @param on Either true or false, depending on the status.
     */
    private void updateTrackingStatus(boolean on) {
        // Clear counter; either gets restored if ON or not if OFF.
        if (counterDown != null) {
            counterDown.cancel();
            counterDown.purge();
            counterDown = null;
        }

        if (on) {
            beginTrackingMenuItem.setEnabled(false);
            endTrackingMenuItem.setEnabled(true);
            alarmStatus.setText(R.string.alarm_status_on);
            countdownDisplayContainer.setVisibility(View.VISIBLE);

            final long TIMER_INTERVAL = 1000;

            counterDown = new Timer();
            counterDown.scheduleAtFixedRate(
                    new CountdownTimerTask(TIMER_INTERVAL),
                    0,
                    TIMER_INTERVAL);
        } else {
            beginTrackingMenuItem.setEnabled(true);
            endTrackingMenuItem.setEnabled(false);
            alarmStatus.setText(R.string.alarm_status_off);
            countdownDisplayContainer.setVisibility(View.GONE);
        }
    }
}
