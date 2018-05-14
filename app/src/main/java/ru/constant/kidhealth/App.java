package ru.constant.kidhealth;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.evernote.android.job.JobManager;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import net.danlew.android.joda.JodaTimeAndroid;

import ru.constant.kidhealth.dagger.AppComponent;
import ru.constant.kidhealth.dagger.ContextModule;

import ru.constant.kidhealth.dagger.DaggerAppComponent;
import ru.constant.kidhealth.dagger.DatabaseModule;
import ru.constant.kidhealth.job.AppJobCreator;


/**
 * Created by Rufim on 03.07.2015.
 */
public class App extends Application {

    private static App singleton;

    public static App getInstance() {
        return singleton;
    }

    private static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
//      Fabric.with(this, new Crashlytics.Builder()
//                .core(new CrashlyticsCore.Builder()
//                        .disabled(true)
//                        .build()).build(), new Crashlytics());
//      CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//              .setDefaultFontPath(Constants.Assets.ROBOTO_FONT_PATH)
//              .setFontAttrId(R.attr.fontPath)
//              .build());
        JobManager.create(this).addJobCreator(new AppJobCreator());
        JodaTimeAndroid.init(this);
        component = DaggerAppComponent.builder()
                .contextModule(new ContextModule(this))
                .databaseModule(new DatabaseModule(this))
                .build();
        FlowManager.init(new FlowConfig.Builder(this).build());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static AppComponent getAppComponent() {
        return component;
    }


    @VisibleForTesting
    public static void setAppComponent(@NonNull AppComponent appComponent) {
        component = appComponent;
    }
}
