package com.ihewro.focus;

import android.content.Context;
import android.util.Log;

import com.blankj.ALog;
import com.ihewro.focus.activity.MainActivity;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.helper.BlockDetectByPrinter;
import com.ihewro.focus.util.ImageLoaderManager;
import com.zxy.recovery.callback.RecoveryCallback;
import com.zxy.recovery.core.Recovery;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/04/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MyApplication extends LitePalApplication {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        LitePal.getDatabase().disableWriteAheadLogging();
        mContext = this;
        initALog();
        initErrorHandle();

        BlockDetectByPrinter.start();
        ImageLoaderManager.init(this);

        Toasty.Config.getInstance()
                .allowQueue(true)
                .apply();

        UserPreference.initCacheMap();
    }

    private void initALog() {
        ALog.Config config = ALog.init(this)
                .setLogSwitch(BuildConfig.DEBUG)
                .setConsoleSwitch(BuildConfig.DEBUG)
                .setGlobalTag(null)
                .setLogHeadSwitch(true)
                .setLog2FileSwitch(false)
                .setDir("")
                .setFilePrefix("")
                .setBorderSwitch(true)
                .setSingleTagSwitch(true)
                .setConsoleFilter(ALog.V)
                .setFileFilter(ALog.V)
                .setStackDeep(1)
                .setStackOffset(0);
        ALog.d(config.toString());
    }

    private void initErrorHandle() {
        Recovery.getInstance()
                .debug(true)
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPage(MainActivity.class)
                .recoverEnabled(true)
                .callback(new MyCrashCallback())
                .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
                .skip(MainActivity.class)
                .init(this);
    }

    static final class MyCrashCallback implements RecoveryCallback {
        @Override
        public void stackTrace(String exceptionMessage) {
            Log.e("zxy", "exceptionMessage:" + exceptionMessage);
        }

        @Override
        public void cause(String cause) {
            Log.e("zxy", "cause:" + cause);
        }

        @Override
        public void exception(String exceptionType, String throwClassName, String throwMethodName, int throwLineNumber) {
            Log.e("zxy", "exceptionClassName:" + exceptionType);
            Log.e("zxy", "throwClassName:" + throwClassName);
            Log.e("zxy", "throwMethodName:" + throwMethodName);
            Log.e("zxy", "throwLineNumber:" + throwLineNumber);
        }

        @Override
        public void throwable(Throwable throwable) {
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
