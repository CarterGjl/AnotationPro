package aop.demo.jetpack.android.i;

public class PreferencesManager {

    private IPreferencesHolder mIPreferencesHolder;

    private PreferencesManager() {
    }
    public static PreferencesManager getInstance() {
        return PreferencesManagerHolder.INSTANCE;
    }

    private static class PreferencesManagerHolder {
        private static PreferencesManager INSTANCE = new PreferencesManager();
    }

    public void setPreferencesHolder(IPreferencesHolder preferencesHolder) {
        this.mIPreferencesHolder = preferencesHolder;
    }

    public IPreferencesHolder getPreferencesHolder() {
        return mIPreferencesHolder;
    }
}
