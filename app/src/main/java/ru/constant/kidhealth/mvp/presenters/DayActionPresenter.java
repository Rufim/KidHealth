package ru.constant.kidhealth.mvp.presenters;

import android.annotation.SuppressLint;
import android.content.Context;

import com.annimon.stream.Stream;
import com.arellomobile.mvp.InjectViewState;

import net.vrallev.android.cat.Cat;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.Constants;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.domain.event.UpdateAction;
import ru.constant.kidhealth.domain.models.ActionStatus;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.LogStatus;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.job.DayActionJob;
import ru.constant.kidhealth.mvp.views.DayActionView;
import ru.constant.kidhealth.service.DatabaseService;
import ru.constant.kidhealth.service.RestService;
import ru.kazantsev.template.mvp.presenter.BasePresenter;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.template.util.PreferenceMaster;
import ru.kazantsev.template.util.TextUtils;

@InjectViewState
public class DayActionPresenter extends BasePresenter<DayActionView> {

    private static final String WEEK_DAY = "weekDay";
    private static final String FINISH_TIME = "finish_time";
    private static final String START_TIME = "startTime";
    private static final String START_ACTION = "startAction";
    private static final String END_ACTION = "endAction";
    private static final String LAST_TIME_ID = "id";

    @Inject
    RestService restService;
    @Inject
    DatabaseService databaseService;
    @Inject
    Context context;

    private DateTimeFormatter ISO = ISODateTimeFormat.dateTime();
    private Timer timer;
    private DayAction dayAction;
    private DateTime startTime;
    private DateTime endTime;
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
        if (TextUtils.notEmpty(id = preferenceMaster.getValue(LAST_TIME_ID))) {
            dayAction = new DayAction();
            dayAction.setId(id);
            dayAction.setDayOfWeek(WeekDay.valueOf(preferenceMaster.getValue(WEEK_DAY)));
            dayAction.setStartDateTime(preferenceMaster.getValue(START_TIME));
            dayAction.setFinishDateTime(preferenceMaster.getValue(FINISH_TIME));
            dayAction.setActive(true);
            dayAction.invalidateTime();
            startTime = ISO.parseDateTime(preferenceMaster.getValue(START_ACTION, preferenceMaster.getValue(START_TIME)));
            endTime = ISO.parseDateTime(preferenceMaster.getValue(END_ACTION, preferenceMaster.getValue(FINISH_TIME)));
        }
    }

    public DayActionPresenter setDayAction(DayAction dayAction) {
        if (this.dayAction == null) {
            if (dayAction != null) {
                initTime(dayAction);
            }
        } else {
            if(dayAction != null) {
                this.dayAction.invalidateTime();
                dayAction.invalidateTime();
                if (!this.dayAction.equals(dayAction)) {
                    initTime(dayAction);
                } else if (dayAction.isRunning()) {
                    getViewState().updateTime(timeFormatter.print(new Duration(startTime, DateTime.now()).toPeriod()));
                }
            }
        }
        invalidateActions();
        return this;
    }

    private void initTime(DayAction dayAction) {
        this.dayAction = dayAction;
        dayAction.invalidateTime();
        if(dayAction.isValid()) {
            startTime = dayAction.getFirstAction().getStart();
            endTime = dayAction.getLastAction().getEnd();
        }
        onReset();
    }

    private void postUpdateActionEvent(DayAction action) {
        if(action != null && action.isValid()) {
            dayAction = action;
            EventBus.getDefault().post(new UpdateAction(action));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void continueAction() {
        stopTimer();
        timer = new Timer();
        if (startTime.isBefore(endTime)) {
            GuiUtils.runInUI(context, var -> getViewState().onStarted());
            preferenceMaster.putValue(LAST_TIME_ID, dayAction.getId())
                    .putValue(START_TIME, dayAction.getStartDateTime())
                    .putValue(FINISH_TIME, dayAction.getFinishDateTime())
                    .putValue(WEEK_DAY, dayAction.getDayOfWeek().name())
                    .putValue(START_ACTION, ISO.print(startTime))
                    .putValue(END_ACTION, ISO.print(endTime))
                    .commit();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    DateTime now = DateTime.now();
                    if (now.isAfter(startTime)) {
                        if (now.isBefore(endTime)) {
                            getViewState().updateTime(timeFormatter.print(new Duration(startTime, now).toPeriod()));
                        } else {
                            onFinish();
                        }
                    }
                }
            }, 0, 1000);
        } else {
            onReset();
            if(DateTime.now().isAfter(endTime)) {
                onFinish();
            }
        }
    }

    public void startAction() {
        dispouseOnDestroy(restService.startAction(dayAction.getId()).subscribe(
                response -> {
                    if (response != null && response.getCurrentStatuses().size() > 0 && response.getCurrentStatuses().first().getStatus() == ActionStatus.STARTED) {
                        if (dayAction != null && dayAction.isActive() && timer == null) {
                            startTime = DateTime.now();
                            continueAction();
                            getViewState().onStarted();
                            databaseService.startDayAction(dayAction);
                            postUpdateActionEvent(dayAction);
                        } else {
                            getViewState().onActionFailure();
                        }
                    }
                },
                error -> {
                    Cat.e(error);
                    getViewState().onActionFailure();
                }
        ));
    }

    public void stopAction() {
        dispouseOnDestroy(restService.stopAction(dayAction.getId()).subscribe(
                response -> {
                    if (response != null && response.getCurrentStatuses().size() > 0 && response.getCurrentStatuses().first().getStatus() == ActionStatus.STOPPED) {
                        if (timer != null) {
                            getViewState().updateTime(timeFormatter.print(new Duration(startTime, DateTime.now()).toPeriod()));
                        }
                        onReset();
                        databaseService.stopDayAction(dayAction);
                        postUpdateActionEvent(dayAction);
                        getViewState().onCanceled();
                    } else {
                        getViewState().onActionFailure();
                    }
                },
                error -> {
                    getViewState().onActionFailure();
                }
        ));

    }

    public void postponeAction() {
        dispouseOnDestroy(restService.postponeAction(dayAction.getId()).subscribe(
                response -> {
                    if (response != null && response.getCurrentStatuses().size() > 0 && response.getCurrentStatuses().first().getStatus() == ActionStatus.POSTPONED) {
                        databaseService.postponeDayAction(dayAction);
                        getViewState().onPostpone();
                        postUpdateActionEvent(dayAction);
                        DayActionJob.startSchedule(dayAction, TimeUnit.MINUTES.toMillis(Constants.App.POSTPONE_MINUTES));
                    } else {
                        getViewState().onActionFailure();
                    }
                },
                error -> {
                    getViewState().onActionFailure();
                }
        ));
    }

    public void finishAction() {
        dispouseOnDestroy(restService.finishAction(dayAction.getId()).subscribe(
                response -> {
                    if (response != null && response.getCurrentStatuses().size() > 0 && response.getCurrentStatuses().first().getStatus() == ActionStatus.FINISHED) {
                        if (startTime.isBefore(endTime))
                            getViewState().updateTime(timeFormatter.print(new Duration(startTime, endTime).toPeriod()));
                        databaseService.finishDayAction(dayAction);
                        postUpdateActionEvent(dayAction);
                        getViewState().onFinished();
                    } else {
                        getViewState().onActionFailure();
                    }
                },
                error -> {
                    getViewState().onActionFailure();
                }
        ));
    }

    private void onReset() {
        stopTimer();
        getViewState().updateTime("00:00");
    }

    private void onFinish() {
        stopTimer();
        GuiUtils.runInUI(context, var -> getViewState().onFinish());
    }

    private void stopTimer() {
        preferenceMaster.putValue(LAST_TIME_ID, "").applay();
        if (timer != null) {
            timer.cancel();
            timer = null;
            new PreferenceMaster(context).putValue(LAST_TIME_ID, "");
        }
    }

    @SuppressLint("CheckResult")
    public void invalidateActions() {
        getViewState().cleanState();
        if(dayAction != null && TextUtils.notEmpty(dayAction.getId())) {
            restService.getAction(dayAction.getId())
                    .subscribe(freshAction -> {
                                dayAction = freshAction;
                                dayAction.invalidateTime();
                                SortedSet<LogStatus> statuses = dayAction.getCurrentStatuses();
                                if (statuses.size() > 0) {
                                    LogStatus started = Stream.of(statuses)
                                            .filter(stat -> stat.getStatus().equals(ActionStatus.STARTED)).findFirst().orElse(null);
                                    if (started != null) {
                                        startTime = started.getDateTime();
                                    }
                                } else {
                                    startTime = dayAction.getFirstAction().getStart();
                                }
                                endTime = dayAction.getLastAction().getEnd();
                                if (!dayAction.isActive()) {
                                    inactiveAction();
                                    return;
                                }
                                if (dayAction.isStopped()) {
                                    getViewState().onCanceled();
                                    return;
                                }

                                if (dayAction.isFinished()) {
                                    getViewState().onFinished();
                                    return;
                                }

                                DateTime now = DateTime.now();
                                if (now.isAfter(endTime)) {
                                    getViewState().updateTime(timeFormatter.print(new Duration(startTime, endTime).toPeriod()));
                                } else {
                                    getViewState().switchStateButton(R.id.day_action_start, true);
                                    getViewState().switchStateButton(R.id.day_action_postpone, true);
                                    getViewState().switchStateButton(R.id.day_action_cancel, true);
                                }

                                if (dayAction.isPostponed() || dayAction.isStarted() || dayAction.isStopped() ||
                                        (Constants.App.POSTPONE_MINUTES >= new Duration(startTime, endTime).getStandardMinutes())) {
                                    getViewState().switchStateButton(R.id.day_action_postpone, false);
                                }
                                if (dayAction.isStarted() && !dayAction.isFinished() && !dayAction.getStopped()) {
                                    getViewState().switchStateButton(R.id.day_action_start, false);
                                    continueAction();
                                }
                            },
                            error -> {
                                getViewState().onActionFailure();
                                if (dayAction.isRunning()) {
                                    continueAction();
                                }
                            });
        }
    }

    private void inactiveAction() {
        getViewState().switchStateButton(R.id.day_action_start, false);
        getViewState().switchStateButton(R.id.day_action_postpone, false);
        getViewState().switchStateButton(R.id.day_action_cancel, false);
        getViewState().switchStateButton(R.id.day_action_finish, false);
    }
}
