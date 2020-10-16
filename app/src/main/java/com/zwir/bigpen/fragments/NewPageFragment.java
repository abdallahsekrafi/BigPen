package com.zwir.bigpen.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zwir.bigpen.R;
import com.zwir.bigpen.customs.SurfaceDraw;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewPageFragment extends DialogFragment {

    private String texture= SurfaceDraw.squareTexture;
    private RadioButton radioButtonSq,radioButtonLn,radioButtonBk;

    // create an AlertDialog and return it
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(),R.style.AppDialogStyle);
        View newPageDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_new_page, null);
        View customTitle = getActivity().getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_fab_new_page);
        ((TextView)customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_new_project_dialog);
        builder.setView(newPageDialogView); // add GUI to dialog
        // set the AlertDialog's message
        builder.setCustomTitle(customTitle);
        radioButtonSq=newPageDialogView.findViewById(R.id.radio_button1);
        radioButtonLn=newPageDialogView.findViewById(R.id.radio_button2);
        radioButtonBk=newPageDialogView.findViewById(R.id.radio_button3);
        // action Listener
        newPageDialogView.findViewById(R.id.card_square_texture).setOnClickListener(cardClickListener);
        newPageDialogView.findViewById(R.id.card_line_texture).setOnClickListener(cardClickListener);
        newPageDialogView.findViewById(R.id.card_blank_texture).setOnClickListener(cardClickListener);
        // Button action
        builder.setPositiveButton(R.string.button_create_new_project, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDrawerFragment().getSurfaceDraw().newPage(texture); // new Page
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
    View.OnClickListener cardClickListener=  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CardView cardView= (CardView) v;
            switch (cardView.getId()) {
                case R.id.card_square_texture:
                    texture=SurfaceDraw.squareTexture;
                    radioButtonSq.setVisibility(View.VISIBLE);
                    radioButtonLn.setVisibility(View.INVISIBLE);
                    radioButtonBk.setVisibility(View.INVISIBLE);
                    break;
                case R.id.card_line_texture:
                    texture=SurfaceDraw.lineTexture;
                    radioButtonSq.setVisibility(View.INVISIBLE);
                    radioButtonLn.setVisibility(View.VISIBLE);
                    radioButtonBk.setVisibility(View.INVISIBLE);
                    break;
                case R.id.card_blank_texture:
                    texture=SurfaceDraw.blankTexture;
                    radioButtonSq.setVisibility(View.INVISIBLE);
                    radioButtonLn.setVisibility(View.INVISIBLE);
                    radioButtonBk.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };
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
