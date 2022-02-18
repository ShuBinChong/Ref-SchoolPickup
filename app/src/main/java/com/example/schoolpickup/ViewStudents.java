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

public class ViewStudents extends AppCompatActivity {

    DrawerLayout drawerLayout;
    RecyclerView mStudentList;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students);

        drawerLayout = findViewById(R.id.drawer);
        mStudentList = findViewById(R.id.studentList);
        fStore = FirebaseFirestore.getInstance();

        Query query = fStore.collection("Students").orderBy("classroom");
        FirestoreRecyclerOptions<StudentObject> options = new FirestoreRecyclerOptions.Builder<StudentObject>()
                .setQuery(query, StudentObject.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<StudentObject, StudentsViewHolder>(options) {
            @NonNull
            @Override
            public StudentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_student2, parent, false);
                return new StudentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull StudentsViewHolder holder, int position, @NonNull StudentObject model) {
                holder.listName.setText(model.getName());
                holder.listICNo.setText(model.getIC());
                holder.listGender.setText(model.getGender());
                holder.listClassroom.setText(model.getClassroom());
            }
        };

        mStudentList.setHasFixedSize(true);
        mStudentList.setLayoutManager(new LinearLayoutManager(this));
        mStudentList.setAdapter(adapter);
    };

    private class StudentsViewHolder extends RecyclerView.ViewHolder {

        private TextView listName;
        private TextView listICNo;
        private TextView listGender;
        private TextView listClassroom;

        public StudentsViewHolder(@NonNull View itemView) {
            super(itemView);

            listName = itemView.findViewById(R.id.listName);
            listICNo = itemView.findViewById(R.id.listICNo);
            listGender = itemView.findViewById(R.id.listGender);
            listClassroom = itemView.findViewById(R.id.listClassroom);
        }
    }

    public void ClickMenu(View view){
        TeacherPage.openDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        TeacherPage.redirectActivity(this,TeacherPage.class);
    }

    public void ClickParentTracking(View view){
        TeacherPage.redirectActivity(this,ParentTracking.class);
    }

    public void ClickViewPickupList(View view){
        TeacherPage.redirectActivity(this,ViewPickupList.class);
    }

    public void ClickViewStudents(View view){
        recreate();
    }

    public void ClickViewTimetable(View view){
        TeacherPage.redirectActivity(this,TeacherViewTimetable.class);
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
        TeacherPage.closeDrawer(drawerLayout);
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