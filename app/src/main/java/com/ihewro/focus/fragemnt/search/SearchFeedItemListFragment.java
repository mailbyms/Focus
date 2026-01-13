package com.ihewro.focus.fragemnt.search;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ihewro.focus.R;
import com.ihewro.focus.activity.MainActivity;
import com.ihewro.focus.activity.PostDetailActivity;
import com.ihewro.focus.adapter.FeedListAdapter;
import com.ihewro.focus.adapter.FeedSearchAdapter;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.databinding.FragmentSearchBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFeedItemListFragment extends Fragment {

    private FragmentSearchBinding binding;
    private FeedSearchAdapter adapter;
    private List<FeedItem> searchResults = new ArrayList<>();
    private Activity activity;

    public SearchFeedItemListFragment() {}

    //这种写法Google不推荐原因是，当activity recreate时候，碎片的参数不会重新调用。我们又不用保存碎片状态
    @SuppressLint("ValidFragment")
    public SearchFeedItemListFragment(Activity activity) {
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSearchAdapter();
    }




    /**
     * 初始化
     */
    private void initSearchAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new FeedSearchAdapter(searchResults);
        adapter.bindToRecyclerView(binding.recyclerView);
        adapter.setEmptyView(R.layout.simple_empty_view,binding.recyclerView);


        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FeedItem item = searchResults.get(position);
                ArrayList<Integer> list = new ArrayList<>();
                for (FeedItem feedItem : searchResults) {
                    list.add(feedItem.getId());
                }
                PostDetailActivity.activityStart(activity, position, searchResults, PostDetailActivity.ORIGIN_SEARCH);

            }
        });
    }

    public void showLoading(){
        adapter.setNewData(null);
        adapter.setEmptyView(R.layout.simple_loading_view,binding.recyclerView);
    }

    public void updateData(List<FeedItem> list){
        if (adapter != null){
            this.searchResults = list;
            if (list!=null && list.size() >0){
                adapter.setNewData(searchResults);
            }else {
                adapter.setNewData(null);
                adapter.setEmptyView(R.layout.simple_empty_view,binding.recyclerView);
            }
        }
    }

    public BaseQuickAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
