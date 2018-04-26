package ru.constant.kidhealth.fragment;

import android.os.Bundle;
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
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.mvp.presenters.SignInPresenter;
import ru.constant.kidhealth.mvp.views.SignInView;
import ru.kazantsev.template.fragments.BaseFragment;

public class LoginFragment extends BaseFragment implements SignInView {

    @InjectPresenter
    SignInPresenter signInPresenter;

    @BindView(R.id.load_more)
    ProgressBar progressBar;
    @BindView(R.id.editTextLogin)
    EditText editTextEmail;
    @BindView(R.id.editTextPassword)
    EditText editTextPassword;
    @BindView(R.id.textViewBtnLogin)
    TextView textViewBtnLogin;
    @BindView(R.id.textViewBtnRegister)
    TextView textViewBtnRegister;
    @BindView(R.id.textViewMessage)
    TextView textViewMessage;

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
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        bind(rootView);
        ((MainActivity)getActivity()).hideKeyboard();
        return rootView;
    }


    @OnClick(R.id.textViewBtnLogin)
    public void onClickLogin() {
        ((MainActivity)getActivity()).hideKeyboard();
        signInPresenter.signIn(editTextEmail.getText().toString(), editTextPassword.getText().toString());
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

    public static LoginFragment show (BaseFragment fragment) {
        return show(fragment, LoginFragment.class);
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
        editTextEmail.setError(null);
        editTextPassword.setError(null);
    }

    @Override
    public void invalidLogin(Integer loginError) {
        editTextEmail.setError(getString(loginError));
    }

    @Override
    public void invalidPassword(Integer passwordError) {
        editTextPassword.setError(getString(passwordError));
    }


    @Override
    public void successSignIn() {
        Log.e("LOGIN", "SUCCESS");
        getBaseActivity().replaceFragment(SchedulePagerFragment.class);
    }
}
