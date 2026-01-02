package com.example.agritrack.Activities.Animaux;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedingDashboardActivity extends AppCompatActivity {

    private AgriTrackRoomDatabase database;
    private AnimalFeedingScheduleDao scheduleDao;

    private TextView tvTodayPending, tvTodayCompleted, tvTodaySkipped;
    private CardView cardTodayFeedings, cardGenerateSchedule, cardFeedingHistory, cardFoodPlans, cardStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_dashboard);

        database = AgriTrackRoomDatabase.getInstance(this);
        scheduleDao = database.animalFeedingScheduleDao();

        initializeViews();
        loadTodayStats();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvTodayPending = findViewById(R.id.tvTodayPending);
        tvTodayCompleted = findViewById(R.id.tvTodayCompleted);
        tvTodaySkipped = findViewById(R.id.tvTodaySkipped);

        cardTodayFeedings = findViewById(R.id.cardTodayFeedings);
        cardGenerateSchedule = findViewById(R.id.cardGenerateSchedule);
        cardFeedingHistory = findViewById(R.id.cardFeedingHistory);
        cardFoodPlans = findViewById(R.id.cardFoodPlans);
        cardStatistics = findViewById(R.id.cardStatistics);

        // Navigation vers les différentes sections
        cardTodayFeedings.setOnClickListener(v -> {
            startActivity(new Intent(this, TodayFeedingsActivity.class));
        });

        cardGenerateSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, GenerateScheduleActivity.class));
        });

        cardFeedingHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, FeedingHistoryActivity.class));
        });

        cardFoodPlans.setOnClickListener(v -> {
            startActivity(new Intent(this, FoodPlansListActivity.class));
        });

        cardStatistics.setOnClickListener(v -> {
            startActivity(new Intent(this, FeedingStatisticsActivity.class));
        });
    }

    private void loadTodayStats() {
        new Thread(() -> {
            try {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // Ces méthodes existent maintenant dans votre DAO
                int pending = scheduleDao.getPendingFeedingsForDate(today).size();
                int completed = scheduleDao.getCompletedFeedingsForDate(today).size();
                int skipped = scheduleDao.getSkippedFeedingsForDate(today).size();

                runOnUiThread(() -> {
                    tvTodayPending.setText(String.valueOf(pending));
                    tvTodayCompleted.setText(String.valueOf(completed));
                    tvTodaySkipped.setText(String.valueOf(skipped));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayStats();
    }
}