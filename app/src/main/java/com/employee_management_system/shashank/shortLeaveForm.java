package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.miscellaneousMethods.getTime;
import static com.employee_management_system.shashank.miscellaneousMethods.getTimeStamp;
import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
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

public class shortLeaveForm extends AppCompatActivity {
    private ImageView imageView;
    private String LEAVE_ID = "";
    private Uri filePath;
    Spinner reportingToSpinner, leaveTypeSpinner;
    private TextView dateTV, timeTV;
    private EditText leaveReason;
    private int leaveHour, leaveMinute;
    private final int PICK_IMAGE_REQUEST = 21;
    private SharedPreferences sharedPreferences;
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private TimePickerDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_leave_form);
        sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        LEAVE_ID = sharedPreferences.getString("empId", "untitled") + getTime("yyMMddHHmmssMS");
        findViewById(R.id.backBtnSRL).setOnClickListener(view -> finish());
        reportingToSpinner =findViewById(R.id.reportingToSpinner_SRL);
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
        leaveTypeSpinner = findViewById(R.id.leaveTypeSpinner_SRL);
        firebaseFirestore.collection("employees")
                .document(sharedPreferences.getString("empId", null))
                .collection("leaveBalance")
                .document("remainLeaveBalanceChart")
                .get()
                .addOnCompleteListener(task -> {
                    if (Objects.requireNonNull(task.getResult().getLong("Short Leave")) <1)
                        new AlertDialog.Builder(this)
                                .setTitle("No Sufficient Balance!")
                                .setCancelable(false)
                                .setMessage("You don't have sufficient balance for Short Leave.")
                                .setPositiveButton("Okay", (dialogInterface, i1) -> this.finish())
                                .show();
                    else leaveTypeSpinner.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, new String[]{"Select","Casual Leave","Duty Leave","Research Leave"}));
                });
        dateTV = findViewById(R.id.date1TV_SRL);
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> dateTV.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth), year, month, day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        dateTV.setOnClickListener(v -> datePickerDialog.show());
        timeTV = findViewById(R.id.timeTV_SRL);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(this, (TimePickerDialog.OnTimeSetListener) (timePicker, hours, mins) -> {
            leaveHour = hours;
            leaveMinute = mins;
            String timeSet;
            if (hours > 12) {
                hours -= 12;
                timeSet = "PM";
            } else if (hours == 0) {
                hours = 12;
                timeSet = "AM";
            } else if (hours == 12) timeSet = "PM";
            else timeSet = "AM";
            String hourString;
            if (hours < 10) hourString = "0" + hours;
            else hourString = String.valueOf(hours);
            String minutes;
            if (mins < 10) minutes = "0" + mins;
            else minutes = String.valueOf(mins);
            timeTV.setText(hourString + ":" + minutes + " " + timeSet);
        }, hour, minute, false);
        timeTV.setOnClickListener(v -> timePickerDialog.show());
        leaveReason = findViewById(R.id.leaveReason_SRL);
        findViewById(R.id.chooseImgBtn_SRL).setOnClickListener(v -> SelectImage());
        imageView = findViewById(R.id.applicationDocSRL);
        findViewById(R.id.submitBtnSRL).setOnClickListener(v -> {
            uploadImage();
            applyLeave();
        });
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
    private void uploadImage() {
        if (filePath != null) FirebaseStorage.getInstance().getReference().child("uploadedDocsForLeave/" + "doc" + LEAVE_ID).putFile(filePath);
    }
    private void applyLeave() {
        if (isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this, noInternetActivity.class));
        else if (reportingToSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select the reporting authority...", Toast.LENGTH_SHORT).show();
            reportingToSpinner.performClick();
        } else if (dateTV.getText().toString().equals("")) {
            Toast.makeText(this, "Pick the leave date...", Toast.LENGTH_SHORT).show();
            datePickerDialog.show();
        } else if (timeTV.getText().toString().equals("")) {
            Toast.makeText(this, "Pick the leave time...", Toast.LENGTH_SHORT).show();
            timePickerDialog.show();
        } else if (leaveHour<10||leaveHour>14) {
            Toast.makeText(this, "Pick time between 10 am to 3 pm...", Toast.LENGTH_SHORT).show();
            timePickerDialog.show();
        } else {
            Map<String, Object> leaveApplication = new HashMap<>();
            leaveApplication.put("reportingOfficer", reportingToSpinner.getSelectedItem().toString());
            leaveApplication.put("leaveType", leaveTypeSpinner.getSelectedItem().toString());
            leaveApplication.put("leaveCategory", "Short Leave");
            leaveApplication.put("leaveDate", getTimeStamp(dateTV.getText().toString() + "T" + leaveHour + ":" + leaveMinute + ":00Z"));
            leaveApplication.put("numberOfLeave", 1);
            leaveApplication.put("readFlag",false);
            leaveApplication.put("appliedOn", FieldValue.serverTimestamp());
            leaveApplication.put("leaveReason", leaveReason.getText().toString());
            leaveApplication.put("leaveStatus", "Pending");
            leaveApplication.put("empId", sharedPreferences.getString("empId", null));
            leaveApplication.put("empName", sharedPreferences.getString("userName", null));
            if (filePath != null) leaveApplication.put("leaveDocID", "doc" + LEAVE_ID);
            else leaveApplication.put("leaveDocID", null);
            firebaseFirestore.collection("leaveApplied")
                    .document("SRL" + LEAVE_ID)
                    .set(leaveApplication)
                    .addOnCompleteListener(task -> {
                        firebaseFirestore.collection("employees")
                                .document(sharedPreferences.getString("empId", null))
                                .collection("leaveBalance")
                                .document("consumedLeaveBalanceChart")
                                .update("Short Leave", FieldValue.increment(+1));
                        firebaseFirestore.collection("employees")
                                .document(sharedPreferences.getString("empId", null))
                                .collection("leaveBalance")
                                .document("remainLeaveBalanceChart")
                                .update("Short Leave", FieldValue.increment(-1))
                                .addOnCompleteListener(task1 -> {
                                    Toast.makeText(this, "Leave Applied", Toast.LENGTH_SHORT).show();
                                    miscellaneousMethods.leaveApplied(this, this, "Leave Applied", "Application Id:", "SNL" + LEAVE_ID, "Leave Type:", "Short Leave", "", "");
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
