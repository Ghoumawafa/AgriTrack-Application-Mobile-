package com.example.agritrack.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import com.example.agritrack.R;
import com.example.agritrack.Models.DashboardModule;

/**
 * Classe utilitaire pour gérer les cartes de modules de l'application AgriTrack
 */
public class ModuleCardHelper {

    /**
     * Configure une carte de module avec les données fournies
     *
     * @param context Le contexte de l'activité
     * @param cardView La vue de la carte à configurer
     * @param module Les données du module
     */
    public static void setupModuleCard(Context context, View cardView, DashboardModule module) {
        if (cardView == null || module == null) return;

        // Trouver les vues à l'intérieur de la carte
        TextView iconView = cardView.findViewById(R.id.module_icon);
        TextView titleView = cardView.findViewById(R.id.module_title);
        TextView descView = cardView.findViewById(R.id.module_description);
        View background = cardView.findViewById(R.id.cardView);

        // Configurer l'icône
        if (iconView != null) {
            iconView.setText(module.getIcon());
        }

        // Configurer le titre
        if (titleView != null) {
            titleView.setText(module.getTitle());
            try {
                titleView.setTextColor(Color.parseColor(module.getColor()));
            } catch (IllegalArgumentException e) {
                titleView.setTextColor(Color.BLACK);
            }
        }

        // Configurer la description
        if (descView != null) {
            descView.setText(module.getDescription());
        }

        // Configurer la couleur de fond (version plus claire pour le fond)
        if (background != null) {
            try {
                int color = Color.parseColor(module.getColor());
                // Créer une version plus claire de la couleur pour le fond
                int lightColor = lightenColor(color, 0.9f);
                background.setBackgroundColor(lightColor);
            } catch (IllegalArgumentException e) {
                background.setBackgroundColor(Color.WHITE);
            }
        }

        // Configurer le clic sur la carte
        if (cardView instanceof CardView) {
            CardView card = (CardView) cardView;
            card.setOnClickListener(v -> {
                if (module.getActivityClass() != null) {
                    try {
                        Intent intent = new Intent(context, module.getActivityClass());
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context,
                                "Module en cours de développement",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context,
                            "Module bientôt disponible",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Configure une carte avec un listener personnalisé
     *
     * @param context Le contexte de l'activité
     * @param cardView La vue de la carte à configurer
     * @param module Les données du module
     * @param clickListener Le listener de clic personnalisé
     */
    public static void setupModuleCard(Context context, View cardView, DashboardModule module,
                                       View.OnClickListener clickListener) {
        if (cardView == null || module == null) return;

        // Configurer les vues
        TextView iconView = cardView.findViewById(R.id.module_icon);
        TextView titleView = cardView.findViewById(R.id.module_title);
        TextView descView = cardView.findViewById(R.id.module_description);
        View background = cardView.findViewById(R.id.cardView);

        if (iconView != null) iconView.setText(module.getIcon());

        if (titleView != null) {
            titleView.setText(module.getTitle());
            try {
                titleView.setTextColor(Color.parseColor(module.getColor()));
            } catch (IllegalArgumentException e) {
                titleView.setTextColor(Color.BLACK);
            }
        }

        if (descView != null) descView.setText(module.getDescription());

        if (background != null) {
            try {
                int color = Color.parseColor(module.getColor());
                int lightColor = lightenColor(color, 0.9f);
                background.setBackgroundColor(lightColor);
            } catch (IllegalArgumentException e) {
                background.setBackgroundColor(Color.WHITE);
            }
        }

        // Configurer le listener personnalisé
        if (cardView instanceof CardView) {
            cardView.setOnClickListener(clickListener);
        }
    }

    /**
     * Éclaircit une couleur en ajoutant du blanc
     *
     * @param color La couleur à éclaircir
     * @param factor Le facteur d'éclaircissement (0.0f = couleur originale, 1.0f = blanc)
     * @return La couleur éclaircie
     */
    private static int lightenColor(int color, float factor) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        red = (int) (red + (255 - red) * factor);
        green = (int) (green + (255 - green) * factor);
        blue = (int) (blue + (255 - blue) * factor);

        return Color.rgb(red, green, blue);
    }

    /**
     * Crée un module Animaux
     */
    public static DashboardModule createAnimalsModule(Class<?> activityClass) {
        return new DashboardModule("🐄", "Animaux", "Gérer le bétail", "#1F5C2E", activityClass);
    }

    /**
     * Crée un module Cultures
     */
    public static DashboardModule createPlantsModule(Class<?> activityClass) {
        return new DashboardModule("🌾", "Cultures", "Suivre les récoltes", "#F57F17", activityClass);
    }

    /**
     * Crée un module Médicaments
     */
    public static DashboardModule createMedicinesModule(Class<?> activityClass) {
        return new DashboardModule("💊", "Médicaments", "Soins & vaccins", "#C62828", activityClass);
    }

    /**
     * Crée un module Matériel
     */
    public static DashboardModule createEquipmentModule(Class<?> activityClass) {
        return new DashboardModule("🚜", "Matériel", "Outils & équipements", "#0277BD", activityClass);
    }

    /**
     * Crée un module Finances
     */
    public static DashboardModule createFinanceModule(Class<?> activityClass) {
        return new DashboardModule("💰", "Finances", "Dépenses & revenus", "#6A1B9A", activityClass);
    }


}