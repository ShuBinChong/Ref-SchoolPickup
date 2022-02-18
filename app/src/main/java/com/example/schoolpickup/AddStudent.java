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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddStudent extends AppCompatActivity {

    DrawerLayout drawerLayout;
    EditText sFullName, sICNo, sClass;
    //EditText sGender;
    RadioGroup radioGroup;
    RadioButton radioGender;
    Button addButton;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        sFullName = findViewById(R.id.fullName);
        sICNo = findViewById(R.id.ICNo);
        radioGroup = findViewById(R.id.radioGroup);
        //sGender = findViewById(R.id.gender);
        sClass = findViewById(R.id.studentClass);
        addButton = findViewById(R.id.addBtn);

        fStore = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fullName = sFullName.getText().toString().trim();
                String ICNo = sICNo.getText().toString().trim();
                //String gender = sGender.getText().toString().trim();
                String studentClass = sClass.getText().toString().trim();

                int gender = radioGroup.getCheckedRadioButtonId();
                radioGender = findViewById(gender);

                if (TextUtils.isEmpty(fullName)) {
                    sFullName.setError("Please enter student full name.");
                    return;
                }

                if (TextUtils.isEmpty(ICNo)) {
                    sICNo.setError("Please enter student IC.");
                    return;
                }

                if (ICNo.length() != 12) {
                    sICNo.setError("Invalid IC format.");
                    return;
                }

                /*if (TextUtils.isEmpty(gender)) {
                    sGender.setError("Please enter student gender.");
                    return;
                }*/

                if (TextUtils.isEmpty(studentClass)) {
                    sClass.setError("Please enter student entry year.");
                    return;
                }

                if (radioGender == null) {
                    Toast.makeText(AddStudent.this, "Please select Student Gender", Toast.LENGTH_LONG).show();
                    return;
                }

                DocumentReference documentReference = fStore.collection("Students").document(ICNo);
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("name",fullName);
                studentInfo.put("ic",ICNo);
                studentInfo.put("gender",radioGender.getText().toString());
                studentInfo.put("classroom",studentClass);
                studentInfo.put("active","1");
                studentInfo.put("status","School");
                documentReference.set(studentInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddStudent.this, "Student profile created successful", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onSuccess: Student profile is created for " + ICNo);
                        finish();
                        startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddStudent.this, "Fail to create student profile", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onFailure: " + e.toString());
                        finish();
                        startActivity(getIntent());
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
        recreate();
    }

    public void ClickAddTeacher(View view){
        AdminPage.redirectActivity(this,AddTeacher.class);
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