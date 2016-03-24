package com.mmlab.n1.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.mmlab.n1.constant.IDENTITY;


/**
 * Created by mmlab on 2015/10/28.
 */
public class Preset {

    /**
     * 設定當前使用者模式
     *
     * @param context
     * @return
     */
    public static int loadPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("identity", IDENTITY.PROXY);
    }

    /**
     * 儲存當前使用者身分
     *
     * @param context
     * @param identity
     */
    public static void savePreferences(Context context, int identity) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("identity", identity).apply();
    }

    public static int loadSubPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("subidentity", IDENTITY.PROXY);
    }

    public static void saveSubPreferences(Context context, int identity) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("subidentity", identity).apply();
    }


    // Filter

    /**
     * 設定當前身份
     *
     * @param context
     * @return
     */
    public static int loadIndentifierPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("identifier", IDENTITY.PROXY);
    }

    /**
     * 儲存當前身分
     *
     * @param context
     * @param identifier
     */
    public static void saveIndentifierPreferences(Context context, int identifier) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("identifier", identifier).apply();
    }

    /**
     * 設定當前身份
     *
     * @param context
     * @return
     */
    public static int loadMediaPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("media", IDENTITY.PROXY);
    }

    /**
     * 儲存當前身分
     *
     * @param context
     * @param media
     */
    public static void saveMediaPreferences(Context context, int media) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("media", media).apply();
    }


    /**
     * 設定當前身份
     *
     * @param context
     * @return
     */
    public static int loadCategoryPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("category", IDENTITY.PROXY);
    }

    /**
     * 儲存當前身分
     *
     * @param context
     * @param category
     */
    public static void saveCategoryPreferences(Context context, int category) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("category", category).apply();
    }



    /**
     * 取得當前導覽模式
     *
     * @param context
     * @return
     */
    public static int loadModePreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("mode", IDENTITY.PROXY);
    }



    /**
     * 儲存當前導覽模式
     *
     * @param context
     * @param mode
     */
    public static void saveModePreference(Context context, int mode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("mode", mode).apply();
    }

    /**
     * 取得已經下載完檔案大小
     *
     * @param context
     * @param fileName
     * @return
     */
    public static long loadFilePreferences(Context context, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(fileName, -1);
    }

    /**
     * 儲存完已下載檔案下載資訊
     *
     * @param context
     * @param fileName
     * @param fileLength
     */
    public static void saveFilePreferences(Context context, String fileName, long fileLength) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(fileName, fileLength).apply();
    }

    /**
     * 取得當前檔案是否已經下載完畢
     *
     * @param fileName 欲查詢檔案名稱
     * @return boolean
     */
    public static void removeFilePreferences(Context context, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(fileName).apply();
    }

    public static void clearPreferences(Context context) {
        SharedPreferences pref = context.getSharedPreferences("DEH_N", Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
}
