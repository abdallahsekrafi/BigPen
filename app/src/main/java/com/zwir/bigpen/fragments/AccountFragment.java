package com.zwir.bigpen.fragments;


import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.zwir.bigpen.R;
import com.zwir.bigpen.customs.BigPenToast;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.data.LocalPreferences;
import com.zwir.bigpen.util.GlobalMethods;

import java.text.DecimalFormat;
import java.text.ParseException;

import static com.zwir.bigpen.util.FirebaseUtil.fireBaseUsers;

public class AccountFragment extends Fragment {

    private DatabaseReference balanceRef,totalStorageRef,usedStorageRef;
    private TextView textViewBalance,textViewTotalStorage,textViewUsedStorage;
    private double userBalance, priseNewStorage;
    private ProgressBar progressBarStorage;
    private int totalStorage,usedStorage;
    private RewardedVideoAd mRewardedVideoAd;
    private BoomMenuButton menuButtonStorage;
    private LocalPreferences localPreferences;
    private int storageToBay;
    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        localPreferences=new LocalPreferences(getContext());
        if (!localPreferences.getStorageTapTarget())
            tapTargetView(view.findViewById(R.id.bmb_pay_strg_option));
        textViewBalance=view.findViewById(R.id.user_balance);
        textViewTotalStorage=view.findViewById(R.id.total_storage);
        textViewUsedStorage=view.findViewById(R.id.used_storage);
        progressBarStorage=view.findViewById(R.id.progress_storage);
        menuButtonStorage=view.findViewById(R.id.bmb_pay_strg_option);
        ((TextView)view.findViewById(R.id.desc_video_reward)).setText(String.format(getString(R.string.play_video_get_balance),String.valueOf(Constants.fireBaseDefaultBalanceValue)));
        progressBarStorage.setOnClickListener(clickListener);
        DatabaseReference myRef=fireBaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        balanceRef = myRef.child(Constants.fireBaseBalance);
        totalStorageRef = myRef.child(Constants.fireBaseTotalStorage);
        usedStorageRef = myRef.child(Constants.fireBaseUsedStorage);
        view.findViewById(R.id.play_video).setOnClickListener(clickListener);
        view.findViewById(R.id.btn_log_out).setOnClickListener(clickListener);
        balanceRef.addValueEventListener(balanceEventListener);
        usedStorageRef.addValueEventListener(usedStorageEventListener);
        totalStorageRef.addValueEventListener(totalStorageEventListener);
        // google ads
        MobileAds.initialize(getContext(), getString(R.string.app_ads_id));
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mRewardedVideoAd.setRewardedVideoAdListener(rewardedVideoAdListener);
        loadRewardedVideoAd();
        loadBoomButton(menuButtonStorage);
        return view;
    }
    private void loadRewardedVideoAd() {
       /* mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());*/
       mRewardedVideoAd.loadAd(getString(R.string.app_video_ads_id),
                new AdRequest.Builder().build());
    }
    private RewardedVideoAdListener rewardedVideoAdListener=new RewardedVideoAdListener() {
        @Override
        public void onRewardedVideoAdLoaded() {

        }

        @Override
        public void onRewardedVideoAdOpened() {

        }

        @Override
        public void onRewardedVideoStarted() {

        }

        @Override
        public void onRewardedVideoAdClosed() {
            loadRewardedVideoAd();
        }

        @Override
        public void onRewarded(RewardItem rewardItem) {
            increaseBalance();
        }

        @Override
        public void onRewardedVideoAdLeftApplication() {

        }

        @Override
        public void onRewardedVideoAdFailedToLoad(int i) {

        }

        @Override
        public void onRewardedVideoCompleted() {

        }
    };
    private ValueEventListener balanceEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userBalance =Double.parseDouble(dataSnapshot.getValue().toString());
            String balance=Constants.usdDollar+String.valueOf(userBalance);
            textViewBalance.setText(balance);
            loadBoomButton(menuButtonStorage);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private ValueEventListener totalStorageEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            totalStorage=Integer.parseInt(dataSnapshot.getValue().toString()) ;
            textViewTotalStorage.setText(convertByte(totalStorage));
            setProgressBarValue();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private ValueEventListener usedStorageEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            usedStorage=Integer.parseInt(dataSnapshot.getValue().toString()) ;
            textViewUsedStorage.setText(convertByte(usedStorage));
            setProgressBarValue();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.play_video:
                   if (mRewardedVideoAd!=null && mRewardedVideoAd.isLoaded())
                        mRewardedVideoAd.show();
                    else{
                        new BigPenToast(getContext(), R.drawable.ic_error, R.string.system_error);
                        loadRewardedVideoAd();
                    }
                    break;
                case R.id.btn_log_out:
                    FirebaseAuth.getInstance().signOut();
                    break;
                case R.id.progress_storage:
                    new StorageDialog().show(getParentFragmentManager(), "storage_dialog");
                    break;
            }

        }
    };
    public void boomMenu(int storage){

        try {
            storageToBay=storage*Constants.toMegabyte;
            priseNewStorage =GlobalMethods.reformDouble(storage*Constants.megabytePrice) ;
            loadBoomButton(menuButtonStorage);
            menuButtonStorage.boom();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    private void increaseBalance(){
        try {
            balanceRef.setValue(GlobalMethods.reformDouble(userBalance +Constants.fireBaseDefaultBalanceValue));
            new BigPenToast(getContext(), R.drawable.gift, R.string.congratulation_balance);
        } catch (ParseException e) {
            e.printStackTrace();
            new BigPenToast(getContext(), R.drawable.ic_error, R.string.system_error);
        }
    }
    private void increaseStorage(){
        try {
            balanceRef.setValue(GlobalMethods.reformDouble(userBalance- priseNewStorage));
            totalStorageRef.setValue(totalStorage+storageToBay);
            new BigPenToast(getContext(), R.drawable.ic_storage, R.string.congratulation_storage);
        } catch (ParseException e) {
            e.printStackTrace();
            new BigPenToast(getContext(), R.drawable.ic_error, R.string.system_error);
        }
    }
    private void loadBoomButton(BoomMenuButton boomMenuButton){
        Typeface typeface= ResourcesCompat.getFont(getContext(), R.font.big_pen_font);
        String bmb_subText=getString(R.string.user_balance)+" "+Constants.usdDollar + String.valueOf(userBalance);
        boomMenuButton.clearBuilders();
        for (int i = 0; i <  boomMenuButton.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder()
                    .normalImageRes(Constants.boomPayOptionImg[i])
                    .normalTextRes(Constants.boomPayTitle[i])
                    .normalTextColorRes(R.color.white)
                    .textSize(14)
                    .typeface(typeface)
                    .subNormalText(i==0?bmb_subText:getString(R.string.coming_soon))
                    .subNormalTextColorRes(i==0? userBalance >= priseNewStorage ?R.color.green:R.color.red:R.color.blue)
                    .subTextSize(12)
                    .subTypeface(typeface)
                    .normalColorRes(R.color.colorPrimary)
                    .highlightedColorRes(R.color.colorPrimary)
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            switch (index){
                                case 0:
                                    //Pay with your Big Pen balance
                                    if (userBalance >= priseNewStorage)
                                       increaseStorage();
                                    else
                                        new BigPenToast(getContext(), R.drawable.ic_error, R.string.insufficient_balance);
                                    break;
                                case 1:
                                    //Pay with sending an SMS
                                    break;
                                case 2:
                                    //Pay with your google play account
                                    break;
                            }
                        }
                    });
            boomMenuButton.addBuilder(builder);
        }
    }
    private void setProgressBarValue(){
        progressBarStorage.setMax(totalStorage);
        progressBarStorage.setProgress(usedStorage);
    }
    private String convertByte(int Byte){
        String result="";
        if (Byte<Constants.toMegabyte)
            result= String.valueOf(new DecimalFormat(Constants.decimalFormat)
                    .format((double)Byte/Constants.toKilobyte))+" "+Constants.kO;

        else if (Byte<Constants.toGigabyte)
            result= String.valueOf(new DecimalFormat(Constants.decimalFormat)
                    .format((double)Byte/Constants.toMegabyte))+" "+Constants.mO;
            else
            result= String.valueOf(new DecimalFormat(Constants.decimalFormat)
                    .format((double)Byte/Constants.toGigabyte))+" "+Constants.gO;

        return result;
    }
    private void tapTargetView(View target) {
        TapTargetView.showFor(getActivity(),
                TapTarget.forView(target, getString(R.string.target_storage_primary_text),
                        getString(R.string.target_prompt_secondary_text))
                        .outerCircleColor(R.color.colorPrimaryDark) // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(25)                  // Specify the size (in sp) of the title text// Specify the color of the title text
                        .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                        .titleTextColor(R.color.black)      // Specify the color of the title text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textTypeface(ResourcesCompat.getFont(getContext(), R.font.big_pen_font))  // Specify a typeface for the text
                        .dimColor(R.color.colorPrimary)       // If set, will dim behind the view with 30% opacity of the given color
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
                        localPreferences.setStorageTapTarget(true);
                    }
                });
    }
    @Override
    public void onDestroy() {
        balanceRef.removeEventListener(balanceEventListener);
        totalStorageRef.removeEventListener(totalStorageEventListener);
        usedStorageRef.removeEventListener(usedStorageEventListener);
        mRewardedVideoAd.destroy(getContext());
        super.onDestroy();
    }
}
