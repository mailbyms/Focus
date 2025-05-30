package com.ihewro.focus.bean;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;
import org.litepal.crud.callback.FindMultiCallback;
import org.litepal.crud.callback.SaveCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/06
 *     desc   : 用户个人设置表
 *     version: 1.0
 * </pre>
 */
public class UserPreference extends LitePalSupport {

    public static final String USE_INTERNET_WHILE_OPEN =  "pref_key_use_internet_while_open";
    public static final String AUTO_SET_FEED_NAME =  "AUTO_SET_FEED_NAME";
    public static final String ODER_CHOICE = "ODER_CHOICE";//排序规则
    public static final String FILTER_CHOICE = "FILTER_CHOICE";//过滤规则
    public static final String notOpenClick = "notOpenClick";//是否禁止上拉打开外链
    public static final String doubleClickStar = "doubleClickStar";//是否禁止双击收藏
    public static final String doubleClickTop = "doubleClickTop";//是否禁止双击顶部
    public static final String not_show_image_in_list = "not_show_image_in_list";//首页列表不要显示图片
    public static final String tim_interval = "tim_interval";//后台请求间隔
    public static final String tim_is_open = "tim_is_open";//定时器是否已经开启
    public static final String back_error = "back_error";//定时器是否已经开启
    public static final String notWifi = "notWifi";//仅仅wifi下请求
    public static final String notUseChrome = "notUseChrome";//不使用Chrome内核
    public static final String READ_BACKGROUND = "READ_BACKGROUND";//阅读背景




    public static final String  RSS_HUB =  "rsshub";
    public static final String FIRST_INTRO_DISCOVER = "FIRST_INTRO_DISCOVER";//首次介绍发现市场的功能，包括手动订阅，添加订阅
    public static final String DEFAULT_RSSHUB = "跟随上一级设置";


    private int id;//主键

    @Column(unique = true)
    private String key;
    private String value;
    private String defaultValue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public  static  Map<String,String> map = new HashMap<>();


    public static void initCacheMap(){
        map.clear();
        LitePal.findAllAsync(UserPreference.class).listen(new FindMultiCallback<UserPreference>() {
            @Override
            public void onFinish(List<UserPreference> list) {
                for (UserPreference userPreference:list){
                    map.put(userPreference.getKey(),userPreference.getValue());
                }
            }
        });
    }

    public UserPreference(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public UserPreference(String key, String value, String defaultValue) {
        this.key = key;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public static String queryValueByKey(String key, String defaultValue){
        if (map.containsKey(key)){
//            ALog.d("缓存数据");
            return map.get(key);
        }else {
//            ALog.d("非缓存数据");
            List<UserPreference> userPreferences = LitePal.where("key = ?", key).find(UserPreference.class);
            if (userPreferences.size()>0){
                UserPreference temp = userPreferences.get(0);
                return temp.getValue();
            }else {
                return defaultValue;
            }
        }
    }




    private static void setValueByKey(final String key, final String value){
        UserPreference userPreference = new UserPreference(key,value);
        userPreference.saveAsync().listen(new SaveCallback() {
            @Override
            public void onFinish(boolean success) {
                map.put(key,value);
            }
        });
    }


    public static void updateOrSaveValueByKey(String key, String value){
        List<UserPreference> userPreferences = LitePal.where("key = ?", key).find(UserPreference.class);
        if (userPreferences.size()>0){
            UserPreference temp = userPreferences.get(0);
            temp.setValue(value);
            temp.save();
            //修改缓存中键值对
            if (map.containsKey(key)){
                map.remove(key);
                map.put(key,value);
            }
        }else {
            setValueByKey(key,value);
        }
    }

    public static void updateOrSaveValueByKeyAsync(final String key, final String value, final FindMultiCallback<UserPreference> callback){
        LitePal.where("key = ?", key).limit(0).findAsync(UserPreference.class).listen(new FindMultiCallback<UserPreference>() {
            @Override
            public void onFinish(List<UserPreference> list) {
                if (list.size() > 0) {//缓存中有该值
                    UserPreference temp = list.get(0);
                    temp.setValue(value);
                    temp.save();
                    //修改缓存中键值对
                    if (map.containsKey(key)) {
                        map.remove(key);
                        map.put(key, value);
                    }
                } else {
                    setValueByKey(key, value);
                }

                callback.onFinish(list);
            }
        });
    }

}
