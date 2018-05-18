package ru.constant.kidhealth.fragment;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;

import butterknife.BindView;
import butterknife.OnClick;
import ru.constant.kidhealth.BuildConfig;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.domain.models.User;
import ru.constant.kidhealth.job.ParentReminderJob;
import ru.constant.kidhealth.mvp.presenters.SignInPresenter;
import ru.constant.kidhealth.mvp.views.SignInView;
import ru.constant.kidhealth.service.RestService;
import ru.constant.kidhealth.utils.AppUtils;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.TextUtils;

public class SignInFragment extends BaseFragment implements SignInView {

    @InjectPresenter
    SignInPresenter signInPresenter;

    @BindView(R.id.load_more)
    ProgressBar progressBar;
    @BindView(R.id.editTextLogin)
    EditText editTextLogin;
    @BindView(R.id.editTextPassword)
    EditText editTextPassword;
    @BindView(R.id.textViewBtnLogin)
    TextView textViewBtnLogin;
    @BindView(R.id.textViewBtnRegister)
    TextView textViewBtnRegister;
    @BindView(R.id.textViewMessage)
    TextView textViewMessage;
    @BindView(R.id.reminderFullText)
    TextView reminderFullText;

    @Override
    public void onStart() {
        super.onStart();
        getBaseActivity().hideActionBar();
    }

    @Override
    public void onStop() {
        getBaseActivity().showActionBar();
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        bind(rootView);
        ((MainActivity)getActivity()).hideKeyboard();
        if(BuildConfig.DEBUG) {
            editTextLogin.setText(BuildConfig.USERNAME);
            editTextPassword.setText(BuildConfig.PASSWORD);
        }
        User user = AppUtils.getLastUser();
        if(user != null && TextUtils.notEmpty(user.getLogin())) {
            editTextLogin.setText(user.getLogin());
            editTextPassword.setText(user.getPassword());
        }
        ParentReminderJob.startSchedule();
        reminderFullText.setMovementMethod(LinkMovementMethod.getInstance());
        reminderFullText.setText(getString(R.string.reminder_full_text, RestService.BASE_URL));
        return rootView;
    }


    @OnClick(R.id.textViewBtnLogin)
    public void onClickLogin() {
        ((MainActivity)getActivity()).hideKeyboard();
        signInPresenter.signIn(editTextLogin.getText().toString(), editTextPassword.getText().toString());
    }

    @OnClick(R.id.textViewBtnRegister)
    public void onClickRegistration() {
        ((MainActivity)getActivity()).hideKeyboard();
        ///RegistrationFragment.show(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public static SignInFragment show (BaseFragment fragment) {
        return show(fragment, SignInFragment.class);
    }

    @Override
    public void startSignIn() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishSignIn() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void failedSignIn(String message) {
        textViewMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideError() {
        textViewMessage.setVisibility(View.GONE);
    }

    @Override
    public void hideFormError() {
        editTextLogin.setError(null);
        editTextPassword.setError(null);
    }

    @Override
    public void invalidLogin(Integer loginError) {
        editTextLogin.setError(getString(loginError));
    }

    @Override
    public void invalidPassword(Integer passwordError) {
        editTextPassword.setError(getString(passwordError));
    }


    @Override
    public void successSignIn() {
        Log.e("LOGIN", "SUCCESS");
        AppUtils.toggleLoggedOnce();
        getBaseActivity().replaceFragment(SchedulePagerFragment.class);
    }
}
