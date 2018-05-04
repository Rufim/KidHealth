package ru.constant.kidhealth.mvp.presenters;


import com.arellomobile.mvp.InjectViewState;

import java.util.ArrayList;
import java.util.Arrays;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.WeekDay;
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
