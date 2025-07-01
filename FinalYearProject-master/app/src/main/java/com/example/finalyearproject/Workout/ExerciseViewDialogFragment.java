package com.example.finalyearproject.Workout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.finalyearproject.R;

public class ExerciseViewDialogFragment extends DialogFragment {

    private static final String ARG_EXERCISE = "exercise";
    private WorkoutExercise exercise;

    public static ExerciseViewDialogFragment newInstance(WorkoutExercise exercise) {
        ExerciseViewDialogFragment fragment = new ExerciseViewDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_exercise_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            exercise = (WorkoutExercise) getArguments().getSerializable(ARG_EXERCISE);
        }

        ((TextView) view.findViewById(R.id.exercise_name)).setText(exercise.getName());
        ((TextView) view.findViewById(R.id.exercise_type)).setText("Type: " + exercise.getType());
        ((TextView) view.findViewById(R.id.exercise_target_muscles)).setText("Target Muscles: " + exercise.getTargetMuscles());
        ((TextView) view.findViewById(R.id.exercise_equipment)).setText("Equipment: " + exercise.getEquipment());
        ((TextView) view.findViewById(R.id.exercise_description)).setText("Description: " + exercise.getDescription());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
