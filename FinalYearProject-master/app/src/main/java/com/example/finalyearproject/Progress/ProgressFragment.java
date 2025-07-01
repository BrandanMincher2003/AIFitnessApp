package com.example.finalyearproject.Progress;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ProgressFragment extends Fragment {

    private RecyclerView progressRecyclerView;
    private FloatingActionButton addFab;
    private ProgressAdapter adapter;
    private final List<ProgressModel> progressList = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressRecyclerView = view.findViewById(R.id.progressRecyclerView);
        addFab = view.findViewById(R.id.addProgressFab);

        progressRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProgressAdapter(requireContext(), progressList, model -> {
            Bundle bundle = new Bundle();
            bundle.putString("docId", model.getDocId());
            bundle.putString("name", model.getName());
            bundle.putString("unit", model.getUnit());
            bundle.putDouble("startingValue", model.getStartingValue());
            bundle.putDouble("targetValue", model.getTargetValue());
            bundle.putString("graphUrl", model.getGraphUrl());

            Navigation.findNavController(view).navigate(R.id.ViewGoalFragment, bundle);
        });

        progressRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null) {
            loadProgressGoals();
        }

        addFab.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.addGoalFragment));
    }

    private void loadProgressGoals() {
        db.collection("users").document(uid).collection("progress")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressList.clear();

                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    for (DocumentSnapshot doc : documents) {
                        ProgressModel model = new ProgressModel(
                                doc.getString("name"),
                                doc.getDouble("startingValue"),
                                doc.getDouble("targetValue"),
                                doc.getString("unit"),
                                doc.getId(),
                                doc.getString("graphUrl")
                        );
                        progressList.add(model);
                        generateAndUploadGraph(model);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("ProgressFragment", "Failed to load progress goals", e));
    }

    private void generateAndUploadGraph(ProgressModel model) {
        CollectionReference trackingRef = db.collection("users")
                .document(uid)
                .collection("progress")
                .document(model.getDocId())
                .collection("tracking");

        trackingRef.get().addOnSuccessListener(snapshot -> {
            List<Entry> entries = new ArrayList<>();
            List<Date> dates = new ArrayList<>();

            List<DocumentSnapshot> docs = snapshot.getDocuments();
            docs.sort(Comparator.comparing(doc -> doc.getDate("date")));

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
                Bitmap chart = ChartUtils.createProgressChart(requireContext(), entries, (float) model.getTargetValue(), dates);
                uploadChartToStorage(chart, model.getName(), model.getDocId());
            }
        });
    }

    private void uploadChartToStorage(Bitmap bitmap, String goalName, String docId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        String path = uid + "/" + goalName + "/progress_graphs/" + docId + ".png";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

        storageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("UploadGraph", "Graph uploaded for " + docId);
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        db.collection("users")
                                .document(uid)
                                .collection("progress")
                                .document(docId)
                                .update("graphUrl", uri.toString())
                                .addOnSuccessListener(aVoid ->
                                        Log.d("UploadGraph", "Graph URL saved to Firestore"))
                                .addOnFailureListener(e ->
                                        Log.e("UploadGraph", "Failed to save URL: " + e.getMessage()));
                    });
                })
                .addOnFailureListener(e ->
                        Log.e("UploadGraph", "Failed to upload graph: " + e.getMessage()));
    }
}
