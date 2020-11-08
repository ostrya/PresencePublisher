package org.ostrya.presencepublisher.ui.initialization;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Nullable;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.ui.contract.DummyActivityResultLauncher;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class AbstractChainedHandler<I, O> implements InitializationHandler {
    protected static final String TAG = "AbstractChainedHandler";
    @Nullable
    private final ActivityResultContract<I, O> contract;
    @Nullable
    private final InitializationHandler nextHandler;
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    @Nullable
    private MainActivity activity = null;
    private ActivityResultLauncher<I> launcher = new DummyActivityResultLauncher<>();

    protected AbstractChainedHandler(@Nullable ActivityResultContract<I, O> contract, Queue<HandlerFactory> handlerChain) {
        this.contract = contract;
        HandlerFactory handlerFactory = handlerChain.poll();
        if (handlerFactory != null) {
            this.nextHandler = handlerFactory.create(handlerChain);
        } else {
            this.nextHandler = null;
        }
    }

    @Override
    public void initialize(MainActivity activity) {
        if (inProgress.compareAndSet(false, true)) {
            if (!activity.equals(this.activity)) {
                this.activity = activity;
                if (contract != null) {
                    launcher = this.activity.registerForActivityResult(contract, this::handleResult);
                }
            }
            HyperLog.i(TAG, "Running initialization for " + getName());
            doInitialize(activity);
        } else {
            HyperLog.d(TAG, "Skipping initialization, already in progress for " + getName());
        }
    }

    protected abstract void doInitialize(MainActivity activity);

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
            nextHandler.initialize(activity);
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
