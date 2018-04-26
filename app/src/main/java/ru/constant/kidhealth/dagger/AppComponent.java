package ru.constant.kidhealth.dagger;


import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.fragment.LoginFragment;
import ru.constant.kidhealth.mvp.presenters.DayActionPresenter;
import ru.constant.kidhealth.mvp.presenters.DayActionsPresenter;
import ru.constant.kidhealth.mvp.presenters.SchedulePresenter;
import ru.constant.kidhealth.mvp.presenters.SignInPresenter;
import ru.constant.kidhealth.net.RestService;

/**
 * Created by Dmitry on 28.06.2016.
 */

@Singleton
@Component(modules = {ContextModule.class, RestServiceModule.class})
public interface AppComponent {
    Context getContext();
    RestService getRestService();

    void inject(SignInPresenter presenter);

    void inject(DayActionsPresenter presenter);

    void inject(MainActivity activity);

    void inject(LoginFragment fragment);

    void inject(SchedulePresenter schedulePresenter);

    void inject(DayActionPresenter dayActionPresenter);
}
