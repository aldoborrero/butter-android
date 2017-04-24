package butter.droid.base.sync;

import com.evernote.android.job.JobManager;

public class ButterJobsScheduler {

    private final JobManager jobManager;

    private ButterJobsScheduler(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public void scheduleJobs() {
        jobManager.addJobCreator(new ButterJobsCreator());
    }

    public void cancelAllScheduledJobs() {
        jobManager.cancelAll();
    }

}
