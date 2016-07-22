package space.potatofrom.cubic20;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Performs startup initializations
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
