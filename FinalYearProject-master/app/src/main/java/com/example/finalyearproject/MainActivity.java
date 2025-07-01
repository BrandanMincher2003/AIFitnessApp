package com.example.finalyearproject;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.finalyearproject.Notifications.NotificationHelper;
import com.example.finalyearproject.Notifications.TimelineNotificationScheduler;
import com.example.finalyearproject.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.example.finalyearproject.Notifications.TimelineDailyScheduler;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null && !notificationManager.areNotificationsEnabled()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                return; // ⛔️ Prevent scheduling until notifications are enabled
            }
        }

        // Set up the Material Toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Preload profile image
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        preloadProfileImage(uid);

        // Get the NavController from the FragmentContainerView
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();

        // Define top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_workout,
                R.id.navigation_progress,
                R.id.navigation_community,
                R.id.navigation_account
        ).build();

        // ✅ Proper setup for top app bar with navController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Setup bottom nav with navController
        NavigationBarView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void preloadProfileImage(String uid) {
        StorageReference ref = FirebaseStorage.getInstance().getReference(uid + "/userIcon");
        ref.getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(getApplicationContext()).load(uri).preload())
                .addOnFailureListener(e -> {
                    // You can log or handle fallback here if needed
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }



}
