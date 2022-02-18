package com.example.schoolpickup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentTracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    //Switch mTrackingSwitch;
    Button mGetNearestParent;
    Boolean requestNearestParent = false;

    String parentID = "";
    RecyclerView mStudentList;
    FirestoreRecyclerAdapter adapter;
    LinearLayout mParentInfo;
    TextView mVehiclePlateNo;
    int radius = 1;
    Boolean parentFound = false;
    LatLng pickupLocation;
    Marker pickupMarker;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseDatabase fDatabase;
    GeoQuery geoQuery;
    String userID;

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_tracking);

        //mTrackingSwitch = findViewById(R.id.trackingSwitch);
        mGetNearestParent = findViewById(R.id.getNearestParentBtn);
        mParentInfo = findViewById(R.id.parentInfo);
        mVehiclePlateNo = findViewById(R.id.parentVehiclePlateNo);
        mStudentList = findViewById(R.id.studentList);
        drawerLayout = findViewById(R.id.drawer);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*mTrackingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    connectTeacher();
                }else {
                    disconnectTeacher();
                }
            }
        });*/

        mGetNearestParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestNearestParent) {
                    endTracking();
                    return;
                }else {
                    connectTeacher();
                    //if(mFusedLocationClient == null) {
                    //    checkLocationPermission();
                    //    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    //}
                    requestNearestParent = true;
                    DatabaseReference databaseReference = fDatabase.getReference("TeachersAvailable");
                    GeoFire geoFire = new GeoFire(databaseReference);
                    geoFire.setLocation(userID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));

                    mGetNearestParent.setText("Searching for Parent");

                    getClosestParent();
                }
            }
        });
    }

    private void getClosestParent() {
        DatabaseReference parentLocation = fDatabase.getReference().child("pickupRequest");

        GeoFire geoFire = new GeoFire(parentLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!parentFound){
                    parentFound = true;
                    parentID = key;

                    DatabaseReference parentRef = fDatabase.getReference().child("Users").child("Parents").child(parentID).child("TeacherTracking");
                    HashMap map = new HashMap();
                    map.put("teacherID", userID);
                    parentRef.updateChildren(map);

                    getParentLocation();
                    getParentInfo();
                    getStudentInfo();
                    getPickupEnded();
                    mGetNearestParent.setText("Retrieving Parent Location...");
                }
                /*if (!parentFound && requestNearestParent) {
                    DatabaseReference parentDataRef = fDatabase.getReference().child("Users").child("Parents").child(key);
                    parentDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                //Toast.makeText(getApplicationContext(), "key is:" + radius, Toast.LENGTH_LONG).show();
                                Map<String, Object> parentMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (parentFound) {
                                    return;
                                }
                                parentFound = true;
                                parentID = dataSnapshot.getKey();
                                Toast.makeText(getApplicationContext(), "key is:" + parentID, Toast.LENGTH_LONG).show();

                                DatabaseReference parentRef = fDatabase.getReference().child("Users").child("Parents").child(parentID).child("TeacherTracking");
                                HashMap map = new HashMap();
                                map.put("teacherID", userID);
                                parentRef.updateChildren(map);

                                getParentLocation();
                                getParentInfo();
                                getStudentInfo();
                                getPickupEnded();
                                mGetNearestParent.setText("Retrieving Parent Location...");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if(error != null) {
                                return;
                            }
                        }
                    });
                }*/
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!parentFound) {
                    radius++;
                    getClosestParent();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                if(error != null) {
                    return;
                }
            }
        });
    }

    Marker mParentMarker;
    DatabaseReference parentLocationRef;
    ValueEventListener parentLocationRefListener;

    private void getParentLocation() {
        parentLocationRef = fDatabase.getReference().child("pickupTracking").child(parentID).child("l");
        parentLocationRefListener = parentLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()/* && requestNearestParent*/) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng parentLatLng = new LatLng(locationLat,locationLng);
                    if(mParentMarker != null) {
                        mParentMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(parentLatLng.latitude);
                    loc2.setLongitude(parentLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if(distance<20) {
                        mGetNearestParent.setText("Parent is here");
                    }else {
                        mGetNearestParent.setText("Parent in range: " + String.valueOf(distance));
                    }

                    mParentMarker = mMap.addMarker(new MarkerOptions().position(parentLatLng).title("Parent"));
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

    private void getParentInfo() {
        mParentInfo.setVisibility(View.VISIBLE);
        DocumentReference parentInfo = fStore.collection("Users").document(parentID);
        parentInfo.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    return;
                }
                mVehiclePlateNo.setText(documentSnapshot.getString("vehiclePlateNo"));
            }
        });
        getStudentInfo();
    }

    private void getStudentInfo() {
        Query query = fStore.collection("Users").document(parentID).collection("Students").orderBy("classroom");
        FirestoreRecyclerOptions<StudentObject> options = new FirestoreRecyclerOptions.Builder<StudentObject>()
                .setQuery(query, StudentObject.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<StudentObject, StudentsViewHolder>(options) {
            @NonNull
            @Override
            public StudentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_student3, parent, false);
                return new StudentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull StudentsViewHolder holder, int position, @NonNull StudentObject model) {
                holder.listName.setText(model.getName());
                holder.listGender.setText(model.getGender());
                holder.listClassroom.setText(model.getClassroom());
            }
        };

        mStudentList.setHasFixedSize(true);
        mStudentList.setLayoutManager(new LinearLayoutManager(this));
        mStudentList.setAdapter(adapter);
        adapter.startListening();
    }

    private class StudentsViewHolder extends RecyclerView.ViewHolder {

        private TextView listName;
        private TextView listGender;
        private TextView listClassroom;

        public StudentsViewHolder(@NonNull View itemView) {
            super(itemView);

            listName = itemView.findViewById(R.id.listName);
            listGender = itemView.findViewById(R.id.listGender);
            listClassroom = itemView.findViewById(R.id.listClassroom);
        }
    }

    DatabaseReference parentPickupEndedRef;
    ValueEventListener parentPickupEndedRefListener;

    private void getPickupEnded(){
        parentPickupEndedRef = fDatabase.getReference().child("Users").child("Parents").child(parentID).child("TeacherTracking").child("teacherID");
        parentPickupEndedRefListener = parentPickupEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endTracking();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(databaseError != null) {
                    return;
                }
            }
        });
    }

    private void endTracking() {

        disconnectTeacher();
        //if(mFusedLocationClient != null) {
        //    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        //}

        requestNearestParent = false;

        geoQuery.removeAllListeners();
        //if(geoQuery != null) {
        //    geoQuery.removeAllListeners();
        //}

        if (parentLocationRefListener != null){
            parentLocationRef.removeEventListener(parentLocationRefListener);
        }

        if(parentID != null) {
            DatabaseReference parentRef = fDatabase.getReference().child("Users").child("Parents").child(parentID).child("TeacherTracking");
            parentRef.removeValue();
            parentID = null;
        }
        parentFound = false;
        radius =1;
        //DatabaseReference ref = fDatabase.getReference("TeachersAvailable");
        //GeoFire geoFire = new GeoFire(ref);
        //geoFire.removeLocation(userID);

        if(pickupMarker != null) {
            pickupMarker.remove();
        }
        if(mParentMarker != null) {
            mParentMarker.remove();
        }
        mGetNearestParent.setText("Get Nearest Parent");

        mParentInfo.setVisibility(View.GONE);
        mVehiclePlateNo.setText("");
        //adapter.stopListening();
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
                                ActivityCompat.requestPermissions(ParentTracking.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }else {
                ActivityCompat.requestPermissions(ParentTracking.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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

    private void connectTeacher() {
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectTeacher() {
        if(mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        DatabaseReference databaseReference = fDatabase.getReference("TeachersAvailable");
        GeoFire geoFire = new GeoFire(databaseReference);
        geoFire.removeLocation(userID);
    }

    public void ClickMenu(View view){
        TeacherPage.openDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        endTracking();
        TeacherPage.redirectActivity(this,TeacherPage.class);
    }

    public void ClickParentTracking(View view){
        TeacherPage.closeDrawer(drawerLayout);
    }

    public void ClickViewPickupList(View view){
        endTracking();
        TeacherPage.redirectActivity(this,ViewPickupList.class);
    }

    public void ClickViewStudents(View view){
        endTracking();
        TeacherPage.redirectActivity(this,ViewStudents.class);
    }

    public void ClickViewTimetable(View view){
        endTracking();
        TeacherPage.redirectActivity(this,TeacherViewTimetable.class);
    }

    public void ClickLogout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endTracking();
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
}