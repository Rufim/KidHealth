package ru.constant.kidhealth.net;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.HashingSource;
import ru.constant.kidhealth.domain.models.Actions;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.DayActions;
import ru.constant.kidhealth.domain.models.User;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.kazantsev.template.util.TextUtils;


public class RestService {

    //public static String BASE_URL = "http://kidhealth.constant.obninsk.ru";
    public static String BASE_URL = "http://192.168.101.156:8080";
    private RestApi restApi;

    public RestService(RestApi restApi) {
        this.restApi = restApi;
    }

    public Observable<ResponseBody> signIn(String login, String password) {
        if("123".equals(login) && "123".equals(password)) {
            return Observable.just(new ResponseBody() {

                @Override
                public MediaType contentType() {
                    return MediaType.parse("application/json");
                }

                @Override
                public long contentLength() {
                    return 0;
                }

                @Override
                public BufferedSource source() {
                    return null;
                }
            });
        }
        return Observable.error(new Exception("123"));
       // return restApi.signIn(login, password);
    }

    public Observable<DayActions> getToday(String token, String id) {
        //return Observable.just(new ArrayList<>());
        return transformActions(restApi.today(token, id));
    }

    public Observable<DayActions> getWeekDay(String token, String id, String weekDay) {
         return testActions(weekDay);
      // return transformActions(restApi.today(token, id, weekDay));
    }

    public Observable<DayActions> getWeek(String token, String id) {
       // return Observable.just(new ArrayList<>());
        return transformActions(restApi.week(token, id));
    }

    public Observable<DayActions> transformActions(Observable<Actions> observable) {
        return observable.map(action ->{
            Map<WeekDay, List<DayAction>> actionMap = action.getActionMap();
            List<DayAction> actions = action.getActions();
            SortedMap<WeekDay, List<DayAction>> sortedMap = new TreeMap<>();
            List<DayActions> actionsList = new ArrayList<>();
            if(actionMap != null) {
                sortedMap.putAll(actionMap);
                Iterator<Map.Entry<WeekDay, List<DayAction>>> it = sortedMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<WeekDay, List<DayAction>> next = it.next();
                    DayActions dayActions = new DayActions();
                    dayActions.setWeekDay(next.getKey());
                    dayActions.setDayActions(next.getValue());
                    actionsList.add(dayActions);
                }
            } else if(actions != null) {
                DayActions dayActions = new DayActions();
                actionsList.add(dayActions);
                for (DayAction dayAction : actions) {
                    if(dayActions.getWeekDay() == null) {
                        dayActions.setWeekDay(dayAction.getDayOfWeek());
                    } else if(dayAction.getDayOfWeek() != dayActions.getWeekDay()) {
                        dayActions = Stream.of(actionsList).filter(da -> da.getWeekDay() == dayAction.getDayOfWeek()).findFirst().orElse(null);
                        if(dayActions == null) {
                            dayActions = new DayActions();
                            dayActions.setWeekDay(dayAction.getDayOfWeek());
                            actionsList.add(dayActions);
                        }
                    }
                    if (dayActions.getDayActions() == null) {
                        dayActions.setDayActions(new ArrayList<>());
                    }
                    dayActions.getDayActions().add(dayAction);
                }
            }
            return actionsList;
        }).flatMap(Observable::fromIterable);
    }

    public Observable<DayActions> testActions(String weekday) {
        DayAction I = new DayAction();
        I.setStartTime("08:00:00");
        I.setFinishTime("09:00:00");
        I.setComment("Кросс по стадиону");
        I.setTitle("Разминка");
        I.setActive(true);
        I.setDayOfWeek(WeekDay.valueOf(weekday));
        I.setId(I.hashCode() + "");
        DayAction II = new DayAction();
        II.setStartTime("9:00:00");
        II.setFinishTime("11:40:00");
        II.setComment("Отжимания");
        II.setActive(true);
        II.setDayOfWeek(WeekDay.valueOf(weekday));
        II.setTitle("3 подхода по 40-50 раз");
        II.setId(I.hashCode() + "");
        DayAction III = new DayAction();
        III.setStartTime("11:40:00");
        III.setFinishTime("13:00:00");
        III.setComment("4 подхода по 60");
        III.setTitle("Присед");
        III.setActive(true);
        III.setDayOfWeek(WeekDay.valueOf(weekday));
        III.setId(I.hashCode() + "");
        DayAction IV = new DayAction();
        IV.setStartTime("13:10:00");
        IV.setFinishTime("14:00:00");
        IV.setComment("4 подхода по 40");
        IV.setTitle("Прыжки");
        IV.setActive(true);
        IV.setDayOfWeek(WeekDay.valueOf(weekday));
        IV.setId(I.hashCode() + "");
        DayAction V = new DayAction();
        V.setStartTime("14:00:00");
        V.setFinishTime("17:00:00");
        V.setComment("3 подхода по 30");
        V.setTitle("Катка в дотан");
        V.setActive(true);
        V.setDayOfWeek(WeekDay.valueOf(weekday));
        V.setId(I.hashCode() + "");
        DayAction VI = new DayAction();
        VI.setStartTime("17:40:00");
        VI.setFinishTime("18:40:00");
        VI.setComment("2 подхода по 20");
        VI.setTitle("Двойной прыжок об воздух");
        VI.setActive(true);
        VI.setDayOfWeek(WeekDay.valueOf(weekday));
        VI.setId(I.hashCode() + "");
        DayAction VII = new DayAction();
        VII.setStartTime("18:40:00");
        VII.setFinishTime("19:30:00");
        VII.setComment("2 подхода по 10");
        VII.setTitle("Дотронутся пяткой до лба");
        VII.setActive(true);
        VII.setDayOfWeek(WeekDay.valueOf(weekday));
        VII.setId(I.hashCode() + "");
        DayAction VIII = new DayAction();
        VIII.setStartTime("19:40:00");
        VIII.setFinishTime("21:00:00");
        VIII.setComment("4 подхода по 5");
        VIII.setTitle("Пробить головой стену");
        VIII.setActive(true);
        VIII.setDayOfWeek(WeekDay.valueOf(weekday));
        VIII.setId(I.hashCode() + "");
        ArrayList<DayActions> actions = new ArrayList<>();
        DayActions dayActions = new DayActions();
        dayActions.setDayActions(Arrays.asList(I,II,III, IV, V, VI, VII, VIII));
        actions.add(dayActions);
        return Observable.just(dayActions);
    }
}
