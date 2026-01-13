package com.ihewro.focus.fragemnt.setting;

import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedFolder;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.callback.FileOperationCallback;
import com.ihewro.focus.task.FixDataTask;
import com.ihewro.focus.task.RecoverDataTask;
import com.ihewro.focus.util.DateUtil;
import com.ihewro.focus.util.FileUtil;
import com.ihewro.focus.util.StringUtil;
import com.ihewro.focus.util.UIUtil;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class DataFragment extends SettingFragment{

    private Preference back_up;
    private Preference recover_data;
    private Preference feed_info;
    private Preference clean_data;
    private Preference database_version;
    private Preference fix_database;
    private Preference test_insert;


    @Override
    public void initView() {
        addPreferencesFromResource(R.xml.pref_data_setting);
    }

    @Override
    public void initPreferenceComponent() {
        feed_info = findPreference(getString(R.string.pref_key_feed_num));

        back_up = findPreference(getString(R.string.pref_key_backup));
        recover_data = findPreference(getString(R.string.pref_key_recover));
        clean_data = findPreference(getString(R.string.pref_key_clean_database));
        database_version = findPreference(getString(R.string.pref_key_database_version));
        fix_database = findPreference(getString(R.string.pref_key_fix_database));
        test_insert = findPreference(getString(R.string.pref_key_test_insert));

    }

    @Override
    public void initPreferencesData() {
        feed_info.setSummary(LitePal.count(FeedFolder.class) + "个分类 " +LitePal.count(Feed.class)+"个订阅 " + LitePal.count(FeedItem.class) + "篇文章");
        database_version.setSummary(LitePal.getDatabase().getVersion() + "");
    }

    @Override
    public void initListener() {

        fix_database.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getActivity())
                        .title("确定修复数据库？")
                        .content("修复数据库可以删除错误数据。如果您是从旧版本升级到2.0+版本，使用该功能可以将您的收藏数据恢复。")
                        .positiveText("修复")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new FixDataTask(getActivity()).execute();
                            }
                        })
                        .show();

                return false;
            }
        });

        back_up.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getActivity())
                        .title("为什么需要备份？")
                        .content("本应用没有云端同步功能，本地备份功能可以尽可能保证您的数据库安全。\n数据库备份不同于OPML导出，不仅包含所有订阅信息，还包括您的已读、收藏信息。但仅限在Focus应用内部交换使用。\n应用会在您有任何操作数据库的操作时候自动备份最新的一次数据库。")
                        .positiveText("开始备份")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                FileUtil.copyFileToTarget(getActivity().getDatabasePath("focus.db").getAbsolutePath(), GlobalConfig.appDirPath + "database/" + DateUtil.getNowDateStr() + ".db", new FileOperationCallback() {
                                    @Override
                                    public void onFinish() {
                                        Toasty.success(getActivity(),"备份数据成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .show();
                return  false;
            }
        });

        recover_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new RecoverDataTask(getActivity()).execute();

                return false;
            }
        });

        clean_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //显示输入弹窗
                new MaterialDialog.Builder(getContext())
                        .title("输入每个订阅要保留的数目")
                        .content("每个订阅将只保留该数目的文章，如果订阅的文章数目小于该数字，则不会清理")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                final String num = dialog.getInputEditText().getText().toString().trim();
                                Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                                if (pattern.matcher(num).matches() && !StringUtil.trim(num).equals("")) {
                                    //清理数据库
                                    //加载对话框
                                    final MaterialDialog dialog1 = new MaterialDialog.Builder(getContext())
                                            .content("马上就好……")
                                            .progress(true, 0)
                                            .show();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            List<Feed> feedList = LitePal.findAll(Feed.class);
                                            int keepNum = Integer.parseInt(num);

                                            for (Feed feed : feedList) {
                                                // 获取该订阅源的文章总数
                                                int totalCount = (int) LitePal.where("feedid = ?", String.valueOf(feed.getId())).count(FeedItem.class);

                                                if (totalCount <= keepNum) {
                                                    // 文章数不足，无需删除
                                                    continue;
                                                }

                                                // 获取要保留的文章ID（最新的N条）
                                                List<FeedItem> keepItems = LitePal.where("feedid = ?", String.valueOf(feed.getId()))
                                                        .order("date desc")
                                                        .limit(keepNum)
                                                        .find(FeedItem.class);

                                                if (keepItems.size() < keepNum) {
                                                    // 实际文章数量不足，无需删除
                                                    continue;
                                                }

                                                // 构建要保留的ID字符串
                                                StringBuilder keepIdsStr = new StringBuilder();
                                                for (int i = 0; i < keepItems.size(); i++) {
                                                    if (i > 0) {
                                                        keepIdsStr.append(",");
                                                    }
                                                    keepIdsStr.append(keepItems.get(i).getId());
                                                }

                                                // 使用 SQL DELETE ... WHERE ... NOT IN 批量删除
                                                // 避免逐条删除，性能大幅提升
                                                LitePal.deleteAll(FeedItem.class,
                                                    "feedid = ? and id not in (" + keepIdsStr.toString() + ")",
                                                    String.valueOf(feed.getId()));
                                            }

                                            UIUtil.runOnUiThread(getActivity(), new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toasty.info(getActivity(), "清理成功！").show();
                                                    EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE_RECOVER));
                                                    if (dialog1.isShowing()) {
                                                        dialog1.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    }).start();




                                }else {
                                    //输入错误
                                    Toasty.info(getActivity(),"老实说你输入的是不是数字").show();
                                }
                            }
                        }).show();

                return false;
            }
        });

        test_insert.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getContext())
                        .title("确定要插入测试数据吗？")
                        .content("将生成并插入1000条随机文章到数据库，用于测试清理功能性能")
                        .positiveText("确定")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // 加载对话框
                                final MaterialDialog dialog1 = new MaterialDialog.Builder(getContext())
                                        .content("正在插入数据，请稍候……")
                                        .progress(true, 0)
                                        .cancelable(false)
                                        .show();

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 获取第一个订阅源，如果没有则创建一个测试订阅源
                                        List<Feed> feedList = LitePal.findAll(Feed.class);
                                        if (feedList.isEmpty()) {
                                            UIUtil.runOnUiThread(getActivity(), new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (dialog1.isShowing()) {
                                                        dialog1.dismiss();
                                                    }
                                                    Toasty.error(getActivity(), "请先添加至少一个订阅源").show();
                                                }
                                            });
                                            return;
                                        }

                                        Feed feed = feedList.get(0);
                                        long feedId = feed.getId();
                                        long currentTime = System.currentTimeMillis();
                                        Random random = new Random();

                                        // 生成1000条随机文章
                                        for (int i = 0; i < 1000; i++) {
                                            FeedItem feedItem = new FeedItem();
                                            feedItem.setFeedId((int) feedId);
                                            feedItem.setFeedName(feed.getName());
                                            feedItem.setTitle("测试文章 #" + (i + 1));
                                            feedItem.setUrl("https://example.com/article/" + (i + 1) + "?" + System.currentTimeMillis() + "-" + i);
                                            feedItem.setContent("这是测试文章 #" + (i + 1) + " 的内容。用于测试数据库清理功能。");
                                            feedItem.setSummary("测试文章摘要 #" + (i + 1));
                                            feedItem.setDate(currentTime - (i * 60000)); // 每篇文章时间间隔1分钟
                                            feedItem.setRead(false);
                                            feedItem.setFavorite(false);
                                            feedItem.save();

                                            // 每100条更新一次进度
                                            if ((i + 1) % 100 == 0) {
                                                final int progress = i + 1;
                                                UIUtil.runOnUiThread(getActivity(), new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dialog1.setContent("正在插入数据：" + progress + "/1000");
                                                    }
                                                });
                                            }
                                        }

                                        UIUtil.runOnUiThread(getActivity(), new Runnable() {
                                            @Override
                                            public void run() {
                                                if (dialog1.isShowing()) {
                                                    dialog1.dismiss();
                                                }
                                                Toasty.success(getActivity(), "成功插入1000条测试数据！").show();
                                                // 更新文章数目显示
                                                feed_info.setSummary(LitePal.count(FeedFolder.class) + "个分类 " +
                                                        LitePal.count(Feed.class) + "个订阅 " +
                                                        LitePal.count(FeedItem.class) + "篇文章");
                                                // 发送事件通知更新界面
                                                EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE_RECOVER));
                                            }
                                        });
                                    }
                                }).start();
                            }
                        })
                        .show();

                return false;
            }
        });

    }
}
