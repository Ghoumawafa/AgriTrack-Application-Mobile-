package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.agritrack.Api.ApiClient;
import com.example.agritrack.Api.WeatherApiService;
import com.example.agritrack.Models.WeatherResponse;
import com.example.agritrack.R;
import com.example.agritrack.Utils.WeatherUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherActivity extends AppCompatActivity {

    private TextView tvCity, tvTemperature, tvDescription, tvHumidity, tvWind, tvPressure, tvAdvice;
    private ImageView ivWeatherIcon;
    private Button btnRefresh, btnBack, btnSelectCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialiser les vues
        initViews();

        // Charger la météo par défaut (Paris)
        loadWeather("Paris");

        // Configurer les boutons
        setupButtons();
    }

    private void initViews() {
        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPressure = findViewById(R.id.tvPressure);
        tvAdvice = findViewById(R.id.tvAdvice);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        btnRefresh = findViewById(R.id.btnRefresh);
        btnBack = findViewById(R.id.btnBack);
        btnSelectCity = findViewById(R.id.btnSelectCity);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnRefresh.setOnClickListener(v -> {
            loadWeather("Paris");
            Toast.makeText(this, "Mise à jour...", Toast.LENGTH_SHORT).show();
        });

        btnSelectCity.setOnClickListener(v -> showCityDialog());
    }

    private void loadWeather(String city) {
        String apiKey = WeatherUtils.getApiKey(this);

        WeatherApiService service = ApiClient.getWeatherService();
        Call<WeatherResponse> call = service.getCurrentWeather(
                city, "metric", "fr", apiKey);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayWeather(response.body());
                    Toast.makeText(WeatherActivity.this,
                            "Météo actualisée", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WeatherActivity.this,
                            "Erreur: Ville non trouvée ou problème API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(WeatherActivity.this,
                        "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWeather(WeatherResponse weather) {
        // Ville et pays
        String cityName = weather.getName();
        String country = weather.getSys() != null ? weather.getSys().getCountry() : "";
        tvCity.setText(cityName + ", " + country);

        // Température
        double temp = weather.getMain() != null ? weather.getMain().getTemp() : 0;
        tvTemperature.setText(String.format("%.1f°C", temp));

        // Description
        if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
            String description = weather.getWeather().get(0).getDescription();
            tvDescription.setText(capitalizeFirstLetter(description));

            // Icône (si vous avez des drawables)
            // String iconCode = weather.getWeather().get(0).getIcon();
            // ivWeatherIcon.setImageResource(WeatherUtils.getWeatherIcon(iconCode));

            // Conseils agricoles
            String mainWeather = weather.getWeather().get(0).getMain();
            int humidity = weather.getMain() != null ? weather.getMain().getHumidity() : 0;
            String advice = WeatherUtils.getAgriculturalAdvice(mainWeather, temp, humidity);
            tvAdvice.setText(advice);
        }

        // Détails
        if (weather.getMain() != null) {
            tvHumidity.setText(weather.getMain().getHumidity() + "%");
            tvPressure.setText(weather.getMain().getPressure() + " hPa");
        }

        if (weather.getWind() != null) {
            tvWind.setText(String.format("%.1f km/h", weather.getWind().getSpeed()));
        }
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void showCityDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Changer de ville");

        final EditText input = new EditText(this);
        input.setHint("Ex: Paris, Lyon, Marseille...");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String city = input.getText().toString().trim();
            if (!city.isEmpty()) {
                loadWeather(city);
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}