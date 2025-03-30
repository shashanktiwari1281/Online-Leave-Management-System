package com.employee_management_system.shashank;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class miscellaneousMethods {
    public static String getTime(String format){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) return DateTimeFormatter.ofPattern(format).format(LocalDateTime.now());
        return "#";
    }
    public static Date getTimeStamp(String date){
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date);
        } catch (ParseException e){
            return null ;
        }
    }
    public static String timeStampToDateFormat(Long timestamp,String format){
        return new SimpleDateFormat(format).format(new Date(timestamp*1000));
    }
    public static void setBottomSlideInDialog(Dialog dialog){
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setAttributes(lp);
    }
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
        return 0;
    }
    public static void leaveApplied(Activity activity,Context context, String title, String textR1C1,String textR1C2,String textR2C1,String textR2C2,String textR3C1,String textR3C2){
        activity.setContentView(R.layout.leave_applied_layout);
        TextView titleTV=activity.findViewById(R.id.title_LeaveApplied);
        titleTV.setText(title);
        LinearLayout linearLayout = activity.findViewById(R.id.linearLayout_leaveApplied);
        ObjectAnimator animator = ObjectAnimator.ofFloat(linearLayout, "translationY", linearLayout.getHeight(), -(miscellaneousMethods.getScreenHeight(context) / 2) + 260);
        animator.setStartDelay(2000);
        animator.setDuration(2000);
        animator.start();
        LottieAnimationView tickAnimation = activity.findViewById(R.id.tickAnimation);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(tickAnimation, "scaleX", 1f, 0.6f);
        scaleXAnimator.setDuration(1000);
        scaleXAnimator.setStartDelay(2000);// Adjust duration as needed
        scaleXAnimator.start();
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(tickAnimation, "scaleY", 1f, 0.6f);
        scaleYAnimator.setDuration(1000);
        scaleXAnimator.setStartDelay(2000);// Adjust duration as needed
        scaleYAnimator.start();
        RelativeLayout relativeLayout = activity.findViewById(R.id.relativeLayout_leaveApplied);
        new Handler().postDelayed(() -> relativeLayout.setVisibility(View.VISIBLE), 2050);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(relativeLayout, "translationY", miscellaneousMethods.getScreenHeight(context), -relativeLayout.getHeight());
        animator2.setDuration(2000);
        animator2.setStartDelay(2000);
        animator2.start();
        TextView textR1C1TV=activity.findViewById(R.id.text_R1C1);
        textR1C1TV.setText(textR1C1);
        TextView textR1C2TV=activity.findViewById(R.id.text_R1C2);
        textR1C2TV.setText(textR1C2);
        TextView textR2C1TV=activity.findViewById(R.id.text_R2C1);
        textR2C1TV.setText(textR2C1);
        TextView textR2C2TV=activity.findViewById(R.id.text_R2C2);
        textR2C2TV.setText(textR2C2);
        TextView textR3C1TV=activity.findViewById(R.id.text_R3C1);
        textR3C1TV.setText(textR3C1);
        TextView textR3C2TV=activity.findViewById(R.id.text_R3C2);
        textR3C2TV.setText(textR3C2);
        activity.findViewById(R.id.goToHomeBtn_LeaveApplied).setOnClickListener(view -> activity.startActivity(new Intent(activity, launcherActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        activity.findViewById(R.id.gotItBtn_LeaveApplied).setOnClickListener(view -> activity.finish());
    }
    public static void successDialog(Activity activity,Context context,String msg){
        Dialog dialog=new Dialog(context);
        dialog.setContentView(R.layout.success_dialog_layout);
        dialog.setCanceledOnTouchOutside(false);
        TextView title=dialog.findViewById(R.id.title_successDialog);
        title.setText(msg);
        dialog.setOnCancelListener(dialogInterface -> activity.finish());
        dialog.findViewById(R.id.gotItBtn_successDialog).setOnClickListener(view -> dialog.cancel());
        dialog.show();
    }
    public static long countDays(int startDay,int startMonth,int startYear,int endDay,int endMonth,int endYear){
        LocalDate startDate = null, endDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startDate = LocalDate.of(startYear, startMonth, startDay);
            endDate = LocalDate.of(endYear, endMonth, endDay);
            return ChronoUnit.DAYS.between(startDate, endDate);
        }
        return 0;
    }
}