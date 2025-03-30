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

public class reportedProblemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reported_problem);
        findViewById(R.id.backBtn_reportedProblemPg).setOnClickListener(view -> finish());
        TextView processingPg=findViewById(R.id.processingTV_reportedProblemPg);
        processingPg.setText(R.string.loading);
        processingPg.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("reportedProblem")
                .orderBy("reportedOn", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) processingPg.setText("No Report Found!");
                    else {
                        processingPg.setVisibility(View.GONE);
                        RecyclerView problemList=findViewById(R.id.appViewRV_reportedProblemPg);
                        problemList.setLayoutManager(new LinearLayoutManager(this));
                        ArrayList<QueryDocumentSnapshot> documentArray=new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) documentArray.add(queryDocumentSnapshot);
                        problemList.setAdapter(new reportedProblemAdapter(this,documentArray ));
                    }
                });
    }
}