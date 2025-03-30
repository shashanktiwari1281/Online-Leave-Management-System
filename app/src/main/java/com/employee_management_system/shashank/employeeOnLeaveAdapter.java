package com.employee_management_system.shashank;
import static com.employee_management_system.shashank.miscellaneousMethods.timeStampToDateFormat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class employeeOnLeaveAdapter extends RecyclerView.Adapter<employeeOnLeaveAdapter.viewHolder> {
    private final Context context;
    private int lastPosition = -1;
    static private ArrayList<QueryDocumentSnapshot> applicationList;
    public employeeOnLeaveAdapter(Context context, ArrayList<QueryDocumentSnapshot> applicationList) {
        this.context = context;
        employeeOnLeaveAdapter.applicationList = applicationList;
    }
    @NonNull
    @Override
    public employeeOnLeaveAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.unapproved_application_row, viewGroup, false);
        return new employeeOnLeaveAdapter.viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull employeeOnLeaveAdapter.viewHolder holder, int i) {
        if (!applicationList.isEmpty()) {
            QueryDocumentSnapshot docs = applicationList.get(holder.getAdapterPosition());
            holder.empName.setText(docs.getString("empName"));
            holder.empId.setText(docs.getString("empId"));
            holder.lvCategory.setText(docs.getString("leaveCategory"));
            if (Objects.equals(docs.getString("leaveCategory"), "Short Leave"))
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy, ( hh:mm a")+timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds()+7200," - hh:mm a )"));
            else if (Objects.equals(docs.getString("leaveCategory"), "Half Day Leave"))
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd-MM-yyyy")+" "+docs.getString("timePeriod"));
            else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))==1)
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd-MM-yyyy"));
            else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))>1)
                holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("startDate")).getSeconds(),"dd-MM-yyyy")+" - "
                        +timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("endDate")).getSeconds(),"dd-MM-yyyy"));
            holder.lvType.setText(docs.getString("leaveType"));
            holder.constraintLayout.setVisibility(View.VISIBLE);
            holder.viewAppBtn.setText("View Profile");
            holder.viewAppBtn.setOnClickListener(v ->
                    context.startActivity(new Intent(context, employeeProfileViewActivity.class)
                            .putExtra("empId",docs.getString("empId"))));
            holder.approveAppBtn.setText("View Application");
            holder.approveAppBtn.setOnClickListener(view ->
                    context.startActivity(new Intent(context, applicationViewActivity.class)
                            .putExtra("empId",docs.getString("empId"))
                            .putExtra("calledBy", "admin")
                            .putExtra("applicationId",docs.getId())));
            holder.rejectAppBtn.setVisibility(View.GONE);
            holder.lineView.setVisibility(View.GONE);
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
        View lineView;
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
            lineView=v.findViewById(R.id.lineViewUnapprovedRow);
        }
    }
    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            view.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
            lastPosition = position;
        }
    }
}
