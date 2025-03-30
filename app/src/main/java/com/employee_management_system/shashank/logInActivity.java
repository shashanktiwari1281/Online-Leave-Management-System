package com.employee_management_system.shashank;
import static com.employee_management_system.shashank.miscellaneousMethods.setBottomSlideInDialog;
import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class logInActivity extends AppCompatActivity {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private boolean isWaitingForOTP=false, isMobileVerified =false, isFirstLogIn=false;
    private int minuteOTP, secondOTP;
    private String mobileNumber, userType="";
    private Spinner userTypeSpinner;
    private EditText mobileNoET,OTP_ET,employeeId_ET;
    private Button validateOTP_Btn,generateOPT,login_Btn;
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private SharedPreferences userDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        userDetails= getSharedPreferences("userDetails", MODE_PRIVATE);
        OTP_ET=findViewById(R.id.otp_ET);
        userTypeSpinner =findViewById(R.id.userTypeSpinner_login);
        userTypeSpinner.setAdapter(new ArrayAdapter<>(this,
                androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
                new String[]{"Select", "Employee", "Admin", "Super Admin", "Registrar"}));
        TextView firstLogin=findViewById(R.id.firstLogin);
        firstLogin.setOnClickListener(view -> {
            if(!isFirstLogIn){
                isFirstLogIn=true;
                generateOPT.setVisibility(View.VISIBLE);
                validateOTP_Btn.setVisibility(View.VISIBLE);
                OTP_ET.setHint("OTP Here");
                firstLogin.setText(R.string.login_using_password_click_here);
            } else{
                isFirstLogIn=false;
                generateOPT.setVisibility(View.GONE);
                validateOTP_Btn.setVisibility(View.GONE);
                OTP_ET.setHint("Password Here");
                firstLogin.setText(R.string.first_login_or_forgot_password_click_here);
            }
        });
        mobileNoET=findViewById(R.id.mobileNumberLogInPg_ET);
        OTP_ET.setHint("Password Here");
        employeeId_ET = findViewById(R.id.empId_ET);
        generateOPT=findViewById(R.id.generateOTP_Btn);
        generateOPT.setOnClickListener(v->{
            if (mobileNoET.getText().toString().length()!=10) Toast.makeText(this,
                    "Enter a valid mobile Number", Toast.LENGTH_SHORT).show();
            else if (noInternetActivity.isConnectionAvailable(getApplicationContext()))
                startActivity(new Intent(this,noInternetActivity.class));
            else if(!isWaitingForOTP) {
                minuteOTP =2;
                secondOTP =0;
                mobileNoET.setEnabled(false);
                generateOPT.setEnabled(false);
                mobileNumber=mobileNoET.getText().toString();
                authenticate("+91"+mobileNumber);
                isWaitingForOTP=true;
                final Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        if ((minuteOTP > 0 || secondOTP > 0)&&isWaitingForOTP) {
                            if (secondOTP == 0) {
                                minuteOTP--;
                                secondOTP = 59;
                            } else secondOTP--;
                            String str;
                            if(secondOTP<10) str= "0"+minuteOTP +":0"+ secondOTP;
                            else str= "0"+minuteOTP +":"+ secondOTP;
                            generateOPT.setText(str);
                            handler.postDelayed(this, 1000);
                        }
                        else {
                            isWaitingForOTP=false;
                            generateOPT.setEnabled(true);
                            generateOPT.setText(R.string.get_otp);
                        }
                    }
                };
                handler.post(r);
            } else Toast.makeText(this, "Wait for "+ minuteOTP +" minute "+ secondOTP +"second.", Toast.LENGTH_SHORT).show();
        });
        validateOTP_Btn=findViewById(R.id.validateOTP_Btn);
        validateOTP_Btn.setOnClickListener(v -> {
            if(isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this,noInternetActivity.class));
            else if(!OTP_ET.getText().toString().equals("")) {
                verifyCode(OTP_ET.getText().toString());
                generateOPT.setEnabled(false);
                OTP_ET.setEnabled(false);
                validateOTP_Btn.setEnabled(false);
                validateOTP_Btn.setText(R.string.verifying);
            } else Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
        });
        login_Btn=findViewById(R.id.logIn_Btn);
        login_Btn.setOnClickListener(view -> {
            userType=userTypeSpinner.getSelectedItem().toString();
            String empId=employeeId_ET.getText().toString();
            if(isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this,noInternetActivity.class));
            else if(userType.equals("Select")) Toast.makeText(this, "Select User Type.", Toast.LENGTH_SHORT).show();
            else if(mobileNoET.getText().length()!=10) Toast.makeText(this, "Enter Correct Mobile Number", Toast.LENGTH_SHORT).show();
            else if(!empId.contains("iise@")) Toast.makeText(this, "Enter Correct Employee Id", Toast.LENGTH_SHORT).show();
            else if(OTP_ET.getText().equals("")) Toast.makeText(this, "Enter PIN/Password", Toast.LENGTH_SHORT).show();
            else firebaseAuth.signInAnonymously().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    login_Btn.setEnabled(false);
                    login_Btn.setText(R.string.please_wait);
                    if(!isFirstLogIn) {
                        if (userType.equals("Super Admin")) regularLoginSuperAdmin();
                        else regularLoginEmployee();
                    } else if(isMobileVerified) {
                        if (userType.equals("Super Admin")) firstLoginSuperAdmin();
                        else firstLoginEmployee();
                    } else Toast.makeText(this, "Verify your Mobile Number", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            });
        });
        findViewById(R.id.logInReset_Btn).setOnClickListener(view -> reset());
    }
    private void reset(){
        mobileNoET.setEnabled(true);
        mobileNoET.setText("");
        generateOPT.setEnabled(true);
        OTP_ET.setEnabled(true);
        OTP_ET.setText("");
        validateOTP_Btn.setEnabled(true);
        employeeId_ET.setEnabled(true);
        employeeId_ET.setText("");
        login_Btn.setEnabled(true);
        isWaitingForOTP=false;
        login_Btn.setText(R.string.login);
        userTypeSpinner.setSelection(0);
    }
    private String verificationId;
    private void authenticate(String phoneNumber){
        PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        final String code = credential.getSmsCode();
                        if (code != null) verifyCode(code);
                    }
                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(verificationId, token);
                        logInActivity.this.verificationId = verificationId;
                        Toast.makeText(logInActivity.this, "OTP sent on mobile", Toast.LENGTH_SHORT).show();
                    }
                }).build());
    }
    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                validateOTP_Btn.setBackgroundColor(Color.GREEN);
                validateOTP_Btn.setTextColor(Color.WHITE);
                validateOTP_Btn.setText(R.string.verified);
                isMobileVerified =true;
            } else {
                isWaitingForOTP=false;
                generateOPT.setEnabled(true);
                OTP_ET.setEnabled(true);
                validateOTP_Btn.setEnabled(true);
                validateOTP_Btn.setText(R.string.validate_otp);
                if (task.getException() != null) Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void regularLoginSuperAdmin(){
        login_Btn.setText(R.string.please_wait);
        firebaseFirestore.collection("superAdmin")
                .whereEqualTo("adminId",employeeId_ET.getText().toString())
                .whereEqualTo("pass",OTP_ET.getText().toString())
                .whereEqualTo("mobile",mobileNoET.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.getResult().isEmpty()){
                        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                        userDetails.edit()
                                .putString("userType", userType)
                                .putString("userName", task.getResult().getDocuments().get(0).getString("name"))
                                .putBoolean("isLoggedIn", true).apply();
                        finish();
                    } else {
                        Toast.makeText(this, "No Record Found...", Toast.LENGTH_SHORT).show();
                        login_Btn.setText(R.string.login);
                        login_Btn.setEnabled(true);
                    }
                });
    }
    private void firstLoginSuperAdmin() {
        firebaseFirestore.collection("superAdmin")
                .whereEqualTo("adminId", employeeId_ET.getText().toString())
                .whereEqualTo("mobile", mobileNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.getResult().isEmpty()) {
                        Dialog createPasswordDialog = new Dialog(this);
                        createPasswordDialog.setContentView(R.layout.create_password_dialog);
                        createPasswordDialog.setCancelable(false);
                        setBottomSlideInDialog(createPasswordDialog);
                        createPasswordDialog.show();
                        createPasswordDialog.findViewById(R.id.submitPassword_CreatePassword).setOnClickListener(view1 -> {
                            EditText newPIN = createPasswordDialog.findViewById(R.id.newPasswordFirst),
                                    reEnterPIN = createPasswordDialog.findViewById(R.id.newPasswordReEnter);
                            if (newPIN.getText().toString().length() < 4)
                                Toast.makeText(this, "Password must be at least 4 characters long", Toast.LENGTH_SHORT).show();
                            else if (!newPIN.getText().toString().equals(reEnterPIN.getText().toString()))
                                Toast.makeText(this, "Re-entered Password not matched", Toast.LENGTH_SHORT).show();
                            else {
                                firebaseFirestore.collection("superAdmin").document(employeeId_ET.getText().toString())
                                        .update("pass", newPIN.getText().toString())
                                        .addOnCompleteListener(task1 -> {
                                            Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                                            userDetails.edit()
                                                    .putString("userType", userType)
                                                    .putString("userName", task.getResult().getDocuments().get(0).getString("name"))
                                                    .putBoolean("isLoggedIn", true).apply();
                                            finish();
                                        });
                            }
                        });
                    } else {
                        Toast.makeText(this, "No record Found!", Toast.LENGTH_SHORT).show();
                        employeeId_ET.setEnabled(true);
                        employeeId_ET.setText("");
                        login_Btn.setEnabled(true);
                        login_Btn.setText(R.string.login);
                    }
                });
    }
    private void regularLoginEmployee(){
        login_Btn.setText(R.string.please_wait);
        firebaseFirestore.collection("employees")
                .whereEqualTo("empId",employeeId_ET.getText().toString())
                .whereEqualTo("pass",OTP_ET.getText().toString())
                .whereEqualTo("userType",userType)
                .whereEqualTo("mobile",mobileNoET.getText().toString())
                .whereEqualTo("employmentStatus","Active")
                .get().addOnCompleteListener(task -> {
                    if(!task.getResult().isEmpty()) {
                        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                        userDetails.edit()
                                .putString("userType", userType)
                                .putString("userName", task.getResult().getDocuments().get(0).getString("name"))
                                .putString("empId", employeeId_ET.getText().toString())
                                .putBoolean("isLoggedIn", true).apply();
                        finish();
                    } else {
                        Toast.makeText(this, "No Record Found...", Toast.LENGTH_SHORT).show();
                        login_Btn.setText(R.string.login);
                        login_Btn.setEnabled(true);
                    }
                });
    }
    private void firstLoginEmployee(){
        firebaseFirestore.collection("employees")
                .whereEqualTo("empId",employeeId_ET.getText().toString())
                .whereEqualTo("mobile",mobileNumber)
                .whereEqualTo("userType",userType)
                .whereEqualTo("employmentStatus","Active")
                .get().addOnCompleteListener(task -> {
                    if (!task.getResult().isEmpty()) {
                        Dialog createPinDialog = new Dialog(this);
                        createPinDialog.setContentView(R.layout.create_password_dialog);
                        createPinDialog.setCancelable(false);
                        setBottomSlideInDialog(createPinDialog);
                        createPinDialog.show();
                        createPinDialog.findViewById(R.id.submitPassword_CreatePassword).setOnClickListener(view1 -> {
                            EditText newPIN = createPinDialog.findViewById(R.id.newPasswordFirst),
                                    reEnterPIN = createPinDialog.findViewById(R.id.newPasswordReEnter);
                            if (newPIN.getText().toString().length() < 4) Toast.makeText(this, "Password must be at least 4 characters long", Toast.LENGTH_SHORT).show();
                            else if (!newPIN.getText().toString().equals(reEnterPIN.getText().toString())) Toast.makeText(this, "Re-entered PIN not matched", Toast.LENGTH_SHORT).show();
                            else {
                                firebaseFirestore.collection("employees")
                                        .document(employeeId_ET.getText().toString())
                                        .update("pass",newPIN.getText().toString())
                                        .addOnCompleteListener(task1 -> {
                                            Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                                            userDetails.edit()
                                                    .putString("userType", userType)
                                                    .putString("userName", task.getResult().getDocuments().get(0).getString("name"))
                                                    .putString("empId", employeeId_ET.getText().toString())
                                                    .putBoolean("isLoggedIn", true).apply();
                                            finish();
                                        });
                            }
                        });
                    } else {
                        Toast.makeText(this, "No record Found!", Toast.LENGTH_SHORT).show();
                        employeeId_ET.setEnabled(true);
                        employeeId_ET.setText("");
                        login_Btn.setEnabled(true);
                        login_Btn.setText(R.string.login);
                    }
                });
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finishAffinity();
        System.exit(0);
    }
}