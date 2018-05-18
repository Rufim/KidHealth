package ru.constant.kidhealth.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.constant.kidhealth.net.RestApi;
import ru.constant.kidhealth.service.RestService;

@Module(includes = {RestApiModule.class})
public class RestServiceModule {
    @Provides
    @Singleton
    public RestService provideGithubService(RestApi authApi) {
        return new RestService(authApi);
    }
}
