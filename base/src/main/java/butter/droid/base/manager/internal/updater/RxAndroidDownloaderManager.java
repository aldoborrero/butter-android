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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RxAndroidDownloaderManager {

    private final Context context;
    private final DownloadManager downloadManager;

    private final LongSparseArray<PublishSubject<DownloadRequest>> subjectsMap = new LongSparseArray<>();

    @Inject
    public RxAndroidDownloaderManager(Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        final DownloadStatusReceiver downloadStatusReceiver = new DownloadStatusReceiver();
        final IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        context.registerReceiver(downloadStatusReceiver, intentFilter);
    }

    public Observable<DownloadRequest> download(DownloadManager.Request request) {
        final long downloadId = downloadManager.enqueue(request);

        final PublishSubject<DownloadRequest> publishSubject = PublishSubject.create();
        subjectsMap.put(downloadId, publishSubject);

        return publishSubject;
    }

    public static class DownloadRequest {

        public static int STATUS_DOWNLOAD_SUCCESS = 0;
        public static int STATUS_DOWNLOAD_FAILED = 1;

        private final long downloadId;
        private final int downloadStatus;
        @Nullable
        private final Uri fileUri;

        public DownloadRequest(final long downloadId, final int downloadStatus, @Nullable final Uri fileUri) {
            this.downloadId = downloadId;
            this.downloadStatus = downloadStatus;
            this.fileUri = fileUri;
        }

        public long getDownloadId() {
            return downloadId;
        }

        public int getDownloadStatus() {
            return downloadStatus;
        }

        @Nullable public Uri getFileUri() {
            return fileUri;
        }
    }

    public static class DownloadManagerException extends RuntimeException {

        public static int ERROR_NULL_CURSOR = 0;

        private final int error;

        public DownloadManagerException(int error) {
            this.error = error;
        }

        public int getError() {
            return error;
        }
    }

    private class DownloadStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);

            final PublishSubject<DownloadRequest> publishSubject = subjectsMap.get(id);
            if (publishSubject == null) {
                return;
            }

            final DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);

            final Cursor cursor = downloadManager.query(query);
            if (!cursor.moveToFirst()) {
                cursor.close();
                downloadManager.remove(id);
                publishSubject.onError(new DownloadManagerException(DownloadManagerException.ERROR_NULL_CURSOR));
                subjectsMap.remove(id);
                return;
            }

            final int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_FAILED == cursor.getInt(statusIndex)) {
                cursor.close();

                downloadManager.remove(id);

                publishSubject.onNext(new DownloadRequest(id, DownloadRequest.STATUS_DOWNLOAD_FAILED, null));
                publishSubject.onComplete();

                subjectsMap.remove(id);

                return;
            }

            final int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            final Uri downloadUri = Uri.parse(cursor.getString(uriIndex));
            cursor.close();

            publishSubject.onNext(new DownloadRequest(id, DownloadRequest.STATUS_DOWNLOAD_SUCCESS, downloadUri));
            publishSubject.onComplete();

            subjectsMap.remove(id);
        }
    }
}