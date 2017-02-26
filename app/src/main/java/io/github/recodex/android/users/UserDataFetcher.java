package io.github.recodex.android.users;

import java.io.IOException;

import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.UserGroups;
import retrofit2.Response;

public class UserDataFetcher {

    private RecodexApi recodexApi;

    public UserDataFetcher(RecodexApi recodexApi) {
        this.recodexApi = recodexApi;
    }

    public UserGroups fetchAndStoreGroups(UserWrapper user) {
        Response<Envelope<UserGroups>> response = null;
        try {
            response = recodexApi.getGroupsForUser(user.getId()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.isSuccessful()) {
            UserGroups userGroups = response.body().getPayload();
            user.setGroupsInfo(userGroups.getStudent(), userGroups.getStats());
            return userGroups;
        }

        return null;
    }

    public void fetchAndStoreAll(UserWrapper user) {
        fetchAndStoreGroups(user);
    }
}
