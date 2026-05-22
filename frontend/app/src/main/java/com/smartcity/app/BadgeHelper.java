package com.smartcity.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public final class BadgeHelper {

    private BadgeHelper() {}

    public static void applyCategoryBadge(TextView view, String category) {
        int color = categoryColor(view.getContext(), category);
        styleBadge(view, color);
        view.setText(view.getContext().getString(
                R.string.label_category,
                DisplayLabels.category(view.getContext(), category)
        ));
    }

    public static void applyStatusBadge(TextView view, String status) {
        int color = statusColor(view.getContext(), status);
        styleBadge(view, color);
        view.setText(view.getContext().getString(
                R.string.label_status,
                DisplayLabels.status(view.getContext(), status)
        ));
    }

    private static void styleBadge(TextView view, int backgroundColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(view, 12));
        drawable.setColor(backgroundColor);
        view.setBackground(drawable);
        view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.badge_text_on_color));
        int padH = (int) dp(view, 10);
        int padV = (int) dp(view, 4);
        view.setPadding(padH, padV, padH, padV);
    }

    private static int categoryColor(Context context, String category) {
        if (category == null) {
            return ContextCompat.getColor(context, R.color.category_general);
        }
        switch (category.toLowerCase()) {
            case "safety":
                return ContextCompat.getColor(context, R.color.category_safety);
            case "lighting":
                return ContextCompat.getColor(context, R.color.category_lighting);
            case "waste":
                return ContextCompat.getColor(context, R.color.category_waste);
            case "water":
                return ContextCompat.getColor(context, R.color.category_water);
            case "infrastructure":
                return ContextCompat.getColor(context, R.color.category_infrastructure);
            default:
                return ContextCompat.getColor(context, R.color.category_general);
        }
    }

    private static int statusColor(Context context, String status) {
        if (status == null) {
            return ContextCompat.getColor(context, R.color.status_pending);
        }
        switch (status.toLowerCase()) {
            case "resolved":
                return ContextCompat.getColor(context, R.color.status_resolved);
            case "in_progress":
                return ContextCompat.getColor(context, R.color.status_in_progress);
            case "pending":
            default:
                return ContextCompat.getColor(context, R.color.status_pending);
        }
    }

    /** Google Maps pin hue aligned with AI categories (safety = red, infrastructure = orange). */
    public static float markerHue(String category) {
        if (category == null) {
            return BitmapDescriptorFactory.HUE_AZURE;
        }
        switch (category.toLowerCase()) {
            case "safety":
                return BitmapDescriptorFactory.HUE_RED;
            case "infrastructure":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "lighting":
                return BitmapDescriptorFactory.HUE_YELLOW;
            case "waste":
                return BitmapDescriptorFactory.HUE_GREEN;
            case "water":
                return BitmapDescriptorFactory.HUE_BLUE;
            default:
                return BitmapDescriptorFactory.HUE_AZURE;
        }
    }

    public static String formatCategoryLabel(Context context, String category) {
        return DisplayLabels.category(context, category);
    }

    private static float dp(TextView view, int dp) {
        return dp * view.getResources().getDisplayMetrics().density;
    }
}
