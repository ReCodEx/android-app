package io.github.recodex.android.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by martin on 2/15/17.
 */

public class ReCodExAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        ReCodExAuthenticator authenticator = new ReCodExAuthenticator(this);
        return authenticator.getIBinder();
    }
}
