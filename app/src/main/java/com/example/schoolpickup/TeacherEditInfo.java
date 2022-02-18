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

public class TeacherEditInfo extends AppCompatActivity {

    EditText uContactNo;
    Button confirmButton, returnButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_edit_info);

        uContactNo = findViewById(R.id.contactNo);
        confirmButton = findViewById(R.id.confirmBtn);
        returnButton = findViewById(R.id.returnBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("Users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                uContactNo.setText(documentSnapshot.getString("contact"));
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactNo = uContactNo.getText().toString().trim();

                if (TextUtils.isEmpty(contactNo)) {
                    uContactNo.setError("Please enter your contact No.");
                    return;
                }

                Map<String,Object> userInfo = new HashMap<>();
                userInfo.put("contact",contactNo);
                documentReference.update(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(TeacherEditInfo.this, "Updated successful", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onSuccess: User profile is updated for " + userID);
                        finish();
                        startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TeacherEditInfo.this, "Fail to update", Toast.LENGTH_LONG).show();
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
                startActivity(new Intent(getApplicationContext(),TeacherPage.class));
                finish();
            }
        });
    }
}