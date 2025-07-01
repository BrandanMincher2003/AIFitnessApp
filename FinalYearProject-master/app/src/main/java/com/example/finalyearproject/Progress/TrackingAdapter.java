package com.example.finalyearproject.Progress;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackingAdapter extends RecyclerView.Adapter<TrackingAdapter.TrackingViewHolder> {

    private final List<ProgressModel.TrackingEntry> trackingEntries;
    private final String unit;

    public TrackingAdapter(List<ProgressModel.TrackingEntry> trackingEntries, String unit) {
        this.trackingEntries = trackingEntries;
        this.unit = unit;
    }

    @NonNull
    @Override
    public TrackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tracking_entry, parent, false);
        return new TrackingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackingViewHolder holder, int position) {
        ProgressModel.TrackingEntry entry = trackingEntries.get(position);
        Date date = new Date(entry.getTimestamp());
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());

        holder.dateText.setText(sdf.format(date));
        holder.valueText.setText(entry.getValue() + " " + unit);
    }

    @Override
    public int getItemCount() {
        return trackingEntries.size();
    }

    static class TrackingViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, valueText;

        public TrackingViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.trackingDate);
            valueText = itemView.findViewById(R.id.trackingValue);
        }
    }
}
