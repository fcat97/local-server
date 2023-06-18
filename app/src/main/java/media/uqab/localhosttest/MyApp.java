package media.uqab.localhosttest;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }
}
