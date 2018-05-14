package ru.constant.kidhealth.mvp.presenters;


import com.arellomobile.mvp.InjectViewState;

import net.vrallev.android.cat.Cat;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.job.DayActionJob;
import ru.constant.kidhealth.net.RestService;
import ru.constant.kidhealth.service.DatabaseService;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

@InjectViewState
public class SchedulePresenter extends DataSourcePresenter<DataSourceView<WeekDay>, WeekDay> {


    @Inject DatabaseService databaseService;
    @Inject RestService restService;

    public SchedulePresenter() {
        App.getAppComponent().inject(this);
        setDataSource((day, size) -> {
            if(day > 0) return new ArrayList<>();
            try {
                databaseService.insertOrUpdateScheduleForWeek(restService.getWeek().blockingFirst());
                DayActionJob.startSchedule(databaseService.nextDayAction(DateTime.now()));
            } catch (Exception ioErrorIgnore) {
                Cat.e(ioErrorIgnore);
            }
            return Arrays.asList(WeekDay.values());
        });
    }

}
