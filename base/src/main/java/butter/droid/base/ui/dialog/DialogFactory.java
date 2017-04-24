package butter.droid.base.ui.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.support.annotation.IntDef;
import butter.droid.base.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DialogFactory {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {ACTION_POSITIVE, ACTION_NEGATIVE, ACTION_NEUTRAL})
    public @interface Action {}

    public static final int ACTION_POSITIVE = 0;
    public static final int ACTION_NEGATIVE = 1;
    public static final int ACTION_NEUTRAL = 2;

    public interface ActionCallback {

        void onButtonClick(Dialog which, @Action int action);
    }

    private DialogFactory() {
        throw new AssertionError("No instances of this class");
    }

    public static AlertDialog createErrorFetchingYoutubeVideoDialog(final Context context, final ActionCallback callback) {
        return new Builder(context)
                .setTitle(R.string.comm_error)
                .setCancelable(false)
                .setMessage(R.string.comm_message)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.onButtonClick(((Dialog) dialog), ACTION_POSITIVE);
                        }
                    }
                }).create();
    }

    public static AlertDialog createNewVersionUpdateDialog(final Context context, final ActionCallback callback) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.update_available)
                .setMessage(R.string.press_install)
                .setCancelable(true)
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (callback != null) {
                            callback.onButtonClick((Dialog) dialog, ACTION_NEGATIVE);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (callback != null) {
                            callback.onButtonClick((Dialog) dialog, ACTION_NEGATIVE);
                        }
                    }
                })
                .setPositiveButton(R.string.install, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (callback != null) {
                            callback.onButtonClick((Dialog) dialog, ACTION_POSITIVE);
                        }
                    }
                })
                .create();
    }
}
