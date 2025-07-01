package com.example.finalyearproject.Community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.Account.AchievementTracker;
import com.example.finalyearproject.R;
import com.example.finalyearproject.Workout.WorkoutExercise;
import com.example.finalyearproject.Workout.WorkoutExerciseAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityDetailsFragment extends Fragment {

    private TextView titleText, typeText, descriptionText, musclesText, equipmentText;
    private RecyclerView exerciseRecycler;
    private View addToWorkoutButton;

    private final List<WorkoutExercise> exerciseList = new ArrayList<>();
    private WorkoutExerciseAdapter adapter;

    private String workoutId;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleText = view.findViewById(R.id.community_workout_title);
        typeText = view.findViewById(R.id.community_workout_type);
        descriptionText = view.findViewById(R.id.community_workout_description);
        musclesText = view.findViewById(R.id.community_target_muscles);
        equipmentText = view.findViewById(R.id.community_equipment);
        exerciseRecycler = view.findViewById(R.id.community_exercise_recycler);
        addToWorkoutButton = view.findViewById(R.id.add_to_your_workout_button);

        adapter = new WorkoutExerciseAdapter(exerciseList, exercise -> {
            CommunityExerciseDialogFragment dialog = CommunityExerciseDialogFragment.newInstance(exercise);
            dialog.setCancelable(true);
            dialog.show(getParentFragmentManager(), "ExerciseDialog");
        });
        exerciseRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        exerciseRecycler.setAdapter(adapter);

        workoutId = getArguments() != null ? getArguments().getString("communityWorkoutId") : null;
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (workoutId != null) {
            loadCommunityWorkoutDetails();
        }

        addToWorkoutButton.setOnClickListener(v -> addWorkoutToUser());
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

    private void loadCommunityWorkoutDetails() {
        db.collection("communityWorkouts").document(workoutId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        titleText.setText(doc.getString("title"));
                        typeText.setText(doc.getString("type"));
                        descriptionText.setText(doc.getString("description"));

                        List<String> muscles = (List<String>) doc.get("targetMuscles");
                        List<String> equipment = (List<String>) doc.get("equipment");
                        musclesText.setText("Target: " + (muscles != null ? String.join(", ", muscles) : "-"));
                        equipmentText.setText("Equipment: " + (equipment != null ? String.join(", ", equipment) : "-"));
                    }
                });

        db.collection("communityWorkouts").document(workoutId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(query -> {
                    exerciseList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        WorkoutExercise ex = doc.toObject(WorkoutExercise.class);
                        exerciseList.add(ex);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void addWorkoutToUser() {
        DocumentReference sourceRef = db.collection("communityWorkouts").document(workoutId);
        sourceRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Map<String, Object> workoutData = new HashMap<>();
                workoutData.put("title", doc.getString("title"));
                workoutData.put("type", doc.getString("type"));
                workoutData.put("description", "For anyone looking to build muscle with limited amount of days they can attend the gym");
                workoutData.put("targetMuscles", doc.get("targetMuscles"));
                workoutData.put("equipment", doc.get("equipment"));
                workoutData.put("imageUrl", doc.get("imageUrl"));
                workoutData.put("timestamp", doc.get("timestamp"));
                workoutData.put("hasCommunity", true);

                db.collection("users").document(currentUserId).collection("workouts")
                        .add(workoutData)
                        .addOnSuccessListener(newWorkoutRef -> {
                            copyExercises(sourceRef.collection("exercises"), newWorkoutRef.collection("exercises"));

                            // ✅ Check for "Squad Sync" achievement
                            View rootView = requireView();
                            AchievementTracker tracker = new AchievementTracker(db, currentUserId, rootView);
                            tracker.checkSquadSyncAchievement();
                        });
            }
        });
    }

    private void copyExercises(CollectionReference from, CollectionReference to) {
        from.get().addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                to.add(doc.getData());
            }
            Snackbar.make(requireView(), "Workout added to your workouts!", Snackbar.LENGTH_SHORT).show();
        });
    }
}
