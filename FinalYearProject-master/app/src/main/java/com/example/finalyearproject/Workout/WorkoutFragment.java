package com.example.finalyearproject.Workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorkoutFragment extends Fragment {

    private RecyclerView yourWorkoutsRecycler;
    private RecyclerView preplannedWorkoutsRecycler;
    private SearchView searchView;
    private FloatingActionButton fab;

    private FirebaseFirestore db;

    private final List<WorkoutItem> allUserWorkouts = new ArrayList<>();
    private final List<WorkoutItem> allPublicWorkouts = new ArrayList<>();
    private WorkoutAdapter yourWorkoutAdapter;
    private WorkoutAdapter publicWorkoutAdapter;

    public WorkoutFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView = view.findViewById(R.id.search_view);
        fab = view.findViewById(R.id.add_workout_fab);
        yourWorkoutsRecycler = view.findViewById(R.id.your_workouts_recycler);
        preplannedWorkoutsRecycler = view.findViewById(R.id.preplanned_workouts_recycler);

        yourWorkoutsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        preplannedWorkoutsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("WorkoutFragment", "User not logged in");
            return;
        }

        String uid = user.getUid();
        CollectionReference workoutsRef = db.collection("users").document(uid).collection("workouts");

        // Load user workouts
        yourWorkoutAdapter = new WorkoutAdapter(new ArrayList<>(), workoutItem -> {
            Bundle bundle = new Bundle();
            bundle.putString("workoutId", workoutItem.getId());
            Navigation.findNavController(view).navigate(R.id.workoutDetailsFragment, bundle);
        });
        yourWorkoutsRecycler.setAdapter(yourWorkoutAdapter);

        workoutsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            allUserWorkouts.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String workoutId = document.getId();
                String title = document.contains("title") ? document.getString("title") : "Untitled Workout";
                String imageUrl = document.contains("imageUrl") ? document.getString("imageUrl") : null;

                WorkoutItem workoutItem = new WorkoutItem(workoutId, title, "Loading...", imageUrl);
                allUserWorkouts.add(workoutItem);

                workoutsRef.document(workoutId).collection("exercises")
                        .get()
                        .addOnSuccessListener(exSnapshot -> {
                            int count = exSnapshot.size();
                            workoutItem.setDescription(count + " exercise" + (count == 1 ? "" : "s"));
                            filterAndDisplayWorkouts(searchView.getQuery().toString());
                        });
            }
            filterAndDisplayWorkouts(searchView.getQuery().toString());
        });

        // Load public (preplanned) workouts
        publicWorkoutAdapter = new WorkoutAdapter(new ArrayList<>(), workoutItem -> {
            Bundle bundle = new Bundle();
            bundle.putString("workoutId", workoutItem.getId());
            Navigation.findNavController(view).navigate(R.id.preplannedWorkoutDetailsFragment, bundle);
        });
        preplannedWorkoutsRecycler.setAdapter(publicWorkoutAdapter);

        db.collection("publicworkouts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allPublicWorkouts.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String workoutId = document.getId();
                String title = document.contains("title") ? document.getString("title") : "Untitled Workout";
                String imageUrl = document.contains("imageUrl") ? document.getString("imageUrl") : null;

                WorkoutItem workoutItem = new WorkoutItem(workoutId, title, "Loading...", imageUrl);
                allPublicWorkouts.add(workoutItem);

                db.collection("publicworkouts")
                        .document(workoutId)
                        .collection("exercises")
                        .get()
                        .addOnSuccessListener(exSnapshot -> {
                            int count = exSnapshot.size();
                            workoutItem.setDescription(count + " exercise" + (count == 1 ? "" : "s"));
                            filterAndDisplayWorkouts(searchView.getQuery().toString());
                        });
            }
            filterAndDisplayWorkouts(searchView.getQuery().toString());
        });

        // Search filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAndDisplayWorkouts(newText);
                return true;
            }
        });

        fab.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.workoutCreatorFragment);
        });
    }

    private void filterAndDisplayWorkouts(String query) {
        String lowerQuery = query.toLowerCase().trim();

        List<WorkoutItem> filteredUserWorkouts = allUserWorkouts.stream()
                .filter(item -> item.getTitle().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        List<WorkoutItem> filteredPublicWorkouts = allPublicWorkouts.stream()
                .filter(item -> item.getTitle().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        yourWorkoutAdapter.updateData(filteredUserWorkouts);
        publicWorkoutAdapter.updateData(filteredPublicWorkouts);
    }
}
