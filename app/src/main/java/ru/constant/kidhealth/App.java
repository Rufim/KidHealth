package ru.constant.kidhealth;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.webkit.CookieSyncManager;

import com.evernote.android.job.JobManager;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import net.danlew.android.joda.JodaTimeAndroid;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import ru.constant.kidhealth.dagger.AppComponent;
import ru.constant.kidhealth.dagger.ContextModule;
import ru.constant.kidhealth.dagger.CookieModule;
import ru.constant.kidhealth.dagger.DaggerAppComponent;
import ru.constant.kidhealth.dagger.DatabaseModule;
import ru.constant.kidhealth.job.AppJobCreator;

import static ru.kazantsev.template.domain.Constants.App.USE_MOXY;


/**
 * Created by Rufim on 03.07.2015.
 */
@ReportsCrashes(
        mailTo = "dmitry.kazantsev@constant.obninsk.ru",
        mode = ReportingInteractionMode.DIALOG,
        alsoReportToAndroidFramework = true,
        resDialogTheme = R.style.AppTheme_Dialog,
        resDialogTitle = R.string.crash_title_text,
        resDialogText = R.string.crash_text
)
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
        USE_MOXY = true;
        JobManager.create(this).addJobCreator(new AppJobCreator());
        JodaTimeAndroid.init(this);
        CookieSyncManager.createInstance(this);
        component = DaggerAppComponent.builder()
                .contextModule(new ContextModule(this))
                .cookieModule(new CookieModule(this))
                .databaseModule(new DatabaseModule(this))
                .build();
        FlowManager.init(new FlowConfig.Builder(this).build());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

    public static AppComponent getAppComponent() {
        return component;
    }


    @VisibleForTesting
    public static void setAppComponent(@NonNull AppComponent appComponent) {
        component = appComponent;
    }
}
