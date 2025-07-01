package com.example.finalyearproject.Workout;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.finalyearproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ExerciseDetailsDialogFragment extends DialogFragment {

    private Exercise exercise;
    private String workoutId;

    public static ExerciseDetailsDialogFragment newInstance(Exercise exercise, String workoutId) {
        ExerciseDetailsDialogFragment fragment = new ExerciseDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("exercise", exercise);
        args.putString("workoutId", workoutId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_exercise_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            exercise = (Exercise) getArguments().getSerializable("exercise");
            workoutId = getArguments().getString("workoutId");
        }

        ((TextView) view.findViewById(R.id.exercise_name)).setText(exercise.getName());
        ((TextView) view.findViewById(R.id.exercise_type)).setText("Type: " + exercise.getType());
        ((TextView) view.findViewById(R.id.exercise_target_muscles)).setText("Target Muscles: " + exercise.getTargetMuscles());
        ((TextView) view.findViewById(R.id.exercise_equipment)).setText("Equipment: " + exercise.getEquipment());
        ((TextView) view.findViewById(R.id.exercise_description)).setText("Description: " + exercise.getDescription());

        TextInputEditText repsInput = view.findViewById(R.id.input_reps);
        TextInputEditText setsInput = view.findViewById(R.id.input_sets);

        MaterialButton addButton = view.findViewById(R.id.button_add_exercise);
        addButton.setOnClickListener(v -> {
            String reps = repsInput.getText() != null ? repsInput.getText().toString().trim() : "";
            String sets = setsInput.getText() != null ? setsInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(reps) || TextUtils.isEmpty(sets)) {
                Snackbar.make(view, "Please enter both sets and reps", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (workoutId == null) {
                Snackbar.make(view, "Workout ID is missing", Snackbar.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> exerciseData = new HashMap<>();
            exerciseData.put("name", exercise.getName());
            exerciseData.put("type", exercise.getType());
            exerciseData.put("targetMuscles", exercise.getTargetMuscles());
            exerciseData.put("equipment", exercise.getEquipment());
            exerciseData.put("description", exercise.getDescription());
            exerciseData.put("difficulty", exercise.getDifficulty());
            exerciseData.put("sets", sets);
            exerciseData.put("reps", reps);

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("workouts")
                    .document(workoutId)
                    .collection("exercises")
                    .add(exerciseData)
                    .addOnSuccessListener(docRef -> {
                        Snackbar.make(view, "Exercise added to workout", Snackbar.LENGTH_SHORT).show();
                        dismiss();

                        // Pop back to WorkoutCreatorFragment2
                        NavController navController = NavHostFragment.findNavController(this);
                        navController.popBackStack(R.id.workoutCreatorFragment2, false);
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(view, "Failed to add exercise: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
        });
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
