package com.example.agritrack.Receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.agritrack.Activities.Animaux.TodayFeedingsActivity;
import com.example.agritrack.R;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "feeding_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String animalName = intent.getStringExtra("animal_name");
        String feedingTime = intent.getStringExtra("feeding_time");
        String foodType = intent.getStringExtra("food_type");

        showNotification(context, animalName, feedingTime, foodType);
    }

    private void showNotification(Context context, String animalName, String feedingTime, String foodType) {
        // 1. Créer le canal (Android 8.0+)
        createNotificationChannel(context);

        // 2. Intent pour ouvrir l'activité
        Intent appIntent = new Intent(context, TodayFeedingsActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 3. Construire la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("🍽️ Heure du repas !")
                .setContentText(animalName + " à " + feedingTime)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Nourrir " + animalName + " à " + feedingTime +
                                (foodType != null && !foodType.isEmpty() ?
                                        "\nNourriture : " + foodType : "")))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{100, 200, 100, 200});

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_MANAGER);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Rappels de repas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications pour les repas des animaux");
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(Context.NOTIFICATION_MANAGER.class);
            manager.createNotificationChannel(channel);
        }
    }
}