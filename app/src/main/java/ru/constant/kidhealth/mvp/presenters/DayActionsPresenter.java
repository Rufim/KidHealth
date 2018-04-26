package ru.constant.kidhealth.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.net.RestService;
import ru.kazantsev.template.lister.ObservableDataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

@InjectViewState
public class DayActionsPresenter extends DataSourcePresenter<DataSourceView<DayAction>, DayAction> {

    @Inject public RestService restService;

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
                    return restService.getWeekDay("", "1", weekDay.name()).flatMapIterable(dayActions -> {
                        if (dayActions.getDayActions() != null) {
                            return dayActions.getDayActions();
                        } else {
                            return new ArrayList<>(0);
                        }
                    });
                }
            }
        });
    }


}
