package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
public class manageLeaveActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_leave);
        findViewById(R.id.backBtnManageLeavePg).setOnClickListener(v -> finish());
        findViewById(R.id.oneDayLeave_TV).setOnClickListener(view -> startActivity(new Intent(this, oneDayLeaveFormActivity.class)));
        findViewById(R.id.halfDayLeave_TV).setOnClickListener(view -> startActivity(new Intent(this, halfDayLeaveForm.class)));
        findViewById(R.id.shortLeave_TV).setOnClickListener(view -> startActivity(new Intent(this, shortLeaveForm.class)));
        findViewById(R.id.applicationStatus_TV).setOnClickListener(view -> startActivity(new Intent(this, myApplicationActivity.class)));
        findViewById(R.id.stationLeave_TV).setOnClickListener(view -> startActivity(new Intent(this, stationLeaveForm.class)));
        findViewById(R.id.viewBalance_TV).setOnClickListener(view -> startActivity(new Intent(this, leaveBalanceActivity.class).putExtra("empId",getSharedPreferences("userDetails", MODE_PRIVATE).getString("empId","Blank"))));
    }
}