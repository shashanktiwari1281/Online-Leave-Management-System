package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class unapprovedApplicationActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unapproved_application);
        findViewById(R.id.backBtnUnapprovedAppPg).setOnClickListener(view -> finish());
        TextView processingPg=findViewById(R.id.processingTV_UnapprovedPg);
        processingPg.setText(R.string.loading);
        processingPg.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("leaveApplied")
                .orderBy("appliedOn", Query.Direction.DESCENDING)
                .where(Filter.or(Filter.equalTo("leaveStatus","Pending"),Filter.equalTo("leaveStatus","Forwarded")))
                .whereEqualTo("reportingOfficer",getSharedPreferences("userDetails", MODE_PRIVATE).getString("userName", "Blank"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) processingPg.setText(R.string.no_application_found);
                    else {
                        processingPg.setVisibility(View.GONE);
                        RecyclerView myApplicationList=findViewById(R.id.appViewRV_UnapprovedPg);
                        myApplicationList.setLayoutManager(new LinearLayoutManager(this));
                        ArrayList<QueryDocumentSnapshot> documentArray=new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) documentArray.add(queryDocumentSnapshot);
                        myApplicationList.setAdapter(new unapprovedAppAdapter(this,documentArray, getSharedPreferences("userDetails", MODE_PRIVATE)));
                    }
                });
    }
}