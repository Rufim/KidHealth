package ru.constant.kidhealth.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.constant.kidhealth.service.DatabaseService;

@Module
public class DatabaseModule {
    private Context mContext;

    public DatabaseModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    public DatabaseService provideDatabaseService() {
        return new DatabaseService(mContext);
    }
}
