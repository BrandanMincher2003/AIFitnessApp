package com.example.finalyearproject.Progress;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalyearproject.R;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGoalFragment extends Fragment {

    private TextInputEditText inputGoalName, inputStartingValue, inputTargetValue, inputUnit;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputGoalName = view.findViewById(R.id.inputGoalName);
        inputStartingValue = view.findViewById(R.id.inputStartingValue);
        inputTargetValue = view.findViewById(R.id.inputTargetValue);
        inputUnit = view.findViewById(R.id.inputUnit);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        MaterialButton submitBtn = view.findViewById(R.id.btnSubmitGoal);
        submitBtn.setOnClickListener(v -> saveGoal());
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

    private void saveGoal() {
        String name = inputGoalName.getText().toString().trim();
        String unit = inputUnit.getText().toString().trim();
        String startText = inputStartingValue.getText().toString().trim();
        String targetText = inputTargetValue.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(unit) || TextUtils.isEmpty(startText) || TextUtils.isEmpty(targetText)) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double startingValue = Double.parseDouble(startText);
        double targetValue = Double.parseDouble(targetText);

        Map<String, Object> goal = new HashMap<>();
        goal.put("name", name);
        goal.put("startingValue", startingValue);
        goal.put("targetValue", targetValue);
        goal.put("unit", unit);

        db.collection("users").document(uid).collection("progress")
                .add(goal)
                .addOnSuccessListener(docRef -> {
                    Date now = new Date();
                    Map<String, Object> trackingEntry = new HashMap<>();
                    trackingEntry.put("value", startingValue);
                    trackingEntry.put("date", new Timestamp(now));

                    docRef.collection("tracking")
                            .add(trackingEntry)
                            .addOnSuccessListener(ref -> {
                                generateAndUploadChart(docRef, name, targetValue, startingValue, now);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to save tracking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save goal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void generateAndUploadChart(DocumentReference docRef, String goalName, double targetValue, double startingValue, Date date) {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, (float) startingValue));

        List<Date> dates = new ArrayList<>();
        dates.add(date);

        Bitmap chart = ChartUtils.createProgressChart(requireContext(), entries, (float) targetValue, dates);

        String path = uid + "/" + goalName + "/progress_graphs/" + docRef.getId() + ".png";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        chart.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    docRef.update("graphUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Goal created successfully", Toast.LENGTH_SHORT).show();
                                requireActivity().onBackPressed();
                            });
                }))
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload chart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
