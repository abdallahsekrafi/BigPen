package com.zwir.bigpen.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramotion.fluidslider.FluidSlider;
import com.zwir.bigpen.R;
import com.zwir.bigpen.customs.SurfaceDraw;

/**
 * A simple {@link Fragment} subclass.
 */
public class LineWidthFragment extends DialogFragment {

    private FluidSlider lineWidthSeekBar;
    private ImageView lineWidthImageView;
    private int lineWidth;
    // create an AlertDialog and return it
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // create dialog
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppDialogStyle);
        View lineWidthDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_line_width, null);
        View customTitle = getActivity().getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_fab_line_width);
        ((TextView)customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_line_width_dialog);
        builder.setView(lineWidthDialogView); // add GUI to dialog
        // set the AlertDialog's message
        builder.setCustomTitle(customTitle);
        // get the ImageView
        lineWidthImageView = lineWidthDialogView.findViewById(
                R.id.lineWidthImageView);
        // get the line width SeekBars
        lineWidthSeekBar = lineWidthDialogView.findViewById(R.id.lineWidthSeekBar);
        // use current drawing color to set SeekBar values
        final SurfaceDraw surfaceDraw = getDrawerFragment().getSurfaceDraw();
        lineWidth=surfaceDraw.getLineWidth();
        lineWidthSeekBar.setPosition((float)(lineWidth/50.0));
        lineWidthSeekBar.setBubbleText(String.valueOf(lineWidth));
        drawLine(lineWidth);
        lineWidthSeekBar.setPositionListener(new Function1<Float, Unit>() {
            @Override
            public Unit invoke(Float aFloat) {
                lineWidth=(((int)(aFloat*50)==0)? 1:(int)(aFloat*50));
                lineWidthSeekBar.setBubbleText(String.valueOf(lineWidth));
                drawLine(lineWidth);
                return Unit.INSTANCE;
            }
        });
        // Button action
        builder.setPositiveButton(R.string.button_set_line_width, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                surfaceDraw.setLineWidth(lineWidth);
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
    private void drawLine(int width){
        final Bitmap bitmap = Bitmap.createBitmap(
                400, 100, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap); // draws into bitmap
        // configure a Paint object for the current SeekBar value
        Path path=new Path();
        Paint p = new Paint();
        p.setColor(
                getDrawerFragment().getSurfaceDraw().getDrawingColor());
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);
        // erase the bitmap and redraw the line
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bitmap.eraseColor(
                    getResources().getColor(R.color.transparent,
                            getContext().getTheme()));
        }
        else {
            bitmap.eraseColor(
                    getResources().getColor(R.color.transparent));
        }
        path.moveTo(30,50);
        path.cubicTo(115,100,285,0,370,50);
        canvas.drawPath(path,p);
        lineWidthImageView.setImageBitmap(bitmap);
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
