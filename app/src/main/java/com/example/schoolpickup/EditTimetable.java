package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class EditTimetable extends AppCompatActivity {

    EditText mon1, mon2, tue1, tue2, wed1, wed2, thu1, thu2, fri1, fri2;
    Button confirmButton1, confirmButton2, returnButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_timetable);

        mon1 = findViewById(R.id.monday1);
        mon2 = findViewById(R.id.monday2);
        tue1 = findViewById(R.id.tuesday1);
        tue2 = findViewById(R.id.tuesday2);
        wed1 = findViewById(R.id.wednesday1);
        wed2 = findViewById(R.id.wednesday2);
        thu1 = findViewById(R.id.thursday1);
        thu2 = findViewById(R.id.thursday2);
        fri1 = findViewById(R.id.friday1);
        fri2 = findViewById(R.id.friday2);

        confirmButton1 = findViewById(R.id.confirmBtn1);
        confirmButton2 = findViewById(R.id.confirmBtn2);
        returnButton = findViewById(R.id.returnBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("Timetable").document("Standard 1 - 3");
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                mon1.setText(documentSnapshot.getString("Monday"));
                tue1.setText(documentSnapshot.getString("Tuesday"));
                wed1.setText(documentSnapshot.getString("Wednesday"));
                thu1.setText(documentSnapshot.getString("Thursday"));
                fri1.setText(documentSnapshot.getString("Friday"));
            }
        });

        DocumentReference documentReference2 = fStore.collection("Timetable").document("Standard 4 - 6");
        documentReference2.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                mon2.setText(documentSnapshot.getString("Monday"));
                tue2.setText(documentSnapshot.getString("Tuesday"));
                wed2.setText(documentSnapshot.getString("Wednesday"));
                thu2.setText(documentSnapshot.getString("Thursday"));
                fri2.setText(documentSnapshot.getString("Friday"));
            }
        });

        confirmButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String monday1 = mon1.getText().toString().trim();
                String tuesday1 = tue1.getText().toString().trim();
                String wednesday1 = wed1.getText().toString().trim();
                String thursday1 = thu1.getText().toString().trim();
                String friday1 = fri1.getText().toString().trim();

                if (TextUtils.isEmpty(monday1)) {
                    mon1.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(tuesday1)) {
                    tue1.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(wednesday1)) {
                    wed1.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(thursday1)) {
                    thu1.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(friday1)) {
                    fri1.setError("Please insert the time.");
                    return;
                }

                Map<String,Object> timeInfo = new HashMap<>();
                timeInfo.put("Monday",monday1);
                timeInfo.put("Tuesday",tuesday1);
                timeInfo.put("Wednesday",wednesday1);
                timeInfo.put("Thursday",thursday1);
                timeInfo.put("Friday",friday1);
                documentReference.update(timeInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditTimetable.this, "Timetable 1 updated successful", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onSuccess: Timetable 1 is updated");
                        finish();
                        startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditTimetable.this, "Fail to update timetable 1", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onFailure: " + e.toString());
                        finish();
                        startActivity(getIntent());
                    }
                });
            }
        });

        confirmButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String monday2 = mon2.getText().toString().trim();
                String tuesday2 = tue2.getText().toString().trim();
                String wednesday2 = wed2.getText().toString().trim();
                String thursday2 = thu2.getText().toString().trim();
                String friday2 = fri2.getText().toString().trim();

                if (TextUtils.isEmpty(monday2)) {
                    mon2.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(tuesday2)) {
                    tue2.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(wednesday2)) {
                    wed2.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(thursday2)) {
                    thu2.setError("Please insert the time.");
                    return;
                }

                if (TextUtils.isEmpty(friday2)) {
                    fri2.setError("Please insert the time.");
                    return;
                }

                Map<String,Object> timeInfo = new HashMap<>();
                timeInfo.put("Monday",monday2);
                timeInfo.put("Tuesday",tuesday2);
                timeInfo.put("Wednesday",wednesday2);
                timeInfo.put("Thursday",thursday2);
                timeInfo.put("Friday",friday2);
                documentReference2.update(timeInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditTimetable.this, "Timetable 2 updated successful", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onSuccess: Timetable 2 is updated");
                        finish();
                        startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditTimetable.this, "Fail to update timetable 2", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onFailure: " + e.toString());
                        finish();
                        startActivity(getIntent());
                    }
                });
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference documentReference3 = fStore.collection("Users").document(userID);
                documentReference3.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fAuth.signOut();
                        startActivity(new Intent(getApplicationContext(),Login.class));
                        finish();
                    }
                });
            }
        });
    }
}