package com.ihewro.focus.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.PostDetailListPagerAdapter;
import com.ihewro.focus.adapter.ReadBackgroundAdapter;
import com.ihewro.focus.bean.Background;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.PostSetting;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.helper.ParallaxTransformer;
import com.ihewro.focus.util.Constants;
import com.ihewro.focus.util.ShareUtil;
import com.ihewro.focus.util.StatusBarUtil;
import com.ihewro.focus.util.UIUtil;
import com.ihewro.focus.util.WebViewUtil;
import com.ihewro.focus.databinding.ActivityPostDetailBinding;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.callback.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;


public class PostDetailActivity extends BackActivity {

    public static final int ORIGIN_SEARCH = 688;
    public static final int ORIGIN_MAIN = 350;
    public static final int ORIGIN_STAR = 836;

    private ActivityPostDetailBinding binding;

    private boolean currentItemReady = false;
    private boolean starItemReady = false;
    private boolean starIconReady = false;


    private List<View> viewList = new ArrayList<>();
    private List<Integer> readList = new ArrayList<>();

    private static final List<Integer> colorList = Arrays.asList(R.color.white, R.color.green, R.color.yellow, R.color.pink, R.color.blue, R.color.blue2, R.color.color3, R.color.color4, R.color.color5);

    private List<Background> backgroundList = new ArrayList<>();

    private PostDetailListPagerAdapter adapter;

    private MaterialDialog ReadSettingDialog;
    private PostSetting postSetting;
    Class useClass;

    private int mIndex;
    private FeedItem currentFeedItem;
    private MenuItem starItem;

    private int origin;

    private int notReadNum = 0;

    private LinearLayoutManager linearLayoutManager;

    private List<FeedItem> feedItemList = new ArrayList<>();


    public static void activityStart(Activity activity, int indexInList, List<FeedItem> feedItemList, int origin) {
        Intent intent = new Intent(activity, PostDetailActivity.class);

        Bundle bundle = new Bundle();

        //使用静态变量传递数据
        GlobalConfig.feedItemList = feedItemList;

        bundle.putInt(Constants.KEY_INT_INDEX, indexInList);
        bundle.putInt(Constants.POST_DETAIL_ORIGIN, origin);

        intent.putExtras(bundle);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mIndex = bundle.getInt(Constants.KEY_INT_INDEX, 0);


        new Thread(new Runnable() {
            @Override
            public void run() {
                feedItemList.addAll(GlobalConfig.feedItemList);

                UIUtil.runOnUiThread(PostDetailActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        //加载完menu才去加载后面的内容
                        initRecyclerView();
                    }
                });
            }
        }).start();

        origin = bundle.getInt(Constants.POST_DETAIL_ORIGIN);



    }


    private void initToolbarColor() {
        //根据偏好设置背景颜色修改toolbar的背景颜色
        binding.toolbar.setBackgroundColor(PostSetting.getBackgroundInt(PostDetailActivity.this));
        StatusBarUtil.setColor(this, PostSetting.getBackgroundInt(PostDetailActivity.this), 0);
    }




    public void initData() {
        currentFeedItem = feedItemList.get(mIndex);
        this.currentItemReady = true;

    }

    private void initRecyclerView() {

        adapter = new PostDetailListPagerAdapter(getSupportFragmentManager(),PostDetailActivity.this);

        //初始化当前文章的对象
        initData();

        //显示未读数目
        new Thread(new Runnable() {
            @Override
            public void run() {
                PostDetailActivity.this.notReadNum = 0;
                if (origin != ORIGIN_STAR) {
                    for (FeedItem feedItem : feedItemList) {
                        if (!feedItem.isRead()) {
                            PostDetailActivity.this.notReadNum++;
                        }
                    }
                }

                adapter.setData(feedItemList);


                UIUtil.runOnUiThread(PostDetailActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        binding.viewPager.setAdapter(adapter);

                        final float PARALLAX_COEFFICIENT = 0.5f;
                        final float DISTANCE_COEFFICIENT = 0.1f;

                        binding.viewPager.setPageTransformer(true, new ParallaxTransformer(adapter,null,PARALLAX_COEFFICIENT, DISTANCE_COEFFICIENT));

                        //移动到当前文章的位置
                        binding.viewPager.setCurrentItem(mIndex);


                        ALog.d("首次加载");
                        //setLikeButton();
                        setCurrentItemStatus();
                        initPostClickListener();

                        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int i, float v, int i1) {

                            }

                            @Override
                            public void onPageSelected(int i) {
                                ALog.d("onPageSelected");
                                mIndex = i;
                                //UI修改
                                starIconReady = false;
                                initData();
                                //setLikeButton();
                                //修改顶部导航栏的收藏状态
                                setCurrentItemStatus();
                                initPostClickListener();
                            }

                            @Override
                            public void onPageScrollStateChanged(int i) {

                            }
                        });


                        if (notReadNum <= 0) {
                            binding.toolbar.setTitle("");
                        } else {
                            binding.toolbar.setTitle(notReadNum + "");
                        }

                    }
                });
            }
        }).start();
    }


    /**
     * 为什么不在adapter里面写，因为recyclerview有缓存机制，没滑到这个时候就给标记为已读了
     */
    private void setCurrentItemStatus() {
        //将该文章标记为已读，并且通知首页修改布局
        if (!currentFeedItem.isRead()) {
            currentFeedItem.setRead(true);
            updateNotReadNum();
            currentFeedItem.saveAsync().listen(new SaveCallback() {
                @Override
                public void onFinish(boolean success) {
                    if (origin == ORIGIN_SEARCH) {//isUpdateMainReadMark 为false表示不是首页进来的
                        readList.add(currentFeedItem.getId());
                    } else if (origin == ORIGIN_MAIN) {
                        readList.add(mIndex);
                    }
                }
            });
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPostClickListener() {


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        binding.toolbar.setTitle("");
        getMenuInflater().inflate(R.menu.post, menu);

        initToolbarColor();


        return true;
    }


    /**
     * 目录按钮的点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_link) {
            openLink(currentFeedItem);
        } else if (id == R.id.action_share) {
            ShareUtil.shareBySystem(PostDetailActivity.this, "text", currentFeedItem.getTitle() + "\n" + currentFeedItem.getUrl());
        } else if (id == R.id.text_setting) {
            ReadSettingDialog = new MaterialDialog.Builder(this)
                    .customView(R.layout.read_setting, true)
                    .neutralText("重置")
                    .positiveText("确定")
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SIZE, PostSetting.FONT_SIZE_DEFAULT);
                            UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SPACING, PostSetting.FONT_SPACING_DEFAULT);
                            UserPreference.updateOrSaveValueByKey(PostSetting.LINE_SPACING, PostSetting.LINE_SPACING_DEFAULT);
//                                adapter.notifyItemChanged(mIndex);
                        }
                    })
                    .show();

            initReadSettingView();
            initReadSettingListener();


            initReadBackgroundView();
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private void initReadBackgroundView() {
        if (ReadSettingDialog.isShowing()) {
            RecyclerView recyclerView = (RecyclerView) ReadSettingDialog.findViewById(R.id.recycler_view);

            backgroundList.clear();
            for (Integer color : colorList) {
                backgroundList.add(new Background(ContextCompat.getColor(PostDetailActivity.this, color)));
            }
            linearLayoutManager = new LinearLayoutManager(PostDetailActivity.this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            final ReadBackgroundAdapter adapter1 = new ReadBackgroundAdapter(PostDetailActivity.this, backgroundList);
            adapter1.bindToRecyclerView(recyclerView);
            adapter1.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter2, View view, int position) {
                    //改变背景颜色，并写入到数据库
                    UserPreference.updateOrSaveValueByKey(UserPreference.READ_BACKGROUND, String.valueOf(backgroundList.get(position).getColor()));
                    //刷新页面
                    //更新UI
                    adapter1.notifyDataSetChanged();
                    //修改背景颜色
                    //根据偏好设置背景颜色修改toolbar的背景颜色
                    initToolbarColor();
                    adapter.notifyItemChanged(mIndex);
                }
            });
        }
    }

    //根据现有的设置，恢复布局
    private void initReadSettingView() {
        if (ReadSettingDialog.isShowing()) {
            //设置字号
            ((SeekBar) ReadSettingDialog.findViewById(R.id.size_setting)).setProgress(Integer.parseInt(PostSetting.getFontSize()));
            ((TextView) ReadSettingDialog.findViewById(R.id.size_setting_info)).setText(PostSetting.getFontSize());

            //设置字间距
            ((SeekBar) ReadSettingDialog.findViewById(R.id.font_space_setting)).setProgress(Integer.parseInt(PostSetting.getFontSpace()));
            ((TextView) ReadSettingDialog.findViewById(R.id.font_space_setting_info)).setText(PostSetting.getFontSpace());


            //设置行间距
            ((SeekBar) ReadSettingDialog.findViewById(R.id.line_space_setting)).setProgress(Integer.parseInt(PostSetting.getLineSpace()));
            ((TextView) ReadSettingDialog.findViewById(R.id.line_space_setting_info)).setText(PostSetting.getLineSpace());
        }
    }

    private void initReadSettingListener() {
        if (ReadSettingDialog.isShowing()) {
            //字号改变
            ((SeekBar) ReadSettingDialog.findViewById(R.id.size_setting)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    //修改左侧数字
                    ((TextView) ReadSettingDialog.findViewById(R.id.size_setting_info)).setText(String.valueOf(seekBar.getProgress()));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //修改文章配置UI
                    UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SIZE, String.valueOf(seekBar.getProgress()));
                    adapter.notifyItemChanged(mIndex);//更新UI


                }
            });

            //字间距改变
            ((SeekBar) ReadSettingDialog.findViewById(R.id.font_space_setting)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    //修改左侧数字
                    ALog.d("");
                    ((TextView) ReadSettingDialog.findViewById(R.id.font_space_setting_info)).setText(String.valueOf(seekBar.getProgress()));

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {


                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //修改文章配置UI
                    UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SPACING, String.valueOf(seekBar.getProgress()));
                    adapter.notifyItemChanged(mIndex);//更新UI
                }
            });

            //行间距改变
            ((SeekBar) ReadSettingDialog.findViewById(R.id.line_space_setting)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    //修改左侧数字
                    ((TextView) ReadSettingDialog.findViewById(R.id.line_space_setting_info)).setText(String.valueOf(seekBar.getProgress()));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    ALog.d("");

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //修改文章配置UI
                    UserPreference.updateOrSaveValueByKey(PostSetting.LINE_SPACING, String.valueOf(seekBar.getProgress()));
                    adapter.notifyItemChanged(mIndex);//更新UI

                }
            });
        }
    }


    private void openLink(FeedItem feedItem) {
        String url = currentFeedItem.getUrl();
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if (pattern.matcher(url).matches()) {
            Toasty.info(this, "该文章没有外链哦").show();
        } else {
            /*if (url.startsWith("/")){//相对地址
                Feed feed = LitePal.find(Feed.class,currentFeedItem.getFeedId());
                String origin = feed.getLink();
                if (!origin.endsWith("/")){
                    origin = origin + "/";
                }
                url = origin + url;
            }*/
            WebViewUtil.openLink(url, PostDetailActivity.this);
        }
    }


    private void updateNotReadNum() {
        this.notReadNum--;

        //UI修改
        if (notReadNum <= 0) {
            binding.toolbar.setTitle("");
        } else {
            binding.toolbar.setTitle(notReadNum + "");
        }

    }

    @Override
    protected void onDestroy() {
        binding = null;

        //将首页中已读的文章样式标记为已读
        if (readList.size() > 0) {
            if (origin == ORIGIN_SEARCH) {//isUpdateMainReadMark 为false表示不是首页进来的
                EventBus.getDefault().post(new EventMessage(EventMessage.MAKE_READ_STATUS_BY_ID_LIST, readList));
            } else if (origin == ORIGIN_MAIN) {
                EventBus.getDefault().post(new EventMessage(EventMessage.MAKE_READ_STATUS_BY_INDEX_LIST, readList));
            }
            //修改首页未读数目
            EventBus.getDefault().post(new EventMessage(EventMessage.MAIN_READ_NUM_EDIT, mIndex));
        }


        super.onDestroy();
        EventBus.getDefault().unregister(this);
        ALog.d("postDetail 被销毁");
    }

}
