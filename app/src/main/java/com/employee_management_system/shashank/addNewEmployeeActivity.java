package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class addNewEmployeeActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 23;
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    TextView warningBar;
    private Uri filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_employee);
        findViewById(R.id.backBtnAddEmp).setOnClickListener(view -> finish());
        warningBar=findViewById(R.id.warningBarAddEmp);
        findViewById(R.id.setImageImgBtnAddEmp).setOnClickListener(view ->
                startActivityForResult(Intent.createChooser(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT), "Select Image from here..."), PICK_IMAGE_REQUEST));
        RadioGroup genderRadioGroup = findViewById(R.id.genderRadioGroup);
        EditText name = findViewById(R.id.nameAddEmp),
                designation=findViewById(R.id.designationAddEmp),
                empId=findViewById(R.id.empIdAddEmp),
                empType=findViewById(R.id.empTypeAddEmp),
                department=findViewById(R.id.departmentAddEmp),
                emailAddress=findViewById(R.id.emailAddEmp),
                mobile=findViewById(R.id.mobileNumberAddEmp),
                workLocation=findViewById(R.id.workLocationAddEmp),
                address=findViewById(R.id.addressAddEmp);
        TextView startDate=findViewById(R.id.startDateAddEmp),
                dob=findViewById(R.id.dobAddEmp);
        Spinner userTypeSpinner =findViewById(R.id.userTypeSpinner_AddEmp);
        ArrayList<String> user=new ArrayList<>();
        user.add("Select");
        user.add("Employee");
        user.add("Registrar");
        if(getSharedPreferences("userDetails",MODE_PRIVATE).getString("userType",null).equals("Super Admin")) user.add("Admin");
        userTypeSpinner.setAdapter(new ArrayAdapter<>(this,
                androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
                user));
        dob.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> dob.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1),year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 568025136000L);
            datePickerDialog.show();
        });
        startDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1),year, month, day);
            datePickerDialog.show();
        });
        findViewById(R.id.submitBtnAddEmp).setOnClickListener(view -> {
            if (isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this, noInternetActivity.class));
            else if(name.getText().toString().equals("")) {invalidEntryReminder("Enter Employee Name");openKeyboard(name);}
            else if(designation.getText().toString().equals("")) {invalidEntryReminder("Enter Employee Designation");openKeyboard(designation);}
            else if(!empId.getText().toString().contains("@")) {invalidEntryReminder("Enter valid Employee Id");openKeyboard(empId);}
            else if (genderRadioGroup.getCheckedRadioButtonId()==-1) invalidEntryReminder("Please select your gender.");
            else if (userTypeSpinner.getSelectedItem().equals("Select")) {invalidEntryReminder("Select user type"); userTypeSpinner.performClick();}
            else if(empType.getText().toString().equals("")) {invalidEntryReminder("Enter Employee Type");openKeyboard(empType);}
            else if(startDate.getText().toString().equals("")) {invalidEntryReminder("Select Start Date"); startDate.performClick();}
            else if(department.getText().toString().equals("")) {invalidEntryReminder("Enter Employee Department");openKeyboard(department);}
            else if(dob.getText().toString().equals("")) {invalidEntryReminder("Select Date of Birth"); dob.performClick();}
            else if(!emailAddress.getText().toString().contains("@")||!emailAddress.getText().toString().contains(".")) {invalidEntryReminder("Enter valid Email Address");openKeyboard(emailAddress);}
            else if(mobile.getText().toString().length()!=10) {invalidEntryReminder("Enter Valid Mobile Number");openKeyboard(mobile);}
            else if(workLocation.getText().toString().equals("")) {invalidEntryReminder("Enter work Location");openKeyboard(workLocation);}
            else if(address.getText().toString().equals("")) {invalidEntryReminder("Enter Employee Address");openKeyboard(address);}
            else {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Adding New Employee...");
                progressDialog.show();
                Map<String, Object> employeeDetails = new HashMap<>();
                if (filePath != null) {
                    uploadImage(empId.getText().toString());
                    employeeDetails.put("profilePictureId", "PRO_IMG_" + empId.getText().toString());
                } else employeeDetails.put("profilePictureId", "");
                employeeDetails.put("name", name.getText().toString());
                employeeDetails.put("designation", designation.getText().toString());
                employeeDetails.put("empId", empId.getText().toString());
                RadioButton radioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
                employeeDetails.put("gender", radioButton.getText().toString());
                employeeDetails.put("userType",userTypeSpinner.getSelectedItem().toString());
                employeeDetails.put("employeeType", empType.getText().toString());
                employeeDetails.put("startDate", startDate.getText().toString());
                employeeDetails.put("department", department.getText().toString());
                employeeDetails.put("leaveYearCompleted",1);
                employeeDetails.put("dateOfBirth", dob.getText().toString());
                employeeDetails.put("employmentStatus", "Active");
                employeeDetails.put("email", emailAddress.getText().toString());
                employeeDetails.put("mobile", mobile.getText().toString());
                employeeDetails.put("workLocation", workLocation.getText().toString());
                employeeDetails.put("address", address.getText().toString());
                firebaseFirestore.collection("employees")
                        .document(empId.getText().toString())
                        .set(employeeDetails)
                        .addOnCompleteListener(task -> {
                            CollectionReference reference = firebaseFirestore.collection("employees")
                                    .document(empId.getText().toString())
                                    .collection("leaveBalance");

                            Map<String, Object> remainLeaveBalance = new HashMap<>(3);
                            FirebaseFirestore.getInstance().collection("allowedLeaveBalance")
                                    .document("leaveBalanceChart")
                                    .get().addOnCompleteListener(task1 -> {
                                        remainLeaveBalance.put("Casual Leave", task1.getResult().get("Casual Leave"));
                                        remainLeaveBalance.put("Medical Leave", task1.getResult().get("Medical Leave"));
                                        remainLeaveBalance.put("Short Leave", task1.getResult().get("Short Leave"));
                                        reference.document("remainLeaveBalanceChart").set(remainLeaveBalance);
                                        Map<String, Object> consumedLeaveBalance = new HashMap<>(6);
                                        consumedLeaveBalance.put("Casual Leave", 0);
                                        consumedLeaveBalance.put("Duty Leave", 0);
                                        consumedLeaveBalance.put("Research Leave", 0);
                                        consumedLeaveBalance.put("Medical Leave", 0);
                                        consumedLeaveBalance.put("Short Leave", 0);
                                        consumedLeaveBalance.put("Special Leave", 0);
                                        reference.document("consumedLeaveBalanceChart")
                                                .set(consumedLeaveBalance)
                                                .addOnCompleteListener(task2 -> {
                                                    progressDialog.dismiss();
                                                    miscellaneousMethods.successDialog(this, this, "Employee Added");
                                                });
                                    });
                        });
            }
        });
    }
    private void invalidEntryReminder(String string){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
        warningBar.setText(string);
    }
    void openKeyboard(EditText editText){
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                ImageView imageView=findViewById(R.id.profileImageAddEmp);
                imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(String empId) {
        if (filePath != null) {
            FirebaseStorage.getInstance().getReference().child("employeeProfilePicture/" + "PRO_IMG_" + empId)//+ UUID.randomUUID().toString());
                    .putFile(filePath);
        }
    }
}