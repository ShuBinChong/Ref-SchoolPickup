package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddTeacher extends AppCompatActivity {

    DrawerLayout drawerLayout;
    EditText tFullName, tEmail, tPassword, tContactNo;
    Button addButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String teacherID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        tFullName = findViewById(R.id.fullName);
        tEmail = findViewById(R.id.email);
        tPassword = findViewById(R.id.password);
        tContactNo = findViewById(R.id.contactNo);
        addButton = findViewById(R.id.addBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fullName = tFullName.getText().toString().trim();
                String email = tEmail.getText().toString().trim();
                String password = tPassword.getText().toString().trim();
                String contactNo = tContactNo.getText().toString().trim();

                if (TextUtils.isEmpty(fullName)) {
                    tFullName.setError("Please enter teacher full name.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    tEmail.setError("Please enter teacher email.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    tPassword.setError("Please enter password.");
                    return;
                }

                if (password.length() < 8) {
                    tPassword.setError("Password must be at least 8 characters.");
                    return;
                }

                if (TextUtils.isEmpty(contactNo)) {
                    tContactNo.setError("Please enter teacher contact No.");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddTeacher.this, "Teacher created successful", Toast.LENGTH_LONG).show();
                            teacherID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("Users").document(teacherID);
                            Map<String, Object> teacherInfo = new HashMap<>();
                            teacherInfo.put("name", fullName);
                            teacherInfo.put("email", email);
                            teacherInfo.put("contact", contactNo);
                            teacherInfo.put("isTeacher", "1");
                            teacherInfo.put("active", "1");
                            documentReference.set(teacherInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddTeacher.this, "Teacher profile created successful", Toast.LENGTH_LONG).show();
                                    Log.d("TAG", "onSuccess: Teacher profile is created for " + teacherID);
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Teachers").child(teacherID);
                                    databaseReference.setValue(true);
                                    fAuth.signOut();
                                    String aEmail = "admin@gmail.com";
                                    String aPassword = "admin123";
                                    fAuth.signInWithEmailAndPassword(aEmail,aPassword);
                                    startActivity(new Intent(getApplicationContext(),AddTeacher.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddTeacher.this, "Fail to create teacher profile", Toast.LENGTH_LONG).show();
                                    Log.d("TAG", "onFailure: " + e.toString());
                                    fAuth.signOut();
                                    String aEmail = "admin@gmail.com";
                                    String aPassword = "admin123";
                                    fAuth.signInWithEmailAndPassword(aEmail,aPassword);
                                    startActivity(new Intent(getApplicationContext(),AddTeacher.class));
                                    finish();
                                }
                            });
                        } else {
                            Toast.makeText(AddTeacher.this, "Failed to create teacher. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    public void ClickMenu(View view){
        AdminPage.openDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        AdminPage.redirectActivity(this,AdminPage.class);
    }

    public void ClickAddStudent(View view){
        AdminPage.redirectActivity(this,AddStudent.class);
    }

    public void ClickAddTeacher(View view){
        recreate();
    }

    public void ClickViewTimetable(View view){
        AdminPage.redirectActivity(this,AdminViewTimetable.class);
    }

    public void ClickLogout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AdminPage.closeDrawer(drawerLayout);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
}