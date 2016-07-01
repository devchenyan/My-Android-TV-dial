package cn.caratel.voip.data.source.remote.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class ServiceProvider {

    private static ServiceProvider instance = null;

    public static ServiceProvider getInstance(String baseUrl) {
        if (instance == null) {
            instance = new ServiceProvider(baseUrl);
        }
        return instance;
    }

    private ServiceProvider(String baseUrl) {
        retrofit = buildRetrofit(baseUrl);
    }

    private Gson gson = buildGson();
    private OkHttpClient okHttpClient = buildOkHttpClient();
    private Retrofit retrofit;

    private Gson buildGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    private OkHttpClient buildOkHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    private Retrofit buildRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
    }

    public ContactService provideContactService() {
        return retrofit.create(ContactService.class);
    }

}
