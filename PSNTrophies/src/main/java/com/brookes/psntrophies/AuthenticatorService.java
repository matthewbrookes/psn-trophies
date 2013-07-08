package com.brookes.psntrophies;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by matt on 08/07/13.
 */
public class AuthenticatorService extends Service {
    public IBinder onBind(Intent intent) {
        Authenticator authenticator = new Authenticator(this);
        return authenticator.getIBinder();
    }
}
