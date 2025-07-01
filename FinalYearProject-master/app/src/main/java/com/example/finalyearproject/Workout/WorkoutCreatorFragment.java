package com.example.finalyearproject.Workout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutCreatorFragment extends Fragment {

    private static final List<String> TYPES = Arrays.asList("Strength", "Muscle Building", "Cardio", "Flexibility", "HIIT", "Endurance");
    private static final List<String> EQUIPMENT_OPTIONS = Arrays.asList("Dumbbells", "Barbell", "Machine", "Bodyweight", "Kettlebell", "Bench", "Bands");
    private static final List<String> MUSCLE_OPTIONS = Arrays.asList("Chest", "Back", "Shoulders", "Biceps", "Triceps", "Quads", "Hamstrings", "Calves", "Core", "Glutes");

    private AutoCompleteTextView typeInput, equipmentInput, musclesInput;
    private List<String> selectedEquipment = new ArrayList<>();
    private List<String> selectedMuscles = new ArrayList<>();
    private String selectedImageUrl = null;

    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;

    public WorkoutCreatorFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_creator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText titleInput = view.findViewById(R.id.title_input);
        TextInputEditText descriptionInput = view.findViewById(R.id.description_input);
        typeInput = view.findViewById(R.id.type_input);
        equipmentInput = view.findViewById(R.id.equipment_input);
        musclesInput = view.findViewById(R.id.muscles_input);
        MaterialButton saveButton = view.findViewById(R.id.save_button);

        imageRecyclerView = view.findViewById(R.id.image_recycler);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        List<String> imageUrls = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageUrls, imageUrl -> selectedImageUrl = imageUrl);
        imageRecyclerView.setAdapter(imageAdapter);

        loadWorkoutImagesFromStorage(imageUrls, view);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, TYPES);
        typeInput.setAdapter(typeAdapter);

        equipmentInput.setFocusable(false);
        equipmentInput.setOnClickListener(v -> showMultiSelectDialog("Select Equipment", EQUIPMENT_OPTIONS, selectedEquipment, equipmentInput));

        musclesInput.setFocusable(false);
        musclesInput.setOnClickListener(v -> showMultiSelectDialog("Select Target Muscles", MUSCLE_OPTIONS, selectedMuscles, musclesInput));

        saveButton.setOnClickListener(v -> {
            String title = getText(titleInput);
            String description = getText(descriptionInput);
            String type = getText(typeInput);

            if (title.isEmpty() || description.isEmpty() || type.isEmpty()
                    || selectedEquipment.isEmpty() || selectedMuscles.isEmpty() || selectedImageUrl == null) {
                Snackbar.make(view, "Please fill in all fields and select an image", Snackbar.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> workout = new HashMap<>();
            workout.put("title", title);
            workout.put("description", description);
            workout.put("type", type);
            workout.put("equipment", selectedEquipment);
            workout.put("targetMuscles", selectedMuscles);
            workout.put("imageUrl", selectedImageUrl);
            workout.put("timestamp", FieldValue.serverTimestamp());

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("workouts")
                    .add(workout)
                    .addOnSuccessListener(documentReference -> {
                        String workoutId = documentReference.getId();
                        Snackbar.make(view, "Workout saved", Snackbar.LENGTH_SHORT).show();

                        new AchievementTracker(FirebaseFirestore.getInstance(), uid, requireView())
                                .checkBlueprintMasterAchievement();

                        Bundle bundle = new Bundle();
                        bundle.putString("workoutId", workoutId);
                        bundle.putString("workoutTitle", title);
                        Navigation.findNavController(view).navigate(R.id.workoutCreatorFragment2, bundle);
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(view, "Failed to save workout: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    });
        });
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

    private void loadWorkoutImagesFromStorage(List<String> imageUrls, View view) {
        StorageReference imagesRef = FirebaseStorage.getInstance().getReference("global_images");

        imagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference ref : listResult.getItems()) {
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageUrls.add(uri.toString());
                            imageAdapter.notifyDataSetChanged();
                        });
                    }
                })
                .addOnFailureListener(e ->
                        Snackbar.make(view, "Failed to load images", Snackbar.LENGTH_SHORT).show());
    }

    private void showMultiSelectDialog(String title, List<String> options, List<String> selectedItems, AutoCompleteTextView targetView) {
        boolean[] checked = new boolean[options.size()];
        for (int i = 0; i < options.size(); i++) {
            checked[i] = selectedItems.contains(options.get(i));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMultiChoiceItems(options.toArray(new String[0]), checked, (dialog, which, isChecked) -> {
                    String item = options.get(which);
                    if (isChecked) {
                        if (!selectedItems.contains(item)) selectedItems.add(item);
                    } else {
                        selectedItems.remove(item);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> targetView.setText(TextUtils.join(", ", selectedItems)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getText(TextView editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
