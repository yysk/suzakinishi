package works.langley.suzakinishi;

import android.app.Application;

import timber.log.Timber;

public class SznApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}