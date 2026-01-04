package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agritrack.Adapters.NotificationAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.Database.AnimalFeedingScheduleEntity;
import com.example.agritrack.R;
import com.example.agritrack.Receivers.NotificationReceiver;
import com.example.agritrack.Utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView tvEmpty;
    private static final int PERMISSION_REQUEST_CODE = 123;

    private AnimalFeedingScheduleDao scheduleDao;
    private AnimalDao animalDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        setupBottomNavigation();
        initializeViews();

        //  Vérifier et demander les permissions
        checkAndRequestPermissions();

        loadNotifications();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        scheduleDao = database.animalFeedingScheduleDao();
        animalDao = database.animalDao();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>(), new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onCancelNotification(long scheduleId) {
                cancelSingleNotification(scheduleId);
            }

            @Override
            public void onRescheduleNotification(long scheduleId) {
                rescheduleNotification(scheduleId);
            }
        });
        recyclerView.setAdapter(adapter);

        // Boutons
        findViewById(R.id.btnTest).setOnClickListener(v -> onTestNotificationClicked());
        findViewById(R.id.btnCancelAll).setOnClickListener(v -> onCancelAllClicked());
    }

    // Ajoutez cette méthode dans la classe NotificationsActivity
    private String getFoodTypeFromSchedule(AnimalFeedingScheduleEntity schedule) {
        // Version simple qui retourne le type principal
        if (schedule.getHayQuantity() > 0 && schedule.getGrainsQuantity() > 0) {
            return "Foin et Céréales";
        } else if (schedule.getHayQuantity() > 0) {
            return "Foin";
        } else if (schedule.getGrainsQuantity() > 0) {
            return "Céréales";
        } else if (schedule.getSupplementsQuantity() > 0) {
            return "Suppléments";
        } else {
            return "Repas standard";
        }
    }

    // Ajoutez cette méthode dans NotificationsActivity
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Demander la permission
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this,
                        "⚠️ Veuillez autoriser les alarmes exactes dans les paramètres",
                        Toast.LENGTH_LONG).show();

                // Optionnel : Ouvrir les paramètres
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
    private void loadNotifications() {
        new Thread(() -> {
            try {
                List<NotificationItem> notificationItems = new ArrayList<>();
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // 1. Récupérer les repas d'aujourd'hui
                List<AnimalFeedingScheduleEntity> todaySchedules = scheduleDao.getSchedulesForDate(today);

                for (AnimalFeedingScheduleEntity schedule : todaySchedules) {
                    if (!schedule.isFed() && !schedule.isSkipped()) {
                        String animalName = animalDao.getById(schedule.getAnimalId()).getName();

                        // Vérifier si une notification est programmée
                        boolean isScheduled = isNotificationScheduled(schedule.getId());

                        notificationItems.add(new NotificationItem(
                                schedule.getId(),
                                animalName,
                                schedule.getScheduledTime(),
                                schedule.getFoodType(),
                                isScheduled,
                                "Aujourd'hui",
                                schedule.getFeedingDate()
                        ));
                    }
                }

                // 2. Récupérer les repas de demain
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                String tomorrowStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(tomorrow.getTime());

                List<AnimalFeedingScheduleEntity> tomorrowSchedules = scheduleDao.getSchedulesForDate(tomorrowStr);

                for (AnimalFeedingScheduleEntity schedule : tomorrowSchedules) {
                    if (!schedule.isFed() && !schedule.isSkipped()) {
                        String animalName = animalDao.getById(schedule.getAnimalId()).getName();
                        boolean isScheduled = isNotificationScheduled(schedule.getId());

                        notificationItems.add(new NotificationItem(
                                schedule.getId(),
                                animalName,
                                schedule.getScheduledTime(),
                                schedule.getFoodType(),
                                isScheduled,
                                "Demain",
                                schedule.getFeedingDate()
                        ));
                    }
                }

                runOnUiThread(() -> {
                    if (notificationItems.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setText("Aucune notification programmée.\nAjoutez des repas pour recevoir des rappels.");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setNotifications(notificationItems);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur de chargement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean isNotificationScheduled(long scheduleId) {
        try {
            Intent intent = new Intent(this, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) scheduleId,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            return pendingIntent != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void onTestNotificationClicked() {
        // Test : programme une notification pour dans 1 minute
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);

        String testTime = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        NotificationHelper.scheduleFeedingNotification(
                this,
                999, // ID test
                "Animal Test",
                testTime,
                "Nourriture Test"
        );

        Toast.makeText(this,
                "✅ Notification test programmée pour " + testTime,
                Toast.LENGTH_SHORT).show();

        // Recharger la liste
        loadNotifications();
    }

    private void onCancelAllClicked() {
        new Thread(() -> {
            List<AnimalFeedingScheduleEntity> schedules = scheduleDao.getAll();
            int canceledCount = 0;

            for (AnimalFeedingScheduleEntity schedule : schedules) {
                if (!schedule.isFed() && !schedule.isSkipped()) {
                    NotificationHelper.cancelNotification(this, schedule.getId());
                    canceledCount++;
                }
            }

            final int finalCount = canceledCount;
            runOnUiThread(() -> {
                Toast.makeText(this,
                        finalCount + " notifications ont été annulées",
                        Toast.LENGTH_SHORT).show();
                loadNotifications();
            });
        }).start();
    }

    private void cancelSingleNotification(long scheduleId) {
        NotificationHelper.cancelNotification(this, scheduleId);
        Toast.makeText(this, "Notification annulée", Toast.LENGTH_SHORT).show();
        loadNotifications();
    }
    private void rescheduleNotification(long scheduleId) {
        new Thread(() -> {
            AnimalFeedingScheduleEntity schedule = getScheduleById(scheduleId);
            if (schedule != null) {
                String animalName = animalDao.getById(schedule.getAnimalId()).getName();

                // Convertir l'heure
                String[] timeParts = schedule.getScheduledTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                // Programme 5 minutes avant
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.add(Calendar.MINUTE, -5);

                // Si l'heure est passée, programmer pour demain
                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                // Créer l'Intent
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("animal_name", animalName);
                intent.putExtra("feeding_time", schedule.getScheduledTime());
                intent.putExtra("food_type", schedule.getFoodType());
                intent.putExtra("schedule_id", schedule.getId());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        (int) schedule.getId(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Programmer - CORRECTION ICI
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE); // Changer Context.ALARM_MANAGER en ALARM_SERVICE
                if (alarmManager != null) {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "✅ Notification reprogrammée",
                            Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
            }
        }).start();
    }


    private AnimalFeedingScheduleEntity getScheduleById(long scheduleId) {
        // Cette méthode devrait exister dans votre DAO
        // Pour l'instant, on récupère tous et on filtre
        List<AnimalFeedingScheduleEntity> allSchedules = scheduleDao.getAll();
        for (AnimalFeedingScheduleEntity schedule : allSchedules) {
            if (schedule.getId() == scheduleId) {
                return schedule;
            }
        }
        return null;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Notifications autorisées", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "⚠️ Les notifications sont désactivées. Vous ne recevrez pas de rappels.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(NotificationsActivity.this, AccueilActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(NotificationsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    // Classe pour représenter une notification
    public static class NotificationItem {
        private long scheduleId;
        private String animalName;
        private String feedingTime;
        private String foodType;
        private boolean isScheduled;
        private String day;
        private String date;

        public NotificationItem(long scheduleId, String animalName, String feedingTime,
                                String foodType, boolean isScheduled, String day, String date) {
            this.scheduleId = scheduleId;
            this.animalName = animalName;
            this.feedingTime = feedingTime;
            this.foodType = foodType;
            this.isScheduled = isScheduled;
            this.day = day;
            this.date = date;
        }

        // Getters
        public long getScheduleId() { return scheduleId; }
        public String getAnimalName() { return animalName; }
        public String getFeedingTime() { return feedingTime; }
        public String getFoodType() { return foodType; }
        public boolean isScheduled() { return isScheduled; }
        public String getDay() { return day; }
        public String getDate() { return date; }
    }
}