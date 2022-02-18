package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickUp extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    Button mArriving, mPickup;
    Boolean pickupBol = false;
    String teacherID = "";
    LatLng pickupLatLng;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseDatabase fDatabase;
    GeoQuery geoQuery;
    String userID, vehiclePlateNo;

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up);

        mArriving = findViewById(R.id.arrivingBtn);
        mPickup = findViewById(R.id.pickupBtn);
        drawerLayout = findViewById(R.id.drawer);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mArriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference studentRef = fStore.collection("Users").document(userID).collection("Students");
                studentRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                String sICNo = document.getId();
                                DocumentReference pickupRef = fStore.collection("PickupRequest").document(sICNo);
                                Map<String, Object> pickupInfo = new HashMap<>();
                                pickupInfo.put("pickupStatus","Arriving Soon");
                                pickupRef.update(pickupInfo);
                            }
                        }
                    }
                });
            }
        });

        mPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference studentRef = fStore.collection("Users").document(userID).collection("Students");
                studentRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if(error != null) {
                            return;
                        }
                        if(querySnapshot.isEmpty()) {
                            Toast.makeText(PickUp.this, "Please add a student first", Toast.LENGTH_LONG).show();
                            endPickup();
                        }
                    }
                });
                if(pickupBol){
                    endPickup();
                }else {
                    if(mFusedLocationClient == null) {
                        checkLocationPermission();
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                    pickupBol = true;
                    DocumentReference documentReference = fStore.collection("Users").document(userID);
                    documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                            if(error != null) {
                                return;
                            }
                            vehiclePlateNo = documentSnapshot.getString("vehiclePlateNo");
                        }
                    });

                    studentRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {
                                for(QueryDocumentSnapshot document : task.getResult()) {
                                    String sICNo = document.getId();
                                    String sFullName = document.getString("name");
                                    String sClassroom = document.getString("classroom");
                                    String sGender = document.getString("gender");
                                    DocumentReference pickupRef = fStore.collection("PickupRequest").document(sICNo);
                                    Map<String, Object> pickupInfo = new HashMap<>();
                                    pickupInfo.put("name",sFullName);
                                    pickupInfo.put("ic",sICNo);
                                    pickupInfo.put("classroom",sClassroom);
                                    pickupInfo.put("gender",sGender);
                                    pickupInfo.put("vehiclePlateNo",vehiclePlateNo);
                                    pickupInfo.put("pickupStatus","On The Way");
                                    pickupRef.set(pickupInfo);
                                }
                            }
                        }
                    });

                    DatabaseReference databaseReference = fDatabase.getReference("pickupRequest");
                    GeoFire geoFire = new GeoFire(databaseReference);
                    geoFire.setLocation(userID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    mArriving.setVisibility(View.VISIBLE);
                    mPickup.setText("Done/Cancel Pickup");

                    getAssignedTeacher();
                }
            }
        });
    }

    private void getAssignedTeacher() {
        DatabaseReference teacherRef = fDatabase.getReference().child("Users").child("Parents").child(userID).child("TeacherTracking").child("teacherID");
        teacherRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    teacherID = dataSnapshot.getValue().toString();
                    getAssignedPickupLocation();
                }else {
                    //endPickup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(error != null) {
                    return;
                }
            }
        });
    }

    Marker pickupMarker;
    private DatabaseReference assignedPickupLocationRef;
    private ValueEventListener assignedPickupLocationRefListener;

    private void getAssignedPickupLocation() {
        assignedPickupLocationRef = fDatabase.getReference().child("TeachersAvailable").child(teacherID).child("l");
        assignedPickupLocationRefListener = assignedPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()/* && !teacherID.equals("")*/) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Here"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(error != null) {
                    return;
                }
            }
        });
    }

    private void endPickup() {

        if(mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        CollectionReference studentRef = fStore.collection("Users").document(userID).collection("Students");
        studentRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        String sICNo = document.getId();
                        DocumentReference pickupRef = fStore.collection("PickupRequest").document(sICNo);
                        pickupRef.delete();
                    }
                }
            }
        });

        DatabaseReference parentRef = fDatabase.getReference().child("Users").child("Parents").child(userID).child("TeacherTracking");
        parentRef.setValue(null);

        DatabaseReference ref = fDatabase.getReference().child("pickupRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userID);
        DatabaseReference ref2 = fDatabase.getReference().child("pickupTracking");
        GeoFire geoFire2 = new GeoFire(ref2);
        geoFire2.removeLocation(userID);
        teacherID = "";
        pickupBol = false;

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (assignedPickupLocationRefListener != null){
            assignedPickupLocationRef.removeEventListener(assignedPickupLocationRefListener);
        }
        mArriving.setVisibility(View.GONE);
        mPickup.setText("Pick Up");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            }else {
                checkLocationPermission();
            }
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()) {
                if(getApplicationContext() != null) {
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    DatabaseReference refRequest = fDatabase.getReference("pickupRequest");
                    DatabaseReference refTracking = fDatabase.getReference("pickupTracking");
                    GeoFire geoFireRequest = new GeoFire(refRequest);
                    GeoFire geoFireTracking = new GeoFire(refTracking);

                    switch(teacherID) {
                        case "":
                            geoFireTracking.removeLocation(userID);
                            geoFireRequest.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                        default:
                            geoFireRequest.removeLocation(userID);
                            geoFireTracking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Grant permission")
                        .setMessage("Grant permission to this app?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(PickUp.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }else {
                ActivityCompat.requestPermissions(PickUp.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Permission required", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public void ClickMenu(View view){
        HomePage.openDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        endPickup();
        HomePage.redirectActivity(this,HomePage.class);
    }

    public void ClickPickUp(View view){
        HomePage.closeDrawer(drawerLayout);
    }

    public void ClickPairStudent(View view){
        endPickup();
        HomePage.redirectActivity(this,PairStudent.class);
    }

    public void ClickMyStudents(View view){
        endPickup();
        HomePage.redirectActivity(this,MyStudents.class);
    }

    public void ClickViewTeacher(View view){
        endPickup();
        HomePage.redirectActivity(this,ViewTeacher.class);
    }

    public void ClickViewTimetable(View view){
        endPickup();
        HomePage.redirectActivity(this,ViewTimetable.class);
    }

    public void ClickLogout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endPickup();
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