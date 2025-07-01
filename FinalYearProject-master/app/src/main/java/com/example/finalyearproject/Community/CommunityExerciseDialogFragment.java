package com.example.finalyearproject.Community;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.finalyearproject.R;
import com.example.finalyearproject.Workout.WorkoutExercise;

public class CommunityExerciseDialogFragment extends DialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_TYPE = "type";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_TARGET = "targetMuscles";
    private static final String ARG_EQUIPMENT = "equipment";

    public static CommunityExerciseDialogFragment newInstance(WorkoutExercise exercise) {
        CommunityExerciseDialogFragment fragment = new CommunityExerciseDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, exercise.getName());
        args.putString(ARG_TYPE, exercise.getType());
        args.putString(ARG_DESCRIPTION, exercise.getDescription());
        args.putString(ARG_TARGET, String.join(", ", exercise.getTargetMuscles()));
        args.putString(ARG_EQUIPMENT, String.join(", ", exercise.getEquipment()));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_exercise_view, container, false);

        TextView nameView = view.findViewById(R.id.exercise_name);
        TextView typeView = view.findViewById(R.id.exercise_type);
        TextView targetView = view.findViewById(R.id.exercise_target_muscles);
        TextView equipmentView = view.findViewById(R.id.exercise_equipment);
        TextView descriptionView = view.findViewById(R.id.exercise_description);

        Bundle args = getArguments();
        if (args != null) {
            nameView.setText(args.getString(ARG_NAME, ""));
            typeView.setText("Type: " + args.getString(ARG_TYPE, ""));
            targetView.setText("Target Muscles: " + args.getString(ARG_TARGET, ""));
            equipmentView.setText("Equipment: " + args.getString(ARG_EQUIPMENT, ""));
            descriptionView.setText("Description: " + args.getString(ARG_DESCRIPTION, ""));
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set full width for the dialog
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
