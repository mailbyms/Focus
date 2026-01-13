package com.ihewro.focus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.BaseViewPagerAdapter;
import com.ihewro.focus.databinding.ActivitySettingBinding;
import com.ihewro.focus.fragemnt.setting.DataFragment;
import com.ihewro.focus.fragemnt.setting.DisplayFragment;
import com.ihewro.focus.fragemnt.setting.SynchroFragment;
import com.ihewro.focus.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SettingActivity extends BackActivity {

    private ActivitySettingBinding binding;

    private List<Fragment> fragmentList = new ArrayList<>();

    private SynchroFragment synchroFragment;
    private DataFragment dataFragment;
    private DisplayFragment displayFragment;

    public static void activityStart(Activity activity) {
        Intent intent = new Intent(activity, SettingActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary), 0);

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        createTabLayout();
    }

    private void createTabLayout() {
        //碎片列表
        fragmentList.clear();
        synchroFragment = new SynchroFragment();
        displayFragment = new DisplayFragment();
        dataFragment = new DataFragment();

        fragmentList.add(synchroFragment);
        fragmentList.add(displayFragment);
        fragmentList.add(dataFragment);

        //标题列表
        List<String> pageTitleList = new ArrayList<>();
        pageTitleList.add("同步");
        pageTitleList.add("显示");
        pageTitleList.add("数据");

        //新建适配器
        BaseViewPagerAdapter adapter = new BaseViewPagerAdapter(getSupportFragmentManager(), fragmentList, pageTitleList);

        //设置背景颜色
        binding.tabLayout.setBackgroundColor(ContextCompat.getColor(SettingActivity.this,R.color.colorPrimary));

        //设置ViewPager
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(3);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
