package com.example.finalyearproject.Workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;

import java.util.List;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder> {

    private final List<WorkoutExercise> exerciseList;
    private final OnExerciseClickListener clickListener;

    // Interface for callback
    public interface OnExerciseClickListener {
        void onExerciseClick(WorkoutExercise exercise);
    }

    // Updated constructor
    public WorkoutExerciseAdapter(List<WorkoutExercise> exerciseList, OnExerciseClickListener clickListener) {
        this.exerciseList = exerciseList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_card, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        WorkoutExercise exercise = exerciseList.get(position);
        holder.nameText.setText(exercise.getName());
        holder.musclesText.setText(exercise.getTargetMuscles());
        holder.setsRepsText.setText("Sets: " + exercise.getSets() + "  |  Reps: " + exercise.getReps());

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onExerciseClick(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, musclesText, setsRepsText;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.exercise_name);
            musclesText = itemView.findViewById(R.id.target_muscles);
            setsRepsText = itemView.findViewById(R.id.sets_reps);
        }
    }
}
