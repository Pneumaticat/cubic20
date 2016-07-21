package space.potatofrom.cubic20;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by kevin on 7/20/16.
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
