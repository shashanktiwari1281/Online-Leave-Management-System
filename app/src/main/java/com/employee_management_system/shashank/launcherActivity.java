package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class launcherActivity extends AppCompatActivity {
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    private final double APP_VERSION=0.1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }
    @Override
    protected void onResume() {
        super.onResume();
        ImageView logo=findViewById(R.id.logoIV);
        logo.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_in_anim));
        SharedPreferences userDetails= getSharedPreferences("userDetails", MODE_PRIVATE);
        if(isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this,noInternetActivity.class));
        else{
            db.collection("basicDetails").document("basicAttributes")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!(APP_VERSION==Objects.requireNonNull(task.getResult().getDouble("appVersion")))){
                                new AlertDialog.Builder(this).setTitle("Update Available!")
                                        .setMessage("Download and Install the new version to continue service...")
                                        .setPositiveButton("Download", (dialog, which) -> {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(task.getResult().getString("appUrl"))));
                                            dialog.dismiss();
                                        }).setNegativeButton("Ignore", (dialog, which) -> {
                                            finish();
                                            dialog.dismiss();
                                        }).create().show();
                            } else if (!userDetails.getBoolean("isLoggedIn", false)) new Handler().postDelayed(()-> startActivity(new Intent(this, logInActivity.class)),1000);
                            else {
                                String userType=userDetails.getString("userType", null);
                                logo.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out_anim));
                                new Handler().postDelayed(() -> {
                                    if (userType.equals("Admin")||userType.equals("Employee")||userType.equals("HOD")||userType.equals("Registrar")) startActivity(new Intent(this, MainActivity.class));
                                    else if (userType.equals("Super Admin")) startActivity(new Intent(this, adminHomeActivity.class));
                                    finish();
                                }, 450);
                            }
                        } else Toast.makeText(getApplicationContext(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}