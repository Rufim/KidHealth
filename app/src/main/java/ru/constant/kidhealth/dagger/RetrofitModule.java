package ru.constant.kidhealth.dagger;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.constant.kidhealth.net.AddTokenInterceptor;
import ru.constant.kidhealth.net.RefreshTokenAuthenticator;
import ru.constant.kidhealth.service.RestService;
import ru.kazantsev.template.net.interceptors.AddCookiesInterceptor;
import ru.kazantsev.template.net.interceptors.ReceivedCookiesInterceptor;

/**
 * Date: 8/26/2016
 * Time: 12:28
 *
 * @author Artur Artikov
 */
@Module
public class RetrofitModule {

	@Provides
	@Singleton
	public Retrofit provideRetrofit(Retrofit.Builder builder) {
		return builder.baseUrl(RestService.BASE_URL).build();
	}

	@Provides
	@Singleton
	public OkHttpClient.Builder provideOkHttpBuilder(Context context) {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

		return new OkHttpClient.Builder()
				.connectTimeout(3, TimeUnit.SECONDS)
				.readTimeout(20, TimeUnit.SECONDS)
				.addNetworkInterceptor(interceptor)
				.authenticator(new RefreshTokenAuthenticator(context))
				.addInterceptor(new AddTokenInterceptor(context))
				.addInterceptor(new AddCookiesInterceptor(context))
				.addInterceptor(new ReceivedCookiesInterceptor(context))
				.addInterceptor(chain -> {
					Request original = chain.request();
					Request request = original.newBuilder()
							.header("X-Requested-With", "XMLHttpRequest")
							.header("Content-Type", "application/json; charset=utf-8")
							.header("Accept", "application/json")
							.method(original.method(), original.body())
							.build();
					return chain.proceed(request);
				});
	}

	@Provides
	@Singleton
	public Retrofit.Builder provideRetrofitBuilder(Converter.Factory converterFactory, OkHttpClient.Builder builder) {
		return new Retrofit.Builder()
				.client(builder.build())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(converterFactory);
	}

	@Provides
	@Singleton
	public Converter.Factory provideConverterFactory(Gson gson) {
		return GsonConverterFactory.create(gson);
	}

	@Provides
	@Singleton
	Gson provideGson() {
		return new GsonBuilder()
				//.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				//.setFieldNamingStrategy(new CustomFieldNamingPolicy())
				.registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
				.setPrettyPrinting()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
				.serializeNulls()
				.create();
	}

	private static class CustomFieldNamingPolicy implements FieldNamingStrategy {
		@Override
		public String translateName(Field field) {
			String name = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field);
			name = name.substring(2, name.length()).toLowerCase();
			return name;
		}
	}

    public static final class DateTimeDeserializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime>
    {
        public static final org.joda.time.format.DateTimeFormatter DATE_TIME_FORMATTER =
                ISODateTimeFormat.dateOptionalTimeParser();

        @Override
        public DateTime deserialize(final JsonElement je, final Type type,
                                    final JsonDeserializationContext jdc) throws JsonParseException
        {
            return je.getAsString().length() == 0 ? null : DATE_TIME_FORMATTER.parseDateTime(je.getAsString());
        }

        @Override
        public JsonElement serialize(final DateTime src, final Type typeOfSrc,
                                     final JsonSerializationContext context)
        {
            return new JsonPrimitive(src == null ? "" :DATE_TIME_FORMATTER.print(src));
        }
    }
}
