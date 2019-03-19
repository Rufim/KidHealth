package ru.constant.kidhealth.dagger;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class CookieModule {
    private Context mContext;

    public CookieModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    public CookieSyncManager provideCookieSyncManager() { return CookieSyncManager.createInstance(mContext); }

    @Provides
    @Singleton
    public CookieManager provideCookieManager() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        return cookieManager;
    }
}
