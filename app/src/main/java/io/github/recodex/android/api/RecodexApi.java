package io.github.recodex.android.api;


import io.github.recodex.android.model.Login;
import io.github.recodex.android.model.Envelope;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RecodexApi {

    @POST("login")
    @FormUrlEncoded
    Call<Envelope<Login>> login(@Field("username") String username, @Field("password") String password);
}
