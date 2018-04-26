package ru.constant.kidhealth.net;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.constant.kidhealth.domain.models.Actions;
import ru.constant.kidhealth.domain.models.User;


/**
 * Date: 18.01.2016
 * Time: 12:07
 *
 * @author Yuri Shmakov
 */
public interface RestApi {

    @FormUrlEncoded
	@POST("/login")
    Observable<ResponseBody> signIn(@Field("username") String login, @Field("password") String password);


    @GET("schedule/{userId}/today")
    Observable<Actions> today(@Header("Authorization") String token, @Path("userId") String userId);


    @GET("schedule/{userId}/week")
    Observable<Actions> week(@Header("Authorization") String token, @Path("userId") String userId);


    @GET("schedule/{userId}/{weekDay}")
    Observable<Actions> today(@Header("Authorization") String token, @Path("userId") String userId, @Path("weekDay") String weekDay);

}
