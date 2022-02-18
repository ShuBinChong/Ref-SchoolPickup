package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText uFullName, uEmail, uPassword, uContactNo, uVehiclePlateNo;
    Button registerButton, backButton;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        uFullName = findViewById(R.id.fullName);
        uEmail = findViewById(R.id.email);
        uPassword = findViewById(R.id.password);
        uContactNo = findViewById(R.id.contactNo);
        uVehiclePlateNo = findViewById(R.id.vehiclePlateNo);
        registerButton = findViewById(R.id.registerBtn);
        backButton = findViewById(R.id.backBtn);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() != null) {
            userID = fAuth.getCurrentUser().getUid();
            DocumentReference documentReference = fStore.collection("Users").document(userID);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.getString("isAdmin") != null){
                        startActivity(new Intent(getApplicationContext(),AdminPage.class));
                        finish();
                    }
                    if(documentSnapshot.getString("isTeacher") != null){
                        startActivity(new Intent(getApplicationContext(),TeacherPage.class));
                        finish();
                    }
                    if(documentSnapshot.getString("isUser") != null){
                        startActivity(new Intent(getApplicationContext(),HomePage.class));
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    fAuth.signOut();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
            });
        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fullName = uFullName.getText().toString().trim();
                String email = uEmail.getText().toString().trim();
                String password = uPassword.getText().toString().trim();
                String contactNo = uContactNo.getText().toString().trim();
                String vehiclePlateNo = uVehiclePlateNo.getText().toString().trim();

                if (TextUtils.isEmpty(fullName)) {
                    uFullName.setError("Please enter your full name.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    uEmail.setError("Please enter your email address.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    uPassword.setError("Please enter your password.");
                    return;
                }

                if (password.length() < 8) {
                    uPassword.setError("Password must be at least 8 characters.");
                    return;
                }

                if (TextUtils.isEmpty(contactNo)) {
                    uContactNo.setError("Please enter your contact No.");
                    return;
                }

                if (TextUtils.isEmpty(vehiclePlateNo)) {
                    uVehiclePlateNo.setError("Please enter your vehicle plate No.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //User registration
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register.this, "User created successful", Toast.LENGTH_LONG).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Parents").child(userID);
                            databaseReference.setValue(true);
                            DocumentReference documentReference = fStore.collection("Users").document(userID);
                            Map<String,Object> userInfo = new HashMap<>();
                            userInfo.put("name",fullName);
                            userInfo.put("email",email);
                            userInfo.put("contact",contactNo);
                            userInfo.put("vehiclePlateNo",vehiclePlateNo);
                            userInfo.put("isUser","1");
                            documentReference.set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG", "onSuccess: User profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),HomePage.class));
                            finish();
                        }else {
                            Toast.makeText(Register.this, "Failed to create user. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

    }
}