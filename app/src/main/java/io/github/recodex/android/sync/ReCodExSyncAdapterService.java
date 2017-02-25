package io.github.recodex.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReCodExSyncAdapterService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ReCodExSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new ReCodExSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
