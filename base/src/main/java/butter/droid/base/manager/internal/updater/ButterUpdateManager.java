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
import android.text.TextUtils;
import butter.droid.base.BuildConfig;
import butter.droid.base.Internal;
import butter.droid.base.R;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.manager.internal.updater.RxAndroidDownloaderManager.DownloadRequest;
import butter.droid.base.manager.internal.updater.model.ApplicationMetadata;
import butter.droid.base.manager.internal.updater.model.UpdaterResponse;
import butter.droid.base.manager.internal.updater.model.UpdaterResponse.Update;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.utils.IntegrityUtils;
import com.google.gson.Gson;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
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

    private static final String DEFAULT_BUTTER_APK_NAME = "butter.apk";

    private static final String LAST_UPDATE_KEY = "last_update";
    private static final String SHA1_TIME = "sha1_update_time";
    private static final String SHA1_KEY = "sha1_update";

    public static int NOTIFICATION_ID = 0x808C049;

    private final String DATA_URLS[] = BuildConfig.UPDATE_URLS;

    private final Context context;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final PrefManager prefManager;
    private final ApplicationMetadata applicationMetadata;
    private final RxAndroidDownloaderManager rxDownloadManager;

    private final AtomicBoolean checkingUpdates = new AtomicBoolean(false);

    @Inject
    public ButterUpdateManager(final Context context, final OkHttpClient okHttpClient, final Gson gson, final PrefManager prefManager,
            final RxAndroidDownloaderManager rxDownloadManager) {
        this.context = context;
        this.httpClient = okHttpClient;
        this.gson = gson;
        this.prefManager = prefManager;
        this.applicationMetadata = ApplicationMetadata.obtain(context);
        this.rxDownloadManager = rxDownloadManager;

        ButterUpdateManager.NOTIFICATION_ID += IntegrityUtils.Checksums.crc32(applicationMetadata.getPackageName());
    }

    public void checkUpdatesManually() {
        checkUpdates(true);
    }

    public void checkUpdates() {
        checkUpdates(false);
    }

    private void checkUpdates(boolean manuallyCheck) {
        if (checkingUpdates.getAndSet(true)) {
            // Ignoring until this is finished
            // TODO: 13/05/2017 Display messages if user presses multiple times the button
            return;
        }

        final Request request = new Request.Builder().url(DATA_URLS[0]).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                updateLastUpdateCheckPreference();
                checkingUpdates.set(false);
                NewUpdatesBroadcastReceiver.setActionErrorChecking(context);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final boolean successful = response.isSuccessful();
                if (successful) {
                    final Reader responseReader = response.body().charStream();
                    final UpdaterResponse data = gson.fromJson(responseReader, UpdaterResponse.class);

                    final Map<String, Update> channels = data.updates.get(applicationMetadata.getChannel());
                    final Update update = channels.get(applicationMetadata.getVariant());

                    // TODO: 13/05/2017 Check if SHA is the same as the current version installed

                    final Uri url = Uri.parse(update.updateUrl);
                    final DownloadManager.Request request = toDownloadManagerRequest(url);

                    rxDownloadManager.download(request).subscribe(new Consumer<DownloadRequest>() {
                        @Override
                        public void accept(@NonNull final DownloadRequest downloadRequest) throws Exception {
                            final int downloadStatus = downloadRequest.getDownloadStatus();
                            switch (downloadStatus) {
//                                case DownloadRequest.STATUS_DOWNLOAD_SUCCESS:
//                                    final Uri fileUri = downloadRequest.getFileUri();
//                                    NewUpdatesBroadcastReceiver.newUpdateAvailable(context);
//                                    break;
//                                case DownloadRequest.STATUS_DOWNLOAD_FAILED:
//                                    NewUpdatesBroadcastReceiver.errorDownloadingUpdate(context);
//                                    break;
                            }
                            checkingUpdates.set(false);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull final Throwable throwable) throws Exception {
                            checkingUpdates.set(false);
                            NewUpdatesBroadcastReceiver.errorDownloadingUpdate(context);
                        }
                    });
                } else {
                    updateLastUpdateCheckPreference();
                    checkingUpdates.set(false);
                    NewUpdatesBroadcastReceiver.errorDownloadingUpdate(context);
                }
            }
        });
    }

    private DownloadManager.Request toDownloadManagerRequest(final Uri url, final boolean manuallyCheck) {
        final File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        final String fileName = TextUtils.isEmpty(url.getLastPathSegment()) ? DEFAULT_BUTTER_APK_NAME : url.getLastPathSegment();

        return new DownloadManager.Request(url)
                .setTitle(context.getString(R.string.butter_updater))
                .setDestinationInExternalPublicDir(downloadsDir.getAbsolutePath(), fileName)
                .setDescription(context.getString(R.string.downloading_new_butter_update))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(false)
                .setAllowedNetworkTypes(!manuallyCheck && prefManager.get(Prefs.WIFI_ONLY, true) ? DownloadManager.Request.NETWORK_WIFI : DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setMimeType(ANDROID_PACKAGE_MIME_TYPE);
    }

    private void updateLastUpdateCheckPreference() {
        prefManager.save(ButterUpdateManager.LAST_UPDATE_CHECK, new Date().getTime());
    }

}
