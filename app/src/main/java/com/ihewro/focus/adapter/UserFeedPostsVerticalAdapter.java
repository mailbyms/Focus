package com.ihewro.focus.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.blankj.ALog;
import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ihewro.focus.R;
import com.ihewro.focus.activity.PostDetailActivity;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.decoration.SuspensionDecoration;
import com.ihewro.focus.helper.ItemTouchHelperAdapter;
import com.ihewro.focus.helper.MyViewHolder;
import com.ihewro.focus.helper.SimpleItemTouchHelperCallback;
import com.ihewro.focus.util.DataUtil;
import com.ihewro.focus.util.DateUtil;
import com.ihewro.focus.util.ImageLoaderManager;
import com.ihewro.focus.util.RSSUtil;
import com.ihewro.focus.util.StringUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.callback.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import skin.support.utils.SkinPreference;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/03/13
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class UserFeedPostsVerticalAdapter extends BaseItemDraggableAdapter<FeedItem, MyViewHolder> implements ItemTouchHelperAdapter {

    private Activity activity;
    private String feedName;
    private List<FeedItem> feedItemList;
    private boolean isRequesting = false;

    private AsyncListDiffer<FeedItem> mDiffer;

    private DiffUtil.ItemCallback<FeedItem> diffCallback = new DiffUtil.ItemCallback<FeedItem>() {
        @Override
        public boolean areItemsTheSame(FeedItem oldItem, FeedItem newItem) {
            return TextUtils.equals(oldItem.getId()+"", newItem.getId()+"");
        }

        @Override
        public boolean areContentsTheSame(FeedItem oldItem, FeedItem newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
        }
    };


    private int not_read_color;
    private int read_color;
    private int not_read_content_color;
    private int read_content_color;

    private SimpleItemTouchHelperCallback simpleItemTouchHelperCallback;
    private SuspensionDecoration suspensionDecoration;


    public UserFeedPostsVerticalAdapter(@Nullable List<FeedItem> data, Activity activity, SuspensionDecoration suspensionDecoration, SimpleItemTouchHelperCallback simpleItemTouchHelperCallback) {
        super(R.layout.item_post, data);
        this.activity = activity;
        this.feedItemList = data;
        this.suspensionDecoration = suspensionDecoration;
        this.simpleItemTouchHelperCallback = simpleItemTouchHelperCallback;


        //初始化
        mDiffer = new AsyncListDiffer<>(this, diffCallback);



        //初始化颜色参数
        if(SkinPreference.getInstance().getSkinName().equals("night")){
            read_color = R.color.text_read_night;
            read_content_color = R.color.text_read_content_night;

            not_read_color = R.color.text_unread_night;
            not_read_content_color = R.color.text_unread_content_night;

        }else {
            read_color = R.color.text_read;
            read_content_color = R.color.text_read_content;

            not_read_color = R.color.text_unread;
            not_read_content_color = R.color.text_unread_content;
        }
    }

    public void setNewDataByDiff(@Nullable List<FeedItem> data,int notReadNum) {
//        ALog.d("设置新的数据" + data.size());
        if (notReadNum==0){
            this.setNewData(data);
        }else {
            updateDecoration(data);
            mDiffer.submitList(data);
        }
    }

    @Override
    public void setNewData(@Nullable List<FeedItem> data) {
        updateDecoration(data);
        super.setNewData(data);
    }


    private void updateDecoration(List<FeedItem> data){
        if (data==null){
            data = new ArrayList<>();
        }
        if (simpleItemTouchHelperCallback != null){
            simpleItemTouchHelperCallback.setmDatas(data);
        }

        if (suspensionDecoration != null){
            suspensionDecoration.setmDatas(data);
        }
    }

    @Override
    protected void convert(final MyViewHolder helper, FeedItem item) {
        //绑定事件
        bindListener(helper,item);

        if (helper.getAdapterPosition() == 0){
//            ALog.d("第一个项目" + item.getTitle());
        }


//        ALog.d(item.getTitle() + "日期：" + item.getDate());
        helper.setText(R.id.post_title,item.getTitle());
        helper.setText(R.id.rss_name,item.getFeedName());
        helper.setText(R.id.post_summay, DataUtil.getOptimizedDesc(item.getSummary()));
        helper.setText(R.id.post_time, DateUtil.getTTimeStringByInt(item.getDate()));



        if (UserPreference.queryValueByKey(UserPreference.not_show_image_in_list,"0").equals("0")){
            String imageUrl = DataUtil.getFeedItemImageUrl(item);
            if (!StringUtil.trim(imageUrl).equals("")){

                imageUrl = RSSUtil.handleImageUrl(imageUrl,item.getUrl(),item.isBadGuy(),item.isChina());

                helper.getView(R.id.post_pic).setVisibility(View.VISIBLE);

                ImageLoader.getInstance().displayImage(StringUtil.trim(imageUrl), (ImageView) helper.getView(R.id.post_pic),ImageLoaderManager.getSubsciptionIconOptions(activity));

            }else {
                helper.getView(R.id.post_pic).setVisibility(View.GONE);
            }
        }else {//无图列表
            helper.getView(R.id.post_pic).setVisibility(View.GONE);
        }
        updateUI(helper,item);
    }

    private void updateUI(final BaseViewHolder helper,FeedItem item){
        if (item.isRead()){
//            ALog.d("是否已读" + item.isRead());
            helper.setTextColor(R.id.post_title,activity.getResources().getColor(read_color));
            helper.setTextColor(R.id.rss_name,activity.getResources().getColor(read_content_color));
            helper.setTextColor(R.id.post_summay,activity.getResources().getColor(read_content_color));
            helper.setTextColor(R.id.post_time,activity.getResources().getColor(read_content_color));
//            helper.setText(R.id.markRead,"标记未读");
        }else {
            helper.setTextColor(R.id.post_title,activity.getResources().getColor(not_read_color));
            helper.setTextColor(R.id.rss_name,activity.getResources().getColor(not_read_content_color));
            helper.setTextColor(R.id.post_summay,activity.getResources().getColor(not_read_content_color));
            helper.setTextColor(R.id.post_time,activity.getResources().getColor(not_read_content_color));
//            helper.setText(R.id.markRead,"标记已读");
        }

    }


    private void bindListener(final BaseViewHolder helper, final FeedItem item){
/*
//        ((EasySwipeMenuLayout)helper.getView(R.id.swipe));
        helper.getView(R.id.markRead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.setRead(!item.isRead());
                //保存到数据库
                FeedItem temp = LitePal.find(FeedItem.class,item.getId());
                temp.setRead(item.isRead());
                temp.save();

                //通知
                if (item.isRead()){
                    Toasty.success(activity,"标记已读成功").show();
                }else {
                    Toasty.success(activity,"标记未读成功").show();
                }
                notifyItemChanged(helper.getAdapterPosition());
                EventBus.getDefault().post(new EventMessage(EventMessage.EDIT_ITEM_READ));
            }
        });*/




        helper.getView(R.id.content_container).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String[] list = new String[]{"将以上部分标记为已读","将当前文章标记已（未）读", "将以下部分标记为已读"};

                if (item.isRead()){
                    list[1] = "将当前文章标记未读";
                }else {
                    list[1] = "将当前文章标记已读";
                }
                new XPopup.Builder(activity)
                        .asCenterList("操作", list, new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                if (position == 0){
                                    markReadOfTop(helper,item);
                                }else if (position == 2){
                                    markReadOfBottom(helper,item);
                                }else if (position == 1){
                                    //当前项目标记已读/未读
                                    item.setRead(!item.isRead());
                                    item.saveAsync().listen(new SaveCallback() {
                                        @Override
                                        public void onFinish(boolean success) {
                                            notifyItemChanged(helper.getAdapterPosition());
                                            //修改首页未读数目相关界面
                                            EventBus.getDefault().post(new EventMessage(EventMessage.MAIN_READ_NUM_EDIT));
                                        }
                                    });
                                }
                            }
                        })
                        .show();

                return true;
            }
        });

        //跳转页面
        helper.getView(R.id.content_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    ArrayList<Integer> list = new ArrayList<>();
                    for (FeedItem feedItem: feedItemList){
                        list.add(feedItem.getId());
                    }
                    //如果当前正在请求数据，则来源变成ORIGIN_SEARCH，否则使用ORIGIN_MAIN，用于更新首页已读样式不同
                    if (isRequesting){
                        PostDetailActivity.activityStart(activity,helper.getAdapterPosition(),feedItemList,PostDetailActivity.ORIGIN_SEARCH);
                    }else {
                        PostDetailActivity.activityStart(activity,helper.getAdapterPosition(),feedItemList,PostDetailActivity.ORIGIN_MAIN);
                    }

            }
        });

    }

    private void markReadOfTop(final BaseViewHolder helper, final FeedItem item){
        for (int i = 0;i<helper.getAdapterPosition();i++){
            feedItemList.get(i).setRead(true);
            feedItemList.get(i).save();
            notifyItemChanged(i);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.MAIN_READ_NUM_EDIT));
    }

    private void markReadOfBottom(final BaseViewHolder helper, final FeedItem item){
        for (int i = helper.getAdapterPosition();i<feedItemList.size();i++){
            feedItemList.get(i).setRead(true);
            feedItemList.get(i).save();
            notifyItemChanged(i);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.MAIN_READ_NUM_EDIT));
    }

    public void setRequesting(boolean requesting) {
        isRequesting = requesting;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        ALog.d("fromPosition" + fromPosition + "toPosition" + toPosition);

    }

    @Override
    public void onItemDismiss(final int position) {
        ALog.d("滑动完毕！！");
        //标记已读未读
        FeedItem item = feedItemList.get(position);
        ALog.d(item.getTitle());
        item.setRead(!item.isRead());

        item.saveAsync().listen(new SaveCallback() {
            @Override
            public void onFinish(boolean success) {
                notifyItemChanged(position);
                EventBus.getDefault().post(new EventMessage(EventMessage.MAIN_READ_NUM_EDIT));
            }
        });
    }

    public void setDecoration(SuspensionDecoration suspensionDecoration, SimpleItemTouchHelperCallback simpleItemTouchHelperCallback){
        this.suspensionDecoration = suspensionDecoration;
        this.simpleItemTouchHelperCallback = simpleItemTouchHelperCallback;
    }


}
