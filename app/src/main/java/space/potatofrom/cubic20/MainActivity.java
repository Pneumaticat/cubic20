package space.potatofrom.cubic20;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private RelativeLayout contentLayout;
    private MenuItem startRemindersMenuItem;
    private MenuItem stopRemindersMenuItem;
    private TextView reminderStatus;
    private LinearLayout countdownDisplayContainer;
    private TextView countdownDisplayText;
    private Timer counterDown;
    private Snackbar dndSnackbar = null;

    private static final int REQUEST_CODE_NOTIFICATION_POLICY_ACCESS = 1;

    private class CountdownTimerTask extends TimerTask {
        private long elapsedMillis = 0;
        private final long TIMER_INTERVAL_MILLIS;
        private final long INITIAL_TIME_UNTIL_ALARM_MILLIS;
        private final String COUNTDOWN_FORMAT;

        public CountdownTimerTask(long timerIntervalMillis) {
            TIMER_INTERVAL_MILLIS = timerIntervalMillis;
            INITIAL_TIME_UNTIL_ALARM_MILLIS =
                    ReminderManager.getTimeUntilAlarmPrefMillis(getBaseContext());
            COUNTDOWN_FORMAT = getString(R.string.time_until_next_reminder_format);
        }

        @Override
        public void run() {
            elapsedMillis += TIMER_INTERVAL_MILLIS;
            if (elapsedMillis >= INITIAL_TIME_UNTIL_ALARM_MILLIS) {
                // Timer's done
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countdownDisplayText.setText(R.string.countdown_display_complete);
                    }
                });
                this.cancel();
            } else {
                final long timeUntilAlarmMillis = INITIAL_TIME_UNTIL_ALARM_MILLIS - elapsedMillis;
                final long seconds = (timeUntilAlarmMillis / 1000) % 60;
                final long minutes = (timeUntilAlarmMillis / (1000 * 60));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countdownDisplayText.setText(
                                String.format(COUNTDOWN_FORMAT, minutes, seconds));
                    }
                });
            }
        }
    }

    private final BroadcastReceiver updateUiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (    action.equals(getString(R.string.intent_start_reminders)) ||
                    action.equals(getString(R.string.intent_stop_reminders)) ||
                    action.equals(getString(R.string.intent_postpone_next_reminder)) ||
                    action.equals(getString(R.string.intent_hit_reminder))) {
                updateReminderUiStatus(ReminderManager.areRemindersActive(context));
            } else {
                throw new UnsupportedOperationException(
                        "The update ui broadcast receiver does not support action " + action);
            }
        }
    };
    private final BroadcastReceiver dndChangeReceiver = new BroadcastReceiver() {
        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)) {
                checkDnDState();
            } else {
                throw new UnsupportedOperationException(
                        "This receiver does not support action " + action);
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
        contentLayout = (RelativeLayout) findViewById(R.id.content_layout);
        startRemindersMenuItem = navigationView.getMenu().findItem(R.id.menu_start_reminders);
        stopRemindersMenuItem = navigationView.getMenu().findItem(R.id.menu_stop_reminders);
        reminderStatus = (TextView) findViewById(R.id.reminder_status);
        countdownDisplayContainer = (LinearLayout) findViewById(R.id.countdown_display_container);
        countdownDisplayText = (TextView) findViewById(R.id.countdown_display_text);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter uiFilter = new IntentFilter();
        uiFilter.setPriority(1); // Less than 2, which is the priority of ReminderManager's, to run after it
        uiFilter.addAction(getString(R.string.intent_start_reminders));
        uiFilter.addAction(getString(R.string.intent_stop_reminders));
        uiFilter.addAction(getString(R.string.intent_postpone_next_reminder));
        uiFilter.addAction(getString(R.string.intent_hit_reminder));
        registerReceiver(updateUiBroadcastReceiver, uiFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            IntentFilter dndFilter = new IntentFilter();
            dndFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
            registerReceiver(dndChangeReceiver, dndFilter);
        }

        updateReminderUiStatus(ReminderManager.areRemindersActive(this));
        checkDnDState();
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(updateUiBroadcastReceiver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            unregisterReceiver(dndChangeReceiver);
        }
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
            default:
                throw new UnsupportedOperationException("Unimplemented menu item " + id);
        }

        //return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_start_reminders:
                ReminderManager.sendStartRemindersBroadcast(this);
                disableStartStopMenuItems(true);
                break;
            case R.id.menu_stop_reminders:
                ReminderManager.sendStopRemindersBroadcast(this);
                disableStartStopMenuItems(false);
                break;
            case R.id.menu_stats:
                startActivity(new Intent(this, StatsActivity.class));
                break;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                throw new UnsupportedOperationException("Unimplemented menu item " + id);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NOTIFICATION_POLICY_ACCESS:
                NotificationManager man =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (man.isNotificationPolicyAccessGranted()) {
                    disableDnD();
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        "No implementation for request code " + requestCode);
        }
    }

    private void checkDnDState() {
        boolean isSnackbarShown = dndSnackbar != null && dndSnackbar.isShownOrQueued();
        if (ReminderManager.isDnDPreventingReminders(this)) {
            if (!isSnackbarShown) {
                showDnDWarning();
            }
        } else {
            if (isSnackbarShown) {
                hideDnDWarning();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void disableDnD() {
        NotificationManager man = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        man.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_ALL);
    }

    private void showDnDWarning() {
        dndSnackbar = Snackbar.make(
                contentLayout,
                R.string.main_snackbar_dnd_text,
                Snackbar.LENGTH_INDEFINITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Only add DnD disable action if version > Marshmallow (only
            // available there)
            dndSnackbar.setAction(
                    R.string.main_snackbar_dnd_disable_dnd,
                    new View.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            NotificationManager man =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            if (man.isNotificationPolicyAccessGranted()) {
                                // Yay, we already have access
                                disableDnD();
                            } else {
                                // Kick off a request Activity, whose result
                                // will be handled in onActivityResult.
                                startActivityForResult(new Intent(
                                        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                                        REQUEST_CODE_NOTIFICATION_POLICY_ACCESS);
                                // Make a toast too, to tell the user what to
                                // do.
                                Toast.makeText(
                                        getBaseContext(),
                                        R.string.toast_notification_policy_access_instructions,
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
        dndSnackbar.show();
    }

    private void hideDnDWarning() {
        dndSnackbar.dismiss();
    }

    /**
     * Disable both the "Start reminders" and "Stop reminders" buttons
     *
     * This is necessary because I've noticed that broadcasts tend to take a
     * while to actually get processed, and the user has the ability to, say,
     * if they pressed the start button, press the stop button before
     * reminders have actually started, thus crashing the app.
     *
     * These disable statuses are later reset when the broadcast comes through
     * by updateReminderUiStatus.
     */
    private void disableStartStopMenuItems(boolean turnedRemindersOn) {
        startRemindersMenuItem.setEnabled(false);
        stopRemindersMenuItem.setEnabled(false);
        reminderStatus.setText(R.string.main_reminder_status_loading);
        Toast.makeText(
                this,
                getString(
                        R.string.toast_loading_reminder_status,
                        (turnedRemindersOn
                                ? getString(R.string.main_reminder_status_on)
                                : getString(R.string.main_reminder_status_off)).toLowerCase()),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Update UI tracking, but without actually creating the alarm
     * @param on Either true or false, depending on the status.
     */
    private void updateReminderUiStatus(boolean on) {
        // Clear counter; either gets restored if ON or not if OFF.
        if (counterDown != null) {
            counterDown.cancel();
            counterDown.purge();
            counterDown = null;
        }

        if (on) {
            startRemindersMenuItem.setEnabled(false);
            stopRemindersMenuItem.setEnabled(true);
            reminderStatus.setText(R.string.main_reminder_status_on);
            countdownDisplayContainer.setVisibility(View.VISIBLE);

            final long TIMER_INTERVAL_MILLIS = 1000;

            counterDown = new Timer();
            counterDown.scheduleAtFixedRate(
                    new CountdownTimerTask(TIMER_INTERVAL_MILLIS),
                    0,
                    TIMER_INTERVAL_MILLIS);
        } else {
            startRemindersMenuItem.setEnabled(true);
            stopRemindersMenuItem.setEnabled(false);
            reminderStatus.setText(R.string.main_reminder_status_off);
            countdownDisplayContainer.setVisibility(View.GONE);
        }
    }
}
