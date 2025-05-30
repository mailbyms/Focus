package com.ihewro.focus.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.FeedListAdapter;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedRequire;
import com.ihewro.focus.bean.Help;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.http.HttpInterface;
import com.ihewro.focus.http.HttpUtil;
import com.ihewro.focus.util.Constants;
import com.ihewro.focus.util.RSSUtil;
import com.ihewro.focus.util.StringUtil;
import com.ihewro.focus.util.UIUtil;
import com.ihewro.focus.view.RequireListPopupView;
import com.lxj.xpopup.XPopup;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FeedListActivity extends BackActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    private FeedListAdapter adapter;
    private List<Feed> feedList = new ArrayList<>();
    private String mName;

    public static void activityStart(Activity activity, String name) {
        Intent intent = new Intent(activity, FeedListActivity.class);
        intent.putExtra("name", name);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_list);
        ButterKnife.bind(this);

        mName = getIntent().getStringExtra("name");
        toolbar.setTitle(mName);

        initRecyclerView();
        initRefreshLayout();
        requestData();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new FeedListAdapter(feedList, this, mName);
        adapter.bindToRecyclerView(recyclerView);
    }

    private void initRefreshLayout() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                requestData();
            }
        });
    }

    private void requestData() {
        Retrofit retrofit = HttpUtil.getRetrofit("bean", GlobalConfig.serverUrl, 10, 10, 10);
        Call<List<Feed>> request = retrofit.create(HttpInterface.class).searchFeedListByName(mName);
        request.enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call, Response<List<Feed>> response) {
                if (response.isSuccessful()) {
                    feedList.clear();
                    feedList.addAll(response.body());
                    adapter.setNewData(feedList);
                    if (feedList.size() == 0) {
                        adapter.setEmptyView(R.layout.simple_empty_view);
                    }
                } else {
                    Toasty.error(FeedListActivity.this, "获取数据失败").show();
                }
                refreshLayout.finishRefresh();
            }

            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Toasty.error(FeedListActivity.this, "网络请求失败").show();
                refreshLayout.finishRefresh();
            }
        });
    }
}
