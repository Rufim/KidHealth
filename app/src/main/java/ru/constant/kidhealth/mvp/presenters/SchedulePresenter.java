package ru.constant.kidhealth.mvp.presenters;



import com.arellomobile.mvp.InjectViewState;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.Actions;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.DayActions;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.net.RestService;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.lister.DataSource;
import ru.kazantsev.template.lister.ObservableDataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

@InjectViewState
public class SchedulePresenter extends DataSourcePresenter<DataSourceView<WeekDay>, WeekDay> {

    public SchedulePresenter() {
        App.getAppComponent().inject(this);
        setDataSource((day, size) -> {
            if(day > 0) return new ArrayList<>();
            return Arrays.asList(WeekDay.values());
        });
    }

}
