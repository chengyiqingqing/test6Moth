package com.meitu.testplugin.plugin.core;

import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import com.meitu.testplugin.plugin.core.bridge.IExecutorFactory;
import com.meitu.testplugin.plugin.core.bridge.IPluginCommandExecutor;
import com.meitu.testplugin.plugin.core.bridge.IPluginDestroyer;
import com.meitu.testplugin.plugin.util.FileUtils;
import com.meitu.testplugin.plugin.util.Reflector;
import com.meitu.testplugin.plugin.util.RunUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * @Author shaowenwen
 * @Date 2020-06-21 17:55
 */
public class Plugin {

    public static final String TAG = "Plugin";
    private Meta mPlugMata;
    private boolean isApk;
    public String mFileName;
    private String mFileAbsolutePath;

    private Context mHostContext;
    private Context mPluginContext;
    private PluginManager mPluginManager;
    // TODO:【来自系统】用来干什么? 2020-06-22
    private PackageParser.Package mPackage;
    private PackageInfo mPackageInfo;
    private PluginPackageManager mPackageManager;
    private String mNativeLibDir;

    private Application mApplication;
    private ClassLoader mClassLoader;
    private Resources mResources;

    private Map<ComponentName, ActivityInfo> mActivityInfos;
    private Map<ComponentName, ActivityInfo> mReceiverInfos;
    private Map<ComponentName, ServiceInfo> mServiceInfos;

    private IPluginCommandExecutor mCommandExecutor;
    private IPluginDestroyer mPluginDestroyer;

    public Plugin(PluginManager pluginManager, Context context, File pluginFile) {
        this(pluginManager, context, pluginFile.getName());
        this.mFileAbsolutePath = pluginFile.getAbsolutePath();
    }

    public Plugin(PluginManager pluginManager, Context context, String fileName) {
        this.mPluginManager = pluginManager;
        this.mHostContext = context;
        this.mFileName = fileName;
        this.isApk = PluginHelper.isApkFile(mFileName);
    }


    public boolean isApk() {
        return isApk;
    }

    public ActivityInfo getActivityInfo(ComponentName component) {
        return mActivityInfos.get(component);
    }

    public ResolveInfo resolveActivity(Intent intent, int flag) {
        List<ResolveInfo> query = queryIntentActivities(intent, flag);
        if (query == null || query.size() == 0) {
            return null;
        }
        return query.get(0);
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();
        ContentResolver contentResolver = mPluginContext.getContentResolver();

        for (PackageParser.Activity activity : mPackage.activities) {
            if (match(activity, component)) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.activityInfo = activity.info;
                resolveInfos.add(resolveInfo);
            } else if (component == null) {
                for (PackageParser.ActivityIntentInfo intentInfo : activity.intents) {
                    if (intentInfo.match(contentResolver, intent, true, TAG) >= 0) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.activityInfo = activity.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }

    private boolean match(PackageParser.Component component, ComponentName target) {
        ComponentName source = component.getComponentName();
        if (source == target) {
            return true;
        }
        if (source!=null && target!=null &&
                source.getClassName().equals(target.getClassName()) &&
                (source.getPackageName().equals(target.getPackageName()) || mHostContext.getPackageName().equals(target.getPackageName()))) {
            return true;
        }
        return false;
    }

    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        ContentResolver resolver = this.mPluginContext.getContentResolver();

        for (PackageParser.Activity receiver : this.mPackage.receivers) {
            if (receiver.getComponentName().equals(component)) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.activityInfo = receiver.info;
                resolveInfos.add(resolveInfo);
            } else if (component == null) {
                for (PackageParser.ActivityIntentInfo intentInfo : receiver.intents) {
                    if (intentInfo.match(resolver, intent, true, TAG) >= 0) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.activityInfo = receiver.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }

    public boolean parseMeta(String meta) {
        /* meta为json字符串 */
        try {
            JSONObject obj = new JSONObject(meta);
            mPlugMata = new Meta();
            mPlugMata.aliasName = obj.getString("aliasName");
            mPlugMata.packageName = obj.getString("packageName");
            mPlugMata.plugVersion = obj.getInt("plugVersion");
            mPlugMata.sdkVersion = obj.getInt("sdkVersion");
            mPlugMata.executorFactory = obj.getString("executorFactory");
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public Meta getPlugMeta() {
        return mPlugMata;
    }

    public void setFileAbsolutePath(String absolutePath) {
        this.mFileAbsolutePath = absolutePath;
    }

    public String getFileAbsolutePath() {
        return mFileAbsolutePath;
    }

    public boolean setup() {
        try {
            if (isApk) {
                setupApk();
            } else {
                setupJar();
            }
            return true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }

    private void setupJar() {
        this.mNativeLibDir = PluginHelper.getNativeLibDir(mHostContext);
        this.mClassLoader = createClassLoader(mHostContext, mFileAbsolutePath, mNativeLibDir, mHostContext.getClassLoader());
        this.mCommandExecutor = createCommandExecutor(mClassLoader, mPlugMata);
        this.mPluginDestroyer = createPluginDestroyer(mClassLoader, mPlugMata);
    }

    private void setupApk() throws Exception {
        File apk = new File(mFileAbsolutePath);
        this.mPackage = PackageParserCompat.parsePackage(mHostContext, apk, PackageParser.PARSE_MUST_BE_APK);
        // TODO:为什么？ 2020-06-22
        this.mPackage.applicationInfo.metaData = this.mPackage.mAppMetaData;
        this.mPackageInfo = new PackageInfo();
        this.mPackageInfo.applicationInfo = this.mPackage.applicationInfo;
        this.mPackageInfo.applicationInfo.sourceDir = mFileAbsolutePath;

        // TODO:签名信息处理 2020-06-22
        if (Build.VERSION.SDK_INT >= 28 || (Build.VERSION.SDK_INT == 27 && Build.VERSION.PREVIEW_SDK_INT != 0)) {
            try {
                this.mPackageInfo.signatures = this.mPackage.mSigningDetails.signatures;
            } catch (Throwable t) {
                PackageInfo info = mHostContext.getPackageManager().getPackageInfo(mHostContext.getPackageName(), PackageManager.GET_SIGNATURES);
                this.mPackageInfo.signatures = info.signatures;
            }
        } else {
            this.mPackageInfo.signatures = this.mPackage.mSignatures;
        }

        this.mPackageInfo.packageName = this.mPackage.packageName;
        this.mPackageInfo.versionCode = this.mPackage.mVersionCode;
        this.mPackageInfo.versionName = this.mPackage.mVersionName;
        this.mPackageInfo.permissions = new PermissionInfo[0];
        // TODO: PluginPackageManager未完待续 2020-06-22
        this.mPackageManager = new PluginPackageManager(mPluginManager, mHostContext.getPackageManager());
        this.mPluginContext = createPluginContext(null);
        this.mNativeLibDir = PluginHelper.getNativeLibDir(mHostContext);
        this.mPackage.applicationInfo.nativeLibraryDir = this.mNativeLibDir;
        this.mResources = createResources(mHostContext, mFileAbsolutePath);
        this.mClassLoader = createClassLoader(mHostContext, mFileAbsolutePath, mNativeLibDir, mClassLoader);

        /* 根据本机cpu类型拷贝so库 */
        FileUtils.copyNativeLib(apk, mHostContext, mPackageInfo, new File(mNativeLibDir));

        cacheActivityInfo();
        cacheReceiverInfo();
        cacheServiceInfo();

        this.mCommandExecutor = createCommandExecutor(mClassLoader, mPlugMata);
        this.mPluginDestroyer = createPluginDestroyer(mClassLoader, mPlugMata);

        invokeApplicationOncreate();
    }

    private void invokeApplicationOncreate() throws Exception {
        final Exception[] es = new Exception[1];
        RunUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mApplication != null) {
                    return;
                }
                try {
                    mApplication = makeApplication(mPluginManager.getInstrumentation());
                } catch (Exception e) {
                    es[0] = e;
                }
            }
        }, true);
        if (es[0] != null) {
            throw es[0];
        }
    }

    private Application makeApplication(Instrumentation instrumentation) throws Exception {
        if (mApplication != null) {
            return mApplication;
        }

        String appClass = mPackage.applicationInfo.className;
        if (TextUtils.isEmpty(appClass)) {
            appClass = "android.app.Application";
        }

        mApplication = instrumentation.newApplication(mClassLoader, appClass, mPluginContext);
        // 给插件application注册宿主application中已经注册的声明周期监听
//        mApplication.registerActivityLifecycleCallbacks(new ActivityLIfecycleCallbacksProxy());
        instrumentation.callApplicationOnCreate(mApplication);
        return mApplication;
    }

    private IPluginDestroyer createPluginDestroyer(ClassLoader classLoader, Meta meta) {
        IPluginDestroyer destroyer = null;
        try {
            if (!TextUtils.isEmpty(meta.executorFactory)) {
                Class clz = classLoader.loadClass(meta.executorFactory);
                destroyer = ((IExecutorFactory) clz.newInstance()).createDestroyer();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return destroyer;
    }

    private IPluginCommandExecutor createCommandExecutor(ClassLoader classLoader, Meta meta) {
        IPluginCommandExecutor executor = null;
        try {
            if (!TextUtils.isEmpty(meta.executorFactory)) {
                Class clz = classLoader.loadClass(meta.executorFactory);
                executor = ((IExecutorFactory) clz.newInstance()).createExecutor();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return executor;
    }

    /**
     * 缓存插件中的activity信息
     */
    private void cacheActivityInfo() throws Exception {
        Map<ComponentName, ActivityInfo> activityInfos = new HashMap<>();
        for (PackageParser.Activity activity : this.mPackage.activities) {
            activity.info.metaData = activity.metaData;
            activityInfos.put(activity.getComponentName(), activity.info);
        }
        this.mActivityInfos = Collections.unmodifiableMap(activityInfos);
        this.mPackageInfo.activities = activityInfos.values().toArray(new ActivityInfo[activityInfos.size()]);
    }

    /**
     * 缓存插件中的receiver信息
     */
    private void cacheReceiverInfo() throws Exception {
        Map<ComponentName, ActivityInfo> receivers = new HashMap<>();
        for (PackageParser.Activity receiver : this.mPackage.receivers) {
            receivers.put(receiver.getComponentName(), receiver.info);

            BroadcastReceiver br = BroadcastReceiver.class.cast(getClassLoader().loadClass(receiver.getComponentName().getClassName()).newInstance());
            for (PackageParser.ActivityIntentInfo filter : receiver.intents) {
                this.mHostContext.registerReceiver(br, filter);
            }
        }
        this.mReceiverInfos = Collections.unmodifiableMap(receivers);
        this.mPackageInfo.receivers = receivers.values().toArray(new ActivityInfo[receivers.size()]);
    }

    /**
     * 缓存插件中的service信息
     */
    private void cacheServiceInfo() {
        Map<ComponentName, ServiceInfo> serviceInfos = new HashMap<>();
        for (PackageParser.Service service : this.mPackage.services) {
            serviceInfos.put(service.getComponentName(), service.info);
        }
        this.mServiceInfos = serviceInfos;
        this.mPackageInfo.services = serviceInfos.values().toArray(new ServiceInfo[serviceInfos.size()]);
    }


    private ClassLoader createClassLoader(Context context, String apkPath, String libsDir, ClassLoader parent) {
        String dexOptDir = PluginHelper.getDexOptDir(context);
        DexClassLoader loader = new DexClassLoader(apkPath, dexOptDir, libsDir, parent);
        return loader;
    }

    private Resources createResources(Context context, String apkPath) throws Exception {
        Resources hostRes = context.getResources();
        AssetManager am = createAssetManager(apkPath);
        return new Resources(am, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
    }

    private AssetManager createAssetManager(String apkPath) throws Exception {
        AssetManager am = AssetManager.class.newInstance();
        Reflector.with(am).method("addAssetPath", String.class).call(apkPath);
        return am;
    }

    private Context createPluginContext(Context context) {
        if (context == null) {
            return new PluginContext(this);
        }
        return new PluginContext(this, context);
    }

    public PluginManager getPluginManager() {
        return mPluginManager;
    }

    public Application getApplication() {
        return mApplication;
    }

    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    public PluginPackageManager getPackageManager() {
        return mPackageManager;
    }

    public Resources getResources() {
        return mResources;
    }

    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    public Resources.Theme getTheme() {
        Resources.Theme theme = mResources.newTheme();
        theme.applyStyle(ThemeCompat.selectDefaultTheme(mPackage.applicationInfo.theme, Build.VERSION.SDK_INT), false);
        return theme;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public Intent getLaunchIntent() {
        ContentResolver resolver = this.mPluginContext.getContentResolver();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

        for (PackageParser.Activity activity : this.mPackage.activities) {
            for (PackageParser.ActivityIntentInfo intentInfo : activity.intents) {
                if (intentInfo.match(resolver, launcher, false, TAG) > 0) {
                    return Intent.makeMainActivity(activity.getComponentName());
                }
            }
        }
        return null;
    }

    public Intent getLeanbackLaunchIntent() {
        ContentResolver resolver = this.mPluginContext.getContentResolver();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        for (PackageParser.Activity activity : this.mPackage.activities) {
            for (PackageParser.ActivityIntentInfo intentInfo : activity.intents) {
                if (intentInfo.match(resolver, launcher, false, TAG) > 0) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(activity.getComponentName());
                    intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                    return intent;
                }
            }
        }

        return null;
    }

    public ApplicationInfo getApplicationInfo() {
        return this.mPackage.applicationInfo;
    }

    public ActivityInfo getReceiverInfo(ComponentName componentName) {
        return this.mReceiverInfos.get(componentName);
    }

    public ServiceInfo getServiceInfo(ComponentName componentName) {
        return this.mServiceInfos.get(componentName);
    }

    public class Meta {
        public String aliasName;
        public String packageName;
        public int plugVersion;
        public int sdkVersion;
        public String executorFactory;
    }

}
