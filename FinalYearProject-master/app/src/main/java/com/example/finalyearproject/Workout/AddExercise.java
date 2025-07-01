package com.example.finalyearproject.Workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddExercise extends Fragment {

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;
    private List<Exercise> exerciseList = new ArrayList<>();
    private List<Exercise> filteredList = new ArrayList<>();

    private SearchView searchView;
    private String workoutId;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference exercisesRef = db.collection("exercises");

    public AddExercise() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId");
        }

        if (workoutId == null) {
            Snackbar.make(view, "Workout ID is missing.", Snackbar.LENGTH_LONG).show();
            return;
        }

        recyclerView = view.findViewById(R.id.exercise_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = view.findViewById(R.id.exercise_search_view);
        searchView.clearFocus();

        adapter = new ExerciseAdapter(filteredList, exercise -> openExerciseDetailsDialog(exercise, view));
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterExercises(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExercises(newText);
                return false;
            }
        });

        fetchExercises(view);
    }

    private void fetchExercises(View view) {
        exercisesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    exerciseList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name") != null ? doc.getString("name") : "";
                        String targetMuscles = doc.getString("targetMuscles") != null ? doc.getString("targetMuscles") : "";
                        String description = doc.getString("description") != null ? doc.getString("description") : "";
                        String difficulty = doc.getString("difficulty") != null ? doc.getString("difficulty") : "";
                        String type = doc.getString("type") != null ? doc.getString("type") : "";

                        String equipment = "";
                        Object equipmentObj = doc.get("equipment");
                        if (equipmentObj instanceof String) {
                            equipment = (String) equipmentObj;
                        } else if (equipmentObj instanceof List) {
                            List<String> equipmentList = (List<String>) equipmentObj;
                            equipment = String.join(", ", equipmentList);
                        } else if (equipmentObj != null) {
                            equipment = equipmentObj.toString();
                        }

                        Exercise exercise = new Exercise(name, targetMuscles, description, difficulty, equipment, type);
                        exerciseList.add(exercise);
                    }

                    filteredList.clear();
                    filteredList.addAll(exerciseList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(view, "Failed to load exercises: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void filterExercises(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(exerciseList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Exercise exercise : exerciseList) {
                if (exercise.getName().toLowerCase().contains(lowerCaseQuery) ||
                        exercise.getTargetMuscles().toLowerCase().contains(lowerCaseQuery) ||
                        exercise.getEquipment().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(exercise);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openExerciseDetailsDialog(Exercise exercise, View parentView) {
        if (workoutId != null) {
            ExerciseDetailsDialogFragment dialog = ExerciseDetailsDialogFragment.newInstance(exercise, workoutId);
            dialog.show(getParentFragmentManager(), "exercise_details_dialog");
        } else {
            Snackbar.make(parentView, "Workout ID not found.", Snackbar.LENGTH_SHORT).show();
        }
    }
}
