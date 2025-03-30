package com.employee_management_system.shashank;

import static com.employee_management_system.shashank.miscellaneousMethods.timeStampToDateFormat;
import static com.employee_management_system.shashank.noInternetActivity.isConnectionAvailable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
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

public class pastLeaveAdapter extends RecyclerView.Adapter<pastLeaveAdapter.viewHolder> {
    private final Context context;
    static private ArrayList<QueryDocumentSnapshot> applicationList;
    public pastLeaveAdapter(Context context, ArrayList<QueryDocumentSnapshot> applicationList) {
        this.context = context;
        pastLeaveAdapter.applicationList = applicationList;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.past_leave_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        QueryDocumentSnapshot docs = applicationList.get(holder.getAdapterPosition());
        holder.lvCategory.setText(docs.getString("leaveCategory"));
        holder.lvType.setText(docs.getString("leaveType"));
        holder.lvReason.setText(docs.getString("leaveReason"));
        if (Objects.requireNonNull(docs.getString("leaveReason")).length() > 0)
            holder.lvReason.setVisibility(View.VISIBLE);
        if (Objects.equals(docs.getString("leaveCategory"), "Short Leave"))
            holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd/MM/yyyy, ( hh:mm a")+timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds()+7200," - hh:mm a )"));
        else if (Objects.equals(docs.getString("leaveCategory"), "Half Day Leave"))
            holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd-MM-yyyy")+" "+docs.getString("timePeriod"));
        else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))==1)
            holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("leaveDate")).getSeconds(),"dd-MM-yyyy"));
        else if(Objects.requireNonNull(docs.getLong("numberOfLeave"))>1)
            holder.lvDate.setText(timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("startDate")).getSeconds(),"dd-MM-yyyy")+" - "
                    +timeStampToDateFormat(Objects.requireNonNull(docs.getTimestamp("endDate")).getSeconds(),"dd-MM-yyyy"));
    }
    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView lvCategory, lvType, lvReason,lvDate;
        public viewHolder(View v) {
            super(v);
            lvCategory = v.findViewById(R.id.leaveCategory_PastLeavePg);
            lvType = v.findViewById(R.id.leaveType_PastLeavePg);
            lvReason=v.findViewById(R.id.leaveReason_PastLeavePg);
            lvDate=v.findViewById(R.id.leaveDate_PastLeavePg);
        }
    }
}
