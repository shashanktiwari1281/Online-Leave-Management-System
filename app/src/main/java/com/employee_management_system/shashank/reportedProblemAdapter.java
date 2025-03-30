package com.employee_management_system.shashank;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Objects;

public class reportedProblemAdapter extends RecyclerView.Adapter<reportedProblemAdapter.viewHolder> {
    private final Context context;
    private int lastPosition = -1;
    static private ArrayList<QueryDocumentSnapshot> reportList;
    public reportedProblemAdapter(Context context, ArrayList<QueryDocumentSnapshot> reportList) {
        this.context = context;
        reportedProblemAdapter.reportList = reportList;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.unapproved_application_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        if (!reportList.isEmpty()) {
            QueryDocumentSnapshot docs = reportList.get(holder.getAdapterPosition());
            holder.relativeLayout.setVisibility(View.GONE);
            holder.constraintLayout.setVisibility(View.VISIBLE);
            holder.empName.setText(docs.getString("empName"));
            holder.empId.setText(docs.getString("empId"));
            holder.lvReason.setText(docs.getString("problem"));
            holder.lvReason.setVisibility(View.VISIBLE);
            holder.lvDate.setText(Objects.requireNonNull(docs.getTimestamp("reportedOn")).toDate().toString());
            holder.approveAppBtn.setText("View Profile");
            if(Boolean.FALSE.equals(docs.getBoolean("readFlag"))) {
                holder.newBadge.setVisibility(View.VISIBLE);
                FirebaseFirestore.getInstance().collection("reportedProblem")
                        .document(docs.getId())
                        .update("readFlag",true);
            }
            holder.approveAppBtn.setOnClickListener(view ->
                    context.startActivity(new Intent(context, employeeProfileViewActivity.class)
                        .putExtra("empId",docs.getString("empId"))));
            holder.rejectAppBtn.setText("Delete");
            holder.rejectAppBtn.setOnClickListener(v ->
                    FirebaseFirestore.getInstance().collection("reportedProblem")
                    .document(docs.getId()).delete()
                    .addOnCompleteListener(task -> {
                        reportList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }));
        }
        else Toast.makeText(context, "Data not fetched!", Toast.LENGTH_SHORT).show();
        setAnimation(holder.itemView, i);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView empName, empId, lvCategory, lvType, lvReason,lvDate;
        Button viewAppBtn, rejectAppBtn, approveAppBtn;
        ConstraintLayout constraintLayout;
        RelativeLayout relativeLayout;
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
            relativeLayout=v.findViewById(R.id.relativeLayout_UnapprovedRow);
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
