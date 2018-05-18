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
import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.fragment.SignInFragment;
import ru.constant.kidhealth.service.RestService;
import ru.constant.kidhealth.utils.AppUtils;

import static ru.constant.kidhealth.service.RestService.AUTHENTICATION_HEADER_NAME;
import static ru.constant.kidhealth.service.RestService.HEADER_PREFIX;

public class RefreshTokenAuthenticator implements Authenticator {

    private final Context context;

    public RefreshTokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if(context != null) {
            Token token = AppUtils.getToken();
            if(!token.isRefreshExpired() && !RestService.isAuthenticationRequest(response.request())) {
                // Refresh your access_token using a synchronous api request
                RestService service = App.getAppComponent().getRestService();
                Observable<Token> bodyObservable = service.refreshToken(HEADER_PREFIX + token.getRefreshToken());
                try {
                    Token accessToken = bodyObservable.blockingFirst();
                    token.setAccessToken(accessToken.getAccessToken());
                    AppUtils.saveToken(token);
                    // Add new header to rejected request and retry it
                    return response.request().newBuilder()
                            .header(AUTHENTICATION_HEADER_NAME, HEADER_PREFIX + token.getAccessToken())
                            .build();
                } catch (Throwable ex) {
                    Cat.e(ex);
                    AppUtils.clearToken();
                    showSignIn();
                    return null;
                }
            } else {
                showSignIn();
                return null;
            }
        } else {
            return null;
        }
    }

    private void showSignIn() {
        MainActivity activity = MainActivity.getInstance();
        if(activity != null) {
            activity.replaceFragment(SignInFragment.class);
        }
    }

}