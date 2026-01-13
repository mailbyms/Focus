package com.ihewro.focus.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.blankj.ALog;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.FeedListAdapter;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.databinding.ActivityFeedListBinding;
import com.ihewro.focus.http.HttpInterface;
import com.ihewro.focus.http.HttpUtil;
import com.ihewro.focus.util.Constants;
import com.ihewro.focus.util.UIUtil;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FeedListActivity extends BackActivity {

    private ActivityFeedListBinding binding;
    private FeedListAdapter feedListAdapter;
    private List<Feed> feedList = new ArrayList<>();

    public static void activityStart(Activity activity, String websiteName) {
        Intent intent = new Intent(activity, FeedListActivity.class);
        intent.putExtra(Constants.KEY_STRING_WEBSITE_ID, websiteName);
        activity.startActivity(intent);
    }

    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        mName = intent.getStringExtra(Constants.KEY_STRING_WEBSITE_ID);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(mName + "的可订阅列表");
        initView();
        bindListener();
        binding.refreshLayout.autoRefresh();
        binding.refreshLayout.setEnableLoadMore(false);
    }

    public void initView() {
        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //初始化列表
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        feedListAdapter = new FeedListAdapter(feedList, FeedListActivity.this, mName);

        feedListAdapter.bindToRecyclerView(binding.recyclerView);
    }

    /**
     * 请求一个网站的可订阅列表
     */
    public void requestData() {
        Retrofit retrofit = HttpUtil.getRetrofit("bean", GlobalConfig.serverUrl, 10, 10, 10);
        ALog.d("名称为" + mName);
        Call<List<Feed>> request = retrofit.create(HttpInterface.class).searchFeedListByName(mName);

        request.enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call, Response<List<Feed>> response) {
                if (response.isSuccessful()) {
                    feedList.clear();
                    feedList.addAll(response.body());

                    feedListAdapter.setNewData(feedList);

                    if (feedList.size() == 0) {
                        feedListAdapter.setEmptyView(R.layout.simple_empty_view, binding.recyclerView);
                    }
                    Toasty.success(UIUtil.getContext(), "请求成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toasty.error(UIUtil.getContext(), "请求失败2" + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
                binding.refreshLayout.finishRefresh(true);
            }

            @SuppressLint("CheckResult")
            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Toasty.error(UIUtil.getContext(), "请求失败2" + t.toString(), Toast.LENGTH_SHORT).show();
                binding.refreshLayout.finishRefresh(false);
            }
        });
    }

    public void bindListener() {
        binding.refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestData();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
