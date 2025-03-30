package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;

public class pastLeaveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unapproved_application);
        TextView title=findViewById(R.id.titleUnapprovedAppPg);
        title.setText("Past Leaves");
        findViewById(R.id.backBtnUnapprovedAppPg).setOnClickListener(view -> finish());
        TextView processingPg=findViewById(R.id.processingTV_UnapprovedPg);
        processingPg.setText(R.string.loading);
        processingPg.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("leaveApplied")
                .orderBy("date1", Query.Direction.DESCENDING)
                .whereEqualTo("empId",getIntent().getStringExtra("empId"))
                .whereEqualTo("leaveStatus","Approved")
                .where(Filter.or(Filter.lessThan("leaveDate", new Timestamp(System.currentTimeMillis())),
                        Filter.lessThan("endDate", new Timestamp(System.currentTimeMillis()))))
                .limit(30)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) processingPg.setText(R.string.no_application_found);
                    else {
                        processingPg.setVisibility(View.GONE);
                        RecyclerView myApplicationList=findViewById(R.id.appViewRV_UnapprovedPg);
                        myApplicationList.setLayoutManager(new LinearLayoutManager(this));
                        ArrayList<QueryDocumentSnapshot> documentArray=new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) documentArray.add(queryDocumentSnapshot);
                        myApplicationList.setAdapter(new pastLeaveAdapter(this,documentArray));
                    }
                });
    }
}