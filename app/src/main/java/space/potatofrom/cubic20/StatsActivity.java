package space.potatofrom.cubic20;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.util.Locale;

public class StatsActivity extends AppCompatActivity {
    private TextView remindersStartedValue;
    private TextView remindersStoppedValue;
    private TextView remindersPostponedValue;
    private TextView remindersHitValue;
    private TextView timePostponedValue;
    private TextView timeRestedValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        remindersStartedValue = (TextView) findViewById(R.id.stats_value_reminders_started);
        remindersStoppedValue = (TextView) findViewById(R.id.stats_value_reminders_stopped);
        remindersPostponedValue = (TextView) findViewById(R.id.stats_value_reminders_postponed);
        remindersHitValue = (TextView) findViewById(R.id.stats_value_reminders_hit);
        timePostponedValue = (TextView) findViewById(R.id.stats_value_time_postponed);
        timeRestedValue = (TextView) findViewById(R.id.stats_value_time_rested);

        refreshStatsUi();
    }

    private void refreshStatsUi() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        remindersStartedValue.setText(String.valueOf(
                prefs.getInt(getString(R.string.pref_key_stats_reminders_started), 0)));
        remindersStoppedValue.setText(String.valueOf(
                prefs.getInt(getString(R.string.pref_key_stats_reminders_stopped), 0)));
        remindersPostponedValue.setText(String.valueOf(
                prefs.getInt(getString(R.string.pref_key_stats_reminders_postponed), 0)));
        remindersHitValue.setText(String.valueOf(
                prefs.getInt(getString(R.string.pref_key_stats_reminders_hit), 0)));

        PeriodFormatter formatter = PeriodFormat.getDefault().withLocale(Locale.getDefault());
        timePostponedValue.setText(formatter.print(
                Period.minutes(
                    prefs.getInt(getString(R.string.pref_key_stats_time_postponed_min), 0))
                .normalizedStandard()));
        timeRestedValue.setText(formatter.print(
                Period.seconds(
                    prefs.getInt(getString(R.string.pref_key_stats_time_rested_sec), 0))
                .normalizedStandard()));
    }
}
