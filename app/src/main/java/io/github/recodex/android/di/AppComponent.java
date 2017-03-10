package io.github.recodex.android.di;

import javax.inject.Singleton;

import dagger.Component;
import io.github.recodex.android.AssignmentSolutionsFragment;
import io.github.recodex.android.AssignmentTextFragment;
import io.github.recodex.android.CasLoginFragment;
import io.github.recodex.android.GroupDetailFragment;
import io.github.recodex.android.GroupListFragment;
import io.github.recodex.android.LoginActivity;
import io.github.recodex.android.NavigationDrawer;
import io.github.recodex.android.RegularLoginFragment;
import io.github.recodex.android.SettingsActivity;
import io.github.recodex.android.SubmissionFragment;
import io.github.recodex.android.TestResultsFragment;
import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.sync.ReCodExSyncAdapter;

/**
 * Created by martin on 2/17/17.
 */

@Singleton
@Component(modules={AppModule.class})
public interface AppComponent {
    void inject(LoginActivity activity);
    void inject(GroupListFragment fragment);
    void inject(GroupDetailFragment fragment);
    void inject(ReCodExAuthenticator activity);
    void inject(NavigationDrawer activity);
    void inject(RegularLoginFragment fragment);
    void inject(CasLoginFragment fragment);
    void inject(ReCodExSyncAdapter adapter);
    void inject(SettingsActivity.GeneralPreferenceFragment fragment);
    void inject(AssignmentTextFragment assignmentTextFragment);
    void inject(AssignmentSolutionsFragment assignmentSolutionsFragment);
    void inject(SubmissionFragment submissionFragment);
    void inject(TestResultsFragment testResultsFragment);
}
