package com.smartcity.app;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public final class ReportUiHelper {

    private ReportUiHelper() {}

    public static void bindDetail(Context context, Report report,
                                  ImageView imageView,
                                  TextView titleView,
                                  TextView categoryView,
                                  TextView statusView,
                                  TextView descriptionView,
                                  TextView coordsView) {
        titleView.setText(report.title);
        BadgeHelper.applyCategoryBadge(categoryView, report.category);
        BadgeHelper.applyStatusBadge(statusView, report.status);
        descriptionView.setText(
                report.description == null || report.description.isEmpty()
                        ? context.getString(R.string.detail_no_description)
                        : report.description
        );

        if (coordsView != null) {
            coordsView.setText(context.getString(
                    R.string.detail_coords,
                    report.latitude,
                    report.longitude
            ));
        }

        if (report.imageUrl != null && !report.imageUrl.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(report.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    public static void showBottomSheet(Context context, Report report, Runnable onFullDetail) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.ThemeOverlay_SmartCity_BottomSheet);
        View sheet = View.inflate(context, R.layout.bottom_sheet_report_detail, null);
        dialog.setContentView(sheet);

        ImageView image = sheet.findViewById(R.id.detailImage);
        TextView title = sheet.findViewById(R.id.detailTitle);
        TextView category = sheet.findViewById(R.id.detailCategory);
        TextView status = sheet.findViewById(R.id.detailStatus);
        TextView description = sheet.findViewById(R.id.detailDescription);

        bindDetail(context, report, image, title, category, status, description, null);

        sheet.findViewById(R.id.btnOpenFullDetail).setOnClickListener(v -> {
            dialog.dismiss();
            if (onFullDetail != null) {
                onFullDetail.run();
            }
        });

        dialog.show();
    }
}
