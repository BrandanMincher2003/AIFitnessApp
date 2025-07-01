package com.example.finalyearproject.Workout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.Account.AchievementTracker;
import com.example.finalyearproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutDetailsFragment extends Fragment {

    private TextView workoutTitle, workoutType, workoutDescription, targetMusclesText, equipmentText;
    private RecyclerView exerciseRecycler;
    private WorkoutExerciseAdapter exerciseAdapter;
    private final List<WorkoutExercise> exerciseList = new ArrayList<>();

    private FirebaseFirestore db;
    private String workoutId;
    private String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workoutTitle = view.findViewById(R.id.workout_title);
        workoutType = view.findViewById(R.id.workout_type);
        workoutDescription = view.findViewById(R.id.workout_description);
        targetMusclesText = view.findViewById(R.id.target_muscles_text);
        equipmentText = view.findViewById(R.id.equipment_text);
        exerciseRecycler = view.findViewById(R.id.exercise_recycler);

        exerciseRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exerciseAdapter = new WorkoutExerciseAdapter(exerciseList, this::showExerciseDetailsDialog);
        exerciseRecycler.setAdapter(exerciseAdapter);

        workoutId = getArguments() != null ? getArguments().getString("workoutId") : null;
        if (workoutId == null) {
            Log.e("WorkoutDetails", "Workout ID is null");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("WorkoutDetails", "User not logged in");
            return;
        }

        uid = user.getUid();
        db = FirebaseFirestore.getInstance();

        loadWorkoutInfo();
        loadExercises();

        View settingsButton = view.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> showSettingsDialog(view));

        View logWorkoutButton = view.findViewById(R.id.log_workout_button);
        logWorkoutButton.setOnClickListener(v -> logWorkoutToTimeline(view));
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBottomNav();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showBottomNav();
    }

    private void hideBottomNav() {
        if (getActivity() != null) {
            BottomNavigationView nav = getActivity().findViewById(R.id.nav_view);
            if (nav != null) {
                nav.setVisibility(View.GONE);
            }
        }
    }

    private void showBottomNav() {
        if (getActivity() != null) {
            BottomNavigationView nav = getActivity().findViewById(R.id.nav_view);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadWorkoutInfo() {
        db.collection("users").document(uid)
                .collection("workouts").document(workoutId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        workoutTitle.setText(doc.getString("title"));
                        workoutType.setText(doc.getString("type"));
                        workoutDescription.setText(doc.getString("description"));

                        List<String> targetMuscles = (List<String>) doc.get("targetMuscles");
                        List<String> equipment = (List<String>) doc.get("equipment");

                        targetMusclesText.setText("Target: " + (targetMuscles != null && !targetMuscles.isEmpty() ? String.join(", ", targetMuscles) : "-"));
                        equipmentText.setText("Equipment: " + (equipment != null && !equipment.isEmpty() ? String.join(", ", equipment) : "-"));
                    }
                })
                .addOnFailureListener(e -> Log.e("WorkoutDetails", "Failed to load workout info", e));
    }

    private void loadExercises() {
        db.collection("users").document(uid)
                .collection("workouts").document(workoutId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(query -> {
                    exerciseList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        WorkoutExercise ex = doc.toObject(WorkoutExercise.class);
                        exerciseList.add(ex);
                    }
                    exerciseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("WorkoutDetails", "Failed to load exercises", e));
    }

    private void showExerciseDetailsDialog(WorkoutExercise exercise) {
        ExerciseViewDialogFragment dialog = ExerciseViewDialogFragment.newInstance(exercise);
        dialog.show(getParentFragmentManager(), "ExerciseViewDialog");
    }

    private void showSettingsDialog(View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Workout Options")
                .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        navigateToEdit(view);
                    } else if (which == 1) {
                        confirmDelete(view);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToEdit(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("workoutId", workoutId);
        bundle.putString("workoutTitle", workoutTitle.getText().toString());
        Navigation.findNavController(view).navigate(R.id.workoutCreatorFragment2, bundle);
    }

    private void confirmDelete(View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete this workout?")
                .setPositiveButton("Delete", (dialog, which) -> deleteWorkout(view))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWorkout(View view) {
        db.collection("users").document(uid)
                .collection("workouts").document(workoutId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        db.collection("users").document(uid)
                                .collection("workouts").document(workoutId)
                                .collection("exercises")
                                .document(doc.getId())
                                .delete();
                    }

                    db.collection("users").document(uid)
                            .collection("workouts").document(workoutId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Snackbar.make(view, "Workout and exercises deleted", Snackbar.LENGTH_SHORT).show();
                                requireActivity().onBackPressed();
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(view, "Failed to delete workout", Snackbar.LENGTH_SHORT).show();
                                Log.e("WorkoutDetails", "Workout delete error", e);
                            });

                })
                .addOnFailureListener(e -> {
                    Snackbar.make(view, "Failed to delete exercises", Snackbar.LENGTH_SHORT).show();
                    Log.e("WorkoutDetails", "Exercise delete error", e);
                });
    }

    private void logWorkoutToTimeline(View view) {
        if (uid == null || workoutTitle.getText() == null) return;

        String title = workoutTitle.getText().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("workoutName", title);
        data.put("timestamp", Timestamp.now());

        db.collection("users").document(uid)
                .collection("timeline")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Snackbar.make(view, "Workout logged to timeline", Snackbar.LENGTH_SHORT).show();

                    AchievementTracker tracker = new AchievementTracker(db, uid, view);
                    tracker.checkFirstRepAchievement();
                    tracker.checkHabitHackerAchievement();
                    tracker.checkWeekendWarriorAchievement();
                })
                .addOnFailureListener(e -> {
                    Log.e("WorkoutDetails", "Failed to log workout", e);
                    Snackbar.make(view, "Failed to log workout", Snackbar.LENGTH_SHORT).show();
                });
    }
}
