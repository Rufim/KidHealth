package ru.constant.kidhealth.service;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.Request;
import ru.constant.kidhealth.domain.models.Credentials;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.net.RestApi;
import ru.kazantsev.template.util.RxUtils;


public class RestService {

    //public static String BASE_URL = "http://kidhealth.constant.obninsk.ru";
    public static String BASE_URL = "http://192.168.101.148:8080";
    private RestApi restApi;

    public static final String AUTHENTICATION_HEADER_NAME = "Authorization";
    public static String HEADER_PREFIX = "Bearer ";

    public RestService(RestApi restApi) {
        this.restApi = restApi;
    }

    public Observable<Token> signIn(String login, String password) {
        /*if("123".equals(signIn) && "123".equals(password)) {
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
        return Observable.error(new Exception("123"));*/
        return restApi.signIn(new Credentials(login, password));
    }

    public Observable<List<DayAction>> getToday() {
        //return Observable.just(new ArrayList<>());
        return restApi.today();
    }

    public Observable<List<DayAction>> getWeekDay(String weekDay) {
       //  return testActions(weekDay);
        return restApi.weekDay(weekDay);
    }

    public Observable<Map<WeekDay, List<DayAction>>> getWeek() {
       // return Observable.just(new ArrayList<>());
        return restApi.week();
    }

    public static Observable<DayAction> transformActions(Observable<List<DayAction>> observable) {
        return observable.flatMap(Observable::fromIterable);
    }

    public Observable<DayAction> testActions(String weekday) {
        DayAction I = new DayAction();
        I.setStartDateTime("08:00:00");
        I.setFinishDateTime("09:00:00");
        I.setComment("Кросс по стадиону");
        I.setTitle("Разминка");
        I.setActive(true);
        I.setDayOfWeek(WeekDay.valueOf(weekday));
        I.setId(I.hashCode() + "");
        DayAction II = new DayAction();
        II.setStartDateTime("9:00:00");
        II.setFinishDateTime("11:40:00");
        II.setComment("Отжимания");
        II.setActive(true);
        II.setDayOfWeek(WeekDay.valueOf(weekday));
        II.setTitle("3 подхода по 40-50 раз");
        II.setId(I.hashCode() + "");
        DayAction III = new DayAction();
        III.setStartDateTime("11:40:00");
        III.setFinishDateTime("13:00:00");
        III.setComment("4 подхода по 60");
        III.setTitle("Присед");
        III.setActive(true);
        III.setDayOfWeek(WeekDay.valueOf(weekday));
        III.setId(I.hashCode() + "");
        DayAction IV = new DayAction();
        IV.setStartDateTime("13:10:00");
        IV.setFinishDateTime("14:00:00");
        IV.setComment("4 подхода по 40");
        IV.setTitle("Прыжки");
        IV.setActive(true);
        IV.setDayOfWeek(WeekDay.valueOf(weekday));
        IV.setId(I.hashCode() + "");
        DayAction V = new DayAction();
        V.setStartDateTime("14:00:00");
        V.setFinishDateTime("17:00:00");
        V.setComment("3 подхода по 30");
        V.setTitle("Катка в дотан");
        V.setActive(true);
        V.setDayOfWeek(WeekDay.valueOf(weekday));
        V.setId(I.hashCode() + "");
        DayAction VI = new DayAction();
        VI.setStartDateTime("17:40:00");
        VI.setFinishDateTime("18:40:00");
        VI.setComment("2 подхода по 20");
        VI.setTitle("Двойной прыжок об воздух");
        VI.setActive(true);
        VI.setDayOfWeek(WeekDay.valueOf(weekday));
        VI.setId(I.hashCode() + "");
        DayAction VII = new DayAction();
        VII.setStartDateTime("18:40:00");
        VII.setFinishDateTime("19:30:00");
        VII.setComment("2 подхода по 10");
        VII.setTitle("Дотронутся пяткой до лба");
        VII.setActive(true);
        VII.setDayOfWeek(WeekDay.valueOf(weekday));
        VII.setId(I.hashCode() + "");
        DayAction VIII = new DayAction();
        VIII.setStartDateTime("19:40:00");
        VIII.setFinishDateTime("21:00:00");
        VIII.setComment("4 подхода по 5");
        VIII.setTitle("Пробить головой стену");
        VIII.setActive(true);
        VIII.setDayOfWeek(WeekDay.valueOf(weekday));
        VIII.setId(I.hashCode() + "");
        List<DayAction> actions = Arrays.asList(I,II,III, IV, V, VI, VII, VIII);
        return Observable.just(actions).flatMapIterable(a -> a);
    }

    public Observable<Token> refreshToken(String refreshToken) {
        return restApi.refreshToken(refreshToken);
    }

    public static boolean isAuthenticationRequest(Request request) {
        return request.url().encodedPath().contains("login") || request.url().encodedPath().contains("refreshToken");
    }

    public Observable<DayAction> getAction(String actionId) {
        return restApi.getAction(actionId).compose(RxUtils.applySchedulers());
    }

    public Observable<DayAction> startAction(String actionId) {
        return restApi.actionCommand(actionId, RestApi.ActionCommand.START, DateTime.now()).compose(RxUtils.applySchedulers());
    }

    public Observable<DayAction> postponeAction(String actionId) {
        return restApi.actionCommand(actionId, RestApi.ActionCommand.POSTPONE, DateTime.now()).compose(RxUtils.applySchedulers());
    }

    public Observable<DayAction> stopAction(String actionId) {
        return restApi.actionCommand(actionId, RestApi.ActionCommand.STOP, DateTime.now()).compose(RxUtils.applySchedulers());
    }

    public Observable<DayAction> finishAction(String actionId) {
        return restApi.actionCommand(actionId, RestApi.ActionCommand.FINISH, DateTime.now()).compose(RxUtils.applySchedulers());
    }

}
