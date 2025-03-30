package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class resetLeavesActivity extends AppCompatActivity {
    private int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_leaves);
        findViewById(R.id.backBtn_resetLeave).setOnClickListener(view -> finish());
        TextView lastResetDate = findViewById(R.id.lastResetDate);

        EditText casualLeave = findViewById(R.id.casualBal_resetLeave),
                medicalLeave = findViewById(R.id.medicalBal_resetLeave),
                shortLeave = findViewById(R.id.shortBal_resetLeave);
        FirebaseFirestore.getInstance().collection("allowedLeaveBalance")
                .document("leaveBalanceChart")
                .get().addOnCompleteListener(task -> {
                    lastResetDate.setText((Objects.requireNonNull(task.getResult().getTimestamp("lastResetDate")).toDate().toString()));
                    casualLeave.setText(String.valueOf(task.getResult().get("Casual Leave")));
                    medicalLeave.setText(String.valueOf(task.getResult().get("Medical Leave")));
                    shortLeave.setText(String.valueOf(task.getResult().get("Short Leave")));
                });
        findViewById(R.id.updateShortBtn_resetLeave).setOnClickListener(view -> {
            if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
            else if (shortLeave.getText().toString().equals("")) Toast.makeText(this, "Enter Short Leave Balance", Toast.LENGTH_SHORT).show();
            else {
                index = 0;
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Updating...");
                progressDialog.show();
                Map<String, Object> remainLeaveBalance = new HashMap<>(3);
                remainLeaveBalance.put("Short Leave", Long.parseLong(shortLeave.getText().toString()));
                remainLeaveBalance.put("lastResetDate",FieldValue.serverTimestamp());
                FirebaseFirestore.getInstance().collection("allowedLeaveBalance")
                        .document("leaveBalanceChart").update(remainLeaveBalance);
                remainLeaveBalance.remove("lastResetDate");
                FirebaseFirestore.getInstance().collection("employees")
                        .get().addOnCompleteListener(task -> {
                            if (!task.getResult().isEmpty()) {
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    FirebaseFirestore.getInstance().collection("employees")
                                            .document(Objects.requireNonNull(documentSnapshot.getString("empId")))
                                            .collection("leaveBalance")
                                            .document("remainLeaveBalanceChart")
                                            .set(remainLeaveBalance);
                                    if (++index == task.getResult().size()) {
                                        progressDialog.dismiss();
                                        miscellaneousMethods.successDialog(this, this, "Leave Balance\nUpdated");
                                    }
                                }
                            } else {
                                progressDialog.dismiss();
                                miscellaneousMethods.successDialog(this, this, "Leave Balance\nUpdated");
                            }
                        });
            }
        });
        findViewById(R.id.updateBtn_resetLeave).setOnClickListener(view -> {
            if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
            else if (casualLeave.getText().toString().equals("") || medicalLeave.getText().toString().equals("") || shortLeave.getText().toString().equals(""))
                Toast.makeText(this, "All fields are mandatory!", Toast.LENGTH_SHORT).show();
            else {
                index = 0;
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Updating...");
                progressDialog.show();
                Map<String, Object> remainLeaveBalance = new HashMap<>(3);
                remainLeaveBalance.put("Casual Leave", Long.parseLong(casualLeave.getText().toString()));
                remainLeaveBalance.put("Medical Leave", Long.parseLong(medicalLeave.getText().toString()));
                remainLeaveBalance.put("Short Leave", Long.parseLong(shortLeave.getText().toString()));
                FirebaseFirestore.getInstance().collection("allowedLeaveBalance")
                        .document("leaveBalanceChart").update(remainLeaveBalance);
                Map<String, Object> consumedLeaveBalance = new HashMap<>(6);
                consumedLeaveBalance.put("Casual Leave", 0);
                consumedLeaveBalance.put("Medical Leave", 0);
                consumedLeaveBalance.put("Short Leave", 0);
                consumedLeaveBalance.put("Duty Leave", 0);
                consumedLeaveBalance.put("Research Leave", 0);
                consumedLeaveBalance.put("Special Leave", 0);
                FirebaseFirestore.getInstance().collection("employees")
                        .get().addOnCompleteListener(task -> {
                            remainLeaveBalance.put("lastResetDate",FieldValue.serverTimestamp());
                            FirebaseFirestore.getInstance().collection("allowedLeaveBalance").document("leaveBalanceChart").update(remainLeaveBalance);
                            remainLeaveBalance.remove("lastResetDate");
                            if (!task.getResult().isEmpty()) {
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    if ((Objects.requireNonNull(documentSnapshot.getLong("leaveYearCompleted")) % 3 != 0)) {
                                        remainLeaveBalance.put("Medical Leave", FieldValue.increment(Long.parseLong(medicalLeave.getText().toString())));
                                        updateLeaveBalance(documentSnapshot.getString("empId"), remainLeaveBalance, consumedLeaveBalance);
                                    } else
                                        updateLeaveBalance(documentSnapshot.getString("empId"), remainLeaveBalance, consumedLeaveBalance);
                                    if (++index == task.getResult().size()) {
                                        progressDialog.dismiss();
                                        miscellaneousMethods.successDialog(this, this, "Leave Balance\nUpdated");
                                    }
                                }
                            } else progressDialog.dismiss();
                        });
            }
        });
    }
    private void updateLeaveBalance(String empId,Map<String,Object> remainLeaveBalance,Map<String,Object> consumedLeaveBalance){
        FirebaseFirestore.getInstance().collection("employees")
                .document(empId)
                .collection("leaveBalance")
                .document("remainLeaveBalanceChart")
                .set(remainLeaveBalance);
        FirebaseFirestore.getInstance().collection("employees")
                .document(empId)
                .collection("leaveBalance")
                .document("consumedLeaveBalanceChart")
                .set(consumedLeaveBalance);
        FirebaseFirestore.getInstance().collection("employees").document(empId).update("leaveYearCompleted",FieldValue.increment(1));
    }
}