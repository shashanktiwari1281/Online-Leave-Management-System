package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.miscellaneousMethods.timeStampToDateFormat;
import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class unapprovedAppAdapter extends RecyclerView.Adapter<unapprovedAppAdapter.viewHolder> {
    private final Context context;
    private int lastPosition = -1;
    private final String userName, userType;
    static private ArrayList<QueryDocumentSnapshot> applicationList;
    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    public unapprovedAppAdapter(Context context, ArrayList<QueryDocumentSnapshot> applicationList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.userName =sharedPreferences.getString("userName", null);
        this.userType=sharedPreferences.getString("userType", null);
        unapprovedAppAdapter.applicationList = applicationList;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.unapproved_application_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        if (!applicationList.isEmpty()) {
            QueryDocumentSnapshot docs = applicationList.get(holder.getAdapterPosition());
            holder.empName.setText(docs.getString("empName"));
            holder.empId.setText(docs.getString("empId"));
            holder.lvCategory.setText(docs.getString("leaveCategory"));
            holder.lvType.setText(docs.getString("leaveType"));
            if(Boolean.FALSE.equals(docs.getBoolean("readFlag"))) {
                holder.newBadge.setVisibility(View.VISIBLE);
                firebaseFirestore.collection("leaveApplied")
                        .document(docs.getId())
                        .update("readFlag",true);
            }



            else holder.newBadge.setVisibility(View.GONE);
            if (Objects.requireNonNull(docs.getString("leaveReason")).length()>0) {
                holder.lvReason.setText(docs.getString("leaveReason"));
                holder.lvReason.setVisibility(View.VISIBLE);
            }
            if (Objects.equals(docs.getString("leaveCategory"), "Short Leave"))
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy, ( hh:mm a")+timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds()+7200," - hh:mm a )"));
            else if (Objects.equals(docs.getString("leaveCategory"), "Half Day Leave"))
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy")+" "+docs.getString("leaveTimePeriod"));
            else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))==1)
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy"));
            else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))>1)
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("startDate")).getSeconds(),"dd/MM/yyyy")+" - "
                        +timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("endDate")).getSeconds(),"dd/MM/yyyy"));
            if(userName.equals(docs.getString("reportingOfficer"))&&(Objects.requireNonNull(docs.getString("leaveStatus")).equals("Pending")||Objects.requireNonNull(docs.getString("leaveStatus")).equals("Forwarded"))) holder.constraintLayout.setVisibility(View.VISIBLE);
            holder.viewAppBtn.setOnClickListener(v -> context.startActivity(new Intent(context, applicationViewActivity.class)
                    .putExtra("empId",docs.getString("empId"))
                    .putExtra("calledBy", "admin")
                    .putExtra("applicationId",docs.getId())));
            if(userType.equals("Admin")&&Objects.requireNonNull(docs.getLong("numberOfLeave"))>3) holder.approveAppBtn.setText("Forward");
            holder.approveAppBtn.setOnClickListener(view -> {
                Map<String,Object> update=new HashMap<>(3);
                update.put("readFlag",false);
                update.put("leaveStatus","Forwarded");
                if (isConnectionAvailable(context)) context.startActivity(new Intent(context, noInternetActivity.class));
                else if(userType.equals("Admin")&&Objects.requireNonNull(docs.getLong("numberOfLeave"))>3) {
                    firebaseFirestore.collection("employees")
                            .document(Objects.requireNonNull(docs.getString("empId")))
                            .get().addOnCompleteListener(task -> firebaseFirestore.collection("superAdmin")
                                    .get().addOnCompleteListener(task1 -> {
                                        update.put("reportingOfficer", task1.getResult().getDocuments().get(0).getString("name"));
                                        firebaseFirestore.collection("leaveApplied").document(docs.getId())
                                                .update(update)
                                                .addOnCompleteListener(task2 -> {
                                                    applicationList.remove(holder.getAdapterPosition());
                                                    notifyItemRemoved(holder.getAdapterPosition());
                                                });
                                    }));
                }else {
                    new AlertDialog.Builder(context)
                            .setTitle("Approve Leave")
                            .setMessage("Are you sure want to Approve the leave of " + docs.getString("empName") + " on " + holder.lvDate.getText() + "?")
                            .setPositiveButton("Yes", (dialogInterface, i1) -> firebaseFirestore.collection("leaveApplied")
                                    .document(docs.getId())
                                    .update("leaveStatus", "Approved")
                                    .addOnCompleteListener(task -> {
                                        applicationList.remove(holder.getAdapterPosition());
                                        notifyItemRemoved(holder.getAdapterPosition());
                                    }))
                            .setNegativeButton("No", ((dialogInterface, i1) -> {}))
                            .show();
                }
            });
            holder.rejectAppBtn.setOnClickListener(v -> {
                Dialog rejectReasonDialog=new Dialog(context);
                rejectReasonDialog.setContentView(R.layout.reject_reason_dialog);
                rejectReasonDialog.setCanceledOnTouchOutside(false);
                rejectReasonDialog.show();
                rejectReasonDialog.findViewById(R.id.submitReasonBtn_RejectDialog).setOnClickListener(view -> {
                    EditText reason=rejectReasonDialog.findViewById(R.id.rejectReason_ET_RejectDialog);
                    if (isConnectionAvailable(context)) context.startActivity(new Intent(context, noInternetActivity.class));
                    else if(reason.getText().toString().equals("")) Toast.makeText(context, "Field is mandatory ***", Toast.LENGTH_SHORT).show();
                    else {
                        Map<String,Object> data=new HashMap<>(2);
                        data.put("leaveStatus","Rejected");
                        data.put("rejectionReason",reason.getText().toString());
                        firebaseFirestore.collection("leaveApplied")
                                .document(docs.getId())
                                .update(data)
                                .addOnCompleteListener(task -> {
                                    String leaveType=Objects.requireNonNull(docs.getString("leaveType"));
                                    if(!(leaveType.equals("Duty Leave")||leaveType.equals("Research Leave"))) firebaseFirestore.collection("employees")
                                            .document(Objects.requireNonNull(docs.getString("empId")))
                                            .collection("leaveBalance")
                                            .document("remainLeaveBalanceChart")
                                            .update(leaveType,FieldValue.increment(+Objects.requireNonNull(docs.getLong("numberOfLeave"))));
                                    firebaseFirestore.collection("employees")
                                            .document(Objects.requireNonNull(docs.getString("empId")))
                                            .collection("leaveBalance")
                                            .document("consumedLeaveBalanceChart")
                                            .update(leaveType,FieldValue.increment(-Objects.requireNonNull(docs.getLong("numberOfLeave"))))
                                            .addOnCompleteListener(task1 -> {
                                                rejectReasonDialog.dismiss();
                                                applicationList.remove(holder.getAdapterPosition());
                                                notifyItemRemoved(holder.getAdapterPosition());
                                            });
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
        return applicationList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView empName, empId, lvCategory, lvType, lvReason,lvDate;
        Button viewAppBtn, rejectAppBtn, approveAppBtn;
        ConstraintLayout constraintLayout;
        ImageView newBadge;
        public viewHolder(View v) {
            super(v);
            empName = v.findViewById(R.id.empNameUnapprovedRow);
            empId = v.findViewById(R.id.empIdUnapprovedRow);
            lvCategory = v.findViewById(R.id.lvCategoryUnapprovedRow);
            lvType = v.findViewById(R.id.lvTypeUnapprovedRow);
            lvReason=v.findViewById(R.id.lvDescUnapprovedRow);
            viewAppBtn = v.findViewById(R.id.viewAppUnapprovedRow);
            rejectAppBtn = v.findViewById(R.id.rejectAppBtn_UnapprovedRow);
            approveAppBtn = v.findViewById(R.id.approveAppBtn_UnapprovedRow);
            lvDate=v.findViewById(R.id.lvDateTV_unapprovedAppPg);
            constraintLayout=v.findViewById(R.id.constraintLayoutUnapproved);
            newBadge=v.findViewById(R.id.newBadgeUnapprovedApp);
        }
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            view.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
            lastPosition = position;
        }
    }
}
