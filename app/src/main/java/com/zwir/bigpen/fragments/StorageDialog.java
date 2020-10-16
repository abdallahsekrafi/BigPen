package com.zwir.bigpen.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.zwir.bigpen.R;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.util.GlobalMethods;

import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 */
public class StorageDialog extends DialogFragment {
    AccountFragment accountFragment;
    TextView resultPrise,storage;
    int index=1;
    // create an AlertDialog and return it
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(),R.style.AppDialogStyle);
        View storageDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_dialog_storage, null);
        View customTitle = getActivity().getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_storage);
        ((TextView)customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_storage_dialog);
        // add GUI to dialog
        builder.setView(storageDialogView);
        // set the AlertDialog's message
        builder.setCustomTitle(customTitle);
        // get action
        resultPrise =storageDialogView.findViewById(R.id.result_coast);
        storage=storageDialogView.findViewById(R.id.counter_index);
        coastResult();
        storageDialogView.findViewById(R.id.counter_plus).setOnClickListener(counterListener);
        storageDialogView.findViewById(R.id.counter_min).setOnClickListener(counterListener);
        // Button action
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               if(accountFragment!=null)
                   accountFragment.boomMenu(index);
                dismiss(); // Cancel dialog
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss(); // Cancel dialog
            }
        });
        return builder.create(); // return dialog
    }
    private void coastResult(){
        try {
            double coast= GlobalMethods.reformDouble(index*Constants.megabytePrice) ;
            String strStorage=index+" "+ Constants.mO;
            storage.setText(strStorage);
            String strCoast=getString(R.string.total_price_str) +": "+ coast+" "+ Constants.usdDollar;
            resultPrise.setText(strCoast);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private View.OnClickListener counterListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.counter_plus:
                    if (index!=Constants.maxBayStorage){
                        index+=1;
                        coastResult();
                    }

                    break;
                case R.id.counter_min:
                    if (index!=1){
                        index-=1;
                        coastResult();
                    }
                    break;
            }
        }
    };
    // tell MainActivityFragment that dialog is now displayed
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       accountFragment=(AccountFragment)getParentFragmentManager().findFragmentByTag("account_fragment");
    }

}
