package com.example.finalyearproject.Account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.finalyearproject.LoginActivity;
import com.example.finalyearproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileOptionsFragment extends Fragment {

    private TextInputEditText usernameInput, weightInput, heightInput;
    private AutoCompleteTextView bodyTypeDropdown, goalDropdown;
    private ImageButton usernameEditBtn, weightEditBtn, heightEditBtn, bodyTypeEditBtn, goalEditBtn;
    private ImageView profileIcon;
    private View changeProfileIconBtn;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_options, container, false);

        usernameInput = view.findViewById(R.id.usernameInput);
        weightInput = view.findViewById(R.id.weightInput);
        heightInput = view.findViewById(R.id.heightInput);
        bodyTypeDropdown = view.findViewById(R.id.bodyTypeDropdown);
        goalDropdown = view.findViewById(R.id.goalDropdown);

        usernameEditBtn = view.findViewById(R.id.usernameEdit);
        weightEditBtn = view.findViewById(R.id.weightEdit);
        heightEditBtn = view.findViewById(R.id.heightEdit);
        bodyTypeEditBtn = view.findViewById(R.id.bodyTypeEdit);
        goalEditBtn = view.findViewById(R.id.goalEdit);

        profileIcon = view.findViewById(R.id.profileIcon);
        changeProfileIconBtn = view.findViewById(R.id.changeProfileIconBtn);
        changeProfileIconBtn.setOnClickListener(v -> openImagePicker());

        View requestDataButton = view.findViewById(R.id.requestDataButton);
        requestDataButton.setOnClickListener(v -> exportUserDocument());

        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        changePasswordButton.setOnClickListener(v -> sendPasswordResetEmail());
        logoutButton.setOnClickListener(v -> logoutUser());

        ArrayAdapter<CharSequence> bodyTypeAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.body_type_options, android.R.layout.simple_dropdown_item_1line);
        bodyTypeDropdown.setAdapter(bodyTypeAdapter);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.goal_options, android.R.layout.simple_dropdown_item_1line);
        goalDropdown.setAdapter(goalAdapter);

        loadUserData();
        loadProfileIcon();

        usernameEditBtn.setOnClickListener(v -> enableField(usernameInput));
        weightEditBtn.setOnClickListener(v -> enableField(weightInput));
        heightEditBtn.setOnClickListener(v -> enableField(heightInput));
        bodyTypeEditBtn.setOnClickListener(v -> enableDropdown(bodyTypeDropdown));
        goalEditBtn.setOnClickListener(v -> enableDropdown(goalDropdown));

        setupEditorSave(usernameInput, "username");
        setupEditorSave(weightInput, "userData.weight");
        setupEditorSave(heightInput, "userData.height");

        bodyTypeDropdown.setOnItemClickListener((parent, view12, position, id) -> {
            saveField("userData.level", bodyTypeDropdown.getText().toString());
            bodyTypeDropdown.setEnabled(false);
        });

        goalDropdown.setOnItemClickListener((parent, view13, position, id) -> {
            saveField("userData.goal", goalDropdown.getText().toString());
            goalDropdown.setEnabled(false);
        });

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

    private void sendPasswordResetEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(unused -> showSnackbar("Password reset email sent to " + user.getEmail()))
                    .addOnFailureListener(e -> showSnackbar("Failed to send email: " + e.getMessage()));
        } else {
            showSnackbar("No user email found.");
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void exportUserDocument() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        writeExportToFile(documentSnapshot);
                    } else {
                        showSnackbar("No data found for export");
                    }
                })
                .addOnFailureListener(e -> showSnackbar("Failed to export: " + e.getMessage()));
    }

    private void writeExportToFile(DocumentSnapshot doc) {
        try {
            Map<String, Object> data = doc.getData();
            if (data == null) return;

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = "myDataExport_" + timestamp + ".json";

            File downloadsDir = new File(android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS).toString());
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            File file = new File(downloadsDir, fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();

            showSnackbar("Saved to Downloads/" + fileName);
        } catch (Exception e) {
            showSnackbar("Error saving file: " + e.getMessage());
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadProfileIcon(imageUri);
        }
    }

    private void uploadProfileIcon(Uri uri) {
        if (uri != null) {
            StorageReference ref = FirebaseStorage.getInstance().getReference(userId + "/userIcon");
            ref.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(this::loadImageFromUrl))
                    .addOnFailureListener(e -> showSnackbar("Upload failed: " + e.getMessage()));
        }
    }

    private void loadProfileIcon() {
        StorageReference ref = FirebaseStorage.getInstance().getReference(userId + "/userIcon");
        ref.getDownloadUrl()
                .addOnSuccessListener(this::loadImageFromUrl)
                .addOnFailureListener(e -> profileIcon.setImageResource(R.drawable.ic_person));
    }

    private void loadImageFromUrl(Uri uri) {
        if (!isAdded()) return;
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_person)
                .into(profileIcon);
    }

    private void enableField(TextInputEditText input) {
        input.setEnabled(true);
        input.requestFocus();
        if (input.getText() != null) {
            input.setSelection(input.getText().length());
        }
    }

    private void enableDropdown(AutoCompleteTextView dropdown) {
        dropdown.setEnabled(true);
        dropdown.requestFocus();
        dropdown.showDropDown();
    }

    private void setupEditorSave(TextInputEditText input, String key) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && input.isEnabled()) {
                saveField(key, input.getText() != null ? input.getText().toString().trim() : "");
                input.setEnabled(false);
            }
        });

        input.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveField(key, input.getText() != null ? input.getText().toString().trim() : "");
                input.setEnabled(false);
                hideKeyboard(input);
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Object username = doc.get("username");
                        if (username != null) usernameInput.setText(String.valueOf(username));

                        Map<String, Object> userData = (Map<String, Object>) doc.get("userData");
                        if (userData != null) {
                            Object weight = userData.get("weight");
                            Object height = userData.get("height");
                            Object level = userData.get("level");
                            Object goal = userData.get("goal");

                            if (weight != null) weightInput.setText(String.valueOf(weight));
                            if (height != null) heightInput.setText(String.valueOf(height));
                            if (level != null) bodyTypeDropdown.setText(String.valueOf(level), false);
                            if (goal != null) goalDropdown.setText(String.valueOf(goal), false);
                        }
                    }
                });
    }

    private void saveField(String key, String value) {
        Map<String, Object> updates = new HashMap<>();

        if (key.equals("username")) {
            updates.put("username", value);
        } else if (key.contains("weight") || key.contains("height")) {
            updates.put(key, parseNumber(value));
        } else {
            updates.put(key, value);
        }

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(unused -> showSnackbar("Saved"))
                .addOnFailureListener(e -> showSnackbar("Save failed"));
    }

    private Double parseNumber(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }
}
