package com.example.agritrack.Receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.agritrack.Activities.Animaux.TodayFeedingsActivity;
import com.example.agritrack.R;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "feeding_channel";
    private static final String CHANNEL_NAME = "Rappels de repas";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d(TAG, "Notification reçue !");

            // Récupérer les données
            String animalName = intent.getStringExtra("animal_name");
            String feedingTime = intent.getStringExtra("feeding_time");
            String foodType = intent.getStringExtra("food_type");
            long scheduleId = intent.getLongExtra("schedule_id", -1);

            Log.d(TAG, "Animal: " + animalName + ", Heure: " + feedingTime);

            // Afficher la notification
            showNotification(context, animalName, feedingTime, foodType, scheduleId);

        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onReceive: " + e.getMessage(), e);
        }
    }

    private void showNotification(Context context, String animalName,
                                  String feedingTime, String foodType, long scheduleId) {
        try {
            // 1. Créer le canal de notification
            createNotificationChannel(context);

            // 2. Intent pour ouvrir l'activité quand on clique
            Intent appIntent = new Intent(context, TodayFeedingsActivity.class);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appIntent.putExtra("schedule_id", scheduleId);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    (int) scheduleId,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 3. Construire le texte de la notification
            String title = "🍽️ Heure du repas !";
            String content = animalName + " à " + feedingTime;
            String bigText = "Il est temps de nourrir " + animalName +
                    "\n⏰ Heure prévue : " + feedingTime;

            if (foodType != null && !foodType.isEmpty()) {
                bigText += "\n🌾 Type : " + foodType;
            }

            // 4. Créer la notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{0, 300, 200, 300})
                    .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);

            // 5. Afficher la notification - ✅ CORRECTION ICI
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.notify((int) scheduleId, builder.build());
                Log.d(TAG, "Notification affichée avec succès");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'affichage de la notification: " + e.getMessage(), e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifications pour rappeler les heures de repas des animaux");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});
            channel.enableLights(true);

            // ✅ CORRECTION ICI AUSSI
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notification créé");
            }
        }
    }
}