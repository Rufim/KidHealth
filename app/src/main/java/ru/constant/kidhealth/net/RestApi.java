package ru.constant.kidhealth.net;


import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.constant.kidhealth.domain.models.Credentials;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.domain.models.User;
import ru.constant.kidhealth.domain.models.WeekDay;


/**
 * Date: 18.01.2016
 * Time: 12:07
 *
 * @author Yuri Shmakov
 */
public interface RestApi {

    enum ActionCommand {
        START,STOP,POSTPONE,FINISH
    }

	@POST("/auth/signIn")
    Observable<Token> signIn(@Body Credentials credentials);

    @GET("/auth/refreshToken")
    Observable<Token> refreshToken(@Header("Authorization") String refreshToken);

    @GET("api/schedule/today")
    Observable<List<DayAction>> today();

    @GET("api/schedule/week")
    Observable<Map<WeekDay, List<DayAction>>> week();

    @GET("api/schedule/{weekDay}")
    Observable<List<DayAction>> weekDay(@Path("weekDay") String weekDay);

    @FormUrlEncoded
    @POST("api/schedule/action/{actionId}")
    Observable<DayAction> actionCommand(@Path("actionId") String id, @Field("command") ActionCommand command, @Field("dateTime") DateTime dateTime);

    @GET("api/schedule/action/{actionId}")
    Observable<DayAction> getAction(@Path("actionId") String id);

    @GET("api/schedule/user/self")
    Observable<User> getUserInfo();
}
