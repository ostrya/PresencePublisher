package org.ostrya.presencepublisher.ui.initialization;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.schedule.Scheduler;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.ostrya.presencepublisher.schedule.Scheduler.NOW_DELAY;

public class CreateSchedule extends AbstractChainedHandler<Void, Void> {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    protected CreateSchedule(MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, null, handlerChain);
    }

    @Override
    protected void doInitialize() {
        HyperLog.i(TAG, "Starting schedule now");
        new Scheduler(activity).scheduleNow();
        // make sure we don't re-schedule until the first run has happened
        executorService.schedule(this::finishInitialization, NOW_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doHandleResult(Void result) {
        HyperLog.w(TAG, "Skipping unexpected result");
    }

    @Override
    protected String getName() {
        return "CreateSchedule";
    }
}
