package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.miscellaneousMethods.countDays;
import static com.employee_management_system.shashank.miscellaneousMethods.getTime;
import static com.employee_management_system.shashank.miscellaneousMethods.getTimeStamp;
import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
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

import com.google.android.gms.tasks.Task;
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
import java.util.concurrent.atomic.AtomicInteger;

public class stationLeaveForm extends AppCompatActivity {
    private ImageView imageView;
    private String LEAVE_ID = "";
    private Uri filePath;
    private TextView startDateTV, endDateTV;
    private EditText leaveReason;
    private final int PICK_IMAGE_REQUEST = 22;
    private StorageReference storageReference;
    private Spinner leaveSpinner, reportingToSpinner;
    private SharedPreferences sharedPreferences;
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private DatePickerDialog datePickerDialog1,datePickerDialog2;
    int selectedStartDay, selectedStartMonth;
    Button chooseImgBtn;
    long numberOfLeaveDays;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_leave_form);
        sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        LEAVE_ID = sharedPreferences.getString("empId", "untitled") + getTime("yyMMddHHmmssMS");
        chooseImgBtn=findViewById(R.id.chooseImgBtn_SNL);
        findViewById(R.id.backBtnSNL).setOnClickListener(view -> finish());
        reportingToSpinner =findViewById(R.id.reportingToSpinner_SNL);
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
        leaveSpinner = findViewById(R.id.leaveTypeSpinner_SNL);
        ArrayList<String> leaveAvailable=new ArrayList<>();
        leaveAvailable.add("Select");
        leaveSpinner.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, leaveAvailable));
        Task<DocumentSnapshot> t=firebaseFirestore.collection("employees")
                .document(sharedPreferences.getString("empId",null))
                .collection("leaveBalance")
                .document("remainLeaveBalanceChart")
                .get().addOnCompleteListener(task -> {
                    if(Objects.requireNonNull(task.getResult().getLong("Casual Leave"))>1)leaveAvailable.add("Casual Leave");
                    if(Objects.requireNonNull(task.getResult().getLong("Medical Leave"))>1)leaveAvailable.add("Medical Leave");
                    leaveAvailable.add("Duty Leave");
                    leaveAvailable.add("Research Leave");
                    leaveSpinner.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, leaveAvailable));
                });
        leaveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                startDateTV.setText("");
                endDateTV.setText("");
                if (leaveSpinner.getSelectedItem().toString().equals("Research Leave")) chooseImgBtn.setText("Choose(Required)");
                else chooseImgBtn.setText("Choose(Optional)");
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        startDateTV = findViewById(R.id.fromDateTV_SNL);
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog1 = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            selectedStartMonth=monthOfYear+1;
            selectedStartDay=dayOfMonth;
            startDateTV.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            endDateTV.setText("");
        }, year, month, day);
        datePickerDialog1.setTitle("Start Date");
        datePickerDialog1.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog1.getDatePicker().setMaxDate(Objects.requireNonNull(getTimeStamp(year+"-12-31T00:00:00Z")).getTime());
        startDateTV.setOnClickListener(v -> datePickerDialog1.show());
        datePickerDialog1.setOnShowListener(dialogInterface -> {
            if(leaveSpinner.getSelectedItem().equals("Select")) {
                dialogInterface.dismiss();
                makeToast("Select Leave Type");
                leaveSpinner.performClick();
            }
        });
        endDateTV = findViewById(R.id.toDateTV_SNL);
        endDateTV.setOnClickListener(v -> {
            if(startDateTV.getText().equals("")) {
                makeToast("Pick Leave Start Date");
                datePickerDialog1.show();
            }
            else {
                datePickerDialog2 = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
                    endDateTV.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    numberOfLeaveDays=countDays(selectedStartDay, selectedStartMonth,year,dayOfMonth,monthOfYear + 1,year1)+1;
                }, year, selectedStartMonth-1, selectedStartDay+1);
                datePickerDialog2.getDatePicker().setMinDate(Objects.requireNonNull(getTimeStamp(year + "-" + selectedStartMonth+ "-" + (selectedStartDay + 1) + "T00:00:00Z")).getTime());
                datePickerDialog2.setTitle("End Date");
                if(!(leaveSpinner.getSelectedItem().equals("Duty Leave")||leaveSpinner.getSelectedItem().equals("Research Leave"))) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, selectedStartMonth-1, selectedStartDay);
                    cal.add(Calendar.DAY_OF_MONTH, Math.toIntExact(Objects.requireNonNull(t.getResult().getLong(leaveSpinner.getSelectedItem().toString()))-1));
                    datePickerDialog2.getDatePicker().setMaxDate(Math.min(cal.getTimeInMillis(), Objects.requireNonNull(getTimeStamp(year + "-12-31T00:00:00Z")).getTime()));
                }
                else datePickerDialog2.getDatePicker().setMaxDate(Objects.requireNonNull(getTimeStamp(year+"-12-31T00:00:00Z")).getTime());
                datePickerDialog2.show();
            }
        });
        leaveReason = findViewById(R.id.leaveReason_SNL);
        chooseImgBtn.setOnClickListener(v -> SelectImage());
        imageView = findViewById(R.id.applicationDocSNL);
        storageReference = FirebaseStorage.getInstance().getReference();
        findViewById(R.id.submitBtnSNL).setOnClickListener(v -> {
            uploadImage();
            applyLeave();
        });
    }
    void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void SelectImage() {
        startActivityForResult(Intent.createChooser(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT), "Select Image from here..."), PICK_IMAGE_REQUEST);
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
    // UploadImage method
    private void uploadImage() {
        if (filePath != null) {
            storageReference.child("uploadedDocsForLeave/" + "doc" + LEAVE_ID).putFile(filePath);
        }
    }
    private void applyLeave() {
        if (isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this, noInternetActivity.class));
        else if (reportingToSpinner.getSelectedItemPosition() == 0) {
            makeToast("Select the reporting authority...");
            reportingToSpinner.performClick();
        } else if (startDateTV.getText().toString().equals("")) {
            makeToast("Pick the leave start date...");
            datePickerDialog1.show();
        } else if (endDateTV.getText().toString().equals("")) {
            makeToast("Pick the leave end date...");
            endDateTV.performClick();
        } else if(leaveSpinner.getSelectedItem().toString().equals("Research Leave") && filePath==null) {
            makeToast("Require a document...");
            chooseImgBtn.performClick();
        } else {
            Map<String, Object> leaveApplication = new HashMap<>();
            leaveApplication.put("reportingOfficer", reportingToSpinner.getSelectedItem().toString());
            leaveApplication.put("leaveType", leaveSpinner.getSelectedItem().toString());
            leaveApplication.put("startDate", getTimeStamp(startDateTV.getText().toString()+"T00:00:00Z"));
            leaveApplication.put("endDate", getTimeStamp(endDateTV.getText().toString()+"T00:00:00Z"));
            leaveApplication.put("numberOfLeave",numberOfLeaveDays);
            leaveApplication.put("readFlag",false);
            leaveApplication.put("appliedOn", FieldValue.serverTimestamp());
            leaveApplication.put("leaveReason", leaveReason.getText().toString());
            leaveApplication.put("leaveStatus","Pending");
            if(filePath != null) leaveApplication.put("leaveDocID", "doc" + LEAVE_ID);
            else leaveApplication.put("leaveDocID", null);
            leaveApplication.put("empId",sharedPreferences.getString("empId", null));
            leaveApplication.put("empName",sharedPreferences.getString("userName", null));
            leaveApplication.put("leaveCategory","Station Leave");
            firebaseFirestore.collection("leaveApplied")
                    .document("SNL" + LEAVE_ID)
                    .set(leaveApplication)
                    .addOnCompleteListener(task -> {
                        String leaveType=leaveSpinner.getSelectedItem().toString();
                        if(!(leaveType.equals("Duty Leave")||leaveType.equals("Research Leave"))) firebaseFirestore.collection("employees")
                                .document(sharedPreferences.getString("empId", null))
                                .collection("leaveBalance")
                                .document("remainLeaveBalanceChart")
                                .update(leaveType,FieldValue.increment(-numberOfLeaveDays));
                        firebaseFirestore.collection("employees")
                                .document(sharedPreferences.getString("empId", null))
                                .collection("leaveBalance")
                                .document("consumedLeaveBalanceChart")
                                .update(leaveType,FieldValue.increment(+numberOfLeaveDays))
                                .addOnCompleteListener(task1 -> {
                                    makeToast("Leave Applied");
                                    miscellaneousMethods.leaveApplied(this,this,"Leave Applied","Application Id:","SNL"+LEAVE_ID,"Leave Type:",leaveSpinner.getSelectedItem().toString(),"","");
                                });
                    })
                    .addOnFailureListener(e -> makeToast("Failed " + e.getMessage()));
        }
    }
}