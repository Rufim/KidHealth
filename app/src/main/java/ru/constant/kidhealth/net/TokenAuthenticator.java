package ru.constant.kidhealth.net;

import android.content.Context;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator  implements Authenticator {

    private final Context context;

    public TokenAuthenticator(Context context) {
        this.context = context;
    }


    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if(context != null) {
            //CMUser user = CMUser.getInstance(context);

            // Refresh your access_token using a synchronous api request
            //Token newAccessToken = service.refreshToken(user.getRefreshToken(), user.getId()).execute().body();
            //user.update(newAccessToken);

            // Add new header to rejected request and retry it
            return response.request().newBuilder()
                    //.header("Authentication", newAccessToken.getAccessToken())
                    .build();
        } else {
            return null;
        }
    }

}