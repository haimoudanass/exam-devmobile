package com.smartcity.app;

import android.content.Context;

/**
 * Traduit les valeurs API (anglais) en libellés français pour l'affichage uniquement.
 * Les clés JSON (safety, pending, etc.) ne sont jamais modifiées.
 */
public final class DisplayLabels {

    private DisplayLabels() {}

    public static String category(Context context, String apiCategory) {
        if (apiCategory == null || apiCategory.isEmpty()) {
            return context.getString(R.string.display_cat_general);
        }
        switch (apiCategory.toLowerCase()) {
            case "safety":
                return context.getString(R.string.display_cat_safety);
            case "infrastructure":
                return context.getString(R.string.display_cat_infrastructure);
            case "lighting":
                return context.getString(R.string.display_cat_lighting);
            case "waste":
                return context.getString(R.string.display_cat_waste);
            case "water":
                return context.getString(R.string.display_cat_water);
            default:
                return context.getString(R.string.display_cat_general);
        }
    }

    public static String status(Context context, String apiStatus) {
        if (apiStatus == null || apiStatus.isEmpty()) {
            return context.getString(R.string.display_stat_pending);
        }
        switch (apiStatus.toLowerCase()) {
            case "resolved":
                return context.getString(R.string.display_stat_resolved);
            case "in_progress":
                return context.getString(R.string.display_stat_in_progress);
            case "pending":
            default:
                return context.getString(R.string.display_stat_pending);
        }
    }
}
