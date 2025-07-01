package com.example.finalyearproject.Community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityFragment extends Fragment {

    private RecyclerView communityRecycler;
    private FloatingActionButton uploadFab;
    private SearchView searchView;
    private CommunityWorkoutAdapter adapter;

    private final List<CommunityWorkoutItem> communityList = new ArrayList<>();
    private final List<CommunityWorkoutItem> allCommunityWorkouts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        communityRecycler = view.findViewById(R.id.community_workout_recycler);
        uploadFab = view.findViewById(R.id.upload_fab);
        searchView = view.findViewById(R.id.community_search_view);

        communityRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommunityWorkoutAdapter(getContext(), communityList, item -> {
            Bundle bundle = new Bundle();
            bundle.putString("communityWorkoutId", item.getId());
            NavHostFragment.findNavController(CommunityFragment.this)
                    .navigate(R.id.communityDetailsFragment, bundle);
        });

        communityRecycler.setAdapter(adapter);

        loadCommunityWorkouts();

        uploadFab.setOnClickListener(v -> showWorkoutSelectionDialog());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterCommunityWorkouts(newText);
                return true;
            }
        });
    }

    private void loadCommunityWorkouts() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("communityWorkouts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allCommunityWorkouts.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String creatorUid = doc.getString("creatorUid");
                        if (creatorUid != null && creatorUid.equals(currentUid)) {
                            continue;
                        }

                        String title = doc.getString("title") != null ? doc.getString("title") : "Untitled";
                        List<String> targetMuscles = extractStringList(doc.get("targetMuscles"));
                        String imageUrl = doc.getString("imageUrl");
                        String description = doc.getString("description");
                        List<String> equipment = extractStringList(doc.get("equipment"));
                        String type = doc.getString("type");
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        String creatorName = doc.getString("creatorName") != null ? doc.getString("creatorName") : "Unknown";
                        String creatorIconUrl = doc.getString("creatorIconUrl");
                        String id = doc.getId();

                        CommunityWorkoutItem item = new CommunityWorkoutItem(
                                title, targetMuscles, imageUrl, description,
                                equipment, type, timestamp,
                                creatorName, creatorIconUrl, id
                        );
                        allCommunityWorkouts.add(item);
                    }
                    filterCommunityWorkouts(searchView.getQuery().toString());
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(), "Failed to load community workouts", Snackbar.LENGTH_SHORT).show()
                );
    }

    private void filterCommunityWorkouts(String query) {
        String lowerQuery = query != null ? query.toLowerCase().trim() : "";
        communityList.clear();
        communityList.addAll(allCommunityWorkouts.stream()
                .filter(item -> {
                    boolean matchesTitle = item.getTitle() != null &&
                            item.getTitle().toLowerCase().contains(lowerQuery);

                    boolean matchesMuscles = item.getTargetMuscles() != null &&
                            item.getTargetMuscles().stream()
                                    .anyMatch(muscle -> muscle.toLowerCase().contains(lowerQuery));

                    return matchesTitle || matchesMuscles;
                })
                .collect(Collectors.toList()));
        adapter.notifyDataSetChanged();
    }

    private void showWorkoutSelectionDialog() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("workouts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Snackbar.make(requireView(), "You have no workouts to upload", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    List<DocumentSnapshot> workoutDocs = new ArrayList<>();
                    List<String> workoutTitles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Boolean hasCommunity = doc.getBoolean("hasCommunity");
                        if (hasCommunity == null || !hasCommunity) {
                            workoutDocs.add(doc);
                            String title = doc.getString("title") != null ? doc.getString("title") : "Untitled";
                            workoutTitles.add(title);
                        }
                    }

                    if (workoutDocs.isEmpty()) {
                        Snackbar.make(requireView(), "All your workouts are already in the community", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Select a Workout to Upload")
                            .setItems(workoutTitles.toArray(new String[0]), (dialog, which) -> {
                                DocumentSnapshot selectedWorkoutDoc = workoutDocs.get(which);
                                uploadSelectedWorkoutToCommunity(selectedWorkoutDoc, uid);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(), "Failed to fetch your workouts", Snackbar.LENGTH_SHORT).show()
                );
    }

    private void uploadSelectedWorkoutToCommunity(DocumentSnapshot workoutDoc, String uid) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String username = userDoc.getString("username") != null ? userDoc.getString("username") : "User";

                    StorageReference iconRef = FirebaseStorage.getInstance().getReference().child(uid + "/userIcon");
                    iconRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String creatorIconUrl = uri.toString();

                        Map<String, Object> workoutData = new HashMap<>(workoutDoc.getData());
                        workoutData.put("creatorName", username);
                        workoutData.put("creatorIconUrl", creatorIconUrl);
                        workoutData.put("creatorUid", uid);

                        FirebaseFirestore.getInstance()
                                .collection("communityWorkouts")
                                .add(workoutData)
                                .addOnSuccessListener(communityRef -> {
                                    copyExercisesToCommunity(workoutDoc.getReference(), communityRef);
                                    workoutDoc.getReference().update("hasCommunity", true);
                                })
                                .addOnFailureListener(e ->
                                        Snackbar.make(requireView(), "Upload failed", Snackbar.LENGTH_SHORT).show()
                                );
                    }).addOnFailureListener(e ->
                            Snackbar.make(requireView(), "Failed to fetch profile image", Snackbar.LENGTH_SHORT).show()
                    );
                });
    }

    private void copyExercisesToCommunity(DocumentReference userWorkoutRef, DocumentReference communityWorkoutRef) {
        CollectionReference userExercises = userWorkoutRef.collection("exercises");
        CollectionReference communityExercises = communityWorkoutRef.collection("exercises");

        userExercises.get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        communityExercises.add(doc.getData());
                    }
                    Snackbar.make(requireView(), "Workout + exercises uploaded!", Snackbar.LENGTH_SHORT).show();
                    loadCommunityWorkouts();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(), "Failed to copy exercises", Snackbar.LENGTH_SHORT).show()
                );
    }

    private List<String> extractStringList(Object field) {
        List<String> result = new ArrayList<>();
        if (field instanceof List<?>) {
            for (Object obj : (List<?>) field) {
                if (obj instanceof String) {
                    result.add((String) obj);
                }
            }
        }
        return result;
    }
}
