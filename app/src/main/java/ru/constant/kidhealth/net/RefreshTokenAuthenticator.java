package ru.constant.kidhealth.net;

import android.content.Context;

import net.vrallev.android.cat.Cat;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.utils.AppUtils;

import static ru.constant.kidhealth.net.RestService.AUTHENTICATION_HEADER_NAME;
import static ru.constant.kidhealth.net.RestService.HEADER_PREFIX;

public class RefreshTokenAuthenticator implements Authenticator {

    private final Context context;

    public RefreshTokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if(context != null) {
            Token token = AppUtils.getToken();
            if(token.isAccessExpired() && !token.isRefreshExpired()) {
                // Refresh your access_token using a synchronous api request
                RestService service = App.getAppComponent().getRestService();
                Observable<Token> bodyObservable = service.refreshToken();
                try {
                    token = bodyObservable.blockingFirst();
                    // Add new header to rejected request and retry it
                    AppUtils.saveToken(token);
                    return response.request().newBuilder()
                            .header(AUTHENTICATION_HEADER_NAME, HEADER_PREFIX + token.getAccessToken())
                            .build();
                } catch (Throwable ex) {
                    Cat.e(ex);
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}