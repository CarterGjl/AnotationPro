package aop.demo.jetpack.android.i;

public interface IPreferencesHolder {

    /**
     * 序列化
     * @param key
     * @param src
     * @return
     */
    String serialize(String key,Object src);

    /**
     * 反序列化
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    <T> T deserialize(String key,Class<T> tClass);


    void remove(String key);
}
