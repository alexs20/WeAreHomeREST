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

import android.annotation.SuppressLint;
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
import com.wolandsoft.wahrest.common.LogEx;
import com.wolandsoft.wahrest.service.CoreMonitorService;
import com.wolandsoft.wahrest.service.ServiceManager;
@SuppressLint("StringFormatInvalid")
public class SettingsFragment extends PreferenceFragmentCompat implements
        AlertDialogFragment.OnDialogToFragmentInteract,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private SwitchPreferenceCompat mChkServiceEnabled;

    SharedPreferences mSharedPreferences;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mChkServiceEnabled = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_service_enabled_key));
        mChkServiceEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isEnabled = (Boolean) newValue;
                return revalidateService(isEnabled);
            }
        });
    }

    private boolean revalidateService(boolean isServiceEnabled) {
        ServiceManager.manageService(getContext(), CoreMonitorService.class, false);
        if(isServiceEnabled) {
            Integer errMsgId = validateSettings(R.string.pref_home_pin_key, R.string.pref_wifi_ssid_key,
                    R.string.pref_first_in_rest_api_key, R.string.pref_last_out_rest_api_key);
            if (errMsgId == null) {
                ServiceManager.manageService(getContext(), CoreMonitorService.class, true);
            } else {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                DialogFragment fragment = AlertDialogFragment.newInstance(R.mipmap.img24dp_error,
                        R.string.label_error, errMsgId, false, null);
                fragment.setCancelable(true);
                fragment.setTargetFragment(SettingsFragment.this, 0); //response is going to be ignored
                transaction.addToBackStack(null);
                fragment.show(transaction, DialogFragment.class.getName());
                return false;
            }
        }
        return true;
    }

    private Integer validateSettings(int ...keys) {
        KeySharedPreferences ksPref = new KeySharedPreferences(mSharedPreferences, getContext());
        for(int key : keys) {
            switch (key){
                case R.string.pref_home_pin_key:
                    String homePin = ksPref.getString(R.string.pref_home_pin_key, (Integer) null);
                    if (homePin == null || homePin.isEmpty()) {
                        return R.string.error_no_home_pin;
                    }
                    break;
                case R.string.pref_wifi_ssid_key:
                    String wifiSsid = ksPref.getString(R.string.pref_wifi_ssid_key, (Integer) null);
                    if (wifiSsid == null || wifiSsid.isEmpty()) {
                        return R.string.error_no_wifi_ssid;
                    }
                    break;
                case R.string.pref_first_in_rest_api_key:
                    String firstInREST = ksPref.getString(R.string.pref_first_in_rest_api_key, (Integer) null);
                    if (firstInREST == null || firstInREST.isEmpty()) {
                        return R.string.error_no_first_in_rest;
                    }
                    break;
                case R.string.pref_last_out_rest_api_key:
                    String lastOutREST = ksPref.getString(R.string.pref_last_out_rest_api_key, (Integer) null);
                    if (lastOutREST == null || lastOutREST.isEmpty()) {
                        return R.string.error_no_last_out_rest;
                    }
                    break;
            }
        }
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.label_settings);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        KeySharedPreferences ksPref = new KeySharedPreferences(mSharedPreferences, getContext());
        boolean isChecked = ksPref.getBoolean(R.string.pref_service_enabled_key, R.bool.pref_service_enabled_value) && ServiceManager.isServiceRunning(getContext(), CoreMonitorService.class);
        mChkServiceEnabled.setChecked(isChecked);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onDialogResult(int requestCode, int result, Bundle args) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        KeySharedPreferences ksPref = new KeySharedPreferences(mSharedPreferences, getContext());
        boolean isChecked = ksPref.getBoolean(R.string.pref_service_enabled_key, R.bool.pref_service_enabled_value);
        if (!revalidateService(isChecked)) {
            mChkServiceEnabled.setChecked(false);
        }
    }
}
