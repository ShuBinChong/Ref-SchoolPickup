package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class PairStudent extends AppCompatActivity {

    DrawerLayout drawerLayout;
    EditText sICNo;
    Button confirmButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_student);

        drawerLayout = findViewById(R.id.drawer);

        sICNo = findViewById(R.id.ICNo);
        confirmButton = findViewById(R.id.confirmBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ICNo = sICNo.getText().toString().trim();

                if (TextUtils.isEmpty(ICNo)) {
                    sICNo.setError("Please enter student IC.");
                    return;
                }

                if (ICNo.length() != 12) {
                    sICNo.setError("Invalid IC format.");
                    return;
                }

                fStore.collection("Students").document(ICNo).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String sFullName = documentSnapshot.getString("name");
                                    String sClassroom = documentSnapshot.getString("classroom");
                                    String sGender = documentSnapshot.getString("gender");
                                    DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Students").document(ICNo);
                                    Map<String, Object> studentInfo = new HashMap<>();
                                    studentInfo.put("name",sFullName);
                                    studentInfo.put("ic",ICNo);
                                    studentInfo.put("classroom",sClassroom);
                                    studentInfo.put("gender",sGender);
                                    documentReference.set(studentInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(PairStudent.this, "Student added successfully", Toast.LENGTH_LONG).show();
                                            Log.d("TAG", "onSuccess: Student with IC " + ICNo + " added for User " + userID);
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PairStudent.this, "Fail to add student", Toast.LENGTH_LONG).show();
                                            Log.d("TAG", "onFailure: " + e.toString());
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    });
                                } else {
                                    Toast.makeText(PairStudent.this, "Student does not exist", Toast.LENGTH_LONG).show();
                                }
                            }
                        });


            }
        });
    }

    public void ClickMenu(View view){
        HomePage.openDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        HomePage.redirectActivity(this,HomePage.class);
    }

    public void ClickPickUp(View view){
        HomePage.redirectActivity(this,PickUp.class);
    }

    public void ClickPairStudent(View view){
        recreate();
    }

    public void ClickMyStudents(View view){
        HomePage.redirectActivity(this,MyStudents.class);
    }

    public void ClickViewTeacher(View view){
        HomePage.redirectActivity(this,ViewTeacher.class);
    }

    public void ClickViewTimetable(View view){
        HomePage.redirectActivity(this,ViewTimetable.class);
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
        HomePage.closeDrawer(drawerLayout);
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