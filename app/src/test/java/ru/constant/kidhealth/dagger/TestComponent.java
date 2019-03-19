package ru.constant.kidhealth.dagger;

import android.content.Context;

import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.fragment.SignInFragment;
import ru.constant.kidhealth.mvp.presenters.DayActionsPresenter;
import ru.constant.kidhealth.mvp.presenters.SignInPresenter;
import ru.constant.kidhealth.mvp.presenters.SchedulePresenter;
import ru.constant.kidhealth.service.RestService;

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
        return restServiceModule.provideRestService(restApiModule.provideAuthApi(
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
    public void inject(SignInFragment fragment) { }

    @Override
    public void inject(SchedulePresenter schedulePresenter) { }
}
