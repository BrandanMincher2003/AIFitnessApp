package com.example.finalyearproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Questionnaire extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private AutoCompleteTextView ageInput, heightInput, weightInput, goalWeightInput;
    private TextInputLayout ageLayout, heightLayout, weightLayout, goalWeightLayout;
    private View rootView;

    private static final Map<String, Map<String, Object>> cachedWorkouts = new HashMap<>();
    private static final Map<String, List<Map<String, Object>>> cachedWorkoutExercises = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_questionnaire);

        rootView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ageInput = findViewById(R.id.age_input);
        heightInput = findViewById(R.id.height_input);
        weightInput = findViewById(R.id.weight_input);
        goalWeightInput = findViewById(R.id.goal_weight_input);

        ageLayout = findViewById(R.id.age_layout);
        heightLayout = findViewById(R.id.height_layout);
        weightLayout = findViewById(R.id.weight_layout);
        goalWeightLayout = findViewById(R.id.goal_weight_layout);

        setupDropdowns();
        setupInputTransitions();
        cacheAIWorkouts(); // cache on startup

        MaterialButton submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                callAIRecommendationAPI(); // now API call happens first
            }
        });
    }

    private void setupDropdowns() {
        String[] ageOptions = new String[83];
        String[] heightOptions = new String[86];
        String[] weightOptions = new String[111];
        for (int i = 0; i < ageOptions.length; i++) ageOptions[i] = String.valueOf(i + 16);
        for (int i = 0; i < heightOptions.length; i++) heightOptions[i] = String.valueOf(i + 140);
        for (int i = 0; i < weightOptions.length; i++) weightOptions[i] = String.valueOf(i + 40);

        setDropdown(R.id.age_input, R.id.age_layout, ageOptions);
        setDropdown(R.id.height_input, R.id.height_layout, heightOptions);
        setDropdown(R.id.weight_input, R.id.weight_layout, weightOptions);
        setDropdown(R.id.goal_weight_input, R.id.goal_weight_layout, weightOptions);

        setDropdown(R.id.sex_input, R.id.sex_layout, new String[]{"Male", "Female"});
        setDropdown(R.id.hypertension_input, R.id.hypertension_layout, new String[]{"No", "Yes"});
        setDropdown(R.id.diabetes_input, R.id.diabetes_layout, new String[]{"No", "Yes"});
        setDropdown(R.id.level_input, R.id.level_layout, new String[]{"Underweight", "Normal", "Overweight", "Obese"});
        setDropdown(R.id.goal_input, R.id.goal_layout, new String[]{"Weight Gain", "Weight Loss"});
    }

    private void setDropdown(int inputId, int layoutId, String[] options) {
        AutoCompleteTextView dropdown = findViewById(inputId);
        TextInputLayout layout = findViewById(layoutId);
        dropdown.setDropDownBackgroundResource(R.drawable.dropdown_background);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, options);
        dropdown.setAdapter(adapter);
        dropdown.setInputType(0);
        dropdown.setKeyListener(null);
        dropdown.setFocusable(false);
        dropdown.setOnClickListener(v -> dropdown.showDropDown());
        dropdown.setOnItemClickListener((parent, view, position, id) -> layout.setError(null));
    }

    private void setupInputTransitions() {
        TextView.OnEditorActionListener hideKeyboardListener = (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        };

        ageInput.setOnEditorActionListener(hideKeyboardListener);
        heightInput.setOnEditorActionListener(hideKeyboardListener);
        weightInput.setOnEditorActionListener(hideKeyboardListener);
        goalWeightInput.setOnEditorActionListener(hideKeyboardListener);
    }

    private boolean validateInputs() {
        boolean isValid = true;
        if (TextUtils.isEmpty(ageInput.getText()) || !TextUtils.isDigitsOnly(ageInput.getText())) {
            ageLayout.setError("Required"); isValid = false;
        } else ageLayout.setError(null);
        if (TextUtils.isEmpty(heightInput.getText())) {
            heightLayout.setError("Required"); isValid = false;
        } else heightLayout.setError(null);
        if (TextUtils.isEmpty(weightInput.getText())) {
            weightLayout.setError("Required"); isValid = false;
        } else weightLayout.setError(null);
        if (TextUtils.isEmpty(goalWeightInput.getText())) {
            goalWeightLayout.setError("Required"); isValid = false;
        } else goalWeightLayout.setError(null);

        isValid &= validateDropdown(R.id.sex_input, R.id.sex_layout);
        isValid &= validateDropdown(R.id.hypertension_input, R.id.hypertension_layout);
        isValid &= validateDropdown(R.id.diabetes_input, R.id.diabetes_layout);
        isValid &= validateDropdown(R.id.level_input, R.id.level_layout);
        isValid &= validateDropdown(R.id.goal_input, R.id.goal_layout);
        return isValid;
    }

    private boolean validateDropdown(int inputId, int layoutId) {
        AutoCompleteTextView dropdown = findViewById(inputId);
        TextInputLayout layout = findViewById(layoutId);
        if (TextUtils.isEmpty(dropdown.getText())) {
            layout.setError("Required"); return false;
        } else { layout.setError(null); return true; }
    }

    private void cacheAIWorkouts() {
        db.collection("aiworkouts").get().addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String title = doc.getString("title");
                if (title != null) {
                    cachedWorkouts.put(title, doc.getData());
                    doc.getReference().collection("exercises").get().addOnSuccessListener(exSnap -> {
                        List<Map<String, Object>> exercises = new ArrayList<>();
                        for (DocumentSnapshot exDoc : exSnap.getDocuments()) {
                            exercises.add(exDoc.getData());
                        }
                        cachedWorkoutExercises.put(title, exercises);
                    });
                }
            }
        });
    }

    private void callAIRecommendationAPI() {
        String uid = auth.getCurrentUser().getUid();

        int age = Integer.parseInt(ageInput.getText().toString());
        double height = Double.parseDouble(heightInput.getText().toString());
        double weight = Double.parseDouble(weightInput.getText().toString());
        double goalWeight = Double.parseDouble(goalWeightInput.getText().toString());
        double bmi = Math.round((weight / Math.pow(height / 100.0, 2)) * 10.0) / 10.0;

        String sex = ((AutoCompleteTextView) findViewById(R.id.sex_input)).getText().toString();
        boolean hypertension = ((AutoCompleteTextView) findViewById(R.id.hypertension_input)).getText().toString().equals("Yes");
        boolean diabetes = ((AutoCompleteTextView) findViewById(R.id.diabetes_input)).getText().toString().equals("Yes");
        String level = ((AutoCompleteTextView) findViewById(R.id.level_input)).getText().toString();
        String goal = ((AutoCompleteTextView) findViewById(R.id.goal_input)).getText().toString();

        Map<String, Object> userData = new HashMap<>();
        userData.put("age", age); userData.put("height", height); userData.put("weight", weight);
        userData.put("goalWeight", goalWeight); userData.put("bmi", bmi);
        userData.put("sex", sex); userData.put("hypertension", hypertension);
        userData.put("diabetes", diabetes); userData.put("level", level); userData.put("goal", goal);

        JSONObject body = new JSONObject();
        try {
            body.put("Age", age);
            body.put("Height", height);
            body.put("Weight", weight);
            body.put("BMI", bmi);
            body.put("Sex", sex);
            body.put("Hypertension", hypertension ? "Yes" : "No");
            body.put("Diabetes", diabetes ? "Yes" : "No");
            body.put("Level", level);
            body.put("Fitness Goal", goal);
        } catch (JSONException e) {
            Snackbar.make(rootView, "JSON error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://finalyearproject-a01f8.nw.r.appspot.com/predict",
                body,
                response -> {
                    try {
                        String recommended = response.getString("recommended_exercise").trim().replaceAll("\\s+", " ");
                        List<String> workoutTitles;
                        if (recommended.equalsIgnoreCase("Squats, deadlifts, bench presses, and overhead presses")) {
                            workoutTitles = Arrays.asList("AI Workout - Strength and Muscle Building");
                        } else if (recommended.equalsIgnoreCase("Squats, yoga, deadlifts, bench presses, and overhead presses")) {
                            workoutTitles = Arrays.asList("AI Workout - Strength and Muscle Building", "AI Workout - Stretching and Yoga");
                        } else if (recommended.equalsIgnoreCase("Brisk walking, cycling, swimming, running , or dancing.")) {
                            workoutTitles = Arrays.asList("AI Workout - Cardio", "AI Workout - Swimming");
                        } else if (recommended.equalsIgnoreCase("Walking, Yoga, Swimming")) {
                            workoutTitles = Arrays.asList("AI Workout - Light Cardio", "AI Workout - Stretching and Yoga");
                        } else if (recommended.equalsIgnoreCase("brisk walking, cycling, swimming, or dancing.")) {
                            workoutTitles = Arrays.asList("AI Workout - Light Cardio", "AI Workout - Swimming");
                        } else {
                            workoutTitles = null;
                        }

                        submitToFirestore(uid, userData, workoutTitles);
                    } catch (JSONException e) {
                        Snackbar.make(rootView, "Response Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                },
                error -> Snackbar.make(rootView, "Volley error: " + error.getMessage(), Snackbar.LENGTH_LONG).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", "brandan-api-key");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private void submitToFirestore(String uid, Map<String, Object> userData, List<String> workoutTitles) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("userData", userData);
        updates.put("hasCompletedQuestionnaire", true);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(unused -> {
                    if (workoutTitles != null && !workoutTitles.isEmpty()) {
                        copyAIWorkouts(uid, workoutTitles, userData);
                    } else {
                        goToMain();
                    }
                });
    }

    private void copyAIWorkouts(String uid, List<String> titles, Map<String, Object> userData) {
        for (String title : titles) {
            Map<String, Object> workoutData = cachedWorkouts.get(title);
            List<Map<String, Object>> exercises = cachedWorkoutExercises.get(title);
            if (workoutData != null && exercises != null) {
                db.collection("users").document(uid).collection("workouts")
                        .add(workoutData)
                        .addOnSuccessListener(userWorkoutRef -> {
                            for (Map<String, Object> ex : exercises) {
                                userWorkoutRef.collection("exercises").add(ex);
                            }
                        });
            }
        }

        double weight = (double) userData.get("weight");
        double goalWeight = (double) userData.get("goalWeight");

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("name", "Weight Goal");
        progressData.put("startingValue", weight);
        progressData.put("targetValue", goalWeight);
        progressData.put("unit", "kg");

        db.collection("users").document(uid).collection("progress")
                .add(progressData)
                .addOnSuccessListener(progressDocRef -> {
                    Map<String, Object> trackingData = new HashMap<>();
                    trackingData.put("value", weight);
                    trackingData.put("date", Timestamp.now());

                    progressDocRef.collection("tracking")
                            .add(trackingData)
                            .addOnSuccessListener(trackingDocRef -> goToMain());
                });
    }

    private void goToMain() {
        Snackbar.make(rootView, "Questionnaire submitted", Snackbar.LENGTH_LONG).show();
        startActivity(new Intent(Questionnaire.this, MainActivity.class));
        finish();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
