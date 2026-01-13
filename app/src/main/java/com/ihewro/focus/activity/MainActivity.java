package com.ihewro.focus.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
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
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedFolder;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.Help;
import com.ihewro.focus.fragemnt.UserFeedUpdateContentFragment;
import com.ihewro.focus.fragemnt.search.SearchFeedFolderFragment;
import com.ihewro.focus.fragemnt.search.SearchFeedItemListFragment;
import com.ihewro.focus.fragemnt.search.SearchLocalFeedListFragment;
import com.ihewro.focus.task.TimingService;
import com.ihewro.focus.databinding.ActivityMainBinding;
import com.ihewro.focus.util.StatusBarUtil;
import com.ihewro.focus.util.UIUtil;
import com.ihewro.focus.view.FeedFolderOperationPopupView;
import com.ihewro.focus.view.FeedListShadowPopupView;
import com.ihewro.focus.view.FeedOperationPopupView;
import com.ihewro.focus.view.FilterPopupView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
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

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;


public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    private static final int DRAWER_FOLDER_ITEM = 847;
    private static final int DRAWER_FOLDER = 301;
    private static final int SHOW_ALL = 14;
    private static final int FEED_FOLDER_IDENTIFY_PLUS = 9999;
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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary), 0);

        binding.toolbar.inflateMenu(R.menu.main);
        binding.toolbar.inflateMenu(R.menu.main);
        binding.toolbar.inflateMenu(R.menu.main);
        binding.toolbar.inflateMenu(R.menu.main);
        binding.toolbar.inflateMenu(R.menu.main);

        if (getIntent() != null){
            boolean flag = getIntent().getBooleanExtra(GlobalConfig.is_need_update_main,false);
            if (flag){
                //更新数据
                /*updateDrawer();
                clickAndUpdateMainFragmentData(new ArrayList<String>(), "全部文章");*/

            }
        }
        setSupportActionBar(binding.toolbar);
        binding.toolbarTitle.setText("全部文章");
        EventBus.getDefault().register(this);


        initEmptyView();

        clickFeedPostsFragment(new ArrayList<String>());

        initListener();

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
        binding.playButton.setOnLongClickListener(new View.OnLongClickListener() {
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
                                        .title(binding.toolbarTitle.getText())
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
                //回顶�?
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


        binding.playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
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
        searchResults = LitePal.where("name like ? or desc like ? or url like ?", text, text, text).find(Feed.class);
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
                    .atView(binding.playButton)
                    .hasShadowBg(true)
                    .setPopupCallback(new SimpleCallback() {
                        public void onShow() {
                            popupView.getAdapter().setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                                    if (view.getId() == R.id.item_view) {
                                        int feedFolderId = popupView.getFeedFolders().get(position).getId();
                                        List<Feed> feeds = LitePal.where("feedfolderid = ?", String.valueOf(feedFolderId)).find(Feed.class);
                                        ArrayList<String> list = new ArrayList<>();

                                        for (int i = 0; i < feeds.size(); i++) {
                                            list.add(String.valueOf(feeds.get(i).getId()));
                                        }
                                        //切换到指定文件夹�?
                                        clickAndUpdateMainFragmentData(list, popupView.getFeedFolders().get(position).getName(),-1);
                                        popupView.dismiss();//关闭弹窗
                                    }
                                }
                            });
                        }

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

        //TODO:构造侧边栏项目 使用线程�?
        createDrawer();

        //构造右侧栏�?
        createRightDrawer();

    }


    public void createDrawer() {

        buildDrawer();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //初始化侧边栏  子线程刷�?不要阻塞
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

        int color = R.color.material_drawer_secondary_text;
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                // .withCompactStyle(true)
//                .withHeaderBackground(R.drawable.moecats)
                .withTextColorRes(color)
                .addProfiles(
                        profile
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        headerResult.getView().findViewById(R.id.material_drawer_account_header_current).setVisibility(View.GONE);




        //初始化侧边栏
        drawer = new DrawerBuilder().withActivity(this)
                .withActivity(this)
                .withToolbar(binding.toolbar)
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

        // 设置自定义汉堡菜单图标（替换 MaterialDrawer 默认图标）
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer();
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

                        //TODO: 当一开始选中文件夹的时候总是报错�?
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
                case DRAWER_FOLDER_ITEM:
//                    ALog.d("名称�? + ((SecondaryDrawerItem) drawerItem).getName() + "id�? + drawerItem.getIdentifier());
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
                    //获取到这个文件夹的数�?
                    new XPopup.Builder(MainActivity.this)
                            .asCustom(new FeedFolderOperationPopupView(MainActivity.this, drawerItem.getIdentifier() - FEED_FOLDER_IDENTIFY_PLUS, ((ExpandableBadgeDrawerItem) drawerItem).getName().toString(), "", new Help(false)))
                            .show();
                    break;
                case DRAWER_FOLDER_ITEM:
                    //获取到这个feed的数�?
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
            feedPostsFragment = UserFeedUpdateContentFragment.newInstance(feedIdList, binding.toolbarTitle,binding.subtitle);
        }
        binding.toolbar.setTitle("全部文章");
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
            binding.toolbarTitle.setText(title);
            feedPostsFragment.updateData(feedIdList);
        }

    }


    /**
     * 获取用户的订阅数据，显示在左侧边栏的drawer�?
     */
    public synchronized void refreshLeftDrawerFeedList(boolean isUpdate) {

        subItems.clear();

        AllDrawerItem = new SecondaryDrawerItem().withName("全部").withIcon(GoogleMaterial.Icon.gmd_home).withSelectable(true).withTag(SHOW_ALL);
        subItems.add(AllDrawerItem);
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
                    //TODO: 加载订阅的图�?
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
            //恢复折叠状�?

            //
//            one.getViewHolder(R.)

            if (haveErrorFeedInCurrentFolder) {
                one.withTextColorRes(R.color.md_red_700);
            }
            if (notReadNum != 0) {
                one.withBadge(notReadNum + "");
            }
            //添加文件�?
            subItems.add(one);
        }

        //要记得把这个list置空
        errorFeedIdList.clear();

    }


    /**
     * 添加或者显�?fragment
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            drawerPopupView.toggle();
        } else if (id == R.id.action_search) {
            if (feedPostsFragment != null) {
                feedPostsFragment.triggerRefresh();
            }
        }
        return true;
    }

    long startTime = 0;

    @Override
    public void onBackPressed() {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - startTime) >= 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                startTime = currentTime;
            } else {
                finish();
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
            }, 100); // 延迟一下，因为数据异步存储需要时�?
        }else if (EventMessage.updateBadge.contains(eventBusMessage.getType())){//只需要修改侧边栏阅读书目
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //打印map
                    ALog.d("更新左侧边栏");
                    updateDrawer();
                }
            }, 100); // 延迟一下，因为数据异步存储需要时�?

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
                    //@Override
                    public void onShow() {

                    }

                    //@Override
                    public void onDismiss() {
                        //刷新当前页面的数据，因为筛选的规则变了
                        if (drawerPopupView.isNeedUpdate()) {
                            clickAndUpdateMainFragmentData(feedPostsFragment.getFeedIdList(), binding.toolbarTitle.getText().toString(),selectIdentify);
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
        binding = null;
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
