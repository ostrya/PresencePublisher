package org.ostrya.presencepublisher.initialization;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class AbstractChainedHandler<I, O> implements InitializationHandler {
    protected static final String TAG = "AbstractChainedHandler";
    @Nullable private final InitializationHandler nextHandler;
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    protected final MainActivity activity;
    private final ActivityResultLauncher<I> launcher;

    protected AbstractChainedHandler(
            MainActivity activity,
            @Nullable ActivityResultContract<I, O> contract,
            Queue<HandlerFactory> handlerChain) {
        this.activity = activity;
        if (contract != null) {
            this.launcher = this.activity.registerForActivityResult(contract, this::handleResult);
        } else {
            this.launcher = new DummyActivityResultLauncher<>();
        }
        HandlerFactory handlerFactory = handlerChain.poll();
        if (handlerFactory != null) {
            this.nextHandler = handlerFactory.create(activity, handlerChain);
        } else {
            this.nextHandler = null;
        }
    }

    @Override
    public void initialize() {
        if (inProgress.compareAndSet(false, true)) {
            DatabaseLogger.d(getName(), "Running initialization");
            doInitialize();
        } else {
            DatabaseLogger.d(getName(), "Skipping initialization, already in progress");
        }
    }

    protected abstract void doInitialize();

    private void handleResult(O result) {
        if (inProgress.get()) {
            doHandleResult(result);
        } else {
            DatabaseLogger.w(getName(), "Skipping result because initialization not in progress");
        }
    }

    protected abstract void doHandleResult(O result);

    protected void finishInitialization() {
        if (nextHandler != null) {
            nextHandler.initialize();
        }
        inProgress.compareAndSet(true, false);
    }

    protected void cancelInitialization() {
        inProgress.compareAndSet(true, false);
    }

    protected ActivityResultLauncher<I> getLauncher() {
        return launcher;
    }

    protected abstract String getName();
}
