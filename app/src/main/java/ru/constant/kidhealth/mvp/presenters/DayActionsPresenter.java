package ru.constant.kidhealth.mvp.presenters;

import android.app.job.JobScheduler;

import com.arellomobile.mvp.InjectViewState;

import net.vrallev.android.cat.Cat;

import org.joda.time.DateTime;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.job.DayActionJob;
import ru.constant.kidhealth.service.RestService;
import ru.constant.kidhealth.service.DatabaseService;
import ru.kazantsev.template.lister.ObservableDataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;
import ru.kazantsev.template.net.HTTPExecutor;

@InjectViewState
public class DayActionsPresenter extends DataSourcePresenter<DataSourceView<DayAction>, DayAction> {

    @Inject public RestService restService;
    @Inject DatabaseService databaseService;

    WeekDay weekDay;

    public void setWeekDay(WeekDay weekDay) {
        this.weekDay = weekDay;
    }

    public DayActionsPresenter() {
        App.getAppComponent().inject(this);
        setDataSource(new ObservableDataSource<DayAction>() {
            @Override
            public Observable<DayAction> getObservableItems(int skip, int size) throws Exception {
                if(skip > 0)  {
                    return Observable.just(new ArrayList<DayAction>()).flatMapIterable(e -> e);
                } else {
                    return RestService.transformActions(restService.getWeekDay(weekDay.name()).map(result ->{
                        try {
                            databaseService.insertOrUpdateScheduleForDay(weekDay, result);
                            DateTime now = DateTime.now();
                            if(weekDay.isToday(now)) {
                                DayActionJob.startSchedule(databaseService.nextDayAction(now));
                            }
                        } catch (Throwable ignore) {
                            Cat.e(ignore);
                        }
                        return result;
                    }));
                }
            }
        });
    }

}
