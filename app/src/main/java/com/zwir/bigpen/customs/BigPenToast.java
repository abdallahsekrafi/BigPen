package com.zwir.bigpen.customs;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.zwir.bigpen.R;

/**
 * Created by Sekrafi_Abdallah on 05/12/2016.
 */

public class BigPenToast {
    private Context context;
    private int message;
    private int imageRes;

    public BigPenToast(Context context, int imageRes, int message) {
        this.context = context;
        this.message=message;
        this.imageRes = imageRes;
        toastShow();
    }

    private void toastShow() {
        // Inflating the layout for the toast
        // Creating the Toast
        final Toast toast = new Toast(context);
        View v = LayoutInflater.from(context).inflate(R.layout.big_pen_toast,null);
// Typecasting and finding the view in the inflated layout
        TextView toastMessage =v.findViewById(R.id.toast_msg);
        ImageView toastIcon=v.findViewById(R.id.toast_icon);
// Setting the text to be displayed in the Toast
        toastMessage.setText(message);
        toastIcon.setImageResource(imageRes);
// Creating the Toast

// Setting the position of the Toast to centre
        toast.setGravity(Gravity.CENTER, 0, 0);

// Setting the duration of the Toast
        toast.setDuration(Toast.LENGTH_SHORT);

// Setting the Inflated Layout to the Toast
        toast.setView(v);

// Showing the Toast
        toast.show();
    }

}
