package io.github.recodex.android.di;

import javax.inject.Singleton;

import dagger.Component;
import io.github.recodex.android.GroupListFragment;
import io.github.recodex.android.LoginActivity;
import io.github.recodex.android.NavigationDrawer;
import io.github.recodex.android.authentication.ReCodExAuthenticator;

/**
 * Created by martin on 2/17/17.
 */

@Singleton
@Component(modules={AppModule.class})
public interface AppComponent {
    void inject(LoginActivity activity);
    void inject(GroupListFragment fragment);
    void inject(ReCodExAuthenticator activity);
    void inject(NavigationDrawer activity);
}
