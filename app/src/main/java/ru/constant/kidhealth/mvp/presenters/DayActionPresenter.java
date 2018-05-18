package ru.constant.kidhealth.mvp.presenters;

import android.content.Context;

import com.annimon.stream.Stream;
import com.arellomobile.mvp.InjectViewState;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.domain.event.UpdateAction;
import ru.constant.kidhealth.domain.models.ActionStatus;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.LogStatus;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.job.DayActionJob;
import ru.constant.kidhealth.mvp.views.DayActionView;
import ru.constant.kidhealth.service.RestService;
import ru.constant.kidhealth.service.DatabaseService;
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
    private static final int POSTMPONE_MINUTES = 10;

    @Inject
    RestService restService;
    @Inject
    DatabaseService databaseService;
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
        if (TextUtils.notEmpty(id = preferenceMaster.getValue(LAST_TIME_ID))) {
            dayAction = new DayAction();
            dayAction.setId(id);
            dayAction.setDayOfWeek(WeekDay.valueOf(preferenceMaster.getValue(WEEK_DAY)));
            dayAction.setStartDateTime(preferenceMaster.getValue(START_TIME));
            dayAction.setFinishDateTime(preferenceMaster.getValue(FINISH_TIME));
            dayAction.setActive(true);
            dayAction.invalidateTime();
            startTime = fmt.parseDateTime(preferenceMaster.getValue(PASSED_TIME));
            endTime = dayAction.getEnd();
        }
    }

    public DayActionPresenter setDayAction(DayAction dayAction) {
        if (this.dayAction == null) {
            if (dayAction != null) {
                initTime(dayAction);
            }
        } else {
            if (!this.dayAction.equals(dayAction)) {
                initTime(dayAction);
            } else if (!stopped) {
                getViewState().updateTime(timeFormatter.print(new Duration(startTime, DateTime.now()).toPeriod()));
            }
        }
        getViewState().cleanState();
        invalidateActions();
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
            preferenceMaster.putValue(LAST_TIME_ID, dayAction.getId())
                    .putValue(START_TIME, dayAction.getStartDateTime())
                    .putValue(FINISH_TIME, dayAction.getFinishDateTime())
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
        dispouseOnDestroy(restService.startAction(dayAction.getId()).subscribe(
                response -> {
                    if (response != null && response.getActionStatuses().size() > 0 && response.getActionStatuses().first().getStatus() == ActionStatus.STARTED) {
                        if (dayAction != null && dayAction.isActive() && timer == null) {
                            startTime = DateTime.now();
                            stopped = false;
                            continueAction();
                            getViewState().onStarted();
                            databaseService.startDayAction(dayAction);
                            postUpdateActionEvent(response);
                        } else {
                            getViewState().onActionFailure();
                        }
                    }
                },
                error -> {
                    getViewState().onActionFailure();
                }
        ));
    }

    public void stopAction() {
        dispouseOnDestroy(restService.stopAction(dayAction.getId()).subscribe(
                response -> {
                    if (response != null && response.getActionStatuses().size() > 0 && response.getActionStatuses().first().getStatus() == ActionStatus.STOPPED) {
                        if (timer != null) {
                            getViewState().updateTime(timeFormatter.print(new Duration(startTime, DateTime.now()).toPeriod()));
                        }
                        onReset();
                        databaseService.stopDayAction(dayAction);
                        postUpdateActionEvent(response);
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
                    if (response != null && response.getActionStatuses().size() > 0 && response.getActionStatuses().first().getStatus() == ActionStatus.POSTPONED) {
                        databaseService.postponeDayAction(dayAction);
                        getViewState().onPostpone();
                        postUpdateActionEvent(response);
                        DayActionJob.startSchedule(dayAction, TimeUnit.MINUTES.toMillis(POSTMPONE_MINUTES));
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
        dispouseOnDestroy(restService.finishAction(dayAction.getId()).subscribe(
                response -> {
                    if (response != null && response.getActionStatuses().size() > 0 && response.getActionStatuses().first().getStatus() == ActionStatus.FINISHED) {
                        if (startTime.isBefore(endTime))
                            getViewState().updateTime(timeFormatter.print(new Duration(startTime, endTime).toPeriod()));
                        databaseService.finishDayAction(dayAction);
                        postUpdateActionEvent(response);
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
        getViewState().cleanState();
        restService.getAction(dayAction.getId())
                .subscribe(freshAction -> {
                            dayAction = freshAction;
                            dayAction.invalidateTime();
                            if (dayAction.getActionStatuses().size() > 0) {
                                LogStatus started = Stream.of(dayAction.getActionStatuses()).filter(stat -> stat.getStatus().equals(ActionStatus.STARTED)).findFirst().orElse(null);
                                if (started != null) {
                                    startTime = started.getDateTime();
                                }
                            } else {
                                startTime = dayAction.getStart();
                            }
                            endTime = dayAction.getEnd();
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
                                    (POSTMPONE_MINUTES >= new Duration(startTime, endTime).getStandardMinutes())) {
                                getViewState().switchStateButton(R.id.day_action_postpone, false);
                            }
                            if (dayAction.isStarted() && !dayAction.isFinished() && !dayAction.getStopped()) {
                                getViewState().switchStateButton(R.id.day_action_start, false);
                                continueAction();
                            }
                        },
                        error -> {
                            getViewState().onActionFailure();
                            if (!stopped) {
                                continueAction();
                            }
                        });

    }

    private void inactiveAction() {
        getViewState().switchStateButton(R.id.day_action_start, false);
        getViewState().switchStateButton(R.id.day_action_postpone, false);
        getViewState().switchStateButton(R.id.day_action_cancel, false);
    }
}
