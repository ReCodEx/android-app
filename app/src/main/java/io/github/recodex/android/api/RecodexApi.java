package io.github.recodex.android.api;


import java.util.List;

import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.Login;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.UserGroups;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RecodexApi {

    @POST("login")
    @FormUrlEncoded
    Call<Envelope<Login>> login(@Field("username") String username, @Field("password") String password);

    @GET("groups/{id}")
    Call<Envelope<Group>> getGroup(@Path("id") String id);

    @GET("users/{id}/groups")
    Call<Envelope<UserGroups>> getGroupsForUser(@Path("id") String userId);

    @GET("exercise-assignments/{id}")
    Call<Envelope<Group>> getAssignment(@Path("id") String id);
}
