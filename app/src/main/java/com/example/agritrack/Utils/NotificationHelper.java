package com.example.agritrack.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.agritrack.Receivers.NotificationReceiver;

import java.util.Calendar;

public class NotificationHelper {

    /**
     * Programme une notification 5 minutes avant le repas
     */
    public static void scheduleFeedingNotification(Context context,
                                                   long scheduleId,
                                                   String animalName,
                                                   String feedingTime,
                                                   String foodType) {

        try {
            // 1. Convertir l'heure (ex: "07:00" -> hour=7, minute=0)
            String[] timeParts = feedingTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // 2. Calculer l'heure (5 minutes avant)
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MINUTE, -5); // 5 minutes avant

            // 3. Si l'heure est passée, programmer pour demain
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // 4. Créer l'Intent
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("animal_name", animalName);
            intent.putExtra("feeding_time", feedingTime);
            intent.putExtra("food_type", foodType);
            intent.putExtra("schedule_id", scheduleId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) scheduleId, // ID unique
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 5. Programmer avec AlarmManager - CORRECTION ICI
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Annule une notification programmée
     */
    public static void cancelNotification(Context context, long scheduleId) {
        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) scheduleId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            pendingIntent.cancel();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test rapide : programme une notification pour dans 1 minute
     */
    public static void testNotification(Context context) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 1); // Dans 1 minute

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("animal_name", "Bella (Test)");
            intent.putExtra("feeding_time", "Maintenant");
            intent.putExtra("food_type", "Foin");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    999, // ID de test
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}