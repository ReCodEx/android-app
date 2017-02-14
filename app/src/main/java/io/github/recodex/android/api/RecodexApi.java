package io.github.recodex.android.api;


import io.github.recodex.android.model.Login;
import io.github.recodex.android.model.Response;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RecodexApi {

    @POST("login")
    @FormUrlEncoded
    Call<Response<Login>> login(@Field("username") String username, @Field("password") String password);
}
