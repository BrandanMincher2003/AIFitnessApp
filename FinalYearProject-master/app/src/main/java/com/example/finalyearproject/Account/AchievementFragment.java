package com.example.finalyearproject.Account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AchievementFragment extends Fragment {

    private RecyclerView achievedRecyclerView, notAchievedRecyclerView;
    private AchievementAdapter achievedAdapter, notAchievedAdapter;
    private final List<AchievementItem> achievedList = new ArrayList<>();
    private final List<AchievementItem> notAchievedList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievement, container, false);

        achievedRecyclerView = view.findViewById(R.id.achievedRecyclerView);
        notAchievedRecyclerView = view.findViewById(R.id.notAchievedRecyclerView);

        // Set horizontal layout managers
        achievedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        notAchievedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        achievedAdapter = new AchievementAdapter(getContext(), achievedList);
        notAchievedAdapter = new AchievementAdapter(getContext(), notAchievedList);

        achievedRecyclerView.setAdapter(achievedAdapter);
        notAchievedRecyclerView.setAdapter(notAchievedAdapter);

        loadAchievements();

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
            NavigationBarView nav = getActivity().findViewById(R.id.nav_view); // ✅ Correct ID
            if (nav != null) {
                nav.setVisibility(View.GONE);
            }
        }
    }

    private void showBottomNav() {
        if (getActivity() != null) {
            NavigationBarView nav = getActivity().findViewById(R.id.nav_view); // ✅ Correct ID
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadAchievements() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("achievements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    achievedList.clear();
                    notAchievedList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        AchievementItem item = doc.toObject(AchievementItem.class);
                        if (item != null) {
                            Boolean achieved = doc.getBoolean("hasAchieved");
                            item.setAchieved(achieved != null && achieved);
                            if (item.isHasAchieved()) {
                                achievedList.add(item);
                            } else {
                                notAchievedList.add(item);
                            }
                        }
                    }

                    achievedAdapter.notifyDataSetChanged();
                    notAchievedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("AchievementFragment", "Error loading achievements", e));
    }
}
