package com.example.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText registrationEmail;
    private EditText registrationPassword;
    private EditText registrationConfirmPassword;
    private Button registrationButton;
    private Button registrationLoginButton;
    private ProgressBar registrationProgressBar;

    private FirebaseAuth mAuth;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registrationPassword = (EditText) findViewById(R.id.regPassword);
        registrationEmail = (EditText) findViewById(R.id.regEmail);
        registrationConfirmPassword = (EditText) findViewById(R.id.regConfirmPassword);
        registrationButton = (Button) findViewById(R.id.regBtn);
        registrationLoginButton = (Button) findViewById(R.id.loginRegestrationBtn);
        registrationProgressBar = (ProgressBar) findViewById(R.id.regProgressBar);
        registrationLoginButton = findViewById(R.id.regLoginBtn);

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registrationEmail.getText().toString();
                String password = registrationPassword.getText().toString();
                String confirmPassword = registrationConfirmPassword.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {

                    if (password.equals(confirmPassword)) {
                        registrationProgressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                                registrationProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Confirm Password and Password are different", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        registrationLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setupIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(setupIntent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
