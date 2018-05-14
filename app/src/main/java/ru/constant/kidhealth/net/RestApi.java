package ru.constant.kidhealth.net;


import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ru.constant.kidhealth.domain.models.Credentials;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.Token;
import ru.constant.kidhealth.domain.models.WeekDay;


/**
 * Date: 18.01.2016
 * Time: 12:07
 *
 * @author Yuri Shmakov
 */
public interface RestApi {

	@POST("/auth/signIn")
    Observable<Token> signIn(@Body Credentials credentials);

    @GET("/auth/token")
    Observable<Token> refreshToken();

    @GET("api/schedule/today")
    Observable<List<DayAction>> today();


    @GET("api/schedule/week")
    Observable<Map<WeekDay, List<DayAction>>> week();


    @GET("api/schedule/{weekDay}")
    Observable<List<DayAction>> today(@Path("weekDay") String weekDay);

}
