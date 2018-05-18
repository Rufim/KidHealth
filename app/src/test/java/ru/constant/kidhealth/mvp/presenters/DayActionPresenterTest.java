package ru.constant.kidhealth.mvp.presenters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import ru.constant.kidhealth.configs.RxImmediateSchedulerRule;
import ru.constant.kidhealth.configs.TestComponentRule;
import ru.constant.kidhealth.configs.TestRunner;
import ru.constant.kidhealth.dagger.MockRetrofitModule;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.DayActions;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.kazantsev.template.mvp.view.DataSourceView$$State;
import ru.kazantsev.template.util.SystemUtils;

import static org.mockito.Mockito.*;


@RunWith(TestRunner.class)
public class DayActionPresenterTest {

    @Rule
    public RxImmediateSchedulerRule schedulers = new RxImmediateSchedulerRule();
    @Mock
    DataSourceView$$State<DayAction> dataSourceViewState;
    @Rule
    public TestComponentRule testComponentRule = new TestComponentRule();


    private MockWebServer server;
    DayActionsPresenter presenter;

    String fileName = "response.json";

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        MockitoAnnotations.initMocks(this);
        presenter = new DayActionsPresenter();
        presenter.setViewState(dataSourceViewState);
        server = MockRetrofitModule.getMockWebServer();
    }

    @Test
    public void testLoadDayActionsFragment() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(readResource(fileName)));
        presenter.setWeekDay(WeekDay.THURSDAY);
        presenter.loadItems(true, 0, 50, null, null);
        verify(dataSourceViewState).startLoading(true);
        verify(dataSourceViewState).addItems(actionsJson().get(0).getDayActions(), 50);
        verify(dataSourceViewState).finishLoad(null, null);
    }

    private String readResource(String fileName) {
        return SystemUtils.readStream(getClass().getClassLoader().getResourceAsStream(fileName), "UTF-8");
    }

    private List<DayActions> actionsJson() {
        DayAction f = new DayAction();
        f.setId("10");
        f.setStartDateTime("10:00:00");
        f.setFinishDateTime("11:00:00");
        f.setComment("Comment 5");
        f.setDayOfWeek(WeekDay.THURSDAY);
        f.setTitle("Title 5");
        f.setType("SCHOOL");
        f.setActive(true);
        f.setDescription("Description 5");
        DayAction s = new DayAction();
        s.setId("11");
        s.setStartDateTime("11:00:00");
        s.setFinishDateTime("12:30:00");
        s.setComment("Comment 6");
        s.setDayOfWeek(WeekDay.THURSDAY);
        s.setTitle("Title 6");
        s.setType("TRAINING");
        s.setActive(true);
        s.setDescription("Description 6");
        ArrayList<DayActions> actions = new ArrayList<>();
        DayActions dayActions = new DayActions();
        dayActions.setDayActions(Arrays.asList(f,s));
        actions.add(dayActions);
        return actions;
    }

    private Observable<List<DayActions>> actionsCustom() {
        DayAction f = new DayAction();
        f.setStartDateTime("08:00");
        f.setFinishDateTime("09:00");
        f.setComment("Кросс по стадиону");
        f.setTitle("Разминка");
        DayAction s = new DayAction();
        s.setStartDateTime("13:45");
        s.setFinishDateTime("14:00");
        s.setComment("Отжимания");
        s.setTitle("3 подхода по 40-50 раз");
        DayAction t = new DayAction();
        t.setStartDateTime("17:40");
        t.setFinishDateTime("18:00");
        t.setComment("4 подхода по 60");
        t.setTitle("Присед");
        ArrayList<DayActions> actions = new ArrayList<>();
        DayActions dayActions = new DayActions();
        dayActions.setDayActions(Arrays.asList(f,s,t));
        actions.add(dayActions);
        return Observable.just(actions);
    }

}
