package com.example.agritrack.Activities.Animaux;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.FeedingScheduleAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.Database.AnimalFeedingScheduleEntity;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayFeedingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FeedingScheduleAdapter adapter;
    private AnimalFeedingScheduleDao scheduleDao;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_feedings);

        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        scheduleDao = database.animalFeedingScheduleDao();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerFeedings);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedingScheduleAdapter(this, scheduleDao);
        recyclerView.setAdapter(adapter);

        loadTodayFeedings();
    }

    private void loadTodayFeedings() {
        new Thread(() -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            List<AnimalFeedingScheduleEntity> schedules = scheduleDao.getSchedulesForDate(today);

            runOnUiThread(() -> {
                if (schedules.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setSchedules(schedules);
                }
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayFeedings();
    }
}