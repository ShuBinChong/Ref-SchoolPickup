package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MyStudents extends AppCompatActivity {

    DrawerLayout drawerLayout;
    RecyclerView mStudentList;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter adapter;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_students);

        drawerLayout = findViewById(R.id.drawer);
        mStudentList = findViewById(R.id.studentList);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        Query query = fStore.collection("Users").document(userID).collection("Students");
        FirestoreRecyclerOptions<StudentObject> options = new FirestoreRecyclerOptions.Builder<StudentObject>()
                .setQuery(query, StudentObject.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<StudentObject, StudentsViewHolder>(options) {
            @NonNull
            @Override
            public StudentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_student, parent, false);
                return new StudentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull StudentsViewHolder holder, int position, @NonNull StudentObject model) {
                holder.listName.setText(model.getName());
                holder.listICNo.setText(model.getIC());
                holder.listClassroom.setText(model.getClassroom());
            }
        };

        mStudentList.setHasFixedSize(true);
        mStudentList.setLayoutManager(new LinearLayoutManager(this));
        mStudentList.setAdapter(adapter);
    }

    private class StudentsViewHolder extends RecyclerView.ViewHolder {

        private TextView listName;
        private TextView listICNo;
        private TextView listClassroom;

        public StudentsViewHolder(@NonNull View itemView) {
            super(itemView);

            listName = itemView.findViewById(R.id.listName);
            listICNo = itemView.findViewById(R.id.listICNo);
            listClassroom = itemView.findViewById(R.id.listClassroom);
        }
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
        HomePage.redirectActivity(this,PairStudent.class);
    }

    public void ClickMyStudents(View view){
        recreate();
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

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}