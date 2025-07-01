package com.example.finalyearproject.Progress;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.Account.AchievementTracker;
import com.example.finalyearproject.R;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewGoalFragment extends Fragment {

    private String name;
    private String graphUrl;
    private String docId;
    private String unit;
    private double targetValue;

    private RecyclerView trackingRecyclerView;
    private TrackingAdapter adapter;
    private final List<ProgressModel.TrackingEntry> trackingList = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;
    private ImageView graphImage;
    private MaterialButton deleteButton;

    public ViewGoalFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            name = getArguments().getString("name");
            graphUrl = getArguments().getString("graphUrl");
            docId = getArguments().getString("docId");
            unit = getArguments().getString("unit");
            targetValue = getArguments().getDouble("targetValue");
        }

        TextView title = view.findViewById(R.id.goalTitle);
        graphImage = view.findViewById(R.id.goalGraphImage);
        deleteButton = view.findViewById(R.id.deleteGoalButton);
        deleteButton.setVisibility(View.GONE);

        title.setText(name);

        Glide.with(requireContext())
                .load(graphUrl)
                .into(graphImage);

        trackingRecyclerView = view.findViewById(R.id.trackingRecyclerView);
        trackingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TrackingAdapter(trackingList, unit);
        trackingRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabUpdateGoal);
        fab.setOnClickListener(v -> showAddTrackingDialog());

        deleteButton.setOnClickListener(v -> confirmAndDeleteGoal());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null && docId != null) {
            loadTrackingEntries();
        }
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

    private void loadTrackingEntries() {
        db.collection("users")
                .document(uid)
                .collection("progress")
                .document(docId)
                .collection("tracking")
                .get()
                .addOnSuccessListener(snapshot -> {
                    trackingList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Date date = doc.getDate("date");
                        Double value = doc.getDouble("value");
                        if (date != null && value != null) {
                            trackingList.add(new ProgressModel.TrackingEntry(date.getTime(), value));
                        }
                    }
                    trackingList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    adapter.notifyDataSetChanged();

                    deleteButton.setVisibility(View.VISIBLE);
                });
    }

    private void showAddTrackingDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_tracking_entry, null);
        TextInputEditText inputField = dialogView.findViewById(R.id.inputTrackingValue);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Progress Entry")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String input = inputField.getText().toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            double value = Double.parseDouble(input);
                            saveTrackingEntry(value);
                        } catch (NumberFormatException e) {
                            Snackbar.make(requireView(), "Invalid number", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(requireView(), "Value cannot be empty", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveTrackingEntry(double value) {
        if (uid == null || docId == null) return;

        Date now = new Date();

        Map<String, Object> entry = new HashMap<>();
        entry.put("value", value);
        entry.put("date", now);

        db.collection("users")
                .document(uid)
                .collection("progress")
                .document(docId)
                .collection("tracking")
                .add(entry)
                .addOnSuccessListener(docRef -> {
                    trackingList.add(0, new ProgressModel.TrackingEntry(now.getTime(), value));
                    adapter.notifyItemInserted(0);
                    trackingRecyclerView.scrollToPosition(0);
                    Snackbar.make(requireView(), "Entry added", Snackbar.LENGTH_SHORT).show();

                    regenerateAndUploadGraph();

                    View rootView = requireView();
                    AchievementTracker tracker = new AchievementTracker(db, uid, rootView);
                    tracker.checkOnTrackAchievement();
                    tracker.checkGoalGetterAchievement();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to save entry", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void regenerateAndUploadGraph() {
        db.collection("users")
                .document(uid)
                .collection("progress")
                .document(docId)
                .collection("tracking")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<DocumentSnapshot> docs = snapshot.getDocuments();
                    docs.sort(Comparator.comparing(doc -> doc.getDate("date")));

                    List<Entry> entries = new ArrayList<>();
                    List<Date> dates = new ArrayList<>();
                    int index = 0;

                    for (DocumentSnapshot doc : docs) {
                        Double value = doc.getDouble("value");
                        Date date = doc.getDate("date");
                        if (value != null && date != null) {
                            entries.add(new Entry(index++, value.floatValue()));
                            dates.add(date);
                        }
                    }

                    if (!entries.isEmpty()) {
                        Bitmap chart = ChartUtils.createProgressChart(requireContext(), entries, (float) targetValue, dates);
                        uploadChartToStorage(chart);
                    }
                });
    }

    private void uploadChartToStorage(Bitmap bitmap) {
        String path = uid + "/" + name + "/progress_graphs/" + docId + ".png";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("users")
                            .document(uid)
                            .collection("progress")
                            .document(docId)
                            .update("graphUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> {
                                Glide.with(requireContext())
                                        .load(uri.toString())
                                        .placeholder(R.drawable.white_placeholder)
                                        .error(R.drawable.white_placeholder)
                                        .into(graphImage);
                            });
                }));
    }

    private void confirmAndDeleteGoal() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to permanently delete this goal and all its entries?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteGoalFromFirestore())
                .show();
    }

    private void deleteGoalFromFirestore() {
        if (uid == null || docId == null) return;

        db.collection("users").document(uid)
                .collection("progress").document(docId)
                .collection("tracking").get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }

                    String imagePath = uid + "/" + name + "/progress_graphs/" + docId + ".png";
                    FirebaseStorage.getInstance().getReference().child(imagePath).delete();

                    db.collection("users").document(uid)
                            .collection("progress").document(docId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Snackbar.make(requireView(), "Goal deleted", Snackbar.LENGTH_SHORT).show();
                                requireActivity().onBackPressed();
                            })
                            .addOnFailureListener(e ->
                                    Snackbar.make(requireView(), "Failed to delete goal: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
                });
    }
}
