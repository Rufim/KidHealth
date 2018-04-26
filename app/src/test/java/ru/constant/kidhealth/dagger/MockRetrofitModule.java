package ru.constant.kidhealth.dagger;

import java.io.IOException;

import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;

public class MockRetrofitModule extends RetrofitModule {

    private static MockWebServer mockWebServer;

    @Override
    public Retrofit provideRetrofit(Retrofit.Builder builder) {
     	return builder.baseUrl(getMockWebServer().url("").toString()).build();
    }

    public synchronized static MockWebServer getMockWebServer() {
        return mockWebServer == null ? (mockWebServer = new MockWebServer()) : mockWebServer;
    }
}
