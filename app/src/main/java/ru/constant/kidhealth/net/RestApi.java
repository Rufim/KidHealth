package ru.constant.kidhealth.net;


import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ru.constant.kidhealth.domain.models.Credentials;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.Token;


/**
 * Date: 18.01.2016
 * Time: 12:07
 *
 * @author Yuri Shmakov
 */
public interface RestApi {

	@POST("/login")
    Observable<Token> signIn(@Body Credentials credentials);

    @GET("/token")
    Observable<Token> refreshToken();

    @GET("schedule/today")
    Observable<List<DayAction>> today();


    @GET("schedule/week")
    Observable<Map<DayOfWeek, List<DayAction>>> week();


    @GET("schedule/{weekDay}")
    Observable<List<DayAction>> today(@Path("weekDay") String weekDay);

}
