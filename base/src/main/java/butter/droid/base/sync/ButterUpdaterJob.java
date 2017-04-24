package butter.droid.base.sync;

import android.support.annotation.NonNull;
import butter.droid.base.ButterApplication;
import butter.droid.base.manager.internal.updater.ButterUpdateManager;
import com.evernote.android.job.Job;
import javax.inject.Inject;

public class ButterUpdaterJob extends Job {

    public static final String TAG = "ButterUpdaterJob";

    @Inject ButterUpdateManager updateManager;

    @NonNull
    @Override
    protected Result onRunJob(final Params params) {
        ButterApplication.getAppContext().getComponent().inject(this);
        updateManager.checkUpdates();
        return Result.SUCCESS;
    }
}
