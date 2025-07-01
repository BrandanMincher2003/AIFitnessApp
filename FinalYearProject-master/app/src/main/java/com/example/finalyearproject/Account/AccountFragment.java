package com.example.finalyearproject.Account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.finalyearproject.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccountFragment extends Fragment {

    private RecyclerView achievementsRecyclerView, timelineRecyclerView;
    private AchievementAdapter adapter;
    private TimelineAdapter timelineAdapter;

    private final List<AchievementItem> achievementList = new ArrayList<>();
    private final List<TimelineItem> timelineList = new ArrayList<>();

    private TextView usernameText, emailText;
    private ImageView profileImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Find views
        achievementsRecyclerView = view.findViewById(R.id.achievementsRecyclerView);
        timelineRecyclerView = view.findViewById(R.id.timelineRecyclerView);
        usernameText = view.findViewById(R.id.usernameText);
        emailText = view.findViewById(R.id.emailText);
        profileImage = view.findViewById(R.id.profileImage);
        ImageButton arrowButton = view.findViewById(R.id.arrowButton);

        // Setup Achievements RecyclerView
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new AchievementAdapter(getContext(), achievementList);
        achievementsRecyclerView.setAdapter(adapter);

        // Setup Timeline RecyclerView
        timelineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        timelineAdapter = new TimelineAdapter(timelineList);
        timelineRecyclerView.setAdapter(timelineAdapter);

        // Navigate to profile options
        view.findViewById(R.id.profileOptionsButton).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.profileOptionsFragment));

        // Navigate to full achievement screen
        arrowButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.achievementFragment));

        loadAchievementsFromFirestore();
        loadTimelineFromFirestore();
        loadUserData();
        loadProfileIcon();

        return view;
    }

    private void loadProfileIcon() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference ref = FirebaseStorage.getInstance().getReference(userId + "/userIcon");

        ref.getDownloadUrl()
                .addOnSuccessListener(this::loadImageFromUrl)
                .addOnFailureListener(e -> profileImage.setImageResource(R.drawable.ic_person));
    }

    private void loadImageFromUrl(android.net.Uri uri) {
        if (!isAdded()) return;

        Glide.with(this)
                .load(uri)
                .circleCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_person)
                .into(profileImage);
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        emailText.setText(email != null ? email : "Unknown email");

                        String username = documentSnapshot.getString("username");
                        usernameText.setText(username != null ? username : "User");
                    }
                })
                .addOnFailureListener(e -> Log.e("UserData", "Failed to fetch user", e));
    }

    private void loadAchievementsFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("achievements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AchievementItem> achieved = new ArrayList<>();
                    List<AchievementItem> notAchieved = new ArrayList<>();

                    achievementList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        AchievementItem item = doc.toObject(AchievementItem.class);
                        if (item != null) {
                            Boolean hasAchieved = doc.getBoolean("hasAchieved");
                            item.setAchieved(hasAchieved != null && hasAchieved);

                            if (item.isHasAchieved()) {
                                achieved.add(item);
                            } else {
                                notAchieved.add(item);
                            }
                        }
                    }

                    // Achieved ones first
                    achievementList.addAll(achieved);
                    achievementList.addAll(notAchieved);

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Achievements", "Error fetching user achievements", e));
    }

    private void loadTimelineFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("timeline")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    timelineList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        String workoutName = doc.getString("workoutName");
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        if (workoutName != null && timestamp != null) {
                            Date date = timestamp.toDate();
                            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
                            timelineList.add(new TimelineItem(workoutName, formattedDate));
                        }
                    }
                    timelineAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Timeline", "Error loading timeline", e));
    }
}
