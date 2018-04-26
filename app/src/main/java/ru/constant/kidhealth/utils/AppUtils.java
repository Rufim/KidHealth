package ru.constant.kidhealth.utils;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.User;
import ru.kazantsev.template.util.PreferenceMaster;

public class AppUtils {

    private static final String TOKEN = "token";
    private static final String LOGIN = "login";
    private static final String ID = "login";
    private static final String PASSWORD = "password";

    public static PreferenceMaster getPrefs() {
        return new PreferenceMaster(App.getAppComponent().getContext());
    }

    public static String getToken() {
        return getPrefs().getValue(TOKEN, "");
    }

    public static void setToken(String token) {
        getPrefs().putValue(TOKEN, token).commit();
    }

    public static void saveUser(String login, String password, String id) {
        getPrefs().putValue(LOGIN, login).putValue(PASSWORD, password).putValue(ID, id).commit();
    }

    public static User getLastUser() {
        User user = new User();
        PreferenceMaster preferenceMaster = getPrefs();
        user.setLogin(preferenceMaster.getValue(LOGIN, ""));
        user.setPassword(preferenceMaster.getValue(PASSWORD, ""));
        user.setId(preferenceMaster.getValue(ID, "1"));
        user.setToken(preferenceMaster.getValue(TOKEN, ""));
        return user;
    }

    public static String fixTime(String time) {
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
}
