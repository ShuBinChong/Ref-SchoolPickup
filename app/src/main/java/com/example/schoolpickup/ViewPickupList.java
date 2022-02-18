package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ViewPickupList extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Button clearButton;
    RecyclerView mPickupList;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pickup_list);

        drawerLayout = findViewById(R.id.drawer);
        clearButton = findViewById(R.id.clearBtn);
        mPickupList = findViewById(R.id.pickupList);
        fStore = FirebaseFirestore.getInstance();

        Query query = fStore.collection("PickupRequest").orderBy("pickupStatus");
        FirestoreRecyclerOptions<PickupObject> options = new FirestoreRecyclerOptions.Builder<PickupObject>()
                .setQuery(query, PickupObject.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PickupObject, PickupViewHolder>(options) {
            @NonNull
            @Override
            public PickupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_pickup, parent, false);
                return new PickupViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PickupViewHolder holder, int position, @NonNull PickupObject model) {
                holder.listName.setText(model.getName());
                //holder.listICNo.setText(model.getIC());
                holder.listGender.setText(model.getGender());
                holder.listClassroom.setText(model.getClassroom());
                holder.listPickupStatus.setText(model.getPickupStatus());
                holder.listVehiclePlateNo.setText(model.getVehiclePlateNo());
                if (holder.listPickupStatus.getText().toString().endsWith("Soon") || holder.listPickupStatus.getText().toString().contains("Soon")) {
                    holder.listPickupStatus.setTextColor(Color.GREEN);
                } else {
                    holder.listPickupStatus.setTextColor(Color.RED);
                }
            }
        };

        mPickupList.setHasFixedSize(true);
        mPickupList.setLayoutManager(new LinearLayoutManager(this));
        mPickupList.setAdapter(adapter);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Clear Pickup List");
                builder.setMessage("Are you sure you want to clear the pickup list?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CollectionReference pickupRef = fStore.collection("PickupRequest");
                        pickupRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {
                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                        String sICNo = document.getId();
                                        DocumentReference ref = fStore.collection("PickupRequest").document(sICNo);
                                        ref.delete();
                                    }
                                }
                            }
                        });

                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    private class PickupViewHolder extends RecyclerView.ViewHolder {

        private TextView listName;
        //private TextView listICNo;
        private TextView listGender;
        private TextView listClassroom;
        private TextView listPickupStatus;
        private TextView listVehiclePlateNo;

        public PickupViewHolder(@NonNull View itemView) {
            super(itemView);

            listName = itemView.findViewById(R.id.listName);
            //listICNo = itemView.findViewById(R.id.listICNo);
            listGender = itemView.findViewById(R.id.listGender);
            listClassroom = itemView.findViewById(R.id.listClassroom);
            listPickupStatus = itemView.findViewById(R.id.listPickupStatus);
            listVehiclePlateNo = itemView.findViewById(R.id.listVehiclePlateNo);
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
        recreate();
    }

    public void ClickViewStudents(View view){
        TeacherPage.redirectActivity(this,ViewStudents.class);
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