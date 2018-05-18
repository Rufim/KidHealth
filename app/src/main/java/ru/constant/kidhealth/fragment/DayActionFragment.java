package ru.constant.kidhealth.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenterTag;

import butterknife.BindView;
import butterknife.OnClick;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.mvp.presenters.DayActionPresenter;
import ru.constant.kidhealth.mvp.presenters.DayActionsPresenter;
import ru.constant.kidhealth.mvp.views.DayActionView;
import ru.constant.kidhealth.utils.AppUtils;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.GuiUtils;

public class DayActionFragment extends BaseFragment implements DayActionView {

    public static final String DAY_ARG = "dayArg";

    public static DayActionFragment show(BaseFragment fragment, DayAction dayAction) {
        return show(fragment, DayActionFragment.class, DAY_ARG, dayAction);
    }

    @InjectPresenter
    DayActionPresenter presenter;

    @BindView(R.id.load_more)
    ProgressBar loadMore;
    @BindView(R.id.day_action_active)
    View dayActionActive;
    @BindView(R.id.day_action_time)
    TextView dayActionTime;
    @BindView(R.id.day_action_title)
    TextView dayActionTitle;
    @BindView(R.id.day_action_comment)
    TextView dayActionComment;
    @BindView(R.id.day_action_status)
    TextView dayActionStatus;
    @BindView(R.id.day_action_time_passed)
    TextView dayActionTimePassed;
    @BindView(R.id.day_action_start)
    Button dayActionStart;
    @BindView(R.id.day_action_cancel)
    Button dayActionCancel;
    @BindView(R.id.day_action_postpone)
    Button dayActionPass;

    private DayAction dayAction;
    private Runnable postInvalidate;

    @ProvidePresenterTag(presenterClass = DayActionsPresenter.class)
    String provideTag() {
        if (dayAction == null) {
            dayAction = getArg(DAY_ARG, null);
        }
        return dayAction.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_day_action, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) view;
        bind(rootView);
        updateTime("00:00");
        presenter.setDayAction(dayAction = getArg(DAY_ARG, null));
        if (dayAction != null) {
            GuiUtils.setText(rootView, R.id.day_action_time, AppUtils.fixTime(dayAction.getStartTime()) + " - " + AppUtils.fixTime(dayAction.getFinishTime()));
            GuiUtils.setText(rootView, R.id.day_action_title, dayAction.getTitle());
            GuiUtils.setText(rootView, R.id.day_action_comment, dayAction.getComment());
            if (dayAction.isActive()) {
                dayActionActive.setBackgroundResource(R.drawable.green_light);
            } else {
                dayActionActive.setBackgroundResource(R.drawable.red_light);
            }
        }
    }

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
    public void switchStateButton(@IdRes int id, boolean state) {
        switch (id) {
            case R.id.day_action_start:
                dayActionStart.setEnabled(state);
                break;
            case R.id.day_action_cancel:
                dayActionCancel.setEnabled(state);
                break;
            case R.id.day_action_postpone:
                dayActionPass.setEnabled(state);
                break;
        }
    }

    @Override
    public void cleanState() {
        dayActionStart.setEnabled(false);
        dayActionCancel.setEnabled(false);
        dayActionPass.setEnabled(false);
        dayActionStatus.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStarted() {
        switchStateButton(R.id.day_action_postpone, false);
        switchStateButton(R.id.day_action_cancel, true);
        switchStateButton(R.id.day_action_start, false);
        getBaseActivity().showSnackbar(R.string.day_action_started);
        dayActionStatus.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCanceled() {
        switchStateButton(R.id.day_action_postpone, false);
        switchStateButton(R.id.day_action_cancel, false);
        switchStateButton(R.id.day_action_start, false);
        dayActionStatus.setVisibility(View.VISIBLE);
        dayActionStatus.setText(R.string.day_action_canceled);
        getBaseActivity().showSnackbar(R.string.day_action_canceled);
        dayActionStatus.setTextColor(getResources().getColor(R.color.md_red_400));
        getBaseActivity().onBackPressed();
    }

    @Override
    public void updateTime(String time) {
        if(isAdded()) getView().post(() -> {if(dayActionTimePassed!= null) dayActionTimePassed.setText(time);});
    }

    @Override
    public void onFinished() {
        switchStateButton(R.id.day_action_postpone, false);
        switchStateButton(R.id.day_action_cancel, false);
        switchStateButton(R.id.day_action_start, false);
        getBaseActivity().showSnackbar(R.string.day_action_finished);
        dayActionStatus.setVisibility(View.VISIBLE);
        dayActionStatus.setText(R.string.day_action_finished);
        dayActionStatus.setTextColor(getResources().getColor(R.color.md_green_400));
    }

    @Override
    public void onPostpone() {
        getBaseActivity().showSnackbar(R.string.day_action_postponed);
        getBaseActivity().onBackPressed();
    }

    @Override
    public void onActionFailure() {
        switchStateButton(R.id.day_action_postpone, false);
        switchStateButton(R.id.day_action_cancel, false);
        switchStateButton(R.id.day_action_start, false);
        getBaseActivity().showSnackbar(R.string.error);
    }

    @OnClick(R.id.day_action_start)
    public void onDayActionStartClicked() {
        presenter.startAction();
    }

    @OnClick(R.id.day_action_cancel)
    public void onDayActionCancelClicked() {
        presenter.stopAction();
    }

    @OnClick(R.id.day_action_postpone)
    public void onDayActionPassClicked() {
        presenter.postponeAction();
    }
}
