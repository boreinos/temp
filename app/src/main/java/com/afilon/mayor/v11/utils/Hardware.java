package com.afilon.mayor.v11.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by ccano on 10/2/2017.
 */

/**
 * Little helper to inspect the hardware permissions needed to run the app
 */
public class Hardware {


    public static boolean hasPermission(Context context, String permission) {

        int res = context.checkCallingOrSelfPermission(permission);

        Log.e(TAG, "permission: " + permission + " = \t\t" +
                (res == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

        return res == PackageManager.PERMISSION_GRANTED;

    }

    public static boolean hasPermissions(Context context, String... permissions) {

        boolean hasAllPermissions = true;

        for(String permission : permissions) {
            if (! hasPermission(context, permission)) {hasAllPermissions = false; }
        }

        return hasAllPermissions;

    }
}
