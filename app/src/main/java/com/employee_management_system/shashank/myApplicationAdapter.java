package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.miscellaneousMethods.timeStampToDateFormat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class myApplicationAdapter extends RecyclerView.Adapter<myApplicationAdapter.viewHolder> {
    private final Context context;
    private int lastPosition = -1;
    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    ArrayList<QueryDocumentSnapshot> myLeaveApplication;
    private final SharedPreferences userDetails;
    public myApplicationAdapter(Context context, ArrayList<QueryDocumentSnapshot> myLeaveApplication,SharedPreferences userDetails) {
        this.context = context;
        this.myLeaveApplication=myLeaveApplication;
        this.userDetails=userDetails;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.my_application_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        if (!myLeaveApplication.isEmpty()) {
            QueryDocumentSnapshot docs = myLeaveApplication.get(holder.getAdapterPosition());
            holder.status.setText(docs.getString("leaveStatus"));
            if (Objects.equals(docs.getString("leaveStatus"), "Approved")) holder.status.setTextColor(Color.GREEN);
            else if(Objects.equals(docs.getString("leaveStatus"), "Rejected")) {
                holder.status.setTextColor(Color.RED);
                holder.rejectReason.setText(docs.getString("rejectionReason"));
                holder.rejectReason.setVisibility(View.VISIBLE);
            }
            holder.lvCategory.setText(docs.getString("leaveCategory"));
            holder.lvType.setText(docs.getString("leaveType"));
            holder.lvReason.setText(docs.getString("leaveReason"));
            if (Objects.requireNonNull(docs.getString("leaveReason")).length()>0) holder.lvReason.setVisibility(View.VISIBLE);
            if (Objects.equals(docs.getString("leaveCategory"), "Short Leave"))
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy, ( hh:mm a")
                        +timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds()+7200," - hh:mm a )"));
            else if (Objects.equals(docs.getString("leaveCategory"), "Half Day Leave"))
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy")+" "+docs.getString("leaveTimePeriod"));
            else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))==1)
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy"));
            else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))>1)
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("startDate")).getSeconds(),"dd/MM/yyyy")+" - "
                        +timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("endDate")).getSeconds(),"dd/MM/yyyy"));
            holder.viewAppBtn.setOnClickListener(v ->
                    context.startActivity(new Intent(context, applicationViewActivity.class)
                            .putExtra("institutionName",context.getSharedPreferences("userDetails", Context.MODE_PRIVATE).getString("institutionName", "Blank"))
                            .putExtra("empId",userDetails.getString("empId","Blank"))
                    .putExtra("calledBy","employee")
                    .putExtra("applicationId",docs.getId())));
            holder.cancelAppBtn.setOnClickListener(view ->
                    new AlertDialog.Builder(context)
                    .setTitle("Cancel Leave")
                    .setMessage("Are you sure want to cancel the leave of date "+holder.lvDate.getText().toString()+" ?")
                    .setPositiveButton("Yes", (dialogInterface, i1) -> firebaseFirestore.collection("leaveApplied")
                            .document(docs.getId())
                            .delete()
                            .addOnCompleteListener(task -> {
                                String leaveType=Objects.requireNonNull(docs.getString("leaveType"));
                                if(!(leaveType.equals("Duty Leave")||leaveType.equals("Research Leave"))) firebaseFirestore.collection("employees")
                                        .document(Objects.requireNonNull(docs.getString("empId")))
                                        .collection("leaveBalance")
                                        .document("remainLeaveBalanceChart")
                                        .update(leaveType, FieldValue.increment(+Objects.requireNonNull(docs.getLong("numberOfLeave"))));
                                firebaseFirestore.collection("employees")
                                        .document(Objects.requireNonNull(docs.getString("empId")))
                                        .collection("leaveBalance")
                                        .document("consumedLeaveBalanceChart")
                                        .update(leaveType,FieldValue.increment(-Objects.requireNonNull(docs.getLong("numberOfLeave"))))
                                        .addOnCompleteListener(task1 -> {
                                            myLeaveApplication.remove(holder.getAdapterPosition());
                                            notifyItemRemoved(holder.getAdapterPosition());
                                        });
                            }))
                    .setNegativeButton("No",((dialogInterface, i1) -> {})).show());
        }
        else Toast.makeText(context, "Data not fetched!", Toast.LENGTH_SHORT).show();
        setAnimation(holder.itemView, i);
    }
    @Override
    public int getItemCount() {
        return myLeaveApplication.size();
    }
    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView lvCategory, lvType, rejectReason, status, lvReason, lvDate;
        Button viewAppBtn, cancelAppBtn;
        public viewHolder(View v) {
            super(v);
            rejectReason = v.findViewById(R.id.rejectReasonMyAppRow);
            status = v.findViewById(R.id.statusMyAppRow);
            lvCategory = v.findViewById(R.id.lvCategoryMyAppRow);
            lvType = v.findViewById(R.id.lvTypeMyAppRow);
            lvReason=v.findViewById(R.id.lvDescMyAppRow);
            viewAppBtn = v.findViewById(R.id.viewAppBtn_MyAppRow);
            cancelAppBtn = v.findViewById(R.id.cancelAppBtn_MyAppRow);
            lvDate=v.findViewById(R.id.lvDateTV_MyAppPg);
        }
    }
    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            view.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
            lastPosition = position;
        }
    }
}