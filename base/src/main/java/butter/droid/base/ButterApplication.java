/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.updater.ButterUpdateManager;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.utils.FileUtils;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.StorageUtils;
import butter.droid.base.utils.VersionUtils;
import com.sjl.foreground.Foreground;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;
import java.io.File;
import javax.inject.Inject;
import timber.log.Timber;

public abstract class ButterApplication extends Application {

    private static String sDefSystemLanguage;
    private static ButterApplication sThis;

    @Inject Picasso picasso;
    @Inject BeamManager beamManager;
    @Inject PreferencesHandler preferencesHandler;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sThis = this;

        inject();

        sDefSystemLanguage = LocaleUtils.getCurrentAsString();

        LeakCanary.install(this);
        Foreground.init(this);

        Constants.DEBUG_ENABLED = false;
        try {
            String packageName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            int flags = packageInfo.applicationInfo.flags;
            Constants.DEBUG_ENABLED = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //initialise logging
        if (Constants.DEBUG_ENABLED) {
            Timber.plant(new Timber.DebugTree());
        }

        if (VersionUtils.isUsingCorrectBuild()) {
            TorrentService.start(this);
        }

        File path = new File(preferencesHandler.getStorageLocation());
        File directory = new File(path, "/torrents/");
        if (preferencesHandler.removeCache()) {
            FileUtils.recursiveDelete(directory);
            FileUtils.recursiveDelete(new File(path + "/subs"));
        } else {
            File statusFile = new File(directory, "status.json");
            statusFile.delete();
        }

        Timber.d("StorageLocations: " + StorageUtils.getAllStorageLocations());
        Timber.i("Chosen cache location: " + directory);

        Picasso.setSingletonInstance(picasso);

        ButterUpdateManager.listenUpdates(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sDefSystemLanguage = LocaleUtils.getCurrentAsString();
    }

    @Override
    public void onTerminate() {
        // Just, so that it exists. Cause it is not executed in production, the whole application is closed anyways on OS level.
        beamManager.onDestroy();
        super.onTerminate();
    }

    public static String getSystemLanguage() {
        return sDefSystemLanguage;
    }

//    @Override
//    public void onNewUpdateAvailable(String updateFile) {
//        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (updateFile.length() > 0) {
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.drawable.ic_notif_logo)
//                    .setContentTitle(getString(R.string.update_available))
//                    .setContentText(getString(R.string.press_install))
//                    .setAutoCancel(true)
//                    .setDefaults(NotificationCompat.DEFAULT_ALL);
//
//            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
//            notificationIntent.setDataAndType(Uri.parse("file://" + updateFile), ButterUpdateManager.ANDROID_PACKAGE_MIME_TYPE);
//
//            notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0));
//
//            nm.notify(ButterUpdateManager.NOTIFICATION_ID, notificationBuilder.build());
//        }
//    }

    public abstract BaseInternalComponent getComponent();

    protected abstract void inject();

    public static ButterApplication getAppContext() {
        return sThis;
    }
}
