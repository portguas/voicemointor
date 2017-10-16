package com.example.administrator.voicemonitor;

/**
 * Created by Administrator on 10/14/2017.
 */

import android.content.SharedPreferences;

        import java.lang.reflect.Type;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Map;
        import java.util.Set;

//import com.google.gson.Gson;

        import android.annotation.TargetApi;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.content.SharedPreferences.Editor;
        import android.os.Build;
        import android.util.SparseArray;

/**
 * Preference 工具类
 * <p/>
 * Created by het on 16/3/29.
 */
public final class PreferencesUtil {

    private static SharedPreferences mPreferences;

    private PreferencesUtil() {
    }

    private static Context getContext() {
        return PApplication.getAppContext();
    }

    public static SharedPreferences getSharedPreferences() {
        if (mPreferences == null) {
            mPreferences =
                    getContext().getSharedPreferences(getContext().getPackageName() + "_pref", Context.MODE_PRIVATE);
        }
        return mPreferences;
    }

    public static Editor getEditor() {
        return getSharedPreferences().edit();
    }

    public static void putBundleString(Map<String, String> map) {
        Editor editor = getEditor();
        for (String key : map.keySet()) {
            editor.putString(key, map.get(key));
        }
        apply(editor);
    }

    public static void putString(String key, String value) {
        apply(getEditor().putString(key, value));
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }

    public static void putInt(String key, int value) {
        apply(getEditor().putInt(key, value));
    }

    public static int getInt(String key) {
        return getInt(key, -1);
    }

    public static int getInt(String key, int defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

    public static void putLong(String key, long value) {
        apply(getEditor().putLong(key, value));
    }

    public static long getLong(String key) {
        return getLong(key, -1);
    }

    public static long getLong(String key, long defaultValue) {
        return getSharedPreferences().getLong(key, defaultValue);
    }

    public static void putFloat(String key, float value) {
        apply(getEditor().putFloat(key, value));
    }

    public static float getFloat(String key) {
        return getFloat(key, -1);
    }

    public static float getFloat(String key, float defaultValue) {
        return getSharedPreferences().getFloat(key, defaultValue);
    }

    public static void putBoolean(String key, boolean value) {
        apply(getEditor().putBoolean(key, value));
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    public static Set<String> getStringSet(String key, Set<String> set) {
        return getSharedPreferences().getStringSet(key, set);
    }

    public static boolean contains(String key) {
        return getSharedPreferences().contains(key);
    }

    public static void remove(String key) {
        apply(getEditor().remove(key));
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static void apply(Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

//    public static <T> List<T> getPreference(String key, Type type) {
//        String str = PreferencesUtil.getString(key, "");
//        List<T> strList = new Gson().fromJson(str, type);
//        if (strList == null) {
//            strList = new ArrayList<>();
//        }
//        return strList;
//    }
//
//    public static <T> SparseArray<T> getSAPreference(String key, Type type) {
//        String str = PreferencesUtil.getString(key, "");
//        SparseArray<T> sparseArray = new Gson().fromJson(str, type);
//        if (sparseArray == null) {
//            sparseArray = new SparseArray<>();
//        }
//        return sparseArray;
//    }
//
//    public static <T> void saveToPreference(String key, List<T> list) {
//        String str = new Gson().toJson(list);
//        PreferencesUtil.putString(key, str);
//    }
}