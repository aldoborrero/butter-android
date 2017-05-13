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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import butter.droid.base.R;
import com.sjl.foreground.Foreground;

public class NewUpdatesBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_UPDATE_AVAILABLE = "butter.droid.updater.NewUpdatesBroadcastReceiver.ACTION_UPDATE_AVAILABLE";
    private static final String ACTION_NO_UPDATE_AVAILABLE = "butter.droid.updater.NewUpdatesBroadcastReceiver.ACTION_NO_UPDATE_AVAILABLE";
    private static final String ACTION_ERROR_CHECKING = "butter.droid.updater.NewUpdatesBroadcastReceiver.ACTION_ERROR_DOWNLOADING";
    private static final String ACTION_ERROR_DOWNLOADING = "butter.droid.updater.NewUpdatesBroadcastReceiver.ACTION_ERROR_DOWNLOADING";

    private static final String FILE_EXTRA = "butter.droid.updater.NewUpdatesBroadcastReceiver.FILE_EXTRA";

    public static void newUpdateAvailable(final Context context) {
        context.sendBroadcast(new Intent(ACTION_UPDATE_AVAILABLE));
    }

    public static void noUpdatesAvailable(final Context context) {
        context.sendBroadcast(new Intent(ACTION_NO_UPDATE_AVAILABLE));
    }

    public static void errorDownloadingUpdate(final Context context) {
        context.sendBroadcast(new Intent(ACTION_ERROR_DOWNLOADING));
    }

    public static void setActionErrorChecking(final Context context) {
        context.sendBroadcast(new Intent(ACTION_ERROR_CHECKING));
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_UPDATE_AVAILABLE:
                handleUpdateAvailableAction(context, intent);
                break;
            case ACTION_NO_UPDATE_AVAILABLE:
                handleNoUpdateAvailableAction(context, intent);
                break;
            case ACTION_ERROR_CHECKING:
                handleErrorCheckingAction(context, intent);
                break;
        }
    }

    private void handleUpdateAvailableAction(final Context context, final Intent intent) {
        final Foreground foreground = Foreground.get();
        if (foreground.isForeground()) {
            ButterUpdaterService.displayUpdateDialog(context);
        } else {
            final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notif_logo)
                    .setContentTitle(context.getString(R.string.update_available))
                    .setContentText(context.getString(R.string.press_install))
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            final String fileName = intent.getStringExtra(FILE_EXTRA);

            final Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setDataAndType(Uri.parse("file://" + fileName), ButterUpdateManager.ANDROID_PACKAGE_MIME_TYPE);
            notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0));

            nm.notify(ButterUpdateManager.NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void handleNoUpdateAvailableAction(final Context context, final Intent intent) {

    }

    private void handleErrorCheckingAction(final Context context, final Intent intent) {
        final Foreground foreground = Foreground.get();
        if (foreground.isForeground()) {
            Toast.makeText(context, R.string.error_checking_for_updates, Toast.LENGTH_SHORT).show();
        }
    }
}
