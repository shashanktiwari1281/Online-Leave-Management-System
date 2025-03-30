package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
public class adminHomeActivity extends AppCompatActivity {
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        TextView t=findViewById(R.id.pendingApplication_TV_AdminTool);
        userName=getSharedPreferences("userDetails", MODE_PRIVATE).getString("userName", null);
        findViewById(R.id.pendingApp_CL_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, unapprovedApplicationActivity.class)));
        findViewById(R.id.approvedApplication_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, approvedAppActivity.class)));
        findViewById(R.id.rejectedApplication_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, rejectedAppActivity.class)));
        findViewById(R.id.empOnLeave_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, employeesOnLeaveActivity.class)));
        findViewById(R.id.resetLeaveBalance_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, resetLeavesActivity.class)));
        findViewById(R.id.addEmployee_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, addNewEmployeeActivity.class)));
        findViewById(R.id.activeEmployees_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, employeeList.class).putExtra("employmentStatus","Active")));
        findViewById(R.id.pastEmployees_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, employeeList.class).putExtra("employmentStatus","Inactive")));
        findViewById(R.id.problemReported_TV_AdminTool).setOnClickListener(view -> startActivity(new Intent(this, reportedProblemActivity.class)));
        if(getSharedPreferences("userDetails", MODE_PRIVATE).getString("userType", null).equals("Super Admin")) findViewById(R.id.logOut_TV_AdminTool).setVisibility(View.VISIBLE);
        findViewById(R.id.logOut_TV_AdminTool).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("userDetails", MODE_PRIVATE).edit().clear().apply();
            startActivity(getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName())
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseFirestore.getInstance().collection("leaveApplied")
                .whereEqualTo("readFlag",false)
                .whereEqualTo("leaveStatus","Pending")
                .whereEqualTo("reportingOfficer",userName)
                .get()
                .addOnCompleteListener(task -> {
                    TextView newApplicationIndicator=findViewById(R.id.pendingAppNewNumber_TV_AdminTool);
                    if(task.getResult().size()>0) {
                        newApplicationIndicator.setText(task.getResult().size()+" New");
                        newApplicationIndicator.setVisibility(View.VISIBLE);
                    }
                    else newApplicationIndicator.setVisibility(View.GONE);
                });
        FirebaseFirestore.getInstance().collection("reportedProblem")
                .whereEqualTo("readFlag",false)
                .get()
                .addOnCompleteListener(task -> {
                    TextView newReportIndicator=findViewById(R.id.problemReportedNewNumber_TV_AdminTool);
                    if (task.getResult().size()>0){
                        newReportIndicator.setText(task.getResult().size()+" New");
                        newReportIndicator.setVisibility(View.VISIBLE);
                    }
                    else newReportIndicator.setVisibility(View.GONE);
                });
    }
}