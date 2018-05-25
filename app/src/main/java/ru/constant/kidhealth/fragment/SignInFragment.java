package ru.constant.kidhealth.fragment;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.rilixtech.CountryCodePicker;

import br.com.sapereaude.maskedEditText.MaskedEditText;
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
    MaskedEditText editTextLogin;
    @BindView(R.id.countryCodePicker)
    CountryCodePicker countryCodePicker;
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
    @BindView(R.id.reminderLayout)
    ViewGroup reminderLayout;

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

    private Pair<Integer, String> splitPhone(String fullNumber, Integer defaultCode) {
        if(fullNumber.startsWith("+")) {
            fullNumber =  fullNumber.substring(1);
        }
        int phoneIndex = fullNumber.length() - 10;
        String number = fullNumber.substring(phoneIndex);
        String code = fullNumber.substring(0, phoneIndex);
        return new Pair<>(TextUtils.extractInt(code, defaultCode), number);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        bind(rootView);
        ((MainActivity)getActivity()).hideKeyboard();
        if(BuildConfig.DEBUG) {
          //  editTextLogin.setText(BuildConfig.USERNAME);
          //  editTextPassword.setText(BuildConfig.PASSWORD);
        }
        User user = AppUtils.getLastUser();
        countryCodePicker.setTextColor(getResources().getColor(R.color.white));
        if(user != null && TextUtils.notEmpty(user.getLogin())) {
            Pair<Integer, String> codePone = splitPhone(user.getLogin(), 7);
            if(codePone.first == 7) {
                countryCodePicker.setCountryForNameCode("RU");
            } else {
                countryCodePicker.setCountryForPhoneCode(codePone.first);
            }
            editTextLogin.setText(codePone.second);
            editTextPassword.setText(user.getPassword());
        }
        ParentReminderJob.startSchedule();
        reminderFullText.setMovementMethod(LinkMovementMethod.getInstance());
        reminderFullText.setText(getString(R.string.reminder_full_text, RestService.BASE_URL));
        if(AppUtils.isLoggedOnce()) {
            reminderLayout.setVisibility(View.GONE);
        }
        return rootView;
    }


    @OnClick(R.id.textViewBtnLogin)
    public void onClickLogin() {
        ((MainActivity)getActivity()).hideKeyboard();
        signInPresenter.signIn(countryCodePicker.getSelectedCountryCodeWithPlus() + editTextLogin.getRawText(), editTextPassword.getText().toString());
    }

    @OnClick(R.id.textViewBtnRegister)
    public void onClickRegistration() {
        ((MainActivity)getActivity()).hideKeyboard();
        ///RegistrationFragment.show(this);
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
        textViewMessage.setVisibility(View.INVISIBLE);
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
