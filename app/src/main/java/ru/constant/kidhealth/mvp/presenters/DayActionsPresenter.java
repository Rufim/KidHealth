package ru.constant.kidhealth.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;

import net.vrallev.android.cat.Cat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.net.RestService;
import ru.constant.kidhealth.service.DatabaseService;
import ru.kazantsev.template.lister.ObservableDataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

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
                    return restService.getWeekDay(weekDay.name());
                }
            }
        });
    }

    @Override
    protected void onException(Throwable ex) {
        Cat.e(ex);
        super.onException(ex);
        List<DayAction> actions = databaseService.getDayActions(weekDay);
        getViewState().addItems(actions, actions.size());
        getViewState().finishLoad(actions, null ,null);
    }
}
