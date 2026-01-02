package com.example.agritrack.Activities.Animaux;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.FoodPlanAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalFoodPlanDao;
import com.example.agritrack.Database.AnimalFoodPlanEntity;
import com.example.agritrack.R;

import java.util.List;

public class FoodPlansListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodPlanAdapter adapter;
    private AnimalFoodPlanDao planDao;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_plans_list);

        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        planDao = database.animalFoodPlanDao();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerPlans);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodPlanAdapter(this);
        recyclerView.setAdapter(adapter);

        loadPlans();
    }

    private void loadPlans() {
        new Thread(() -> {
            List<AnimalFoodPlanEntity> plans = planDao.getAllPlans();

            runOnUiThread(() -> {
                if (plans.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setPlans(plans);
                }
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
    }
}