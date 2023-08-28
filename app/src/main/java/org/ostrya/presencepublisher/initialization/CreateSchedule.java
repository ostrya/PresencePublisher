package org.ostrya.presencepublisher.initialization;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.schedule.Scheduler;

import java.util.Queue;

public class CreateSchedule extends AbstractChainedHandler<Void, Void> {
    protected CreateSchedule(MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, null, handlerChain);
    }

    @Override
    protected void doInitialize() {
        DatabaseLogger.i(TAG, "Ensure schedule is active");
        new Thread(() -> new Scheduler(activity).ensureSchedule()).start();
        finishInitialization();
    }

    @Override
    protected void doHandleResult(Void result) {
        DatabaseLogger.w(TAG, "Skipping unexpected result");
    }

    @Override
    protected String getName() {
        return "CreateSchedule";
    }
}
