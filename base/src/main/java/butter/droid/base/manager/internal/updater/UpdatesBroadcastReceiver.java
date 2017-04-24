package butter.droid.base.manager.internal.updater;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import butter.droid.base.R;
import com.sjl.foreground.Foreground;

public class UpdatesBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_UPDATE_AVAILABLE = "butter.droid.updater.UpdatesBroadcastReceiver.ACTION_UPDATE_AVAILABLE";
    private static final String ACTION_NO_UPDATE_AVAILABLE = "butter.droid.updater.UpdatesBroadcastReceiver.ACTION_NO_UPDATE_AVAILABLE";

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

            final Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setDataAndType(Uri.parse("file://" + updateFile), ButterUpdateManager.ANDROID_PACKAGE_MIME_TYPE);
            notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0));

            nm.notify(ButterUpdateManager.NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void handleNoUpdateAvailableAction(final Context context, final Intent intent) {

    }

}
