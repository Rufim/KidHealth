package ru.constant.kidhealth.mvp.presenters;

import android.text.TextUtils;

import com.arellomobile.mvp.InjectViewState;

import net.vrallev.android.cat.Cat;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import ru.constant.kidhealth.App;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.mvp.views.SignInView;
import ru.constant.kidhealth.service.RestService;
import ru.constant.kidhealth.utils.AppUtils;
import ru.kazantsev.template.mvp.presenter.BasePresenter;
import ru.kazantsev.template.util.RxUtils;


@InjectViewState
public class SignInPresenter extends BasePresenter<SignInView> {

    @Inject
    public RestService restService;

    public SignInPresenter() {
        App.getAppComponent().inject(this);
    }

    public void signIn(final String login, final String password) {

        getViewState().hideFormError();

        if (TextUtils.isEmpty(login)) {
            getViewState().invalidLogin(R.string.login_error);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            getViewState().invalidPassword(R.string.login_password_error);
            return;
        }

        getViewState().startSignIn();

        Disposable disposable = restService.signIn(login, password)
                .compose(RxUtils.applySchedulers())
                .subscribe(token -> {
                    AppUtils.saveToken(token);
                    AppUtils.saveUser(login, password);
                    getViewState().finishSignIn();
                    getViewState().successSignIn();
                }, exception -> {
                    Cat.e(exception);
                    getViewState().finishSignIn();
                    getViewState().failedSignIn(exception.getMessage());
                });

        dispouseOnDestroy(disposable);
    }

    public void onErrorCancel() {
        getViewState().hideError();
    }
}
