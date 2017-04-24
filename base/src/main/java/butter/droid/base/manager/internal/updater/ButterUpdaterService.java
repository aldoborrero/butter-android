package butter.droid.base.manager.internal.updater;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.view.ContextThemeWrapper;
import butter.droid.base.R;
import butter.droid.base.ui.dialog.DialogFactory;
import butter.droid.base.ui.dialog.DialogFactory.Action;
import butter.droid.base.ui.dialog.DialogFactory.ActionCallback;

public class ButterUpdaterService extends Service {

    private static final String DISPLAY_UPDATE_DIALOG_ACTION = "butter.droid.base.manager.internal.updater.ButterUpdaterService.DISPLAY_UPDATE_DIALOG_ACTION";

    private AlertDialog dialog;

    public static void displayUpdateDialog(final Context context) {
        context.startService(new Intent(context, ButterUpdaterService.class));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final boolean displayDialog = intent.hasExtra(DISPLAY_UPDATE_DIALOG_ACTION);
        if (displayDialog) {
            displayDialog();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        removeDialog();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    private void displayDialog() {
        if (dialog == null) {
            dialog = createNewVersionUpdateDialog();
        }
        dialog.show();
    }

    private void removeDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private AlertDialog createNewVersionUpdateDialog() {
        final ContextThemeWrapper context = new ContextThemeWrapper(getApplication(), R.style.Theme_Butter_UpdaterDialog);
        return DialogFactory.createNewVersionUpdateDialog(context, new ActionCallback() {
            @Override
            public void onButtonClick(final Dialog which, @Action final int action) {
                switch (action) {
                    case DialogFactory.ACTION_NEGATIVE:
                        stopSelf();
                        break;
                    case DialogFactory.ACTION_POSITIVE:
                        break;
                }
            }
        });
    }
}
