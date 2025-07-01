package com.example.finalyearproject.Workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkoutCreatorFragment2 extends Fragment {

    private String workoutId;
    private String workoutTitle;

    private RecyclerView recyclerView;
    private WorkoutExerciseAdapter adapter;
    private final List<WorkoutExercise> exerciseList = new ArrayList<>();

    private FloatingActionButton addExerciseFab;
    private View submitWorkoutButton;

    public WorkoutCreatorFragment2() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId");
            workoutTitle = getArguments().getString("workoutTitle");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_workout_creator2, container, false);
        requireActivity().setTitle("Workout Editor");

        TextView titleText = view.findViewById(R.id.workout_title_text);
        addExerciseFab = view.findViewById(R.id.add_exercise_fab);
        submitWorkoutButton = view.findViewById(R.id.submit_workout_button);
        recyclerView = view.findViewById(R.id.exercise_recycler_view);

        if (workoutTitle != null) {
            titleText.setText("Workout: " + workoutTitle);
        }

        setupRecyclerView();

        addExerciseFab.setOnClickListener(v -> {
            if (workoutId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("workoutId", workoutId);
                Navigation.findNavController(view).navigate(R.id.addExerciseFragment, bundle);
            }
        });

        submitWorkoutButton.setOnClickListener(v -> {
            if (exerciseList.isEmpty()) {
                Snackbar.make(view, "Please add at least one exercise before submitting.", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(view, "Workout submitted successfully.", Snackbar.LENGTH_LONG).show();
                NavController navController = Navigation.findNavController(view);
                navController.popBackStack(R.id.navigation_workout, false);
            }
        });

        fetchExercisesFromFirestore();

        return view;
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

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutExerciseAdapter(exerciseList, exercise -> {
            int position = exerciseList.indexOf(exercise);
            ExerciseEditDialogFragment dialog = ExerciseEditDialogFragment.newInstance(exercise, position, workoutId);
            dialog.setExerciseUpdateListener(new ExerciseEditDialogFragment.ExerciseUpdateListener() {
                @Override
                public void onExerciseUpdated(WorkoutExercise updatedExercise, int pos) {
                    exerciseList.set(pos, updatedExercise);
                    adapter.notifyItemChanged(pos);
                }

                @Override
                public void onExerciseDeleted(int pos) {
                    exerciseList.remove(pos);
                    adapter.notifyItemRemoved(pos);
                    submitWorkoutButton.setEnabled(!exerciseList.isEmpty());
                }
            });
            dialog.show(getParentFragmentManager(), "ExerciseEditDialog");
        });
        recyclerView.setAdapter(adapter);
    }

    private void fetchExercisesFromFirestore() {
        if (workoutId == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("workouts")
                .document(workoutId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    exerciseList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        WorkoutExercise exercise = new WorkoutExercise(
                                doc.getString("name"),
                                doc.getString("targetMuscles"),
                                doc.getString("description"),
                                doc.getString("difficulty"),
                                doc.getString("equipment"),
                                doc.getString("type"),
                                doc.getString("sets"),
                                doc.getString("reps"),
                                doc.getId()
                        );
                        exerciseList.add(exercise);
                    }

                    adapter.notifyDataSetChanged();
                    submitWorkoutButton.setEnabled(!exerciseList.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to load exercises.", Snackbar.LENGTH_LONG).show();
                    submitWorkoutButton.setEnabled(false);
                });
    }
}
