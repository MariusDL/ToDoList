package com.marius.todo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText email, password;
    private Button registerButton;
    private TextView goToLogin;

    private FirebaseAuth mAuth;

    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        toolbar = findViewById(R.id.registrationToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");

        email = findViewById(R.id.registrationEmail);
        password = findViewById(R.id.registrationPassword);
        registerButton = findViewById(R.id.registrationButton);
        goToLogin = findViewById(R.id.goToLoginText);

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailTxt = email.getText().toString().trim();
                String passwordTxt = password.getText().toString().trim();

                if(TextUtils.isEmpty(emailTxt)){
                    email.setError("Please enter your email");
                    return;
                }
                if(TextUtils.isEmpty(passwordTxt)){
                    password.setError("Please enter your password");
                    return;
                } else {
                    loader.setMessage("Registration in progress");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    mAuth.createUserWithEmailAndPassword(emailTxt, passwordTxt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                                startActivity(intent);
                                loader.dismiss();
                                finish();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(RegistrationActivity.this, "Registration failed: " + error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }

            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}