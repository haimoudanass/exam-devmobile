package com.smartcity.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    private final List<Report> items = new ArrayList<>();
    private final OnReportClickListener listener;

    public ReportListAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Report> reports) {
        items.clear();
        if (reports != null) {
            items.addAll(reports);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = items.get(position);
        Context context = holder.itemView.getContext();

        holder.title.setText(report.title);
        BadgeHelper.applyCategoryBadge(holder.category, report.category);
        BadgeHelper.applyStatusBadge(holder.status, report.status);

        if (report.imageUrl != null && !report.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(report.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .into(holder.thumb);
        } else {
            holder.thumb.setImageDrawable(null);
            holder.thumb.setBackgroundResource(R.drawable.bg_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumb;
        final TextView title;
        final TextView category;
        final TextView status;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.imageThumb);
            title = itemView.findViewById(R.id.textTitle);
            category = itemView.findViewById(R.id.textCategory);
            status = itemView.findViewById(R.id.textStatus);
        }
    }
}
