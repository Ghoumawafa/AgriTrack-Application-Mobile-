package com.example.agritrack.Utils;

import android.content.Context;
import android.util.Log;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.Database.AnimalFeedingScheduleEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    /**
     * Programme les notifications pour aujourd'hui et demain
     */
    public static void scheduleUpcomingNotifications(Context context) {
        new Thread(() -> {
            try {
                AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(context);
                AnimalFeedingScheduleDao scheduleDao = database.animalFeedingScheduleDao();
                AnimalDao animalDao = database.animalDao();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                // Aujourd'hui
                String today = dateFormat.format(new Date());
                scheduleNotificationsForDate(context, scheduleDao, animalDao, today);

                // Demain
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                String tomorrowStr = dateFormat.format(tomorrow.getTime());
                scheduleNotificationsForDate(context, scheduleDao, animalDao, tomorrowStr);

                Log.d(TAG, "✅ Notifications programmées avec succès");

            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la programmation des notifications", e);
            }
        }).start();
    }

    /**
     * Programme les notifications pour une date spécifique
     */
    private static void scheduleNotificationsForDate(Context context,
                                                     AnimalFeedingScheduleDao scheduleDao,
                                                     AnimalDao animalDao,
                                                     String date) {
        try {
            List<AnimalFeedingScheduleEntity> schedules = scheduleDao.getSchedulesForDate(date);

            for (AnimalFeedingScheduleEntity schedule : schedules) {
                // Ne programmer que si le repas n'est pas encore donné ou sauté
                if (!schedule.isFed() && !schedule.isSkipped()) {
                    AnimalEntity animal = animalDao.getById(schedule.getAnimalId());

                    if (animal != null) {
                        NotificationHelper.scheduleFeedingNotification(
                                context,
                                schedule.getId(),
                                animal.getName(),
                                schedule.getScheduledTime(),
                                schedule.getFoodType()
                        );

                        Log.d(TAG, "Notification programmée pour " + animal.getName() +
                                " à " + schedule.getScheduledTime());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la programmation pour " + date, e);
        }
    }

    /**
     * Annule toutes les notifications programmées
     */
    public static void cancelAllNotifications(Context context) {
        new Thread(() -> {
            try {
                AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(context);
                AnimalFeedingScheduleDao scheduleDao = database.animalFeedingScheduleDao();

                List<AnimalFeedingScheduleEntity> schedules = scheduleDao.getAll();

                for (AnimalFeedingScheduleEntity schedule : schedules) {
                    NotificationHelper.cancelNotification(context, schedule.getId());
                }

                Log.d(TAG, "✅ Toutes les notifications ont été annulées");

            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de l'annulation des notifications", e);
            }
        }).start();
    }

    /**
     * Reprogramme les notifications après un redémarrage du téléphone
     */
    public static void rescheduleAfterBoot(Context context) {
        Log.d(TAG, "Reprogrammation des notifications après redémarrage...");
        scheduleUpcomingNotifications(context);
    }
}