package com.employee_management_system.shashank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

public class noInternetActivity extends AppCompatActivity {
    private int sec=5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        Button tryAgain= findViewById(R.id.tryAgainBtnNoInternetPg);
        tryAgain.setOnClickListener(v -> {
            tryAgain.setText(R.string.getting_status);
            tryAgain.setEnabled(false);
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    if(sec > 0) {
                        if (!isConnectionAvailable(getApplicationContext())) finish();
                        else {
                            sec--;
                            handler.postDelayed(this, 1000);
                        }
                    }
                    else {
                        tryAgain.setText(R.string.try_again);
                        tryAgain.setEnabled(true);
                        sec=5;
                    }
                }
            };
            handler.post(r);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnectionAvailable(this)) finish();
    }
    static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo == null || !netInfo.isConnectedOrConnecting() || !netInfo.isAvailable();
        }
        return true;
    }
}