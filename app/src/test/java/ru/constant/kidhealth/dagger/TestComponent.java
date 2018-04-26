package ru.constant.kidhealth.dagger;

import android.content.Context;

import org.robolectric.RuntimeEnvironment;

import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.fragment.LoginFragment;
import ru.constant.kidhealth.mvp.presenters.DayActionsPresenter;
import ru.constant.kidhealth.mvp.presenters.SchedulePresenter;
import ru.constant.kidhealth.mvp.presenters.SignInPresenter;
import ru.constant.kidhealth.net.RestApi;
import ru.constant.kidhealth.net.RestService;

public class TestComponent implements AppComponent {
    RetrofitModule retrofitModule = new MockRetrofitModule();
    RestApiModule restApiModule = new RestApiModule();
    ContextModule contextModule = new MockContextModule();
    RestServiceModule restServiceModule = new RestServiceModule();

    @Override
    public Context getContext() {
        return contextModule.provideContext();
    }

    @Override
    public RestService getRestService() {
        return restServiceModule.provideGithubService(restApiModule.provideAuthApi(
                retrofitModule.provideRetrofit(
                        retrofitModule.provideRetrofitBuilder(
                                retrofitModule.provideConverterFactory(
                                        retrofitModule.provideGson()),
                                retrofitModule.provideOkHttpBuilder(getContext())))));
    }

    @Override
    public void inject(SignInPresenter presenter) {
        presenter.restService = getRestService();
    }

    @Override
    public void inject(DayActionsPresenter presenter) {
        presenter.restService = getRestService();
    }

    @Override
    public void inject(MainActivity activity) { }

    @Override
    public void inject(LoginFragment fragment) { }

    @Override
    public void inject(SchedulePresenter schedulePresenter) { }
}
