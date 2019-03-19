package ru.constant.kidhealth.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;

import net.vrallev.android.cat.Cat;

import org.joda.time.DateTime;

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
import ru.kazantsev.template.mvp.view.DataSourceViewNoPersist;

@InjectViewState
public class DayActionsPresenter extends DataSourcePresenter<DataSourceViewNoPersist<DayAction>, DayAction> {

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
            public Maybe<List<DayAction>> getObservableItems(int skip, int size) throws Exception {
                if(skip > 0 || weekDay == null)  {
                    return Maybe.empty();
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
