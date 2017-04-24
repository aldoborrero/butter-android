package butter.droid.base.manager.internal.updater.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import butter.droid.base.BuildConfig;
import java.io.File;
import java.util.Locale;
import timber.log.Timber;

public class ApplicationMetadata {

    private final String packageName;
    @Nullable private final Integer versionCode;
    private final File appApkFile;
    private final String variant;
    private final String channel;
    private final String abi;

    public static ApplicationMetadata obtain(final Context context) {
        final String packageName = context.getPackageName();
        final Integer versionCode = getVersionCode(context, packageName);
        final File appApkFile = new File(context.getApplicationInfo().sourceDir);
        final String abi = getCpuAbi();
        final String variant = packageName.contains("tv") ? "tv" : "mobile";
        final String channel = getAppChannel();
        return new ApplicationMetadata(packageName, versionCode, appApkFile, variant, channel, abi);
    }

    @Nullable private static Integer getVersionCode(final Context context, final String packageName) {
        try {
            final PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e("Error obtaining version code: ", e);
            return null;
        }
    }

    private static String getCpuAbi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return Build.CPU_ABI.toLowerCase(Locale.US);
        } else {
            return Build.SUPPORTED_ABIS[0].toLowerCase(Locale.US);
        }
    }

    private static String getAppChannel() {
        if (BuildConfig.RELEASE_TYPE.toLowerCase(Locale.US).contains("release")) {
            return "release";
        } else {
            return BuildConfig.GIT_BRANCH;
        }
    }

    private ApplicationMetadata(final String packageName, @Nullable final Integer versionCode, final File appApkFile, final String variant,
            final String channel, final String abi) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.appApkFile = appApkFile;
        this.variant = variant;
        this.channel = channel;
        this.abi = abi;
    }

    public String getPackageName() {
        return packageName;
    }

    @Nullable public Integer getVersionCode() {
        return versionCode;
    }

    public File getAppApkFile() {
        return appApkFile;
    }

    public String getVariant() {
        return variant;
    }

    public String getChannel() {
        return channel;
    }

    public String getAbi() {
        return abi;
    }
}
