package com.ihewro.focus.fragemnt.setting;

import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.ihewro.focus.R;
import com.ihewro.focus.bean.UserPreference;

/**
 * 同步的设置
 */
public class SynchroFragment extends SettingFragment {

    private SwitchPreferenceCompat use_internet_while_open;
    private SwitchPreferenceCompat auto_name;
    private Preference time_interval;
    private SwitchPreferenceCompat only_wifi;

    public SynchroFragment() {
    }

    @Override
    public void initView() {
        addPreferencesFromResource(R.xml.pref_synchro_setting);
    }

    @Override
    public void initPreferenceComponent() {
        use_internet_while_open = (SwitchPreferenceCompat)findPreference(getString(R.string.pref_key_use_internet_while_open));
        auto_name = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_key_auto_name));
        time_interval = findPreference(getString(R.string.pref_key_refresh_interval));
        only_wifi = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_key_only_use_wifi));
    }

    @Override
    public void initPreferencesData() {
        if (UserPreference.queryValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN,"0").equals("0")){
            use_internet_while_open.setChecked(false);
        }else {
            use_internet_while_open.setChecked(true);
        }

        if (UserPreference.queryValueByKey(UserPreference.AUTO_SET_FEED_NAME,"0").equals("0")){
            auto_name.setChecked(false);
        }else {
            auto_name.setChecked(true);
        }

        if (UserPreference.queryValueByKey(UserPreference.notWifi,"0").equals("0")){
            only_wifi.setChecked(false);
        }else {
            only_wifi.setChecked(true);
        }
    }

    @Override
    public void initListener() {
        use_internet_while_open.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (use_internet_while_open.isChecked()){
                    UserPreference.updateOrSaveValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN,"1");
                }else {
                    UserPreference.updateOrSaveValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN,"0");
                }
                return false;
            }
        });

        only_wifi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (only_wifi.isChecked()){
                    UserPreference.updateOrSaveValueByKey(UserPreference.notWifi,"1");
                }else {
                    UserPreference.updateOrSaveValueByKey(UserPreference.notWifi,"0");
                }
                return false;
            }
        });

        auto_name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (auto_name.isChecked()){
                    UserPreference.updateOrSaveValueByKey(UserPreference.AUTO_SET_FEED_NAME,"1");
                }else {
                    UserPreference.updateOrSaveValueByKey(UserPreference.AUTO_SET_FEED_NAME,"0");
                }
                return false;
            }
        });
    }
}
