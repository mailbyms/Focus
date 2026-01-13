package com.ihewro.focus.fragemnt.setting;


import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.task.TimingService;

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
        //查询数据库
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
        time_interval.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final int select = GlobalConfig.refreshIntervalInt.indexOf(Integer.parseInt(UserPreference.queryValueByKey(UserPreference.tim_interval, String.valueOf(-1))));//默认选择最后一项，即-1

                new MaterialDialog.Builder(getActivity())
                        .title("选择刷新间隔")
                        .items(GlobalConfig.refreshInterval)
                        .itemsCallbackSingleChoice(select, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                UserPreference.updateOrSaveValueByKey(UserPreference.tim_interval, String.valueOf(GlobalConfig.refreshIntervalInt.get(which)));
                                TimingService.startService(getActivity(),true);
                                return false;
                            }
                        })

                        .positiveText("选择")
                        .show();


                return false;
            }
        });

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
