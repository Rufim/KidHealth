package ru.constant.kidhealth.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import net.vrallev.android.cat.Cat;

import org.greenrobot.eventbus.Subscribe;


import okhttp3.Response;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.fragment.LoginFragment;
import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.template.domain.event.FragmentAttachedEvent;
import ru.kazantsev.template.domain.event.NetworkEvent;
import ru.kazantsev.template.util.GuiUtils;


public class MainActivity extends BaseActivity {

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        disableNavigationBar = true;
        toolbarClassic = true;
        super.onCreate(savedInstanceState);
        // Восстанавливаем фрагмент при смене ориентации экрана.
        Fragment sectionFragment = getLastFragment(savedInstanceState);
        if (sectionFragment != null) {
            restoreFragment(sectionFragment);
        } else {
            replaceFragment(LoginFragment.class);
        }
    }

    @Override
    protected void handleIntent(Intent intent) {
    }


    @Subscribe
    public void onEvent(FragmentAttachedEvent fragmentAttached) {
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(this.getText(R.string.dialog_loading_please_wait));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            return;
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
        }
    }

    public void showMessage(String error) {
        GuiUtils.runInUI(this, (v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(error);
            builder.setPositiveButton(R.string.dialog_ok, null);
            builder.setCancelable(false);
            builder.create().show();
        });
    }

    public void showMessage(int stringId) {
        showMessage(getString(stringId));
    }
}
