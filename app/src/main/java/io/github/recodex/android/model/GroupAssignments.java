package io.github.recodex.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by teyras on 17.2.17.
 */

public class GroupAssignments {
    private List<String> all;

    @SerializedName("public")
    private List<String> publicItems;

    public List<String> getAll() {
        return all;
    }

    public List<String> getPublic() {
        return publicItems;
    }
}
