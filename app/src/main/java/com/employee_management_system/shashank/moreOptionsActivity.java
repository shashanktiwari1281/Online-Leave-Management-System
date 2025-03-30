package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class moreOptionsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_options);
        findViewById(R.id.backBtnMoreOptionPg).setOnClickListener(v -> finish());
        sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        userName=sharedPreferences.getString("userName", null);
        findViewById(R.id.editProfileTV_MoreOptionPg).setOnClickListener(v -> startActivity(new Intent(this, editProfile.class)
                .putExtra("empId", sharedPreferences.getString("empId", null))
                .putExtra("calledBy", "employee")));
        findViewById(R.id.contactUsTV_MoreOptionPg).setOnClickListener(v -> {
            LinearLayout linearLayout = findViewById(R.id.contactUsLL_MoreOptionPg);
            if (linearLayout.getVisibility() == View.VISIBLE) linearLayout.setVisibility(View.GONE);
            else linearLayout.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.callTV_MoreOptionPg).setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 11);
            else {
                FirebaseFirestore.getInstance().collection("basicDetails")
                        .document("basicAttributes")
                        .get()
                        .addOnCompleteListener(task -> startActivity(new Intent(Intent.ACTION_CALL)
                                .setData(Uri.parse("tel:+91" + task.getResult().getString("supportMobile")))));
            }
        });
        findViewById(R.id.emailTV_MoreOptionPg).setOnClickListener(view ->
                FirebaseFirestore.getInstance().collection("basicDetails")
                        .document("basicAttributes")
                        .get().addOnCompleteListener(task -> startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO,
                                Uri.parse("mailto:"+task.getResult().getString("supportEmail"))),"Choose Mailer"))));
        findViewById(R.id.reportProblem_MoreOptionPg).setOnClickListener(view -> reportProblem());
        TextView activeEmployees=findViewById(R.id.activeEmployees_MoreOptionPg);
        activeEmployees.setOnClickListener(view -> startActivity(new Intent(this, employeeList.class).putExtra("employmentStatus","Active")));
        ConstraintLayout forwardApplication=findViewById(R.id.forwardApp_CL_MoreOptionPg);
        forwardApplication.setOnClickListener(view -> startActivity(new Intent(this, unapprovedApplicationActivity.class)));
        TextView addNewEmployee=findViewById(R.id.addNewEmployee_MoreOptionPg);
        addNewEmployee.setOnClickListener(view -> startActivity(new Intent(this, addNewEmployeeActivity.class)));
        if(sharedPreferences.getString("userType", null).equals("Registrar")) {
            activeEmployees.setVisibility(View.VISIBLE);
            addNewEmployee.setVisibility(View.VISIBLE);
        }
    }
    private void reportProblem() {
        Dialog reportDialog = new Dialog(this);
        reportDialog.setContentView(R.layout.reject_reason_dialog);
        reportDialog.setCanceledOnTouchOutside(false);
        reportDialog.show();
        TextView title = reportDialog.findViewById(R.id.titleRejectDialog);
        title.setText(R.string.report_the_problem);
        reportDialog.findViewById(R.id.submitReasonBtn_RejectDialog).setOnClickListener(view -> {
            EditText editText = reportDialog.findViewById(R.id.rejectReason_ET_RejectDialog);
            if (isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this, noInternetActivity.class));
            else if (editText.getText().toString().equals("")) Toast.makeText(this, "Enter the problem", Toast.LENGTH_SHORT).show();
            else {
                Map<String, Object> data = new HashMap<>();
                data.put("problem", editText.getText().toString());
                data.put("empId", sharedPreferences.getString("empId", null));
                data.put("empName",userName);
                data.put("readFlag",false);
                data.put("reportedOn", FieldValue.serverTimestamp());
                FirebaseFirestore.getInstance().collection("reportedProblem").document().set(data).addOnCompleteListener(task -> reportDialog.dismiss());
            }
        });
    }
}