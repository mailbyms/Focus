package com.ihewro.focus.task;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.blankj.ALog;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.activity.MainActivity;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.callback.RequestDataCallback;
import com.ihewro.focus.util.DateUtil;
import com.ihewro.focus.util.UIUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimingService extends Service {

    private int num;//总共需要请求的数目
    private int okNum = 0;//已经请求的数目

    private int feedItemNum;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builderProgress;
    private Notification notification;


    private String interval;// 分钟
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static void startService(Context context,boolean isNewAlarm){
        Intent intent = new Intent(context, TimingService.class);
        intent.putExtra("isNewAlarm",isNewAlarm);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }

    }

    /**
     * 方式三：采用AlarmManager机制
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent!=null){
            boolean isNewAlarm = intent.getBooleanExtra("isNewAlarm",false);//是否需要重新设置定时器

            this.interval = UserPreference.queryValueByKey(UserPreference.tim_interval,"-1");
            int is_open = Integer.parseInt(UserPreference.queryValueByKey(UserPreference.tim_is_open,"0"));

            startForeground(2,createNotice("后台更新守护服务","当看到该通知说明服务正常周期运行，否则表示服务运行失败"));

            if (this.interval.equals("-1")){
                stopForeground(true);
                stopSelf();
            }else {
                //TODO: 如果已经启动了定时器，当打开应用的时候，不应该再次重新设置定时器
                //初始化参数
                int temp = Integer.parseInt(this.interval);//分钟
                AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
                long triggerAtTime = SystemClock.elapsedRealtime() + temp*60*1000;//每隔temp分钟执行一次
//            long triggerAtTime = SystemClock.elapsedRealtime() + 30*1000;//每隔30s执行一次
                Intent intent2 = new Intent(this, AutoUpdateReceiver.class);
                //如果存在这个pendingIntent 则复用

                PendingIntent pi = null;

                if (isNewAlarm){
                    PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);//重新创建定时器
                }else {
                    PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);//复用之前的定时器
                }

                // pendingIntent 为发送广播
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //android 6.0以上
                    manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pi);
                } else{
                    manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
                }

                //执行任务
                if (is_open == 1){//说明是定时器启动的，只有定时器启动这个service才会执行请求数据的任务
                    UserPreference.updateOrSaveValueByKey(UserPreference.tim_is_open,"0");

                    ALog.d("执行定时任务" + DateUtil.getNowDateStr());
                    List<Feed> feedList = LitePal.findAll(Feed.class);

                    //初始化参数
                    this.okNum = 0;
                    this.num =feedList.size();
                    this.feedItemNum = LitePal.count(FeedItem.class);

                    for (int i = 0;i < feedList.size();i++){
                        //改为线程池调用
                        RequestFeedListDataTask task = new RequestFeedListDataTask(new RequestDataCallback() {
                            @Override
                            public void onSuccess(List<FeedItem> feedItemList) {
                                //主线程
                                updateUI();
                            }
                        });

                        mNotificationManager.notify(1,createNotice("后台更新：开始获取数据中……",0));
                        ExecutorService mExecutor = Executors.newCachedThreadPool();
                        task.executeOnExecutor(mExecutor,feedList.get(i));
                    }

                }
            }

        }
         return super.onStartCommand(intent, flags, startId);
    }

    //主线程
    private void updateUI(){
        //主线程
        okNum++;

        //计算出当前的process
        int process = (int) (okNum *1.0 / num * 100);
        mNotificationManager.notify(1, createNotice("后台更新：获取中……",process));

        if (okNum >= num){//数据全部请求完毕

            int num = LitePal.count(FeedItem.class);
            final int sub = num - this.feedItemNum;
            if (sub>0){
                mNotificationManager.notify(1, createNotice("后台更新：共有"+sub+"篇新文章",100));
            }else {
                mNotificationManager.notify(1, createNotice("后台更新：暂无新数据",100));
            }
        }

    }



    private Notification createNotice(String title, int progress){
        //消息管理
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initChannels(getApplicationContext());
        builderProgress = new NotificationCompat.Builder(getApplicationContext(), "focus_pull_data");
        builderProgress.setContentTitle(title);
        builderProgress.setSmallIcon(R.mipmap.ic_launcher_round);
//        builderProgress.setTicker("进度条通知");

        if (progress > 0){//全部获取完的时候不需要显示进度条了
            builderProgress.setContentText(progress + "%");
            builderProgress.setProgress(100, progress, false);
        }
        if (progress == 100){
            builderProgress.setContentText(title);
        }
        //绑定点击事件
        Intent intent = new Intent(UIUtil.getContext(), MainActivity.class);
        intent.putExtra(GlobalConfig.is_need_update_main,true);
        PendingIntent pending_intent_go = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builderProgress.setAutoCancel(true);
        builderProgress.setContentIntent(pending_intent_go);

        notification = builderProgress.build();

        return notification;
    }


    private Notification createNotice(String title, String content){

        //消息管理
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initChannels(getApplicationContext());
        builderProgress = new NotificationCompat.Builder(getApplicationContext(), "focus_pull_data");
        builderProgress.setContentTitle(title);
        builderProgress.setSmallIcon(R.mipmap.ic_launcher_round);
//        builderProgress.setTicker("进度条通知");

        builderProgress.setContentText(content);

        //绑定点击事件
        Intent intent = new Intent(UIUtil.getContext(), MainActivity.class);
        intent.putExtra(GlobalConfig.is_need_update_main,true);
        PendingIntent pending_intent_go = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builderProgress.setContentIntent(pending_intent_go);

        notification = builderProgress.build();

        return notification;
    }


    /**
     * android 8.0 新增的notification channel这里需要做一个判断
     *
     * @param context
     */
    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("focus_pull_data",
                "Channel focus",
                NotificationManager.IMPORTANCE_MIN);
        channel.setDescription("更新订阅的数据");
        mNotificationManager.createNotificationChannel(channel);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

