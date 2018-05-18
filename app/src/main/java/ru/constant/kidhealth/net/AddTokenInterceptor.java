package ru.constant.kidhealth.net;

import android.content.Context;

import net.vrallev.android.cat.Cat;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.service.RestService;
import ru.constant.kidhealth.utils.AppUtils;

import static ru.constant.kidhealth.service.RestService.AUTHENTICATION_HEADER_NAME;
import static ru.constant.kidhealth.service.RestService.HEADER_PREFIX;

public class AddTokenInterceptor implements Interceptor {


    private final Context context;

    public AddTokenInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if(!RestService.isAuthenticationRequest(chain.request())) {
            Token token = AppUtils.getToken();
            builder.header(AUTHENTICATION_HEADER_NAME, HEADER_PREFIX + token.getAccessToken());
            Cat.v("Adding access token: " + token.getAccessToken());
        }
        return chain.proceed(builder.build());
    }

}
