package com.example.finalyearproject.Account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final List<TimelineItem> timelineList;

    public TimelineAdapter(List<TimelineItem> timelineList) {
        this.timelineList = timelineList;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineItem item = timelineList.get(position);
        holder.workoutName.setText(item.getWorkoutName());
        holder.date.setText(item.getFormattedDate());
    }

    @Override
    public int getItemCount() {
        return timelineList.size();
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView workoutName, date;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutName = itemView.findViewById(R.id.timeline_workout_name);
            date = itemView.findViewById(R.id.timeline_date);
        }
    }
}
