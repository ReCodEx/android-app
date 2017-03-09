package io.github.recodex.android.api;


import java.util.List;

import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.Login;
import io.github.recodex.android.model.Submission;
import io.github.recodex.android.model.User;
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

    @POST("login/{serviceId}")
    @FormUrlEncoded
    Call<Envelope<Login>> externalLogin(@Path("serviceId") String serviceId, @Field("username") String username, @Field("password") String password);

    @GET("users/{id}")
    Call<Envelope<User>> getUser(@Path("id") String userId);

    @GET("groups/{id}")
    Call<Envelope<Group>> getGroup(@Path("id") String id);

    @GET("users/{id}/groups")
    Call<Envelope<UserGroups>> getGroupsForUser(@Path("id") String userId);

    @GET("exercise-assignments/{id}")
    Call<Envelope<Assignment>> getAssignment(@Path("id") String id);

    @GET("exercise-assignments/{id}/users/{userId}/submissions")
    Call<Envelope<List<Submission>>> getAssignmentSubmissions(@Path("id") String id, @Path("userId") String userId);

    @GET("submissions/{id}")
    Call<Envelope<Submission>> getSubmission(@Path("id") String submissionId);
}
