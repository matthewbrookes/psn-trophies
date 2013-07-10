package com.brookes.psntrophies;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by matt on 09/07/13.
 */
public class SyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapterImpl sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapterImpl(getApplicationContext());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
