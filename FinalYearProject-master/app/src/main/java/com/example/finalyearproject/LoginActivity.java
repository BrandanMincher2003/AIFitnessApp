package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private TextInputLayout emailLayout, passwordLayout;
    private SwitchMaterial rememberSwitch;
    private FirebaseAuth mAuth;

    private static final String PREFS_NAME = "loginPrefs";
    private static final String KEY_REMEMBER = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View rootView = findViewById(R.id.main); // For snackbar anchor

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // UI Elements
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        rememberSwitch = findViewById(R.id.remember_switch);
        Button loginButton = findViewById(R.id.login_button);
        TextView signupRedirect = findViewById(R.id.signup_redirect_text);
        signupRedirect.setText(android.text.Html.fromHtml(getString(R.string.signup_redirect_text)));

        // Shared Preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean remember = prefs.getBoolean(KEY_REMEMBER, false);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Auto login if remembered and verified
        if (remember && currentUser != null && currentUser.isEmailVerified()) {
            redirectBasedOnQuestionnaire(currentUser.getUid());
            return;
        }

        // Set toggle state
        rememberSwitch.setChecked(remember);

        // Redirect to Sign Up
        signupRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        // Login logic
        loginButton.setOnClickListener(v -> {
            clearErrors();

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            boolean isValid = true;

            if (TextUtils.isEmpty(email)) {
                emailLayout.setError("Email is required");
                isValid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.setError("Invalid email format");
                isValid = false;
            }

            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError("Password is required");
                isValid = false;
            }

            if (!isValid) {
                Snackbar.make(rootView, "Please fix the errors above.", Snackbar.LENGTH_LONG).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                prefs.edit().putBoolean(KEY_REMEMBER, rememberSwitch.isChecked()).apply();
                                redirectBasedOnQuestionnaire(user.getUid());
                            } else {
                                Snackbar.make(rootView, "Please verify your email before logging in.", Snackbar.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        } else {
                            Snackbar.make(rootView,
                                    "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    private void redirectBasedOnQuestionnaire(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean hasCompleted = documentSnapshot.getBoolean("hasCompletedQuestionnaire");

                    if (hasCompleted != null && hasCompleted) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, Questionnaire.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(findViewById(R.id.main), "Failed to retrieve user data.", Snackbar.LENGTH_SHORT).show();
                });
    }
}
