package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class employeeList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        findViewById(R.id.backBtnEmpList).setOnClickListener(view -> finish());
        TextView title=findViewById(R.id.titleEmpList);
        if (getIntent().getStringExtra("employmentStatus").equals("Active")) title.setText(R.string.active_employees);
        else title.setText(R.string.past_employees);
        TextView processingPg=findViewById(R.id.processingTV_EmpList);
        processingPg.setText(R.string.loading);
        processingPg.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("employees")
                .orderBy("name", Query.Direction.ASCENDING)
                .whereEqualTo("employmentStatus",getIntent().getStringExtra("employmentStatus"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) processingPg.setText(R.string.empty_list);
                    else {
                        processingPg.setVisibility(View.GONE);
                        RecyclerView myApplicationList=findViewById(R.id.appViewRV_EmpList);
                        myApplicationList.setLayoutManager(new LinearLayoutManager(this));
                        ArrayList<QueryDocumentSnapshot> documentArray=new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            documentArray.add(queryDocumentSnapshot);
                            processingPg.setText(queryDocumentSnapshot.toString());
                        }
                        myApplicationList.setAdapter(new empListAdapter(this, documentArray, getSharedPreferences("userDetails", MODE_PRIVATE).getString("userType",null)));
                    }
                });
    }
}