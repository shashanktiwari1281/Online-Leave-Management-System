package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class employeeProfileViewActivity extends AppCompatActivity {
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private TextView empId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout linearLayout=findViewById(R.id.tableLinearLayoutMainPg);
        TextView leaveBalanceTV=new TextView(this);
        leaveBalanceTV.setPaintFlags(leaveBalanceTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        leaveBalanceTV.setText("View Leave Balance");
        leaveBalanceTV.setTextColor(Color.BLUE);
        leaveBalanceTV.setTypeface(null, Typeface.BOLD);
        linearLayout.addView(leaveBalanceTV);
        leaveBalanceTV.setOnClickListener(view -> startActivity(new Intent(this, leaveBalanceActivity.class).putExtra("empId",getIntent().getStringExtra("empId"))));
        firebaseFirestore.collection("employees")
                .document(getIntent().getStringExtra("empId"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (!Objects.equals(task.getResult().getString("profilePictureId"), null)) {
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
                        empId=findViewById(R.id.employeeID_TV);
                        empId.setText(task.getResult().getString("empId"));
                        TextView gender=findViewById(R.id.gender_TV);
                        gender.setText(task.getResult().getString("gender"));
                        TextView empType=findViewById(R.id.employeeType_TV);
                        empType.setText(task.getResult().getString("employeeType"));
                        TextView startDate=findViewById(R.id.startDate_TV);
                        startDate.setText(task.getResult().getString("startDate"));
                        TextView department=findViewById(R.id.department_TV);
                        if(Objects.equals(task.getResult().getString("employmentStatus"), "Inactive")){
                            findViewById(R.id.employmentStatusRow).setVisibility(View.VISIBLE);
                            findViewById(R.id.employmentStatusLine).setVisibility(View.VISIBLE);
                            TextView employmentStatus=findViewById(R.id.employmentStatus_TV);
                            employmentStatus.setText(task.getResult().getString("employmentStatus"));
                            findViewById(R.id.endDateRow).setVisibility(View.VISIBLE);
                            findViewById(R.id.endDateLine).setVisibility(View.VISIBLE);
                            TextView endDate=findViewById(R.id.endDate_TV);
                            endDate.setText(task.getResult().getString("endDate"));
                        }
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
                    }
                    else{
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        Button manageLeaveBtn= findViewById(R.id.manageLeaveBtn);
        manageLeaveBtn.setText(R.string.back);
        manageLeaveBtn.setOnClickListener(v ->finish());
        Button moreOptionBtn= findViewById(R.id.moreOptionBtn);
        moreOptionBtn.setText(R.string.edit);
        moreOptionBtn.setOnClickListener(v -> startActivity(new Intent(this,editProfile.class)
                .putExtra("empId",getIntent().getStringExtra("empId"))
                .putExtra("calledBy","admin")));
        navigationDrawer();
    }
    boolean isNavDrawerOpen=false;
    void navigationDrawer(){
        ImageButton navBtn=findViewById(R.id.navigationDrawerBtn_MainPg);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navBtn.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu=navigationView.getMenu();
        menu.clear();
        menu.add(Menu.NONE, R.id.editProfile_mainDrawer, Menu.NONE, "Edit Profile");
        menu.add(Menu.NONE, R.id.oneDayLeave_mainDrawer, Menu.NONE, "Leave Balance");
        menu.add(Menu.NONE, R.id.stationLeave_mainDrawer, Menu.NONE, "Leave Applications");
        menu.add(Menu.NONE, R.id.shortLeave_mainDrawer, Menu.NONE, "Past Leaves");
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.editProfile_mainDrawer) startActivity(new Intent(this,editProfile.class)
                    .putExtra("empId",getIntent().getStringExtra("empId"))
                    .putExtra("calledBy","admin"));
            else if (id == R.id.oneDayLeave_mainDrawer) startActivity(new Intent(this, leaveBalanceActivity.class)
                    .putExtra("empId",getIntent().getStringExtra("empId")));
            else if (id == R.id.stationLeave_mainDrawer) startActivity(new Intent(this, empAllApplications.class)
                    .putExtra("empId",empId.getText().toString()));
            else if (id == R.id.shortLeave_mainDrawer) startActivity(new Intent(this, pastLeaveActivity.class)
                    .putExtra("empId",empId.getText().toString()));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}