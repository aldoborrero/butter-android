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

package butter.droid.base.manager.internal.updater;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import butter.droid.base.BuildConfig;
import butter.droid.base.Internal;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.updater.model.ApplicationMetadata;
import butter.droid.base.manager.internal.updater.model.UpdaterResponse;
import butter.droid.base.manager.internal.updater.model.UpdaterResponse.Update;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.utils.IntegrityUtils;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Internal
public class ButterUpdateManager {

    public static final String ANDROID_PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

    public static final String LAST_UPDATE_CHECK = "update_check";
    public static final String UPDATE_FILE = "update_file";

    private static final String LAST_UPDATE_KEY = "last_update";
    private static final String SHA1_TIME = "sha1_update_time";
    private static final String SHA1_KEY = "sha1_update";

    public static int NOTIFICATION_ID = 0x808C049;

    private final String DATA_URLS[] = BuildConfig.UPDATE_URLS;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final PreferencesHandler preferencesHandler;
    private final PrefManager prefManager;
    private final ApplicationMetadata applicationMetadata;

    private final AtomicBoolean checkingUpdates = new AtomicBoolean(false);

    @Inject
    public ButterUpdateManager(Context context, OkHttpClient okHttpClient, Gson gson, PreferencesHandler preferencesHandler, PrefManager prefManager) {
        this.httpClient = okHttpClient;
        this.gson = gson;
        this.preferencesHandler = preferencesHandler;
        this.prefManager = prefManager;
        this.applicationMetadata = ApplicationMetadata.obtain(context);

        ButterUpdateManager.NOTIFICATION_ID += IntegrityUtils.Checksums.crc32(applicationMetadata.getPackageName());
    }

    public void checkUpdates() {
        if (checkingUpdates.getAndSet(true)) {
            // Ignoring until this is finished
            return;
        }

        final Request request = new Request.Builder().url(DATA_URLS[0]).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                // Notify update failed
                checkingUpdates.set(false);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final boolean successful = response.isSuccessful();
                if (successful) {
                    final Reader responseReader = response.body().charStream();
                    final UpdaterResponse data = gson.fromJson(responseReader, UpdaterResponse.class);

                    final Map<String, Update> channels = data.updates.get(applicationMetadata.getChannel());
                    final Update update = channels.get(applicationMetadata.getVariant());

                    downloadFile(Uri.parse(update.updateUrl));
                } else {
                    checkingUpdates.set(false);
                }
            }
        });

//            try {
//                if (response.isSuccessful()) {
//                    final UpdaterResponse data = gson.fromJson(response.body().charStream(), UpdaterResponse.class);
//
//                    final Map<String, Map<String, UpdaterResponse.Arch>> variant;
//
//                    if (variantStr.equals("tv")) {
//                        variant = data.tv;
//                    } else {
//                        variant = data.mobile;
//                    }
//
//                    UpdaterResponse.Arch channel = null;
//                    if (variant.containsKey(channelStr) && variant.get(channelStr).containsKey(abi)) {
//                        channel = variant.get(channelStr).get(abi);
//                    }
//
//                    final ApplicationInfo appinfo = context.getApplicationInfo();
//                    if ((channel == null || channel.checksum.equals(IntegrityUtils.SHA1.calculate(appinfo.sourceDir)) || channel.versionCode <= versionCode) && VersionUtils.isUsingCorrectBuild()) {
////                        setChanged();
////                        notifyObservers(STATUS_NO_UPDATE);
//                    } else {
//                        downloadFile(channel.updateUrl);
////                        setChanged();
////                        notifyObservers(STATUS_GOT_UPDATE);
//                    }
//                } else {
////                    setChanged();
////                    notifyObservers(STATUS_NO_UPDATE);
//                }
//            } catch (Exception e) {
//                Timber.e("Error while trying to obtain a response from server: ", e);
//            }
//        }

//        final File appApkFile = applicationMetadata.getAppApkFile();
//        if (appApkFile.lastModified() > this.prefManager.get(SHA1_TIME, 0L)) {
//            prefManager.save(SHA1_KEY, IntegrityUtils.SHA1.calculate(appApkFile.getAbsolutePath()));
//            prefManager.save(SHA1_TIME, System.currentTimeMillis());
//
//            final String updateFilePath = this.prefManager.get(UPDATE_FILE, "");
//            if (updateFilePath.length() > 0) {
//                final File updateApkFile = new File(updateFilePath);
//                if (updateApkFile.delete()) {
//                    this.prefManager.remove(UPDATE_FILE);
//                }
//            }
//        }
//
//        final long now = System.currentTimeMillis();
//        final long lastUpdate = prefManager.get(LAST_UPDATE_CHECK, 0L);
//
//        prefManager.save(LAST_UPDATE_CHECK, now);
//
//        if ((lastUpdate + UPDATE_INTERVAL) < now) {
//            prefManager.save(LAST_UPDATE_KEY, now);
//
//            if (!forced && BuildConfig.GIT_BRANCH.contains("local")) {
//                return;
//            }
//
//            final Request request = new Request.Builder().url(DATA_URLS[currentUrl]).build();
//            httpClient.newCall(request).enqueue(callback);
//        } else if (prefManager.contains(UPDATE_FILE)) {
//            final String fileName = prefManager.get(UPDATE_FILE, "");
//            if (fileName.length() > 0) {
//                if (!new File(fileName).exists()) {
//                    prefManager.remove(UPDATE_FILE);
//                } else {
////                    if (listener != null) {
////                        listener.onNewUpdateAvailable(fileName);
////                    }
//                }
//            }
//        }
    }

    private void downloadFile(final Uri url) {
        final File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        final DownloadManager.Request request = new DownloadManager.Request(url)
                .setTitle("Title")
                .setDestinationInExternalPublicDir(downloadsDir.getAbsolutePath(), "farfurfel.apk")
                .setDescription("meilishuo desc")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)
//                .allowScanningByMediaScanner()
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setMimeType(ANDROID_PACKAGE_MIME_TYPE);


//        downloadId = downloadManager.enqueue(request);

//        final Request request = new Request.Builder().url(url).build();
//
//        httpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // Update failed, notify back
//                checkingUpdates.set(false);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    final String fileName = url.substring(url.lastIndexOf('/') + 1);
//                    final File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                    final File downloadedFile = new File(downloadDirectory, fileName);
//
//                    final BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
//                    sink.writeAll(response.body().source());
//                    sink.close();
//
//                    final String updateFilePath = downloadDirectory.getAbsolutePath() + "/" + fileName;
//
//                    prefManager.getPrefs().edit()
//                            .putString(UPDATE_FILE, updateFilePath)
//                            .putString(SHA1_KEY, IntegrityUtils.SHA1.calculate(updateFilePath))
//                            .putLong(SHA1_TIME, System.currentTimeMillis())
//                            .apply();
//
////                    if (listener != null) {
////                        listener.onNewUpdateAvailable(updateFilePath);
////                    }
//                } else {
//                    // Update failed, notify back
//                    checkingUpdates.set(false);
//                }
//            }
//        });
    }

}
