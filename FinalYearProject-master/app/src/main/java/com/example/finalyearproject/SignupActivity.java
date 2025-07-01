package com.example.finalyearproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText emailInput, usernameInput, passwordInput, confirmPasswordInput;
    private TextInputLayout emailLayout, usernameLayout, passwordLayout, confirmPasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // Inputs
        emailLayout = findViewById(R.id.email_layout);
        usernameLayout = findViewById(R.id.username_layout);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);

        emailInput = findViewById(R.id.email_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);

        MaterialButton signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this::registerUser);

        TextView loginRedirect = findViewById(R.id.login_redirect);
        loginRedirect.setText(android.text.Html.fromHtml(getString(R.string.login_redirect_text)));
        loginRedirect.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void registerUser(View view) {
        clearErrors();

        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (!password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{6,}$")) {
            passwordLayout.setError("Include A-Z, 0-9, symbol, 6+ chars");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        if (!isValid) {
            Snackbar.make(view, "Please correct the errors above.", Snackbar.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            saveUserToFirestore(user.getUid(), email, username);
                                            uploadDefaultProfileIcon(user.getUid());
                                            Snackbar.make(view, "Verification email sent. Please verify before login.", Snackbar.LENGTH_LONG).show();
                                            mAuth.signOut();

                                            // Navigate to login screen after delay
                                            view.postDelayed(() -> {
                                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }, 2500);
                                        } else {
                                            Snackbar.make(view, "Failed to send verification email.", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Snackbar.make(view, "Sign up failed: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void clearErrors() {
        emailLayout.setError(null);
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private void saveUserToFirestore(String uid, String email, String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(uid);

        Map<String, Object> userData = new HashMap<>();
        userData.put("age", 0);
        userData.put("height", 0.0f);
        userData.put("weight", 0.0f);
        userData.put("bmi", 0.0f);
        userData.put("hypertension", false);
        userData.put("diabetes", false);
        userData.put("level", "");
        userData.put("goal", "");
        userData.put("type", "");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("username", username);
        userMap.put("hasCompletedQuestionnaire", false);
        userMap.put("userData", userData);

        userRef.set(userMap)
                .addOnSuccessListener(aVoid -> {
                    db.collection("achievements")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    Map<String, Object> achievementData = new HashMap<>(doc.getData());
                                    achievementData.put("hasAchieved", false);
                                    db.collection("users")
                                            .document(uid)
                                            .collection("achievements")
                                            .document(doc.getId())
                                            .set(achievementData);
                                }
                            });
                });
    }

    private void uploadDefaultProfileIcon(String uid) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(uid + "/userIcon");
        Uri defaultIconUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.ic_person_default);

        storageRef.putFile(defaultIconUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Optional: Log or handle success
                })
                .addOnFailureListener(e -> {
                    // Optional: Log or handle error
                });
    }
}
