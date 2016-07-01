package cn.caratel.voip.data.source.remote.service;

import java.util.List;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.model.JsonResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by wurenhai on 2016/6/28.
 */
public interface ContactService {

    @GET("/contact/all")
    Call<List<Contact>> all();

    @GET("/contact/find/{id}")
    Call<Contact> find(@Path("id") String id);

    @POST("/contact/save")
    Call<JsonResponse> save(@Body Contact contact);

    @POST("/contact/insert")
    Call<JsonResponse> insert(@Body Contact contact);

    @POST("/contact/update")
    Call<JsonResponse> update(@Body Contact contact);

    @GET("/contact/delete/{id}")
    Call<JsonResponse> delete(@Path("id") String id);

    @GET("/contact/deleteAll")
    Call<JsonResponse> deleteAll();

}
