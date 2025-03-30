package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
public class MainActivity extends AppCompatActivity {
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences= getSharedPreferences("userDetails", MODE_PRIVATE);
        firebaseFirestore.collection("employees")
                .document(sharedPreferences.getString("empId",null))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (!Objects.equals(task.getResult().get("profilePictureId"), null)) {
                            File localFile;
                            try {
                                localFile = File.createTempFile("images", "jpg");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            File finalLocalFile = localFile;
                            FirebaseStorage.getInstance().getReference("employeeProfilePicture/" + task.getResult().getString("profilePictureId"))
                                    .getFile(finalLocalFile)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        ImageView profilePicture = findViewById(R.id.profileImageMainActivity);
                                        profilePicture.setImageBitmap(BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath()));
                                    });
                        }
                        TextView empName=findViewById(R.id.employeeNameTV);
                        empName.setText(task.getResult().getString("name"));
                        TextView designation=findViewById(R.id.employeeDesignationTV);
                        designation.setText(task.getResult().getString("designation"));
                        TextView empId=findViewById(R.id.employeeID_TV);
                        empId.setText(task.getResult().getString("empId"));
                        TextView gender=findViewById(R.id.gender_TV);
                        gender.setText(task.getResult().getString("gender"));
                        TextView empType=findViewById(R.id.employeeType_TV);
                        empType.setText(task.getResult().getString("employeeType"));
                        TextView startDate=findViewById(R.id.startDate_TV);
                        startDate.setText(task.getResult().getString("startDate"));
                        TextView department=findViewById(R.id.department_TV);
                        department.setText(task.getResult().getString("department"));
                        TextView dateOfBirth=findViewById(R.id.DOB_TV);
                        dateOfBirth.setText(task.getResult().getString("dateOfBirth"));
                        TextView mobileNumber=findViewById(R.id.mobileNumber_TV);
                        mobileNumber.setText(task.getResult().getString("mobile"));
                        TextView email=findViewById(R.id.email_TV);
                        email.setText(task.getResult().getString("email"));
                        TextView workLocation=findViewById(R.id.workLocation_TV);
                        workLocation.setText(task.getResult().getString("workLocation"));
                        TextView address=findViewById(R.id.address_TV);
                        address.setText(task.getResult().getString("address"));
                    } else Toast.makeText(this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                });
        findViewById(R.id.manageLeaveBtn).setOnClickListener(v -> startActivity(new Intent(this,manageLeaveActivity.class)));
        findViewById(R.id.moreOptionBtn).setOnClickListener(v -> startActivity(new Intent(this,moreOptionsActivity.class)));
        navigationDrawer();
    }
    private void navigationDrawer() {
        ImageButton navBtn=findViewById(R.id.navigationDrawerBtn_MainPg);
        drawerLayout = findViewById(R.id.drawer_layout);
        navBtn.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        NavigationView navigationView = findViewById(R.id.nav_view);
        if(sharedPreferences.getString("userType",null).equals("Admin"))
            navigationView.getMenu().findItem(R.id.adminMode_mainDrawer).setVisible(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id==R.id.adminMode_mainDrawer){
                ProgressDialog progressDialog=new ProgressDialog(this);
                progressDialog.setTitle("Authenticating");
                firebaseFirestore.collection("employees").document(sharedPreferences.getString("empId",null))
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()&& Objects.equals(task.getResult().get("userType"), "Admin")&&Objects.equals(task.getResult().get("name"), sharedPreferences.getString("userName",null)))
                                startActivity(new Intent(this, adminHomeActivity.class));
                            else Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        });
            }
            else if (id == R.id.halfDayLeave_mainDrawer) startActivity(new Intent(this, halfDayLeaveForm.class));
            else if (id == R.id.oneDayLeave_mainDrawer) startActivity(new Intent(this, oneDayLeaveFormActivity.class));
            else if (id == R.id.stationLeave_mainDrawer) startActivity(new Intent(this, stationLeaveForm.class));
            else if (id == R.id.shortLeave_mainDrawer) startActivity(new Intent(this, shortLeaveForm.class));
            else if (id == R.id.viewBalance_mainDrawer) startActivity(new Intent(this, leaveBalanceActivity.class)
                .putExtra("empId",sharedPreferences.getString("empId",null)));
            else if (id == R.id.myApplication_mainDrawer) startActivity(new Intent(this, myApplicationActivity.class));
            else if (id == R.id.editProfile_mainDrawer) startActivity(new Intent(this, editProfile.class)
                .putExtra("empId", sharedPreferences.getString("empId", null))
                .putExtra("calledBy", "employee"));
            else if (id == R.id.logOut_mainDrawer) logout();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        sharedPreferences.edit().clear().apply();
        startActivity(getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}