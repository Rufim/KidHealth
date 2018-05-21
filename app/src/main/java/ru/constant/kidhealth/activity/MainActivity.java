package ru.constant.kidhealth.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.dagger.RetrofitModule;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.fragment.DayActionFragment;
import ru.constant.kidhealth.fragment.SchedulePagerFragment;
import ru.constant.kidhealth.fragment.SignInFragment;
import ru.constant.kidhealth.job.DayActionJob;
import ru.constant.kidhealth.job.ParentReminderJob;
import ru.constant.kidhealth.service.DatabaseService;
import ru.constant.kidhealth.utils.AppUtils;
import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.template.domain.event.FragmentAttachedEvent;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;

import static android.R.attr.textColor;


public class MainActivity extends BaseActivity {

    private ProgressDialog progressDialog;
    @Inject
    DatabaseService databaseService;

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        disableNavigationBar = true;
        toolbarClassic = true;
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        // Восстанавливаем фрагмент при смене ориентации экрана.
        Fragment sectionFragment = getLastFragment(savedInstanceState);
        if (sectionFragment != null) {
            restoreFragment(sectionFragment);
        } else {
            Token token = AppUtils.getToken();
            if(!token.isValid() || false) {
                replaceFragment(SignInFragment.class);
            } else {
                replaceFragment(SchedulePagerFragment.class);
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        instance = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    protected void handleIntent(Intent intent) {
        if(intent.getAction() != null) {
            if(intent.getAction().startsWith(DayActionJob.NOTIFICATION_ACTION_NAME)) {
                if (intent.getExtras() != null && intent.getExtras().containsKey(DayActionJob.DAY_ACTION_ID)) {
                    DayAction action = databaseService.getDayAction(intent.getExtras().getString(DayActionJob.DAY_ACTION_ID));
                    if (action != null) {
                        BaseFragment baseFragment = (BaseFragment) getCurrentFragment();
                        if (baseFragment != null) {
                            if (baseFragment.getClass() != DayActionFragment.class) {
                                DayActionFragment.show(baseFragment, action);
                            } else {
                                new FragmentBuilder(getSupportFragmentManager())
                                        .putArg(DayActionFragment.DAY_ARG, action)
                                        .refresh(baseFragment);
                            }
                        } else {
                            replaceFragment(DayActionFragment.class, new FragmentBuilder(getSupportFragmentManager()).putArg(DayActionFragment.DAY_ARG, action));
                        }
                    }
                }
            }
            if(intent.getAction().startsWith(ParentReminderJob.NOTIFICATION_ACTION_NAME)) {
                replaceFragment(SignInFragment.class);
            }
        }
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

    @Override
    public void showSnackbar(String message) {
        showSnackbar(message, getResources().getColor(R.color.primary));
    }
}
