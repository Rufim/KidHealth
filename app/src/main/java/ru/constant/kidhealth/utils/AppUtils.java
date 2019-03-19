package ru.constant.kidhealth.utils;

import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.io.IOException;

import java.net.URI;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.domain.models.User;
import ru.constant.kidhealth.service.RestService;
import ru.kazantsev.template.util.PreferenceMaster;

public class AppUtils {

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    private static final String ACCESS_EXPIRES = "accessExpires";
    private static final String REFRESH_EXPIRES = "refreshExpires";
    private static final String IS_LOGGED = "is_logged";

    private static final String LOGIN = "signIn";
    private static final String ID = "id";
    private static final String PASSWORD = "password";

    public static PreferenceMaster getPrefs() {
        return new PreferenceMaster(App.getAppComponent().getContext());
    }

    public static void saveUser(String login, String password) {
        getPrefs().putValue(LOGIN, login)
                .putValue(PASSWORD, password)
                .commit();
    }

    public static User getLastUser() {
        User user = new User();
        PreferenceMaster preferenceMaster = getPrefs();
        user.setLogin(preferenceMaster.getValue(LOGIN, ""));
        user.setPassword(preferenceMaster.getValue(PASSWORD, ""));
        user.setId(preferenceMaster.getValue(ID, "1"));
        return user;
    }

    public static String fixTime(String time) {
        if(time == null) return "--:--";
        if(time.length() > 5) {
            if (time.startsWith("0")) {
                return time.substring(1, 5);
            } else {
                return time.substring(0, 5);
            }
        } else {
            return time;
        }
    }

    public static void saveToken(Token token) {
        PreferenceMaster master = getPrefs();
        master.putValue(ACCESS_TOKEN, token.getAccessToken());
        master.putValue(REFRESH_TOKEN, token.getRefreshToken());
    }

    public static void clearToken() {
        PreferenceMaster master = getPrefs();
        master.putValue(ACCESS_TOKEN, "");
        master.putValue(REFRESH_TOKEN, "");
    }

    public static Token getToken() {
        Token token = new Token();
        PreferenceMaster master = getPrefs();
        token.setAccessToken(master.getValue(ACCESS_TOKEN, ""));
        token.setRefreshToken(master.getValue(REFRESH_TOKEN, ""));
        token.setAccount(getLastUser());
        return token;
    }

    public static void toggleLoggedOnce() {
        getPrefs().putValue(IS_LOGGED, true);
    }

    public static boolean isLoggedOnce() {
        return getPrefs().getValue(IS_LOGGED, false);
    }
}
