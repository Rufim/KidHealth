package ru.constant.kidhealth.mvp.presenters;


import com.arellomobile.mvp.InjectViewState;

import net.vrallev.android.cat.Cat;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.job.DayActionJob;
import ru.constant.kidhealth.service.DatabaseService;
import ru.constant.kidhealth.service.RestService;
import ru.kazantsev.template.lister.ObservableDataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

@InjectViewState
public class SchedulePresenter extends DataSourcePresenter<DataSourceView<List<DayAction>>, List<DayAction>> {


    @Inject
    DatabaseService databaseService;
    @Inject
    RestService restService;

    public SchedulePresenter() {
        App.getAppComponent().inject(this);
        setDataSource(new ObservableDataSource<List<DayAction>>() {
            @Override
            public Maybe<List<List<DayAction>>> getObservableItems(int day, int size) {
                if (day > 0) {
                    getViewState().stopLoading();
                    return Maybe.empty();
                }
                return restService.getWeek().map(map -> {
                    databaseService.insertOrUpdateScheduleForWeek(map);
                    DayActionJob.startSchedule(databaseService.nextDayAction(DateTime.now()));
                    List<List<DayAction>> weekActions = new ArrayList<>(7);
                    for (WeekDay weekDay : WeekDay.values()) {
                        List<DayAction> actions = map.get(weekDay);
                        if (actions == null) {
                            actions = new ArrayList<>();
                        }
                        weekActions.add(weekDay.ordinal(), actions);
                    }
                    return weekActions;
                }).firstElement();
            }
        });
    }

    @Override
    protected void onException(Throwable ex) {
        super.onException(ex);
        try {
            List<List<DayAction>> weekActions = new ArrayList<>(WeekDay.values().length);
            for (WeekDay weekDay : WeekDay.values()) {
                try {
                    weekActions.add(databaseService.getDayActions(weekDay));
                } catch (Throwable ignore) {
                    Cat.e(ignore);
                    weekActions.add(new ArrayList<>());
                }
            }
            getViewState().addFinalItems(weekActions);
        } catch (Throwable ignore) {
            Cat.e(ignore);
        }

    }
}
