package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class myApplicationActivity extends AppCompatActivity {
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_application);
        findViewById(R.id.backBtnMyAppPg).setOnClickListener(view -> finish());
        SharedPreferences sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        TextView processingPg=findViewById(R.id.processingTV_lvStatusPg);
        processingPg.setText(R.string.loading);
        processingPg.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("leaveApplied")
                .whereEqualTo("empId",sharedPreferences.getString("empId",null))
                .orderBy("appliedOn", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) processingPg.setText(R.string.no_application_found);
                    else {
                        processingPg.setVisibility(View.GONE);
                        RecyclerView myApplicationList=findViewById(R.id.recyclerView_myApplicationPg);
                        ArrayList<QueryDocumentSnapshot> documentArray=new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) documentArray.add(queryDocumentSnapshot);
                        myApplicationList.setLayoutManager(new LinearLayoutManager(this));
                        myApplicationList.setAdapter(new myApplicationAdapter(this,documentArray,sharedPreferences));
                    }
                });
    }
}