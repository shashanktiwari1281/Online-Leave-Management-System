package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class empListAdapter extends RecyclerView.Adapter<empListAdapter.viewHolder> {
    private final Context context;
    private int lastPosition = -1;
    private final String userType;
    static private ArrayList<QueryDocumentSnapshot> employeeList;
    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    public empListAdapter(Context context, ArrayList<QueryDocumentSnapshot> employeeList,String userType) {
        this.context = context;
        this.userType =userType;
        empListAdapter.employeeList = employeeList;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.employee_list_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        if (!employeeList.isEmpty()) {
            QueryDocumentSnapshot docs = employeeList.get(holder.getAdapterPosition());
            if (!Objects.equals(docs.getString("profilePictureId"), "")) {
                File localFile;
                try {
                    localFile = File.createTempFile("images", "jpg");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File finalLocalFile = localFile;
                FirebaseStorage.getInstance().getReference("employeeProfilePicture/" + docs.getString("profilePictureId"))
                        .getFile(finalLocalFile)
                        .addOnSuccessListener(taskSnapshot ->
                            holder.profilePic.setImageBitmap(BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath()))
                        );
            }
            holder.empName.setText(docs.getString("name"));
            holder.designation.setText(docs.getString("designation"));
            holder.department.setText(docs.getString("department"));
            holder.workLocation.setText(docs.getString("workLocation"));
            if(userType.equals("Registrar")&& Objects.equals(docs.getString("userType"), "Admin")) holder.constraintLayout.setVisibility(View.GONE);
            holder.viewProfile.setOnClickListener(v ->
                context.startActivity(new Intent(context, employeeProfileViewActivity.class)
                        .putExtra("empId",docs.getString("empId"))));
            holder.removeEmp.setOnClickListener(v -> {
                Dialog removeDateDialog=new Dialog(context);
                removeDateDialog.setContentView(R.layout.reject_reason_dialog);
                removeDateDialog.setCanceledOnTouchOutside(false);
                removeDateDialog.show();
                TextView title=removeDateDialog.findViewById(R.id.titleRejectDialog);
                title.setText(R.string.enter_job_end_date);
                EditText reason=removeDateDialog.findViewById(R.id.rejectReason_ET_RejectDialog);
                reason.getLayoutParams().height=50;
                reason.setInputType(InputType.TYPE_CLASS_DATETIME);
                reason.setHint("dd-mm-yyyy");
                removeDateDialog.findViewById(R.id.submitReasonBtn_RejectDialog).setOnClickListener(view -> {
                    if (isConnectionAvailable(context)) context.startActivity(new Intent(context, noInternetActivity.class));
                    else if(reason.getText().toString().equals(""))
                        Toast.makeText(context, "Field is mandatory ***", Toast.LENGTH_SHORT).show();
                    else {
                        Map<String,Object> data=new HashMap<>();
                        data.put("employmentStatus","Inactive");
                        data.put("endDate",reason.getText().toString());
                        firebaseFirestore.collection("employees")
                                .document(Objects.requireNonNull(docs.getString("empId")))
                                .update(data)
                                .addOnCompleteListener(task -> {
                                    removeDateDialog.dismiss();
                                    employeeList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                });
                    }
                });
            });
        }
        else Toast.makeText(context, "Data not fetched!", Toast.LENGTH_SHORT).show();
        setAnimation(holder.itemView, i);
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView empName,  designation, department, workLocation;
        Button viewProfile, removeEmp;
        ConstraintLayout constraintLayout;
        public viewHolder(View v) {
            super(v);
            profilePic=v.findViewById(R.id.profilePic_EmpListRow);
            empName = v.findViewById(R.id.name_EmpListRow);
            designation=v.findViewById(R.id.designation_EmpListRow);
            department=v.findViewById(R.id.department_EmpListRow);
            workLocation=v.findViewById(R.id.workLocation_EmpListRow);
            viewProfile=v.findViewById(R.id.viewProfile_EmpListRow);
            removeEmp=v.findViewById(R.id.remove_EmpListRow);
            constraintLayout=v.findViewById(R.id.buttonLayoutEmpListRow);
        }
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            view.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
            lastPosition = position;
        }
    }
}
