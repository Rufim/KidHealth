package ru.constant.kidhealth.mvp.presenters;

import android.content.Context;

import com.arellomobile.mvp.InjectViewState;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.mvp.views.DayActionView;
import ru.constant.kidhealth.net.RestService;
import ru.kazantsev.template.mvp.presenter.BasePresenter;
import ru.kazantsev.template.util.PreferenceMaster;
import ru.kazantsev.template.util.TextUtils;

@InjectViewState
public class DayActionPresenter extends BasePresenter<DayActionView> {

    private static final String PASSED_TIME = "passedTime";
    private static final String WEEK_DAY = "weekDay";
    private static final String FINISH_TIME = "finish_time";
    private static final String START_TIME = "startTime";
    private static final String LAST_TIME_ID = "id";

    @Inject
    RestService restService;
    @Inject
    Context context;

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private Timer timer;
    private DayAction dayAction;
    private DateTime startTime;
    private DateTime endTime;
    private boolean stopped = false;
    private PreferenceMaster preferenceMaster;
    private PeriodFormatter timeFormatter = new PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendMinutes()
            .appendSuffix(":")
            .appendSeconds()
            .toFormatter();



    public DayActionPresenter() {
        App.getAppComponent().inject(this);
        preferenceMaster = new PreferenceMaster(context, false);
        String id;
        if(TextUtils.notEmpty(id = preferenceMaster.getValue(LAST_TIME_ID))) {
            dayAction = new DayAction();
            dayAction.setId(id);
            dayAction.setDayOfWeek(WeekDay.valueOf(preferenceMaster.getValue(WEEK_DAY)));
            dayAction.setStartTime(preferenceMaster.getValue(START_TIME));
            dayAction.setFinishTime(preferenceMaster.getValue(FINISH_TIME));
            dayAction.setActive(true);
            dayAction.invalidateTime();
            startTime = fmt.parseDateTime(preferenceMaster.getValue(PASSED_TIME));
            endTime = dayAction.getEnd();
        }
    }

    public DayActionPresenter setDayAction(DayAction dayAction) {
        if(this.dayAction == null) {
            if (dayAction != null) {
                initTime(dayAction);
            }
        } else {
            if(!this.dayAction.equals(dayAction)) {
                initTime(dayAction);
            } else if(!stopped){
                getViewState().updateTime(timeFormatter.print(new Duration(startTime, DateTime.now()).toPeriod()));
                continueAction();
            }
        }
        return this;
    }

    private void initTime(DayAction dayAction) {
        stopped = false;
        this.dayAction = dayAction;
        dayAction.invalidateTime();
        startTime = dayAction.getStart();
        endTime = dayAction.getEnd();
        onReset();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        invalidateActions();
    }

    public void continueAction() {
        stopTimer();
        timer = new Timer();
        if (startTime.isBefore(endTime)) {
            getViewState().onStarted();
            preferenceMaster.putValue(LAST_TIME_ID, dayAction.getId())
                    .putValue(START_TIME, dayAction.getStartTime())
                    .putValue(FINISH_TIME, dayAction.getFinishTime())
                    .putValue(WEEK_DAY, dayAction.getDayOfWeek().name())
                    .commit();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    DateTime now = DateTime.now();
                    if (now.isAfter(startTime)) {
                        if (now.isBefore(endTime)) {
                            preferenceMaster.putValue(PASSED_TIME, fmt.print(startTime)).applay();
                            getViewState().updateTime(timeFormatter.print(new Duration(startTime, now).toPeriod()));
                        } else {
                            onFinish();
                        }
                    }
                }
            }, 0, 1000);
        } else {
            onReset();
        }
    }

    public void startAction() {
        if (dayAction != null && dayAction.getActive() && timer == null) {
            startTime = DateTime.now();
            stopped = false;
            continueAction();
        }
    }

    public void stopAction() {
        if(timer != null) {
            getViewState().updateTime(timeFormatter.print(new Duration(startTime, DateTime.now()).toPeriod()));
        }
        onReset();
    }

    private void onReset() {
        stopTimer();
        getViewState().updateTime("00:00");
    }

    private void onFinish() {

        if(startTime.isBefore(endTime)) getViewState().updateTime(timeFormatter.print(new Duration(startTime, endTime).toPeriod()));
        getViewState().onFinished();
        stopTimer();
    }

    private void stopTimer() {
        preferenceMaster.putValue(LAST_TIME_ID, "").applay();
        stopped = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
            new PreferenceMaster(context).putValue(LAST_TIME_ID, "");
        }
    }

    public void invalidateActions() {
        if(!dayAction.getActive()) {
            getViewState().hideButton(R.id.day_action_start, 0);
        } else {
            DateTime now = DateTime.now();
            if (now.isAfter(endTime)) {
                getViewState().hideButton(R.id.day_action_start, 0);
            } else {
                getViewState().hideButton(R.id.day_action_start, new Duration(now, endTime).getMillis() + 1000);
            }
        }
    }
}
