package aop.demo.jetpack.android.anotationpro;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.mmkv.MMKV;

import aop.demo.jetpack.android.i.IPreferencesHolder;

public class PregerencesMKVHolder implements IPreferencesHolder {
    @Override
    public String serialize(String key, Object src) {

        String json = new Gson().toJson(src);
        MMKV kv = MMKV.defaultMMKV();
        kv.putString(key, json);
        return json;
    }

    @Override
    public <T> T deserialize(String key, Class<T> tClass) {
        MMKV kv = MMKV.defaultMMKV();
        String json = kv.decodeString(key, "");
        if (!TextUtils.isEmpty(json)) {
            return new Gson().fromJson(json, tClass);
        }
        return null;
    }

    @Override
    public void remove(String key) {
        MMKV kv = MMKV.defaultMMKV();
        kv.remove(key);
    }
}
