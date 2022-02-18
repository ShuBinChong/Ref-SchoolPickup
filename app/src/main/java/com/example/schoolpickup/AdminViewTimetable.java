package com.example.schoolpickup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class AdminViewTimetable extends AppCompatActivity {

    DrawerLayout drawerLayout;
    TextView mon1, mon2, tue1, tue2, wed1, wed2, thu1, thu2, fri1, fri2;
    Button pEditButton;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_timetable);

        drawerLayout = findViewById(R.id.drawer);

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

        pEditButton = findViewById(R.id.editBtn);

        fStore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = fStore.collection("Timetable").document("Standard 1 - 3");
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    return;
                }
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
                if(error != null) {
                    return;
                }
                mon2.setText(documentSnapshot.getString("Monday"));
                tue2.setText(documentSnapshot.getString("Tuesday"));
                wed2.setText(documentSnapshot.getString("Wednesday"));
                thu2.setText(documentSnapshot.getString("Thursday"));
                fri2.setText(documentSnapshot.getString("Friday"));
            }
        });

        pEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),EditTimetable.class));
                finish();
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
        AdminPage.redirectActivity(this,AddTeacher.class);
    }

    public void ClickViewTimetable(View view){
        recreate();
    }

    public void ClickLogout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
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