package com.example.agritrack.Activities.Animaux;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.FeedingHistoryAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalFeedingRecordDao;
import com.example.agritrack.Database.AnimalFeedingRecordEntity;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.Database.AnimalFeedingScheduleEntity;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class FeedingHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FeedingHistoryAdapter adapter;
    private AnimalFeedingRecordDao recordDao;
    private AnimalFeedingScheduleDao scheduleDao;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_history);

        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        recordDao = database.animalFeedingRecordDao();
        scheduleDao = database.animalFeedingScheduleDao();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerHistory);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedingHistoryAdapter(this);
        recyclerView.setAdapter(adapter);

        loadTodayHistory();
    }

    private void loadTodayHistory() {
        new Thread(() -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            List<AnimalFeedingRecordEntity> records = recordDao.getByDate(today);

            runOnUiThread(() -> {
                if (records.isEmpty()) {
                    new Thread(() -> {
                        List<AnimalFeedingScheduleEntity> completed = scheduleDao.getCompletedFeedingsForDate(today);
                        List<AnimalFeedingScheduleEntity> skipped = scheduleDao.getSkippedFeedingsForDate(today);

                        List<AnimalFeedingRecordEntity> synthetic = new ArrayList<>();
                        for (AnimalFeedingScheduleEntity s : completed) {
                            AnimalFeedingRecordEntity r = new AnimalFeedingRecordEntity(
                                    s.getAnimalId(),
                                    s.getId(),
                                    s.getFeedingDate(),
                                    s.getActualTime() != null ? s.getActualTime() : s.getScheduledTime(),
                                    "FED"
                            );
                            r.setQuantityGiven(s.getHayQuantity() + s.getGrainsQuantity() + s.getSupplementsQuantity());
                            r.setFedBy(s.getFedBy());
                            r.setNotes(s.getNotes());
                            synthetic.add(r);
                        }
                        for (AnimalFeedingScheduleEntity s : skipped) {
                            AnimalFeedingRecordEntity r = new AnimalFeedingRecordEntity(
                                    s.getAnimalId(),
                                    s.getId(),
                                    s.getFeedingDate(),
                                    s.getActualTime() != null ? s.getActualTime() : s.getScheduledTime(),
                                    "SKIPPED"
                            );
                            r.setNotes(s.getSkipReason() != null ? "Raison: " + s.getSkipReason() : "");
                            synthetic.add(r);
                        }

                        runOnUiThread(() -> {
                            if (synthetic.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                adapter.setRecords(synthetic);
                            }
                        });
                    }).start();
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setRecords(records);
                }
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayHistory();
    }
}
