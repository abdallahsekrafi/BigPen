package com.zwir.bigpen.util;

import com.zwir.bigpen.R;

public final class Constants {
    public static final String projectExtension =".jpj";
    public static final int toKilobyte =1024;
    public static final int toMegabyte =1048576;
    public static final int toGigabyte =1073741824;
    public static final int fireBaseDefaultStorage =5242880;// 5 Mo
    public static final String mO ="Mo";// Mo
    public static final String kO ="Ko";// Ko
    public static final String gO ="Go";// Go
    public static final int hourToRemind =4*60*60*1000;// hour
    public static final int NOTIFICATION_REQUEST_CODE=100;
    public static final int bookStoreType =0;
    public static final int bookPurchasedType =1;
    public static final int bookCartType =2;
    public static final String fireBaseTotalStorage ="totalStorage";
    public static final String fireBaseUsedStorage ="usedStorage";
    public static final String actionString ="actionToDo";
    public static final String toAccountFrag="accountFrag";
    public static final String toPurchaseFrag="purchaseFrag";
    public static final String toProjectFrag="projectFrag";
    public static final String toCartFrag="cartFrag";
    public static final String addBookToCart="addBookToCart";
    public static final String pageFragmentTag ="frag_page";
    public static final String uploadProject ="uploadProject";
    public static final String usdDollar="$";
    public static final double megabytePrice=1.5;
    public static final int maxBayStorage =5;
    public static final String decimalFormat="#.##";
    public static final String pageUrl="pageUrl";
    public static final String parcelableBookPages="bookPagesList";
    public static final String pagesPosition="pagesPosition";
    public static final String fireBaseBooksRef="Books";
    public static final String fireBaseBooksCover="bookCoverUrl";
    public static final String fireBaseBooksPrice="bookPrice";
    public static final String fireBasePagesRef="Pages";
    public static final String fireBaseCartsRef="Cart";
    public static final String fireBaseUsers="Users";
    public static final String fireBaseBalance="balance";
    public static final double fireBaseDefaultBalanceValue =0.4;
    public static final String fireBaseUsersProject="usersProjects";
    public static final int[] boomPayTitle = new int[]{
            R.string.pay_with_bp_account,
            R.string.pay_with_paypal,
            R.string.pay_with_google_play_account
    };
    public static final int[] boomPayOptionImg = new int[]{
            R.drawable.ic_account,
            R.drawable.ic_paypal,
            R.drawable.ic_google
    };
}
