package org.ostrya.presencepublisher.initialization;

import androidx.annotation.Nullable;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class AbstractChainedHandler implements InitializationHandler {
    protected static final String TAG = "AbstractChainedHandler";
    @Nullable
    private final InitializationHandler nextHandler;
    private final int requestCode;
    private final AtomicBoolean inProgress = new AtomicBoolean(false);

    protected AbstractChainedHandler(int requestCode, Queue<HandlerFactory> handlerChain) {
        this.requestCode = requestCode;
        HandlerFactory handlerFactory = handlerChain.poll();
        if (handlerFactory != null) {
            this.nextHandler = handlerFactory.create(handlerChain);
        } else {
            this.nextHandler = null;
        }
    }

    @Override
    public void initialize(MainActivity context) {
        if (inProgress.compareAndSet(false, true)) {
            doInitialize(context);
        } else {
            HyperLog.d(TAG, "Skipping initialization, already in progress for request code " + getRequestCode());
        }
    }

    protected abstract void doInitialize(MainActivity context);

    @Override
    public void handleResult(MainActivity context, int triggeringRequestCode, int resultCode) {
        if (triggeringRequestCode == requestCode) {
            doHandleResult(context, resultCode);
        } else if (nextHandler != null) {
            nextHandler.handleResult(context, triggeringRequestCode, resultCode);
        } else {
            HyperLog.i(TAG, "Skipping unexpected request code " + triggeringRequestCode);
        }
    }

    protected abstract void doHandleResult(MainActivity context, int resultCode);

    protected void finishInitialization(MainActivity context) {
        if (nextHandler != null) {
            nextHandler.initialize(context);
        }
        inProgress.compareAndSet(true, false);
    }

    protected int getRequestCode() {
        return requestCode;
    }
}
