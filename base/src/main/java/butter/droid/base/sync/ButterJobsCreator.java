package butter.droid.base.sync;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class ButterJobsCreator implements JobCreator {

    @Override
    public Job create(final String tag) {
        switch (tag) {
            case ButterUpdaterJob.TAG:
                return new ButterUpdaterJob();
            default:
                return null;
        }
    }
    
}
