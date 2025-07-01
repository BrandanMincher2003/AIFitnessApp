package com.example.finalyearproject.Community;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class CommunityWorkoutAdapter extends RecyclerView.Adapter<CommunityWorkoutAdapter.WorkoutViewHolder> {

    public interface OnCommunityWorkoutClickListener {
        void onClick(CommunityWorkoutItem item);
    }

    private final Context context;
    private final List<CommunityWorkoutItem> workoutList;
    private final OnCommunityWorkoutClickListener listener;

    public CommunityWorkoutAdapter(Context context, List<CommunityWorkoutItem> workoutList, OnCommunityWorkoutClickListener listener) {
        this.context = context;
        this.workoutList = workoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        CommunityWorkoutItem item = workoutList.get(position);

        holder.title.setText(item.getTitle() != null ? item.getTitle() : "Untitled Workout");
        holder.muscles.setText(item.getTargetMusclesText());
        holder.creatorName.setText(item.getCreatorName() != null ? "By: " + item.getCreatorName() : "By: Unknown");

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.white_placeholder)
                .error(R.drawable.ic_logo_basic)
                .centerCrop()
                .into(holder.workoutImage);

        Glide.with(context)
                .load(item.getCreatorIconUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.creatorIcon);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView workoutImage;
        ShapeableImageView creatorIcon;
        TextView title, muscles, creatorName;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutImage = itemView.findViewById(R.id.workout_image);
            creatorIcon = itemView.findViewById(R.id.creator_icon);
            title = itemView.findViewById(R.id.workout_title);
            muscles = itemView.findViewById(R.id.target_muscles);
            creatorName = itemView.findViewById(R.id.creator_name);
        }
    }
}
