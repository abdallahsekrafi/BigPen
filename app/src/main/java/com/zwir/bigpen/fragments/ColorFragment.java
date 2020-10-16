package com.zwir.bigpen.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ramotion.fluidslider.FluidSlider;
import com.zwir.bigpen.R;
import com.zwir.bigpen.adapters.HistoricPenAdapter;
import com.zwir.bigpen.customs.SurfaceDraw;
import com.zwir.bigpen.data.Pen;
import com.zwir.bigpen.data.PenViewModel;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColorFragment extends DialogFragment {
    private FluidSlider redSeekBar;
    private FluidSlider greenSeekBar;
    private FluidSlider blueSeekBar;
    private ScrollView scrollViewColor;
    private CardView colorView;
    private int color;
    private HistoricPenAdapter historicPenAdapter;
    private PenViewModel penViewModel;

    // create an AlertDialog and return it
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // create dialog
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(),R.style.AppDialogStyle);
        View colorDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_color, null);
        View customTitle = getActivity().getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_fab_color_rgb);
        ((TextView)customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_color_dialog);
        builder.setView(colorDialogView); // add GUI to dialog
        penViewModel = ViewModelProviders.of(this).get(PenViewModel.class);
        // set the AlertDialog's message
        builder.setCustomTitle(customTitle);
        // get the color SeekBars
        redSeekBar = colorDialogView.findViewById(R.id.redSeekBar);
        greenSeekBar = colorDialogView.findViewById(R.id.greenSeekBar);
        blueSeekBar = colorDialogView.findViewById(R.id.blueSeekBar);
        colorView = colorDialogView.findViewById(R.id.colorView);
        //
        scrollViewColor=colorDialogView.findViewById(R.id.scroll_view_color);
        // use current drawing color to set SeekBar values
        final SurfaceDraw surfaceDraw = getDrawerFragment().getSurfaceDraw();
        fillColors(surfaceDraw.getDrawingColor());
        //
        RecyclerView recycler_list_historic_pencil = colorDialogView.findViewById(R.id.recycler_list_historic_pen);
        recycler_list_historic_pencil.setHasFixedSize(true);
        recycler_list_historic_pencil.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        historicPenAdapter =new HistoricPenAdapter(this,getContext(),penViewModel);
        DividerItemDecoration verticalDecoration = new DividerItemDecoration(recycler_list_historic_pencil.getContext(),
                DividerItemDecoration.HORIZONTAL);
        Drawable verticalDivider = ContextCompat.getDrawable(getContext(), R.drawable.divider);
        verticalDecoration.setDrawable(verticalDivider);
        recycler_list_historic_pencil.addItemDecoration(verticalDecoration);
        recycler_list_historic_pencil.setAdapter(historicPenAdapter);
        recycler_list_historic_pencil.setNestedScrollingEnabled(false);
        penViewModel.getPenList().observe(this, new Observer<List<Pen>>() {
            @Override
            public void onChanged(@Nullable final List<Pen> pens) {
                // Update the list of pens in the adapter.
                Collections.reverse(pens);
                historicPenAdapter.setPenList(pens);
            }
        });
        //
        scrollToBottom();
        // register SeekBar event listeners
        redSeekBar.setPositionListener(new Function1<Float, Unit>() {
            @Override
            public Unit invoke(Float aFloat) {
                color = Color.rgb(
                        (int)(aFloat*255), (int)(greenSeekBar.getPosition()*255),
                        (int)(blueSeekBar.getPosition()*255));
                redSeekBar.setBubbleText(String.valueOf((int)(aFloat*255)));
                colorView.setCardBackgroundColor(color);
                return Unit.INSTANCE;
            }
        });
        greenSeekBar.setPositionListener(new Function1<Float, Unit>() {
            @Override
            public Unit invoke(Float aFloat) {
                color = Color.rgb(
                        (int)(redSeekBar.getPosition()*255), (int)(aFloat*255),
                        (int)(blueSeekBar.getPosition()*255));
                greenSeekBar.setBubbleText(String.valueOf((int)(aFloat*255)));
                colorView.setCardBackgroundColor(color);
                return Unit.INSTANCE;
            }
        });
        blueSeekBar.setPositionListener(new Function1<Float, Unit>() {
            @Override
            public Unit invoke(Float aFloat) {
                color = Color.rgb(
                        (int)(redSeekBar.getPosition()*255), (int)(greenSeekBar.getPosition()*255),
                        (int)(aFloat*255));
                blueSeekBar.setBubbleText(String.valueOf((int)(aFloat*255)));
                colorView.setCardBackgroundColor(color);
                return Unit.INSTANCE;
            }
        });
        // save color
        colorDialogView.findViewById(R.id.save_historic_pen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historicPenAdapter.setAction(historicPenAdapter.insert_pen);
                penViewModel.savePen(new Pen(color));
                scrollToBottom();
            }
        });
        // Button action
        builder.setPositiveButton(R.string.button_set_color, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                surfaceDraw.setDrawingColor(color);
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
    // scroll to bottom
    private void scrollToBottom(){
        scrollViewColor.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollViewColor.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },500);
    }
    // fill colors
    public void fillColors(int newColor){
        color=newColor;
        redSeekBar.setPosition((float)(Color.red(newColor)/255.0));
        redSeekBar.setBubbleText(String.valueOf(Color.red(newColor)));
        greenSeekBar.setPosition((float)(Color.green(newColor)/255.0));
        greenSeekBar.setBubbleText(String.valueOf(Color.green(newColor)));
        blueSeekBar.setPosition((float)(Color.blue(newColor)/255.0));
        blueSeekBar.setBubbleText(String.valueOf(Color.blue(newColor)));
        colorView.setCardBackgroundColor(newColor);
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
