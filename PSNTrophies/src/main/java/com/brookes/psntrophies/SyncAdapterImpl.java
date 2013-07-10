package com.brookes.psntrophies;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
//import android.app.NotificationManager;

/**
 * Created by matt on 09/07/13.
 */

public class SyncAdapterImpl extends AbstractThreadedSyncAdapter{
    private Context mContext;

    public SyncAdapterImpl(Context context) {
        super(context, true);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        mContext = getContext();
        Intent intent = new Intent(mContext, Sync.class);
        mContext.startService(intent);

        Log.i("PSN Trophies", "Syncing: " + account.name);

    }

}


