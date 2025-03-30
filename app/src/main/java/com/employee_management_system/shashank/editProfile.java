package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class editProfile extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 24;
    private ImageView profilePicture;
    private Uri filePath;
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private final StorageReference storageReference= FirebaseStorage.getInstance().getReference();
    private EditText name,designation,empId,empType,department,emailAddress,mobile,workLocation,address;
    private Spinner userTypeSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_employee);
        TextView title = findViewById(R.id.titleAddEmp);
        title.setText(R.string.edit_profile);
        findViewById(R.id.backBtnAddEmp).setOnClickListener(view -> finish());
        profilePicture = findViewById(R.id.profileImageAddEmp);
        findViewById(R.id.setImageImgBtnAddEmp).setOnClickListener(view ->
                startActivityForResult(Intent.createChooser(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT), "Select Image from here..."), PICK_IMAGE_REQUEST));
        RadioGroup genderRadioGroup = findViewById(R.id.genderRadioGroup);
        genderRadioGroup.setVisibility(View.GONE);
        ArrayList<String> user=new ArrayList<>();
        user.add("Select");
        user.add("Employee");
        user.add("Registrar");
        String userType=getSharedPreferences("userDetails",MODE_PRIVATE).getString("userType",null);
        if(userType.equals("Super Admin")||userType.equals("Admin")) user.add("Admin");
        userTypeSpinner=findViewById(R.id.userTypeSpinner_AddEmp);
        userTypeSpinner.setAdapter(new ArrayAdapter<>(this,
                androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
                user));
        findViewById(R.id.genderTV_AddEmp).setVisibility(View.GONE);
        name = findViewById(R.id.nameAddEmp);
        designation = findViewById(R.id.designationAddEmp);
        empId = findViewById(R.id.empIdAddEmp);
        empType = findViewById(R.id.empTypeAddEmp);
        department = findViewById(R.id.departmentAddEmp);
        emailAddress = findViewById(R.id.emailAddEmp);
        mobile = findViewById(R.id.mobileNumberAddEmp);
        workLocation = findViewById(R.id.workLocationAddEmp);
        address = findViewById(R.id.addressAddEmp);
        TextView startDate = findViewById(R.id.startDateAddEmp),
                dob = findViewById(R.id.dobAddEmp);
        if (getIntent().getStringExtra("calledBy").equals("employee")){
            designation.setEnabled(false);
            empType.setEnabled(false);
            department.setEnabled(false);
            workLocation.setEnabled(false);
            startDate.setEnabled(false);
        }
        dob.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> dob.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1), year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 568025136000L);
            datePickerDialog.show();
        });
        startDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1), year, month, day);
            datePickerDialog.show();
        });
        firebaseFirestore.collection("employees")
                .document(getIntent().getStringExtra("empId"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!Objects.equals(task.getResult().getString("profilePictureId"), null)) {
                            File localFile;
                            try {
                                localFile = File.createTempFile("images", "jpg");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            File finalLocalFile = localFile;
                            FirebaseStorage.getInstance().getReference("employeeProfilePicture/" + task.getResult().getString("profilePictureId"))
                                    .getFile(finalLocalFile)
                                    .addOnSuccessListener(taskSnapshot -> profilePicture.setImageBitmap(BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath())));
                        }
                        name.setText(task.getResult().getString("name"));
                        designation.setText(task.getResult().getString("designation"));
                        empId.setText(task.getResult().getString("empId"));
                        empId.setEnabled(false);
                        empType.setText(task.getResult().getString("employeeType"));
                        userTypeSpinner.setSelection(user.indexOf(task.getResult().getString("userType")));
                        startDate.setText(task.getResult().getString("startDate"));
                        department.setText(task.getResult().getString("department"));
                        dob.setText(task.getResult().getString("dateOfBirth"));
                        mobile.setText(task.getResult().getString("mobile"));
                        emailAddress.setText(task.getResult().getString("email"));
                        workLocation.setText(task.getResult().getString("workLocation"));
                        address.setText(task.getResult().getString("address"));
                        SharedPreferences sharedPreferences=getSharedPreferences("userDetails", MODE_PRIVATE);
                        if(!sharedPreferences.getString("userType",null).equals("Employee") && !sharedPreferences.getString("empId",null).equals(task.getResult().getString("empId"))) findViewById(R.id.userType_LL_AddEmp).setVisibility(View.VISIBLE);
                        else findViewById(R.id.userType_LL_AddEmp).setVisibility(View.GONE);
                    } else Toast.makeText(this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                });
        findViewById(R.id.submitBtnAddEmp).setOnClickListener(view -> {
            if (isConnectionAvailable(getApplicationContext()))
                startActivity(new Intent(this, noInternetActivity.class));
            else if (name.getText().toString().equals(""))
                Toast.makeText(this, "Enter Employee Name", Toast.LENGTH_SHORT).show();
            else if (designation.getText().toString().equals(""))
                Toast.makeText(this, "Enter Employee Designation", Toast.LENGTH_SHORT).show();
            else if (empType.getText().toString().equals(""))
                Toast.makeText(this, "Enter Employee Type", Toast.LENGTH_SHORT).show();
            else if (startDate.getText().toString().equals(""))
                Toast.makeText(this, "Select Start Date", Toast.LENGTH_SHORT).show();
            else if (userTypeSpinner.getSelectedItem().equals("Select")) {
                Toast.makeText(this, "Select user type", Toast.LENGTH_SHORT).show(); userTypeSpinner.performClick();}
            else if (department.getText().toString().equals(""))
                Toast.makeText(this, "Enter Employee Department", Toast.LENGTH_SHORT).show();
            else if (dob.getText().toString().equals(""))
                Toast.makeText(this, "Select Date of Birth", Toast.LENGTH_SHORT).show();
            else if (!emailAddress.getText().toString().contains("@") || !emailAddress.getText().toString().contains("."))
                Toast.makeText(this, "Enter valid Email Address ", Toast.LENGTH_SHORT).show();
            else if (mobile.getText().toString().length() != 10)
                Toast.makeText(this, "Enter Valid Mobile Number", Toast.LENGTH_SHORT).show();
            else if (workLocation.getText().toString().equals(""))
                Toast.makeText(this, "Enter work Location", Toast.LENGTH_SHORT).show();
            else if (address.getText().toString().equals(""))
                Toast.makeText(this, "Enter Employee Address", Toast.LENGTH_SHORT).show();
            else {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Adding New Employee...");
                progressDialog.show();
                Map<String, Object> employeeDetails = new HashMap<>();
                if (filePath != null) {
                    uploadImage(empId.getText().toString());
                    employeeDetails.put("profilePictureId", "PRO_IMG_" + empId.getText().toString());
                } else employeeDetails.put("profilePictureId", null);
                employeeDetails.put("name", name.getText().toString());
                employeeDetails.put("dateOfBirth", dob.getText().toString());
                employeeDetails.put("email", emailAddress.getText().toString());
                employeeDetails.put("mobile", mobile.getText().toString());
                employeeDetails.put("address", address.getText().toString());
                if (getIntent().getStringExtra("calledBy").equals("admin")) {
                    employeeDetails.put("workLocation", workLocation.getText().toString());
                    employeeDetails.put("department", department.getText().toString());
                    employeeDetails.put("userType",userTypeSpinner.getSelectedItem().toString());
                    employeeDetails.put("startDate", startDate.getText().toString());
                    employeeDetails.put("designation", designation.getText().toString());
                    employeeDetails.put("employeeType", empType.getText().toString());
                }
                firebaseFirestore.collection("employees")
                        .document(empId.getText().toString())
                        .update(employeeDetails)
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            finish();
                        });
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                profilePicture.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(String empId) {
            storageReference.child("employeeProfilePicture/" + "PRO_IMG_" + empId).putFile(filePath);
    }
}