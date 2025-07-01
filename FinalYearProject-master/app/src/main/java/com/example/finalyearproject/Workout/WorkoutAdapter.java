package com.example.finalyearproject.Workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;

import java.util.ArrayList;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private final List<WorkoutItem> workoutList;
    private final OnWorkoutClickListener clickListener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(WorkoutItem workoutItem);
    }

    public WorkoutAdapter(List<WorkoutItem> workoutList, OnWorkoutClickListener clickListener) {
        this.workoutList = new ArrayList<>(workoutList); // ensure internal copy
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_card, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutItem item = workoutList.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.white_placeholder)
                .error(R.drawable.ic_logo_basic)
                .centerCrop()
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> clickListener.onWorkoutClick(item));
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public void updateData(List<WorkoutItem> newData) {
        workoutList.clear();
        workoutList.addAll(newData);
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.workout_image);
            title = itemView.findViewById(R.id.workout_title);
            description = itemView.findViewById(R.id.target_muscles);
        }
    }
}
