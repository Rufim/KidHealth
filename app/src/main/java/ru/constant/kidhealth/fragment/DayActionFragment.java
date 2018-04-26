package ru.constant.kidhealth.fragment;

import android.os.Bundle;
import android.os.Handler;
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
import com.arellomobile.mvp.presenter.PresenterType;
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

    @InjectPresenter(type = PresenterType.GLOBAL)
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
    @BindView(R.id.day_action_time_passed)
    TextView dayActionTimePassed;
    @BindView(R.id.day_action_start)
    Button dayActionStart;
    @BindView(R.id.day_action_cancel)
    Button dayActionCancel;
    @BindView(R.id.day_action_pass)
    Button dayActionPass;

    private DayAction dayAction;
    private Handler forButtons;
    private Runnable postInvalidate;

    @ProvidePresenterTag(presenterClass = DayActionsPresenter.class, type = PresenterType.GLOBAL)
    String provideTag() {
        if(dayAction == null) {
            dayAction = getArg(DAY_ARG, null);
        }
        return dayAction.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        forButtons = new Handler();
        presenter.setDayAction(dayAction = getArg(DAY_ARG, null));
        View view = inflater.inflate(R.layout.fragment_day_action, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) view;
        bind(rootView);
        if (dayAction != null) {
            GuiUtils.setText(rootView, R.id.day_action_time, AppUtils.fixTime(dayAction.getStartTime()) + " - " + AppUtils.fixTime(dayAction.getFinishTime()));
            GuiUtils.setText(rootView, R.id.day_action_title, dayAction.getTitle());
            GuiUtils.setText(rootView, R.id.day_action_comment, dayAction.getComment());
            if (dayAction.getActive()) {
                dayActionActive.setBackgroundResource(R.drawable.green_light);
            } else {
                dayActionActive.setBackgroundResource(R.drawable.red_light);
            }
            presenter.invalidateActions();
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
    public void hideButton(@IdRes int id, long delay) {
        if (delay <= 0) {
            switch (id) {
                case R.id.day_action_start:
                    dayActionStart.setEnabled(false);
                    break;
                case R.id.day_action_cancel:
                    dayActionCancel.setVisibility(View.GONE);
                    break;
                case R.id.day_action_pass:
                    dayActionPass.setVisibility(View.GONE);
                    break;
            }
        } else {
            if(postInvalidate != null) {
                forButtons.removeCallbacks(postInvalidate);
            }
            postInvalidate = () -> presenter.invalidateActions();
            forButtons.postDelayed(postInvalidate, delay);
        }
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void updateTime(String time) {
        getView().post(() -> dayActionTimePassed.setText(time));
    }

    @Override
    public void onFinished() {

    }

    @OnClick(R.id.day_action_start)
    public void onDayActionStartClicked() {
        presenter.startAction();
    }

    @OnClick(R.id.day_action_cancel)
    public void onDayActionCancelClicked() {
        presenter.stopAction();
    }

    @OnClick(R.id.day_action_pass)
    public void onDayActionPassClicked() {

    }
}
