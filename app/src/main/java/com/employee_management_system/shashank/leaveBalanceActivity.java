package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class leaveBalanceActivity extends AppCompatActivity {
    TextView casualRemainLeaveBal, casualConsumedLeaveBal, dutyConsumedLeaveBal, researchConsumedLeaveBal, medicalConsumedLeaveBal, medicalRemainLeaveBal, specialConsumedLeaveBal, shortRemainLeaveBal, shortConsumedLeaveBal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_balance);
        findViewById(R.id.backBtnLvBalPg).setOnClickListener(view -> finish());
        casualRemainLeaveBal =findViewById(R.id.casualRemainLeaveBal);
        casualConsumedLeaveBal=findViewById(R.id.casualConsumedLeaveBal);
        dutyConsumedLeaveBal=findViewById(R.id.dutyConsumedLeaveBal);
        researchConsumedLeaveBal =findViewById(R.id.researchConsumedLeaveBal);
        medicalRemainLeaveBal =findViewById(R.id.medicalRemainLeaveBal);
        medicalConsumedLeaveBal=findViewById(R.id.medicalConsumedLeaveBal);
        specialConsumedLeaveBal =findViewById(R.id.specialConsumedLeaveBal);
        shortRemainLeaveBal =findViewById(R.id.shortRemainLeaveBal);
        shortConsumedLeaveBal=findViewById(R.id.shortConsumedLeaveBal);
        FirebaseFirestore.getInstance().collection("employees")
                .document(getIntent().getStringExtra("empId"))
                .collection("leaveBalance")
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot consumedBalance=task.getResult().getDocuments().get(0);
                    DocumentSnapshot remainBalance=task.getResult().getDocuments().get(1);
                    casualRemainLeaveBal.setText(Objects.requireNonNull(remainBalance.get("Casual Leave")).toString());
                    casualConsumedLeaveBal.setText(Objects.requireNonNull(consumedBalance.get("Casual Leave")).toString());
                    dutyConsumedLeaveBal.setText(Objects.requireNonNull(consumedBalance.get("Duty Leave")).toString());
                    researchConsumedLeaveBal.setText(Objects.requireNonNull(consumedBalance.get("Research Leave")).toString());
                    medicalRemainLeaveBal.setText(Objects.requireNonNull(remainBalance.get("Medical Leave")).toString());
                    medicalConsumedLeaveBal.setText(Objects.requireNonNull(consumedBalance.get("Medical Leave")).toString());
                    specialConsumedLeaveBal.setText(Objects.requireNonNull(consumedBalance.get("Special Leave")).toString());
                    shortRemainLeaveBal.setText(Objects.requireNonNull(remainBalance.get("Short Leave")).toString());
                    shortConsumedLeaveBal.setText(Objects.requireNonNull(consumedBalance.get("Short Leave")).toString());
                });
    }
}