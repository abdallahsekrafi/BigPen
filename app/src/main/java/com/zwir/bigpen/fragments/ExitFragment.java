package com.zwir.bigpen.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.zwir.bigpen.R;
import com.zwir.bigpen.activitys.MainBigPen;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExitFragment extends DialogFragment {
    // create an AlertDialog and return it
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(),R.style.AppDialogStyle);
        View customTitle = getActivity().getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_exit);
        ((TextView)customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_close_app);
        // set the AlertDialog's message
        builder.setCustomTitle(customTitle);
        builder.setMessage(R.string.message_close_app);
        // Button action
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (FirebaseAuth.getInstance().getCurrentUser()!=null)
                    FirebaseAuth.getInstance().signOut();
                ((MainBigPen)getActivity()).registerAlarm();
                getActivity().finishAffinity();
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
    // gets a reference to the MainActivityFragment
    private DrawerFragment getDrawerFragment() {
        return (DrawerFragment) getParentFragmentManager().findFragmentById(
                R.id.surfaceDrawFragment);
    }

    // tell MainActivityFragment that dialog is now displayed
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DrawerFragment fragment = getDrawerFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(true);
    }

    // tell MainActivityFragment that dialog is no longer displayed
    @Override
    public void onDetach() {
        super.onDetach();
        DrawerFragment fragment = getDrawerFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(false);
    }
}
