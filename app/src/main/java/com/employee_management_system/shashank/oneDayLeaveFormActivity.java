package com.employee_management_system.shashank;
import static com.employee_management_system.shashank.miscellaneousMethods.getTime;
import static com.employee_management_system.shashank.miscellaneousMethods.getTimeStamp;
import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
public class oneDayLeaveFormActivity extends AppCompatActivity {
    private ImageView imageView;
    private String LEAVE_ID = "";
    private Uri filePath;
    private TextView leaveDateTV;
    private EditText leaveReason;
    private final int PICK_IMAGE_REQUEST = 22;
    private StorageReference storageReference;
    private Spinner leaveSpinner, reportingToSpinner;
    private SharedPreferences sharedPreferences;
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private DatePickerDialog datePickerDialog;
    Button chooseImgBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_day_leave_form);
        chooseImgBtn=findViewById(R.id.chooseImgBtn_ODL);
        sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        LEAVE_ID = sharedPreferences.getString("empId", "untitled") + getTime("yyMMddHHmmssMS");
        findViewById(R.id.backBtnODL).setOnClickListener(view -> finish());
        reportingToSpinner =findViewById(R.id.reportingToSpinner_ODL);
        ArrayList<String> reportingToNames=new ArrayList<>();
        reportingToNames.add("Select");
        reportingToSpinner.setAdapter(new ArrayAdapter<>(this, androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item, reportingToNames));
        if(sharedPreferences.getString("userType",null).equals("Admin"))
            firebaseFirestore.collection("superAdmin").get().addOnCompleteListener(task -> {
            reportingToNames.add(task.getResult().getDocuments().get(0).getString("name"));
            reportingToSpinner.setSelection(1);
        });
        else firebaseFirestore.collection("employees").whereEqualTo("userType","Admin")
                .get().addOnCompleteListener(task ->{
                    for(DocumentSnapshot documentSnapshot:task.getResult()) reportingToNames.add(documentSnapshot.getString("name"));
                });
        leaveSpinner=findViewById(R.id.leaveTypeSpinner_ODL);
        ArrayList<String> leaveAvailable=new ArrayList<>();
        leaveAvailable.add("Select");
        leaveSpinner.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, leaveAvailable));
        firebaseFirestore.collection("employees")
                .document(sharedPreferences.getString("empId",null))
                .collection("leaveBalance")
                .document("remainLeaveBalanceChart")
                .get().addOnCompleteListener(task -> {
                    if(Objects.requireNonNull(task.getResult().getLong("Casual Leave"))>=1)leaveAvailable.add("Casual Leave");
                    if(Objects.requireNonNull(task.getResult().getLong("Medical Leave"))>=1)leaveAvailable.add("Medical Leave");
                    leaveAvailable.add("Duty Leave");
                    leaveAvailable.add("Research Leave");
                    leaveSpinner.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, leaveAvailable));
                });
        leaveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (leaveSpinner.getSelectedItem().toString().equals("Research Leave")) chooseImgBtn.setText("Choose(Required)");
                else chooseImgBtn.setText("Choose(Optional)");
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        leaveDateTV=findViewById(R.id.leaveDateTV_ODL);
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> leaveDateTV.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        leaveDateTV.setOnClickListener(v -> datePickerDialog.show());
        leaveReason=findViewById(R.id.leaveReason_ODL);
        chooseImgBtn.setOnClickListener(v ->
                startActivityForResult(Intent.createChooser(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT), "Select Image from here..."), PICK_IMAGE_REQUEST));
        imageView = findViewById(R.id.applicationDocODL);
        storageReference = FirebaseStorage.getInstance().getReference();
        findViewById(R.id.submitBtnODL).setOnClickListener(v -> {
            uploadImage();
            applyLeave();
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePath != null) storageReference.child("uploadedDocsForLeave/" + "doc" + LEAVE_ID).putFile(filePath);
    }
    private void applyLeave() {
        if (isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this, noInternetActivity.class));
        else if (reportingToSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select the reporting authority...", Toast.LENGTH_SHORT).show();
            reportingToSpinner.performClick();
        } else if (leaveSpinner.getSelectedItemPosition()==0) {
            Toast.makeText(this, "Select the leave type...", Toast.LENGTH_SHORT).show();
            leaveSpinner.performClick();
        } else if (leaveDateTV.getText().toString().equals("")) {
            Toast.makeText(this, "Pick the leave date...", Toast.LENGTH_SHORT).show();
            datePickerDialog.show();
        } else if(leaveSpinner.getSelectedItem().toString().equals("Research Leave") && filePath==null){
            Toast.makeText(this, "Require a document...", Toast.LENGTH_SHORT).show();
            chooseImgBtn.performClick();
        } else {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Applying Leave...");
            progressDialog.show();
            Map<String,Object> leaveApplication = new HashMap<>();
            leaveApplication.put("reportingOfficer", reportingToSpinner.getSelectedItem().toString());
            leaveApplication.put("leaveType", leaveSpinner.getSelectedItem().toString());
            leaveApplication.put("leaveDate", getTimeStamp(leaveDateTV.getText().toString()+"T00:00:00Z"));
            leaveApplication.put("appliedOn", FieldValue.serverTimestamp());
            leaveApplication.put("leaveReason", leaveReason.getText().toString());
            leaveApplication.put("leaveStatus","Pending");
            leaveApplication.put("readFlag",false);
            leaveApplication.put("leaveCategory","One Day Leave");
            leaveApplication.put("numberOfLeave",1);
            leaveApplication.put("empId",sharedPreferences.getString("empId", null));
            leaveApplication.put("empName",sharedPreferences.getString("userName", null));
            if (filePath != null)leaveApplication.put("leaveDocID", "doc" + LEAVE_ID);
            else leaveApplication.put("leaveDocID", null);
            firebaseFirestore.collection("leaveApplied")
                    .document("ODL" + LEAVE_ID)
                    .set(leaveApplication)
                    .addOnCompleteListener(task -> {
                        String leaveType=leaveSpinner.getSelectedItem().toString();
                        if(!(leaveType.equals("Duty Leave")||leaveType.equals("Research Leave"))) firebaseFirestore.collection("employees")
                                .document(sharedPreferences.getString("empId", null))
                                .collection("leaveBalance")
                                .document("remainLeaveBalanceChart")
                                .update(leaveType,FieldValue.increment(-1));
                        firebaseFirestore.collection("employees")
                                .document(sharedPreferences.getString("empId", null))
                                .collection("leaveBalance")
                                .document("consumedLeaveBalanceChart")
                                .update(leaveType,FieldValue.increment(+1))
                                .addOnCompleteListener(task1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Leave Applied", Toast.LENGTH_SHORT).show();
                                    miscellaneousMethods.leaveApplied(this,this,"Leave Applied","Application Id:","ODL"+LEAVE_ID,"Leave Date", leaveDateTV.getText().toString(),"Leave Type:",leaveSpinner.getSelectedItem().toString());
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed! " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}