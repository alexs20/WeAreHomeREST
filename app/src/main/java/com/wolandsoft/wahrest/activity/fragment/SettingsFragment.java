/*
    Copyright 2018 Alexander Shulgin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.wolandsoft.wahrest.activity.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.View;

import com.wolandsoft.wahrest.R;
import com.wolandsoft.wahrest.common.KeySharedPreferences;
import com.wolandsoft.wahrest.service.CoreMonitorService;
import com.wolandsoft.wahrest.service.ServiceManager;

public class SettingsFragment extends PreferenceFragmentCompat implements AlertDialogFragment.OnDialogToFragmentInteract {

    private SwitchPreferenceCompat mChkServiceEnabled;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        mChkServiceEnabled = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_service_enabled_key));
        mChkServiceEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isEnabled = (Boolean) newValue;
                if (isEnabled) {
                    SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                    KeySharedPreferences ksPref = new KeySharedPreferences(shPref, getContext());
                    String homePin = ksPref.getString(R.string.pref_home_pin_key, (Integer) null);
                    if (homePin == null || homePin.isEmpty()) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        DialogFragment fragment = AlertDialogFragment.newInstance(R.mipmap.img24dp_error,
                                R.string.label_error, R.string.error_no_home_pin, false, null);
                        fragment.setCancelable(true);
                        fragment.setTargetFragment(SettingsFragment.this, 0); //response is going to be ignored
                        transaction.addToBackStack(null);
                        fragment.show(transaction, DialogFragment.class.getName());
                        return false;
                    }
                    String wifiSsid = ksPref.getString(R.string.pref_wifi_ssid_key, (Integer) null);
                    if (wifiSsid == null || wifiSsid.isEmpty()) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        DialogFragment fragment = AlertDialogFragment.newInstance(R.mipmap.img24dp_error,
                                R.string.label_error, R.string.error_no_wifi_ssid, false, null);
                        fragment.setCancelable(true);
                        fragment.setTargetFragment(SettingsFragment.this, 0); //response is going to be ignored
                        transaction.addToBackStack(null);
                        fragment.show(transaction, DialogFragment.class.getName());
                        return false;
                    }
                    String firstInREST = ksPref.getString(R.string.pref_first_in_rest_api_key, (Integer) null);
                    if (firstInREST == null || firstInREST.isEmpty()) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        DialogFragment fragment = AlertDialogFragment.newInstance(R.mipmap.img24dp_error,
                                R.string.label_error, R.string.error_no_first_in_rest, false, null);
                        fragment.setCancelable(true);
                        fragment.setTargetFragment(SettingsFragment.this, 0); //response is going to be ignored
                        transaction.addToBackStack(null);
                        fragment.show(transaction, DialogFragment.class.getName());
                        return false;
                    }
                    String lastOutREST = ksPref.getString(R.string.pref_last_out_rest_api_key, (Integer) null);
                    if (lastOutREST == null || lastOutREST.isEmpty()) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        DialogFragment fragment = AlertDialogFragment.newInstance(R.mipmap.img24dp_error,
                                R.string.label_error, R.string.error_no_last_out_rest, false, null);
                        fragment.setCancelable(true);
                        fragment.setTargetFragment(SettingsFragment.this, 0); //response is going to be ignored
                        transaction.addToBackStack(null);
                        fragment.show(transaction, DialogFragment.class.getName());
                        return false;
                    }
                }
                ServiceManager.manageService(getContext(), CoreMonitorService.class, isEnabled);
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.label_settings);
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        KeySharedPreferences ksPref = new KeySharedPreferences(shPref, getContext());
        boolean isChecked = ksPref.getBoolean(R.string.pref_service_enabled_key, R.bool.pref_service_enabled_value) && ServiceManager.isServiceRunning(getContext(), CoreMonitorService.class);
        mChkServiceEnabled.setChecked(isChecked);
    }

    @Override
    public void onDialogResult(int requestCode, int result, Bundle args) {

    }
}
