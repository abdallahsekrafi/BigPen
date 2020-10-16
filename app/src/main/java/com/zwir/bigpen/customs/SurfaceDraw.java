package com.zwir.bigpen.customs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.print.PrintHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zwir.bigpen.R;
import com.zwir.bigpen.data.BookPage;
import com.zwir.bigpen.util.Constants;

import java.io.ByteArrayOutputStream;


// custom View for drawing
public class SurfaceDraw extends View {

    private Path drawPath; //drawing path
    private Paint paintLine;//drawing and canvas paint
    private Canvas bitmapCanvas; // used to to draw on the resultBitmapDrawing
    private Bitmap resultBitmapDrawing; // drawing area for displaying or saving
    private final Paint paintScreen; // used to draw resultBitmapDrawing onto screen
    private Boolean handMode=false; // used to check if mode is draw or hand
    private float previousTouchX,previousTouchY;
    public static final String squareTexture="square";
    public static final String lineTexture="line";
    public static final String blankTexture ="blank";
    private Boolean isFirstTime=true;
    // SurfaceDraw constructor initializes the SurfaceDraw
    public SurfaceDraw(Context context, AttributeSet attrs) {
        super(context, attrs); // pass context to View's constructor
        paintScreen = new Paint(Paint.DITHER_FLAG); // used to display resultBitmapDrawing onto screen
        drawPath = new Path(); // used to catch each path
        // set the initial display settings for the painted line
        paintLine = new Paint(Paint.DITHER_FLAG);
        paintLine.setAntiAlias(true); // smooth edges of drawn line
        paintLine.setDither(true);
        paintLine.setColor(Color.BLACK); // default color is black
        paintLine.setStyle(Paint.Style.STROKE); // solid line
        paintLine.setStrokeWidth(5); // set the default line width
        paintLine.setStrokeCap(Paint.Cap.ROUND); // rounded line ends
        paintLine.setStrokeJoin(Paint.Join.ROUND);
    }
    // creates Bitmap and Canvas based on View's size
    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        if (isFirstTime){
        resultBitmapDrawing = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(resultBitmapDrawing);
            newPage(squareTexture);
            isFirstTime=false;
        }

    }
    // newPage the painting
    public void newPage(String texture) {
        Bitmap bitmapTexture;
        resultBitmapDrawing.eraseColor(Color.WHITE);
        switch (texture){
            case squareTexture:
                bitmapTexture= Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(getResources(), R.drawable.square_texture),
                        getWidth(),getHeight(),true);
                bitmapCanvas.drawBitmap(bitmapTexture,0,0,paintScreen);
                bitmapTexture.recycle();
                break;
            case lineTexture:
                bitmapTexture= Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(getResources(), R.drawable.line_texture),
                        getWidth(),getHeight(),true);
                bitmapCanvas.drawBitmap(bitmapTexture,0,0,paintScreen);
                bitmapTexture.recycle();
                break;
        }
        invalidate(); // refresh the screen
    }
    //
    public Boolean getHandMode(){
        return handMode;
    }
    public void setHandMode(Boolean state){
        handMode=state;
    }
    // set the painted line's color
    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    // return the painted line's color
    public int getDrawingColor() {
        return paintLine.getColor();
    }

    // set the painted line's width
    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    // return the painted line's width
    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    // perform custom drawing when the SurfaceDraw is refreshed on screen
    @Override
    protected void onDraw(Canvas canvas) {
        // draw the background screen
        canvas.drawBitmap(resultBitmapDrawing, 0, 0, paintScreen);
        // draw line
        canvas.drawPath(drawPath, paintLine);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!handMode) {
            //respond to down, move and slide_from_top events
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStarted(event.getX(),event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMoved(event);
                    break;
                case MotionEvent.ACTION_UP:
                    touchEnded();
                    break;
                default:
                    return false;
            }
            //redraw
            invalidate();
        }
        return true;
    }
    private void touchStarted(float touchX, float touchY){
        // move to the coordinates of the touch
        drawPath.moveTo(touchX, touchY);
        previousTouchX=touchX;
        previousTouchY=touchY;
    }
    private void touchMoved(MotionEvent event){
        // get the new coordinates
        float newX = event.getX();
        float newY = event.getY();
        // move the path to the new location
        drawPath.quadTo(previousTouchX, previousTouchY, (newX + previousTouchX) / 2,
                (newY + previousTouchY) / 2);
        // store the new coordinates
        previousTouchX=newX;
        previousTouchY=newY;
    }
    private void touchEnded() {
        bitmapCanvas.drawPath(drawPath, paintLine);// draw to bitmapCanvas
        drawPath.reset();// reset the Path
    }
    // print the current image
    public void printImage() {
        if (PrintHelper.systemSupportsPrint()) {
            // use Android Support Library's PrintHelper to print image
            PrintHelper printHelper = new PrintHelper(getContext());

            // fit image in page bounds and print the image
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap("big_pen_page", resultBitmapDrawing);
        }
      else {
            // display message indicating that system does not allow printing
            new BigPenToast(getContext(),R.drawable.ic_error,R.string.message_error_printing);
        }
    }
    public void saveImage(Context context) {
        String message=(context).getString(R.string.please_wait);
        BigPenProgressDialog bigPenProgressDialog=new BigPenProgressDialog(context,message);
        bigPenProgressDialog.showMe();
        // use "bp_" followed by current time as the image name
        final String name ="BP"+System.currentTimeMillis() + Constants.projectExtension;
        // insert the image on the device
        String location = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), resultBitmapDrawing, name,
                "big_pen_project");
        bigPenProgressDialog.dismissMe();
        if (location != null) {
            // display a message indicating that the image was saved
            new BigPenToast(getContext(), R.drawable.ic_fab_save,R.string.message_saved);
        }
        else {
            // display a message indicating that there was an error saving
            new BigPenToast(getContext(),R.drawable.ic_error,R.string.message_error_saving);
        }
    }
    // draw image from local
    public void drawImageFromLocal(Context context, Uri imagePath){
        resultBitmapDrawing.eraseColor(Color.WHITE);
        final int width=getWidth();
        final int height=getHeight();
        String message=(context).getString(R.string.loading_progress_dialog);
        final BigPenProgressDialog bigPenProgressDialog=new BigPenProgressDialog(context,message);
        Glide.with(context)
                .asBitmap()
                .override(width,height)
                .load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new Target<Bitmap>() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        bigPenProgressDialog.showMe();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        bigPenProgressDialog.dismissMe();
                        new BigPenToast(getContext(),R.drawable.ic_error,R.string.message_loading_books_error);
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmapCanvas.drawBitmap(Bitmap.createScaledBitmap(resource,width,height,true)
                                ,0,0,paintScreen);
                        invalidate();
                        bigPenProgressDialog.dismissMe();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void getSize(@NonNull SizeReadyCallback cb) {

                    }

                    @Override
                    public void removeCallback(@NonNull SizeReadyCallback cb) {

                    }

                    @Override
                    public void setRequest(@Nullable Request request) {

                    }

                    @Nullable
                    @Override
                    public Request getRequest() {
                        return null;
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onStop() {

                    }

                    @Override
                    public void onDestroy() {

                    }
                });
    }
    public void drawImageFromCloud(Context context, String bookPageUrl){
        String message=(context).getString(R.string.please_wait);
        final BigPenProgressDialog bigPenProgressDialog=new BigPenProgressDialog(context,message);
        Glide.with(context)
                .asBitmap()
                .override(getWidth(),getHeight())
                .load(bookPageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new Target<Bitmap>() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        bigPenProgressDialog.showMe();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        bigPenProgressDialog.dismissMe();
                        new BigPenToast(getContext(),R.drawable.ic_error,R.string.message_loading_books_error);
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmapCanvas.drawBitmap(Bitmap.createScaledBitmap(resource,getWidth(),getHeight(),true)
                                ,0,0,paintScreen);
                        invalidate();
                        bigPenProgressDialog.dismissMe();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void getSize(@NonNull SizeReadyCallback cb) {

                    }

                    @Override
                    public void removeCallback(@NonNull SizeReadyCallback cb) {

                    }

                    @Override
                    public void setRequest(@Nullable Request request) {

                    }

                    @Nullable
                    @Override
                    public Request getRequest() {
                        return null;
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onStop() {

                    }

                    @Override
                    public void onDestroy() {

                    }
                });
    }
    public void uploadBitmap(final Context context, int totalStorage, final int usedStorage) {
        String message = (context).getString(R.string.uploading_progress_dialog);
        final BigPenProgressDialog bigPenProgressDialog = new BigPenProgressDialog(context, message);
        bigPenProgressDialog.showMe();
        final String projectName = System.currentTimeMillis() + Constants.projectExtension;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resultBitmapDrawing.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] data = baos.toByteArray();
        final int newSize=data.length + usedStorage;
        if (newSize <= totalStorage) {
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference fileRef = FirebaseStorage.getInstance()
                    .getReference(Constants.fireBaseUsersProject)
                    .child(userId).child(projectName);
            UploadTask uploadTask = fileRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    String projectUrl = urlTask.getResult().toString();
                    DatabaseReference myRef = FirebaseDatabase.getInstance()
                            .getReference(Constants.fireBaseUsers)
                            .child(userId);
                    DatabaseReference projectRef = myRef.child(Constants.fireBaseUsersProject);
                    DatabaseReference usedStorageRef = myRef.child(Constants.fireBaseUsedStorage);
                    BookPage bookPage = new BookPage();
                    bookPage.setPageUrl(projectUrl);
                    bookPage.setPageSize(data.length);
                    projectRef.push().setValue(bookPage);
                    usedStorageRef.setValue(newSize);
                    bigPenProgressDialog.dismissMe();
                    new BigPenToast(context, R.drawable.ic_fab_upload, R.string.message_uploading_page_success);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    bigPenProgressDialog.dismissMe();
                    new BigPenToast(context,R.drawable.ic_error,R.string.message_uploading_page_error);
                }
            });

        }
        else {
            bigPenProgressDialog.dismissMe();
            new BigPenToast(context, R.drawable.ic_error, R.string.message_uploading_storage_space);
        }
    }
}
