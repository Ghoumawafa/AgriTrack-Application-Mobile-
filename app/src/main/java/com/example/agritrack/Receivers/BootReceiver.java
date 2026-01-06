package com.example.agritrack.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.agritrack.Utils.NotificationScheduler;

/**
 * Reprogramme automatiquement les notifications après le redémarrage du téléphone
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Téléphone redémarré - Reprogrammation des notifications");
            NotificationScheduler.rescheduleAfterBoot(context);
        }
    }
}