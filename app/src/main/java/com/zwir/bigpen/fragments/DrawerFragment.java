package com.zwir.bigpen.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.zwir.bigpen.R;
import com.zwir.bigpen.activitys.BookRepository;
import com.zwir.bigpen.adapters.PenAdapter;
import com.zwir.bigpen.customs.BigPenToast;
import com.zwir.bigpen.customs.SurfaceDraw;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.data.LocalPreferences;
import com.zwir.bigpen.data.Pen;
import com.zwir.bigpen.util.GlobalMethods;


import java.util.ArrayList;
import java.util.List;


import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawerFragment extends Fragment {
    private SurfaceDraw surfaceDraw; // handles touch events and draws
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private boolean dialogOnScreen = false;
    private PenAdapter penAdapter;
    private LocalPreferences localPreferences;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference totalStorageRef,usedStorageRef;
    // value used to determine whether user shook the device to erase
    private static final int ACCELERATION_THRESHOLD = 100000;
    private int totalStorage,usedStorage;
    // used to identify the request for using external storage, which
    // the save image feature needs
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;
    // cloud Image request code
    static final int REQUEST_IMAGE_CLOUD = 2;
    // local Image request code
    static final int REQUEST_IMAGE_LOCAL = 3;
    //
    private BoomMenuButton bmbCloudService;
    //
    private InterstitialAd mInterstitialAd;
    private static int[] boomCloudServiceImg = new int[]{
            R.drawable.ic_fab_upload,
            R.drawable.ic_fab_download,
    };
    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_drawer, container, false);
        mAuth = FirebaseAuth.getInstance();
        localPreferences=new LocalPreferences(getContext());
        if (!localPreferences.getMenuTapTarget())
            tapTargetView(view.findViewById(R.id.bmb_cloud_service));
        RecyclerView recycler_view_list_pen = view.findViewById(R.id.recycler_view_list_pen);
        recycler_view_list_pen.setHasFixedSize(true);
        recycler_view_list_pen.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        penAdapter =new PenAdapter(listPen(),this);
        DividerItemDecoration verticalDecoration = new DividerItemDecoration(recycler_view_list_pen.getContext(),
                DividerItemDecoration.HORIZONTAL);
        Drawable verticalDivider = ContextCompat.getDrawable(getContext(), R.drawable.divider);
        verticalDecoration.setDrawable(verticalDivider);
        recycler_view_list_pen.addItemDecoration(verticalDecoration);
        recycler_view_list_pen.setAdapter(penAdapter);
        recycler_view_list_pen.setNestedScrollingEnabled(false);
        // get reference to the SurfaceDraw
        surfaceDraw = view.findViewById(R.id.surfaceDraw);

        // initialize acceleration values
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        view.findViewById(R.id.floating_new_prj).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_hand_mode).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_color).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_line_width).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_insert_local_img).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_cloud_service).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_save).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_print).setOnClickListener(floatingListener);
        view.findViewById(R.id.floating_about).setOnClickListener(floatingListener);
        bmbCloudService=view.findViewById(R.id.bmb_cloud_service);
        loadBoomButton();
        MobileAds.initialize(getContext(), getString(R.string.app_ads_id));
        initializeGoogleAds();
        // active listen to user logged in or not.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user==null){
                    if (totalStorageRef!=null)
                        totalStorageRef.removeEventListener(totalStorageEventListener);
                    if (usedStorageRef!=null)
                        usedStorageRef.removeEventListener(usedStorageEventListener);
                }
                else {
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.fireBaseUsers)
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    // fire base storage
                    totalStorageRef = myRef.child(Constants.fireBaseTotalStorage);
                    totalStorageRef.addValueEventListener(totalStorageEventListener);
                    usedStorageRef = myRef.child(Constants.fireBaseUsedStorage);
                    usedStorageRef.addValueEventListener(usedStorageEventListener);

                }
            }
        };
        return view;
    }

    private void tapTargetView(View target) {
        TapTargetView.showFor(getActivity(),
                TapTarget.forView(target, getString(R.string.target_prompt_primary_text),
                        getString(R.string.target_prompt_secondary_text))
                        .outerCircleColor(R.color.colorPrimaryDark)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(25)                  // Specify the size (in sp) of the title text// Specify the color of the title text
                        .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                        .titleTextColor(R.color.black)      // Specify the color of the title text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textTypeface(ResourcesCompat.getFont(getContext(), R.font.big_pen_font))  // Specify a typeface for the text
                        .dimColor(R.color.colorPrimary)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .icon(AppCompatResources.getDrawable(getContext(), R.drawable.ic_fab_open_menu))  // Specify a custom drawable to draw as the target
                        .targetRadius(25),                  // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        localPreferences.setMenuTapTarget(true);
                    }
                });
    }

    private View.OnClickListener floatingListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FloatingActionButton floatingActionButton= (FloatingActionButton) view;
            switch (floatingActionButton.getId()) {
                case R.id.floating_new_prj:
                    confirmNewPage(); // confirm before erasing image
                    break;
                case R.id.floating_hand_mode:
                    if (surfaceDraw.getHandMode()){
                        surfaceDraw.setHandMode(false);
                        floatingActionButton.setImageResource(R.drawable.ic_fab_read_only);
                    new BigPenToast(getContext(),R.drawable.ic_fab_read_write,R.string.mode_read_write);
                    }
                    else {
                        surfaceDraw.setHandMode(true);
                        floatingActionButton.setImageResource(R.drawable.ic_fab_read_write);
                       new BigPenToast(getContext(),R.drawable.ic_fab_read_only,R.string.mode_read);
                    }
                    break;
                case R.id.floating_color:
                    new ColorFragment().show(getParentFragmentManager(), "color_dialog");
                    break;
                case R.id.floating_line_width:
                    new LineWidthFragment().show(getParentFragmentManager(), "line_width_dialog");
                    break;
                case R.id.floating_insert_local_img:
                    Intent intent=new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent,REQUEST_IMAGE_LOCAL);
                    break;
                case R.id.floating_save:
                    saveImage(); // check permission and save current image
                    break;
                case R.id.floating_cloud_service:
                    if (isInternetConnected()){
                        initializeGoogleAds();
                        setBoomButton(floatingActionButton.getX(),floatingActionButton.getY(),bmbCloudService);
                    }

                    else
                        new BigPenToast(getContext(),R.drawable.ic_error,R.string.check_connexion);
                    break;
                case R.id.floating_print:
                    surfaceDraw.printImage(); // print the current images
                    break;
                case R.id.floating_about: // about
                    new AboutFragment().show(getParentFragmentManager(),"about_dialog");
                    break;
            }
        }
    };
    private void loadBoomButton(){
        for (int i = 0; i <  bmbCloudService.getPiecePlaceEnum().pieceNumber(); i++) {
            SimpleCircleButton.Builder builder = new SimpleCircleButton.Builder()
                    .normalImageRes(boomCloudServiceImg[i])
                    .normalColorRes(R.color.colorPrimary)
                    .highlightedColorRes(R.color.colorPrimary)
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            switch (index){
                                case 0:
                                        uploadProject();
                                    break;
                                case 1:
                                    loadGoogleAds();
                                    break;
                            }
                        }
                    });
            bmbCloudService.addBuilder(builder);
        }
    }

    private void initializeGoogleAds(){
        // google ads
        mInterstitialAd = new InterstitialAd(getContext());
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.setAdUnitId(getString(R.string.app_interstitial_ads_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                startActivityForResult(new Intent(getActivity(), BookRepository.class),REQUEST_IMAGE_CLOUD);
            }
        });
    }

    private void loadGoogleAds() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded())
            mInterstitialAd.show();
         else
            startActivityForResult(new Intent(getActivity(), BookRepository.class),REQUEST_IMAGE_CLOUD);
    }

    private void setBoomButton(float x, float y,BoomMenuButton bmb){
        bmb.setX(x);
        bmb.setY(y);
        bmb.boom();
    }
    //
    private List<Pen> listPen(){
        int[] penColorList = getContext().getResources().getIntArray(R.array.pen_color_list);
        List<Pen> result=new ArrayList<>();
        for (int color:penColorList){
            result.add(new Pen(color));
        }
        return result;
    }
    // start listening for sensor events
    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening(); // listen for shake event
    }

    // enable listening for accelerometer events
    private void enableAccelerometerListening() {
        // get the SensorManager
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(
                        Context.SENSOR_SERVICE);

        // register to listen for accelerometer events
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // stop listening for accelerometer events
    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening(); // stop listening for shake
    }

    // disable listening for accelerometer events
    private void disableAccelerometerListening() {
        // get the SensorManager
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(
                        Context.SENSOR_SERVICE);

        // stop listening for accelerometer events
        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    // event handler for accelerometer events
    private final SensorEventListener sensorEventListener =
            new SensorEventListener() {
                // use accelerometer to determine whether user shook device
                @Override
                public void onSensorChanged(SensorEvent event) {
                    // ensure that other dialogs are not displayed
                    if (!dialogOnScreen) {
                        // get x, y, and z values for the SensorEvent
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        // save previous acceleration value
                        lastAcceleration = currentAcceleration;

                        // calculate the current acceleration
                        currentAcceleration = x * x + y * y + z * z;

                        // calculate the change in acceleration
                        acceleration = currentAcceleration *
                                (currentAcceleration - lastAcceleration);

                        // if the acceleration is above a certain threshold
                        if (acceleration > ACCELERATION_THRESHOLD)
                            confirmNewPage();
                    }
                }

                // required method of interface SensorEventListener
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

    // confirm whether image should be erase
    private void confirmNewPage() {
        NewPageFragment newPageFragment = new NewPageFragment();
        newPageFragment.show(getParentFragmentManager(), "new_page");
    }
    //
    // returns the SurfaceDraw
    public SurfaceDraw getSurfaceDraw() {
        return surfaceDraw;
    }
    //
    public PenAdapter getPenAdapter(){
        return penAdapter;
    }
    // indicates whether a dialog is displayed
    public void setDialogOnScreen(boolean visible) {
        dialogOnScreen = visible;
    }
    private void saveImage() {
        // checks if the app does not have permission needed
        // to save the image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {

                // shows an explanation for why permission is needed
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(getActivity());

                    // set Alert Dialog's message
                    builder.setMessage(R.string.permission_explanation);

                    // add an OK button to the dialog
                    builder.setPositiveButton(R.string.ok_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // request permission
                                    requestPermissions(new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                                }
                            }
                    );

                    // display the dialog
                    builder.create().show();
                }
                else {
                    // request permission
                    requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                }
            }
            else { // if app already has permission to write to external storage
                surfaceDraw.saveImage(getContext()); // save the image
            }
        }
        else { // if app already has permission to write to external storage
            surfaceDraw.saveImage(getContext()); // save the image
        }
    }
    // upload image to fire base account
    private void uploadProject(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            surfaceDraw.uploadBitmap(getContext(),totalStorage,usedStorage);
        }
        else {
            if (GlobalMethods.isGooglePlayServicesAvailable(getActivity())) {
                LogInOrCreateFragment logInOrCreateFragment = new LogInOrCreateFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.actionString, Constants.uploadProject);
                logInOrCreateFragment.setArguments(bundle);
                logInOrCreateFragment.show(getParentFragmentManager(), "login_or_create");
            }
        }

    }
    // called by the system when the user either grants or denies the
    // permission for saving an image
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // switch chooses appropriate action based on which feature
        // requested permission
        switch (requestCode) {
            case SAVE_IMAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    surfaceDraw.saveImage(getContext()); // save the image
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode) {
                case REQUEST_IMAGE_LOCAL:
                    Uri uri = data.getData();
                    surfaceDraw.drawImageFromLocal(getContext(), uri);
                    break;
                case REQUEST_IMAGE_CLOUD:
                    String pageUrl = data.getStringExtra("pageUrl");
                    surfaceDraw.drawImageFromCloud(getContext(),pageUrl);
                    break;
            }
        }
    }
    private boolean isInternetConnected(){
        ConnectivityManager connectivityManager=(ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
    private ValueEventListener totalStorageEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            totalStorage=Integer.parseInt(dataSnapshot.getValue().toString()) ;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private ValueEventListener usedStorageEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            usedStorage=Integer.parseInt(dataSnapshot.getValue().toString()) ;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
