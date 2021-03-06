package org.ostrya.presencepublisher.ui.initialization;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Nullable;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.MainActivity;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class AbstractChainedHandler<I, O> implements InitializationHandler {
    protected static final String TAG = "AbstractChainedHandler";
    @Nullable
    private final InitializationHandler nextHandler;
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    protected final MainActivity activity;
    private final ActivityResultLauncher<I> launcher;

    protected AbstractChainedHandler(MainActivity activity, @Nullable ActivityResultContract<I, O> contract, Queue<HandlerFactory> handlerChain) {
        this.activity = activity;
        this.launcher = this.activity.registerForActivityResult(contract, this::handleResult);
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
            HyperLog.i(TAG, "Running initialization for " + getName());
            doInitialize();
        } else {
            HyperLog.d(TAG, "Skipping initialization, already in progress for " + getName());
        }
    }

    protected abstract void doInitialize();

    private void handleResult(O result) {
        if (inProgress.get()) {
            doHandleResult(result);
        } else {
            HyperLog.w(TAG, "Skipping result because initialization not in progress for " + getName());
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
