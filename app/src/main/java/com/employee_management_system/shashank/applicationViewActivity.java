package com.employee_management_system.shashank;
import static com.employee_management_system.shashank.miscellaneousMethods.timeStampToDateFormat;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class applicationViewActivity extends AppCompatActivity {
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_view);
        findViewById(R.id.backBtnAppViewPg).setOnClickListener(view -> finish());
        TextView title = findViewById(R.id.titleAppViewPg);
        title.setText(getIntent().getStringExtra("applicationId"));
        firebaseFirestore.collection("leaveApplied")
                .document(getIntent().getStringExtra("applicationId"))
                .get()
                .addOnCompleteListener(task -> {
                    if (getIntent().getStringExtra("calledBy").equals("employee")) {
                        findViewById(R.id.empNameAppViewPg).setVisibility(View.GONE);
                        findViewById(R.id.empNameLabelAppViewPg).setVisibility(View.GONE);
                        findViewById(R.id.empIdLabelAppViewPg).setVisibility(View.GONE);
                        findViewById(R.id.empIdAppViewPg).setVisibility(View.GONE);
                        try {
                            setViews(task.getResult());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (getIntent().getStringExtra("calledBy").equals("admin")) {
                        try {
                            TextView empName = findViewById(R.id.empNameAppViewPg);
                            empName.setText(task.getResult().getString("empName"));
                            TextView empId = findViewById(R.id.empIdAppViewPg);
                            empId.setText(task.getResult().getString("empId"));
                            setViews(task.getResult());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
    private void setViews(DocumentSnapshot docs) throws IOException {
        TextView applyDate=findViewById(R.id.applyDateAppViewPg);
        applyDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("appliedOn")).getSeconds(),"dd-MM-yyyy, hh:mm:s a"));
        TextView reportingTo=findViewById(R.id.reportingToAppViewPg);
        reportingTo.setText(docs.getString("reportingOfficer"));
        TextView leaveCategory=findViewById(R.id.leaveCategoryAppViewPg);
        leaveCategory.setText(docs.getString("leaveCategory"));
        TextView lvType=findViewById(R.id.leaveTypeAppViewPg);
        lvType.setText(docs.getString("leaveType"));
        TextView lvDate=findViewById(R.id.leaveDateAppViewPg);if (Objects.equals(docs.getString("leaveCategory"), "Short Leave"))
            lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy, ( hh:mm a")+timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds()+7200," - hh:mm a )"));
        else if (Objects.equals(docs.getString("leaveCategory"), "Half Day Leave"))
            lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd-MM-yyyy")+" "+docs.getString("timePeriod"));
        else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))==1)
            lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd-MM-yyyy"));
        else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))>1)
            lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("startDate")).getSeconds(),"dd-MM-yyyy")+" - "
                    +timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("endDate")).getSeconds(),"dd-MM-yyyy"));
        TextView status=findViewById(R.id.lvStatusAppViewPg);
        status.setText(docs.getString("leaveStatus"));
        if(Objects.equals(docs.getString("leaveStatus"), "Rejected")) {
            TextView rejectReason=findViewById(R.id.rejectionReasonAppViewPg);
            rejectReason.setText(docs.getString("rejectionReason"));
            findViewById(R.id.rejectReasonLabelAppViewPg).setVisibility(View.VISIBLE);
            rejectReason.setVisibility(View.VISIBLE);
        }
        if (Objects.requireNonNull(docs.getString("leaveReason")).length()>0) {
            TextView lvReason=findViewById(R.id.lvDescAppViewPg);
            lvReason.setText(docs.getString("leaveReason"));
            findViewById(R.id.lvDescLabelAppViewPg).setVisibility(View.VISIBLE);
            lvReason.setVisibility(View.VISIBLE);
        }
        if (!Objects.equals(docs.get("leaveDocID"), null)) {
            File localFile = File.createTempFile("images", "jpg");
            FirebaseStorage.getInstance().getReference("uploadedDocsForLeave/" + docs.getString("leaveDocID"))
                    .getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        ImageView docAttached = findViewById(R.id.docAttachedAppViewPg);
                        docAttached.setVisibility(View.VISIBLE);
                        findViewById(R.id.docAttachedLabelAppViewPg).setVisibility(View.VISIBLE);
                        docAttached.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                    });

        }
    }
}