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

public class SettingsFragment extends PreferenceFragmentCompat {

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
        boolean isChecked =ksPref.getBoolean(R.string.pref_service_enabled_key, R.bool.pref_service_enabled_value) && ServiceManager.isServiceRunning(getContext(), CoreMonitorService.class);
        mChkServiceEnabled.setChecked(isChecked);
    }
}
