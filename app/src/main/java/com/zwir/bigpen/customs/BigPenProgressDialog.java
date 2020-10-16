package com.zwir.bigpen.customs;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
import com.zwir.bigpen.R;


public class BigPenProgressDialog {
    private Dialog progressDialog;
   public BigPenProgressDialog(Context context, String message) {
         progressDialog = new Dialog(context, R.style.ProgressDialog);
         progressDialog.setCancelable(false);
         progressDialog.setContentView(R.layout.progress_dialog_view);
         TextView msgView= progressDialog.findViewById(R.id.progress_msg);
         msgView.setText(message);
    }
    public void showMe(){
        progressDialog.show();
    }
    public void dismissMe(){
        progressDialog.dismiss();
    }
}
