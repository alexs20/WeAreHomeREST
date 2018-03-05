package com.wolandsoft.wahrest.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;

import com.wolandsoft.wahrest.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alexander on 2018-03-04.
 */

public class PermissionsManager {
    public static final String[] ALL_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    public static boolean sPermissionGranted(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : ALL_PERMISSIONS) {
                int permissionGranted = ContextCompat.checkSelfPermission(ctx, permission);
                if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermission(Activity act) {
        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(act);
        Set<String> askedPermissions = shPref.getStringSet(act.getString(R.string.pref_permission_asked_type_key), new HashSet<String>());
        boolean askDirect = true;
        for (String permission : ALL_PERMISSIONS) {
            int permissionGranted = ContextCompat.checkSelfPermission(act, permission);
            if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
                askDirect = askDirect && (!askedPermissions.contains(permission) || act.shouldShowRequestPermissionRationale(permission));
            }
        }
        if (askDirect) {
            act.requestPermissions(ALL_PERMISSIONS, 0);
            Collections.addAll(askedPermissions, ALL_PERMISSIONS);
            shPref.edit().putStringSet(act.getString(R.string.pref_permission_asked_type_key), askedPermissions).apply();
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", act.getPackageName(), null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            act.startActivityForResult(intent, 0);
        }
    }
}
