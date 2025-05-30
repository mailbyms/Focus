package com.ihewro.focus.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.BaseViewPagerAdapter;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedFolder;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.Help;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.fragemnt.UserFeedUpdateContentFragment;
import com.ihewro.focus.fragemnt.search.SearchFeedFolderFragment;
import com.ihewro.focus.fragemnt.search.SearchFeedItemListFragment;
import com.ihewro.focus.fragemnt.search.SearchLocalFeedListFragment;
import com.ihewro.focus.task.TimingService;
import com.ihewro.focus.util.UIUtil;
import com.ihewro.focus.view.FeedFolderOperationPopupView;
import com.ihewro.focus.view.FeedListShadowPopupView;
import com.ihewro.focus.view.FeedOperationPopupView;
import com.ihewro.focus.view.FilterPopupView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import skin.support.SkinCompatManager;
import skin.support.utils.SkinPreference;


public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.playButton)
    ButtonBarLayout playButton;
    @BindView(R.id.fl_main_body)
    FrameLayout flMainBody;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;

    private static final int DRAWER_FOLDER_ITEM = 847;
    private static final int DRAWER_FOLDER = 301;
    private static final int SHOW_ALL = 14;
    private static final int SHOW_STAR = 876;
    private static final int SHOW_DISCOVER = 509;
    private static final int ADD_AUTH = 24;
    private static final int FEED_MANAGE = 460;
    private static final int SETTING = 911;
    private static final int PAY_SUPPORT = 71;
    private static final int FEED_FOLDER_IDENTIFY_PLUS = 9999;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.search_view_content)
    LinearLayout searchViewContent;
    @BindView(R.id.subtitle)
    TextView subtitle;
    public static final int RQUEST_STORAGE_READ = 8;

    private int[] expandPositions;



    private UserFeedUpdateContentFragment feedPostsFragment;
    private Fragment currentFragment = null;
    private List<IDrawerItem> subItems = new ArrayList<>();
    private Drawer drawer;
    private FeedListShadowPopupView popupView;//点击顶部标题的弹窗
    private FilterPopupView drawerPopupView;//右侧边栏弹窗
    private List<String> errorFeedIdList = new ArrayList<>();

    private List<Fragment> fragmentList = new ArrayList<>();
    private SearchLocalFeedListFragment searchLocalFeedListFragment;
    private SearchFeedFolderFragment searchFeedFolderFragment;
    private SearchFeedItemListFragment searchFeedItemListFragment;
    private IDrawerItem AllDrawerItem;

    private long selectIdentify;

    private List<Long> expandFolderIdentify = new ArrayList<>();

    public static void activityStart(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);



        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            toolbar.inflateMenu(R.menu.main_night);
        } else {
            toolbar.inflateMenu(R.menu.main);
        }

        if (getIntent() != null){
            boolean flag = getIntent().getBooleanExtra(GlobalConfig.is_need_update_main,false);
            if (flag){
                //更新数据
                /*updateDrawer();
                clickAndUpdateMainFragmentData(new ArrayList<String>(), "全部文章");*/

            }
        }
        setSupportActionBar(toolbar);
        toolbarTitle.setText("全部文章");
        EventBus.getDefault().register(this);


        initEmptyView();

        clickFeedPostsFragment(new ArrayList<String>());

        initListener();

        createTabLayout();

        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!EasyPermissions.hasPermissions(this, perms)) {
            //没有权限 1. 申请权限
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RQUEST_STORAGE_READ, perms)
                            .setRationale("需要存储器读写权限以便后续备份和导入导出功能使用")
                            .setPositiveButtonText("确定")
                            .setNegativeButtonText("取消")
                            .build());
        }

        //开启定时任务
        startTimeService();


    }

    private void startTimeService(){
        TimingService.startService(this,false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals("")) {
                    updateTabLayout(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    updateTabLayout(newText);
                }
                return true;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                searchViewContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                searchViewContent.setVisibility(View.GONE);
            }
        });

        playButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //加载框
                final MaterialDialog loading = new MaterialDialog.Builder(MainActivity.this)
                        .content("统计数据中……")
                        .progress(false, 0, true)
                        .build();
                loading.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int feedItemNum = feedPostsFragment.getFeedItemNum();
                        final int notReadNum = feedPostsFragment.getNotReadNum();
                        UIUtil.runOnUiThread(MainActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                loading.dismiss();
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(toolbarTitle.getText())
                                        .content("全部数目" + feedItemNum + "\n" + "未读数目" + notReadNum)
                                        .show();
                            }
                        });
                    }
                }).start();
                return true;
            }
        });

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {//双击事件
                //回顶部
                ALog.d("双击");
                EventBus.getDefault().post(new EventMessage(EventMessage.GO_TO_LIST_TOP));
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                ALog.d("单击");
                toggleFeedListPopupView();
                return true;
            }
        });

        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void createTabLayout() {
        //碎片列表
        fragmentList.clear();
        searchFeedFolderFragment = new SearchFeedFolderFragment(this);
        searchLocalFeedListFragment = new SearchLocalFeedListFragment(this);
        searchFeedItemListFragment = new SearchFeedItemListFragment(this);
        fragmentList.add(searchFeedFolderFragment);
        fragmentList.add(searchLocalFeedListFragment);
        fragmentList.add(searchFeedItemListFragment);

        //标题列表
        List<String> pageTitleList = new ArrayList<>();
        pageTitleList.add("文件夹");
        pageTitleList.add("订阅");
        pageTitleList.add("文章");

        //新建适配器
        BaseViewPagerAdapter adapter = new BaseViewPagerAdapter(getSupportFragmentManager(), fragmentList, pageTitleList);

        //设置ViewPager
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        //适配夜间模式
        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            tabLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary_night));
        } else {
            tabLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary));
        }
    }

    private void updateTabLayout(final String text) {
        //显示动画
        searchFeedItemListFragment.showLoading();
        searchLocalFeedListFragment.showLoading();
        searchFeedFolderFragment.showLoading();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<FeedItem> searchResults;
                String text2 = "%" + text + "%";
                searchResults = LitePal.where("title like ? or summary like ?", text2, text2).find(FeedItem.class);

                final List<Feed> searchResults2;
                text2 = "%" + text + "%";
                searchResults2 = LitePal.where("name like ? or desc like ?", text2, text2).find(Feed.class);

                final List<FeedFolder> searchResult3s;
                text2 = "%" + text + "%";
                searchResult3s = LitePal.where("name like ?", text2).find(FeedFolder.class);

                UIUtil.runOnUiThread(MainActivity.this,new Runnable() {
                    @Override
                    public void run() {
                        searchFeedItemListFragment.updateData(searchResults);
                        searchLocalFeedListFragment.updateData(searchResults2);
                        searchFeedFolderFragment.updateData(searchResult3s);
                    }
                });
            }
        }).start();
    }

    /**
     * 全文搜索🔍
     *
     * @param text
     * @return
     */
    public void queryFeedItemByText(String text) {
        List<FeedItem> searchResults;
        text = "%" + text + "%";
        searchResults = LitePal.where("title like ? or summary like ?", text, text).find(FeedItem.class);
        searchFeedItemListFragment.updateData(searchResults);
    }


    public void queryFeedByText(String text) {
        List<Feed> searchResults;
        text = "%" + text + "%";
        searchResults = LitePal.where("name like ? or desc like ?", text, text).find(Feed.class);
        searchLocalFeedListFragment.updateData(searchResults);
    }

    public void queryFeedFolderByText(String text) {
        List<FeedFolder> searchResults;
        text = "%" + text + "%";
        searchResults = LitePal.where("name like ?", text).find(FeedFolder.class);
        searchFeedFolderFragment.updateData(searchResults);
    }


    private void toggleFeedListPopupView() {
        //显示弹窗
        if (popupView == null) {
            popupView = (FeedListShadowPopupView) new XPopup.Builder(MainActivity.this)
                    .atView(playButton)
                    .hasShadowBg(true)
                    .setPopupCallback(new SimpleCallback() {
                        @Override
                        public void onShow() {
                            popupView.getAdapter().setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                                @Override
                                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                                    if (view.getId() == R.id.item_view) {
                                        int feedFolderId = popupView.getFeedFolders().get(position).getId();
                                        List<Feed> feeds = LitePal.where("feedfolderid = ?", String.valueOf(feedFolderId)).find(Feed.class);
                                        ArrayList<String> list = new ArrayList<>();

                                        for (int i = 0; i < feeds.size(); i++) {
                                            list.add(String.valueOf(feeds.get(i).getId()));
                                        }
                                        //切换到指定文件夹下
                                        clickAndUpdateMainFragmentData(list, popupView.getFeedFolders().get(position).getName(),-1);
                                        popupView.dismiss();//关闭弹窗
                                    }
                                }
                            });
                        }

                        @Override
                        public void onDismiss() {
                        }
                    })
                    .asCustom(new FeedListShadowPopupView(MainActivity.this));
        }
        popupView.toggle();
    }


    public void initEmptyView() {
        initDrawer();
    }

    //初始化侧边栏
    public void initDrawer() {

        //TODO:构造侧边栏项目 使用线程！
        createDrawer();

        //构造右侧栏目
        createRightDrawer();

    }


    public void createDrawer() {

        buildDrawer();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //初始化侧边栏  子线程刷新 不要阻塞
                refreshLeftDrawerFeedList(false);
                UIUtil.runOnUiThread(MainActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        drawer.setItems(subItems);
                    }
                });
            }
        }).start();




    }


    private void buildDrawer(){
        //顶部
        // Create a few sample profile
        final IProfile profile = new ProfileDrawerItem().withName("本地RSS").withEmail("数据备份在本地");

        int color;
        if(SkinPreference.getInstance().getSkinName().equals("night")){
            color = R.color.material_drawer_dark_secondary_text;
        }else {
            color = R.color.material_drawer_secondary_text;
        }
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
//                .withHeaderBackground(R.drawable.moecats)
                .withTextColorRes(color)
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName("添加第三方服务").withDescription("添加内容源").withIcon(GoogleMaterial.Icon.gmd_add).withIdentifier(ADD_AUTH)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {

                        if (!currentProfile){
                            switch ((int) profile.getIdentifier()){
                                case ADD_AUTH:
                                    AuthListActivity.activityStart(MainActivity.this);
                                    break;
                            }
                        }

                        return false;
                    }
                })
                .build();

        headerResult.getView().findViewById(R.id.material_drawer_account_header_current).setVisibility(View.GONE);




        //初始化侧边栏
        drawer = new DrawerBuilder().withActivity(this)
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .withAccountHeader(headerResult)
                .addDrawerItems((IDrawerItem[]) Objects.requireNonNull(subItems.toArray(new IDrawerItem[subItems.size()])))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        drawerItemClick(drawerItem);
                        return false;
                    }
                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {
                        drawerLongClick(drawerItem);
                        return true;
                    }
                })
                .withStickyFooter(R.layout.component_drawer_foooter)
                .withStickyFooterShadow(false)
                .build();



        //初始化顶部的内容包括颜色
        boolean flag = false;
        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            flag = true;
            ((TextView)(drawer.getStickyFooter().findViewById(R.id.mode_text))).setText("日间");
        }else {
            ((TextView)(drawer.getStickyFooter().findViewById(R.id.mode_text))).setText("夜间");
        }

        final boolean finalFlag = flag;
        drawer.getStickyFooter().findViewById(R.id.mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!finalFlag) {//flag true 表示夜间模式
                    SkinCompatManager.getInstance().loadSkin("night", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    }, 200); // 延时1秒
                } else {
                    SkinCompatManager.getInstance().restoreDefaultTheme();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    }, 200); // 延时1秒
                }
            }
        });


        drawer.getStickyFooter().findViewById(R.id.manage).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                FeedManageActivity.activityStart(MainActivity.this);
            }
        });


        drawer.getStickyFooter().findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingActivity.activityStart(MainActivity.this);
            }
        });

    }



    private void updateDrawer() {
        //初始化侧边栏
        new Thread(new Runnable() {
            @Override
            public void run() {
                selectIdentify = drawer.getCurrentSelection();
                ALog.d("选择项" + selectIdentify);
                expandPositions = drawer.getExpandableExtension().getExpandedItems();
                refreshLeftDrawerFeedList(true);
                UIUtil.runOnUiThread(MainActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        List<IDrawerItem> templist = new ArrayList<>(subItems);
                        drawer.setItems(templist);
                        //恢复折叠
                        int[] temp = expandPositions.clone();

                        for(int i = 0;i<temp.length;i++){
                            drawer.getExpandableExtension().expand(temp[i]);
                        }

                        //TODO: 当一开始选中文件夹的时候总是报错！
                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                drawer.setSelection(selectIdentify);
                            }
                        }, 800);*/

                    }
                });
            }
        }).start();
    }

    private void drawerItemClick(IDrawerItem drawerItem) {
        if (drawerItem.getTag() != null) {
            switch ((int) drawerItem.getTag()) {
                case SHOW_ALL:
                    clickAndUpdateMainFragmentData(new ArrayList<String>(), "全部文章",drawerItem.getIdentifier());
                    break;
                case SHOW_STAR:
                    StarActivity.activityStart(MainActivity.this);
                    break;
                case DRAWER_FOLDER_ITEM:
                    ArrayList<String> list = new ArrayList<>();
                    list.add(String.valueOf(drawerItem.getIdentifier()));
                    clickAndUpdateMainFragmentData(list, ((SecondaryDrawerItem) drawerItem).getName().toString(),drawerItem.getIdentifier());
                    break;
                case DRAWER_FOLDER:
                    break;
            }
        }
    }


    private void drawerLongClick(IDrawerItem drawerItem) {

        if (drawerItem.getTag() != null) {
            switch ((int) drawerItem.getTag()) {
                case DRAWER_FOLDER:
                    //获取到这个文件夹的数据
                    new XPopup.Builder(MainActivity.this)
                            .asCustom(new FeedFolderOperationPopupView(MainActivity.this, drawerItem.getIdentifier() - FEED_FOLDER_IDENTIFY_PLUS, ((ExpandableBadgeDrawerItem) drawerItem).getName().toString(), "", new Help(false)))
                            .show();
                    break;
                case DRAWER_FOLDER_ITEM:
                    //获取到这个feed的数据
                    new XPopup.Builder(MainActivity.this)
                            .asCustom(new FeedOperationPopupView(MainActivity.this, drawerItem.getIdentifier(), ((SecondaryDrawerItem) drawerItem).getName().toString(), "", new Help(false)))
                            .show();
                    break;
            }
        }
    }

    /**
     * 初始化主fragment
     *
     * @param feedIdList
     */
    private void clickFeedPostsFragment(ArrayList<String> feedIdList) {
        if (feedPostsFragment == null) {
            feedPostsFragment = UserFeedUpdateContentFragment.newInstance(feedIdList, toolbarTitle,subtitle);
        }
        toolbar.setTitle("全部文章");
        addOrShowFragment(getSupportFragmentManager().beginTransaction(), feedPostsFragment);
    }

    /**
     * 更新主fragment的内部数据并修改UI
     *
     * @param feedIdList
     * @param title
     */
    private void clickAndUpdateMainFragmentData(ArrayList<String> feedIdList, String title,long identify) {
        if (feedPostsFragment == null) {
            ALog.d("出现未知错误");
        } else {
            toolbarTitle.setText(title);
            feedPostsFragment.updateData(feedIdList);
        }

    }


    /**
     * 获取用户的订阅数据，显示在左侧边栏的drawer中
     */
    public synchronized void refreshLeftDrawerFeedList(boolean isUpdate) {

        subItems.clear();

        AllDrawerItem = new SecondaryDrawerItem().withName("全部").withIcon(GoogleMaterial.Icon.gmd_home).withSelectable(true).withTag(SHOW_ALL);
        subItems.add(AllDrawerItem);
        subItems.add(new SecondaryDrawerItem().withName("收藏").withIcon(GoogleMaterial.Icon.gmd_star).withSelectable(false).withTag(SHOW_STAR));
        subItems.add(new SectionDrawerItem().withName("订阅源").withDivider(false));




        List<FeedFolder> feedFolderList = LitePal.order("ordervalue").find(FeedFolder.class);
        for (int i = 0; i < feedFolderList.size(); i++) {

            int notReadNum = 0;

            List<IDrawerItem> feedItems = new ArrayList<>();
            List<Feed> feedList = LitePal.where("feedfolderid = ?", String.valueOf(feedFolderList.get(i).getId())).order("ordervalue").find(Feed.class);

            boolean haveErrorFeedInCurrentFolder = false;
            for (int j = 0; j < feedList.size(); j++) {
                final Feed temp = feedList.get(j);
                int current_notReadNum = LitePal.where("read = ? and feedid = ?", "0", String.valueOf(temp.getId())).count(FeedItem.class);

                final SecondaryDrawerItem secondaryDrawerItem = new SecondaryDrawerItem().withName(temp.getName()).withSelectable(true).withTag(DRAWER_FOLDER_ITEM).withIdentifier(feedList.get(j).getId());
                if (feedList.get(j).isOffline()){
                    secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_cloud_off);
                }else if (feedList.get(j).isErrorGet()) {
                    haveErrorFeedInCurrentFolder = true;
                    secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_sync_problem);
                } else {
                    //TODO: 加载订阅的图标
                    secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_rss_feed);



                }

                if (current_notReadNum != 0) {
                    secondaryDrawerItem.withBadge(current_notReadNum + "");
                }
                //不需要这样了，因为都是直接setitems来更新的
                /*if (isUpdate) {
                    drawer.updateItem(secondaryDrawerItem);
                }*/
                feedItems.add(secondaryDrawerItem);

                notReadNum += current_notReadNum;
            }
            ExpandableBadgeDrawerItem one = new ExpandableBadgeDrawerItem().withName(feedFolderList.get(i).getName()).withSelectable(true).withIdentifier(feedFolderList.get(i).getId()+FEED_FOLDER_IDENTIFY_PLUS).withTag(DRAWER_FOLDER).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700)).withSubItems(
                    feedItems
            );

            ALog.d("文件夹的identity" + (feedFolderList.get(i).getId()+FEED_FOLDER_IDENTIFY_PLUS));
            //恢复折叠状态

            //
//            one.getViewHolder(R.)

            if (haveErrorFeedInCurrentFolder) {
                one.withTextColorRes(R.color.md_red_700);
            }
            if (notReadNum != 0) {
                one.withBadge(notReadNum + "");
            }
            //添加文件夹
            subItems.add(one);
        }

        //要记得把这个list置空
        errorFeedIdList.clear();

    }


    /**
     * 添加或者显示 fragment
     *
     * @param transaction
     * @param fragment
     */
    private void addOrShowFragment(FragmentTransaction transaction, Fragment fragment) {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        //当前的fragment就是点击切换的目标fragment，则不用操作
        if (currentFragment == fragment) {
            return;
        }

        Fragment willCloseFragment = currentFragment;//上一个要切换掉的碎片
        currentFragment = fragment;//当前要显示的碎片

        if (willCloseFragment != null) {
            transaction.hide(willCloseFragment);
        }
        if (!fragment.isAdded()) { // 如果当前fragment未被添加，则添加到Fragment管理器中
            transaction.add(R.id.fl_main_body, currentFragment).commitAllowingStateLoss();
        } else {
            transaction.show(currentFragment).commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            getMenuInflater().inflate(R.menu.main_night, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_filter:

                drawerPopupView.toggle();
                break;

            case R.id.action_rsshub:
                //显示弹窗
                //之前选择的位置
                final int select = GlobalConfig.rssHub.indexOf(UserPreference.queryValueByKey(UserPreference.RSS_HUB,GlobalConfig.OfficialRSSHUB));
                ALog.d(UserPreference.getRssHubUrl());
                List<String> list = GlobalConfig.rssHub;
                new MaterialDialog.Builder(this)
                        .title("rsshub源选择")
                        .items(list)
                        .itemsCallbackSingleChoice(select, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which>=0 && which<3){
                                    UserPreference.updateOrSaveValueByKey(UserPreference.RSS_HUB,GlobalConfig.rssHub.get(which));
                                    return true;
                                }
                                return false;
                            }
                        })
                        .positiveText("选择")
                        .show();
                break;
        }
        return true;
    }

    long startTime = 0;

    @Override
    public void onBackPressed() {
        //返回键关闭🔍搜索
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - startTime) >= 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                startTime = currentTime;
            } else {
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void refreshUI(EventMessage eventBusMessage) {
        if (EventMessage.feedAndFeedFolderAndItemOperation.contains(eventBusMessage.getType())) {//更新整个左侧边栏
//            ALog.d("收到新的订阅添加，更新！" + eventBusMessage);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ALog.d("重构");
                    updateDrawer();
                }
            }, 100); // 延迟一下，因为数据异步存储需要时间
        }else if (EventMessage.updateBadge.contains(eventBusMessage.getType())){//只需要修改侧边栏阅读书目
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //打印map
                    ALog.d("更新左侧边栏");
                    updateDrawer();
                }
            }, 100); // 延迟一下，因为数据异步存储需要时间

        } else if (Objects.equals(eventBusMessage.getType(), EventMessage.FEED_PULL_DATA_ERROR)) {
//            ALog.d("收到错误FeedId List");
//            errorFeedIdList = eventBusMessage.getIds();
        }
    }


    private void createRightDrawer() {
        drawerPopupView = (FilterPopupView) new XPopup.Builder(this)
                .popupPosition(PopupPosition.Right)//右边
                .hasStatusBarShadow(true) //启用状态栏阴影
                .setPopupCallback(new SimpleCallback() {
                    @Override
                    public void onShow() {

                    }

                    @Override
                    public void onDismiss() {
                        //刷新当前页面的数据，因为筛选的规则变了
                        if (drawerPopupView.isNeedUpdate()) {
                            clickAndUpdateMainFragmentData(feedPostsFragment.getFeedIdList(), toolbarTitle.getText().toString(),selectIdentify);
                            drawerPopupView.setNeedUpdate(false);
                        }
                    }
                })
                .asCustom(new FilterPopupView(MainActivity.this));
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (currentFragment == null && fragment instanceof UserFeedUpdateContentFragment) {
            currentFragment = fragment;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        ALog.d("mainActivity 被销毁");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }



    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
        // This is needed to prevent welcome screens from being
        // automatically shown multiple times

        // This is the only one needed because it is the only one that
        // is shown automatically. The others are only force shown.
    }

}
