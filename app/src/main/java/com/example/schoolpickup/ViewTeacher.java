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

public class ViewTeacher extends AppCompatActivity {

    DrawerLayout drawerLayout;
    RecyclerView mTeacherList;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_teacher);

        drawerLayout = findViewById(R.id.drawer);
        mTeacherList = findViewById(R.id.teacherList);
        fStore = FirebaseFirestore.getInstance();

        Query query = fStore.collection("Users").whereEqualTo("isTeacher", "1");
        FirestoreRecyclerOptions<TeacherObject> options = new FirestoreRecyclerOptions.Builder<TeacherObject>()
                .setQuery(query, TeacherObject.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<TeacherObject, TeachersViewHolder>(options) {
            @NonNull
            @Override
            public TeachersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_teacher, parent, false);
                return new TeachersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TeachersViewHolder holder, int position, @NonNull TeacherObject model) {
                holder.listName.setText(model.getName());
                holder.listEmail.setText( model.getEmail());
                holder.listContact.setText(model.getContact());
            }
        };

        mTeacherList.setHasFixedSize(true);
        mTeacherList.setLayoutManager(new LinearLayoutManager(this));
        mTeacherList.setAdapter(adapter);

    }

    private class TeachersViewHolder extends RecyclerView.ViewHolder {

        private TextView listName;
        private TextView listEmail;
        private TextView listContact;

        public TeachersViewHolder(@NonNull View itemView) {
            super(itemView);

            listName = itemView.findViewById(R.id.listName);
            listEmail = itemView.findViewById(R.id.listEmail);
            listContact = itemView.findViewById(R.id.listContact);
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
        HomePage.redirectActivity(this,MyStudents.class);
    }

    public void ClickViewTeacher(View view){
        recreate();
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