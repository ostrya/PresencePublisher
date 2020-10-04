package org.ostrya.presencepublisher.initialization;

import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.schedule.Scheduler;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.ostrya.presencepublisher.schedule.Scheduler.NOW_DELAY;

public class CreateSchedule extends AbstractChainedHandler {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    protected CreateSchedule(Queue<HandlerFactory> handlerChain) {
        super(-1, handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity context) {
        HyperLog.i(TAG, "Starting schedule now");
        new Scheduler(context).scheduleNow();
        // make sure we don't re-schedule until the first run has happened
        executorService.schedule(() -> finishInitialization(context), NOW_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doHandleResult(MainActivity context, int resultCode) {
        HyperLog.w(TAG, "Skipping unexpected result with request code -1");
    }
}
