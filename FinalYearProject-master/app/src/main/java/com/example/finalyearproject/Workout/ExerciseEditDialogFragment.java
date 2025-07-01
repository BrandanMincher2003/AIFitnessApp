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

import com.example.finalyearproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ExerciseEditDialogFragment extends DialogFragment {

    private WorkoutExercise exercise;
    private int position;
    private ExerciseUpdateListener updateListener;
    private String workoutId;

    public interface ExerciseUpdateListener {
        void onExerciseUpdated(WorkoutExercise updatedExercise, int position);
        void onExerciseDeleted(int position);
    }

    // Updated to accept workoutId
    public static ExerciseEditDialogFragment newInstance(WorkoutExercise exercise, int position, String workoutId) {
        ExerciseEditDialogFragment fragment = new ExerciseEditDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("exercise", exercise);
        args.putInt("position", position);
        args.putString("workoutId", workoutId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setExerciseUpdateListener(ExerciseUpdateListener listener) {
        this.updateListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            exercise = (WorkoutExercise) getArguments().getSerializable("exercise");
            position = getArguments().getInt("position");
            workoutId = getArguments().getString("workoutId");
        }

        ((TextView) view.findViewById(R.id.exercise_name)).setText(exercise.getName());
        ((TextView) view.findViewById(R.id.exercise_type)).setText("Type: " + exercise.getType());
        ((TextView) view.findViewById(R.id.exercise_target_muscles)).setText("Target Muscles: " + exercise.getTargetMuscles());
        ((TextView) view.findViewById(R.id.exercise_equipment)).setText("Equipment: " + exercise.getEquipment());
        ((TextView) view.findViewById(R.id.exercise_description)).setText("Description: " + exercise.getDescription());

        TextInputEditText repsInput = view.findViewById(R.id.input_reps);
        TextInputEditText setsInput = view.findViewById(R.id.input_sets);
        repsInput.setText(exercise.getReps());
        setsInput.setText(exercise.getSets());

        MaterialButton updateButton = view.findViewById(R.id.button_add_exercise);
        MaterialButton deleteButton = view.findViewById(R.id.button_delete_exercise);

        // Update Firestore
        updateButton.setOnClickListener(v -> {
            String reps = repsInput.getText() != null ? repsInput.getText().toString().trim() : "";
            String sets = setsInput.getText() != null ? setsInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(reps) || TextUtils.isEmpty(sets)) {
                Snackbar.make(view, "Please enter both sets and reps", Snackbar.LENGTH_LONG).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String docId = exercise.getDocumentId();

            if (docId != null && workoutId != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .collection("workouts")
                        .document(workoutId)
                        .collection("exercises")
                        .document(docId)
                        .update("sets", sets, "reps", reps)
                        .addOnSuccessListener(aVoid -> {
                            exercise.setSets(sets);
                            exercise.setReps(reps);
                            if (updateListener != null) {
                                updateListener.onExerciseUpdated(exercise, position);
                            }
                            dismiss();
                        })
                        .addOnFailureListener(e -> Snackbar.make(view, "Update failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
            }
        });

        // Delete from Firestore
        deleteButton.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String docId = exercise.getDocumentId();

            if (docId != null && workoutId != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .collection("workouts")
                        .document(workoutId)
                        .collection("exercises")
                        .document(docId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            if (updateListener != null) {
                                updateListener.onExerciseDeleted(position);
                            }
                            dismiss();
                        })
                        .addOnFailureListener(e -> Snackbar.make(view, "Delete failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
            }
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
