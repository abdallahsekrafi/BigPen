package com.zwir.bigpen.util;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.text.DecimalFormat;
import java.text.ParseException;

public class GlobalMethods {

    public static Boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }
    public static double reformDouble(double input) throws ParseException {
        return DecimalFormat.getNumberInstance()
                .parse(new DecimalFormat(Constants.decimalFormat).format(input))
                .doubleValue();
    }
}
