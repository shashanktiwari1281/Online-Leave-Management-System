package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.miscellaneousMethods.getTime;
import static com.employee_management_system.shashank.miscellaneousMethods.getTimeStamp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class employeesOnLeaveActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees_on_leave);
        SharedPreferences sharedPreferences=getSharedPreferences("userDetails", MODE_PRIVATE);
        ArrayList<QueryDocumentSnapshot> documentArray=new ArrayList<>();
        findViewById(R.id.backBtnEmpOnLv).setOnClickListener(view -> finish());
        TextView processingPg=findViewById(R.id.processingTV_empOnLv);
        processingPg.setText(R.string.loading);
        processingPg.setVisibility(View.VISIBLE);
        RecyclerView myApplicationList=findViewById(R.id.appViewRV_empOnLv);
        myApplicationList.setLayoutManager(new LinearLayoutManager(this));
        myApplicationList.setAdapter(new employeeOnLeaveAdapter(this,documentArray ));
        firebaseFirestore.collection("leaveApplied")
                .where(Filter.and(Filter.greaterThanOrEqualTo("leaveDate", getTimeStamp(getTime("yyyy-MM-dd")+"T00:00:00Z")),
                        Filter.lessThanOrEqualTo("leaveDate", getTimeStamp(getTime("yyyy-MM-dd")+"T23:59:00Z"))))
                .whereEqualTo("leaveStatus","Approved")
                .get().addOnCompleteListener(task -> {
                    if (!task.getResult().isEmpty()) for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) documentArray.add(queryDocumentSnapshot);
                    firebaseFirestore.collection("leaveApplied")
                            .where(Filter.and(Filter.greaterThanOrEqualTo("endDate", getTimeStamp(getTime("yyyy-MM-dd")+"T"+getTime("HH:mm:ss")+"Z")),
                                    Filter.lessThanOrEqualTo("startDate", getTimeStamp(getTime("yyyy-MM-dd")+"T"+getTime("HH:mm:ss")+"Z"))))
                            .whereEqualTo("leaveStatus", "Approved")
                            .get().addOnCompleteListener(task1 -> {
                                if (task1.getResult().isEmpty() && documentArray.isEmpty())
                                    processingPg.setText("Empty List!");
                                else {
                                    processingPg.setVisibility(View.GONE);
                                    if (!task1.getResult().isEmpty()) {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                            documentArray.add(queryDocumentSnapshot);
                                        }
                                    }
                                }
                            });
                });
    }
}