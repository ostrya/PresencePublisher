package org.ostrya.presencepublisher.ui.initialization;

import org.ostrya.presencepublisher.MainActivity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public interface InitializationHandler {
    List<HandlerFactory> HANDLER_CHAIN =
            Arrays.asList(
                    EnsureLocationPermission::new,
                    EnsureBackgroundLocationPermission::new,
                    EnsureLocationServiceEnabled::new,
                    EnsureBluetoothServiceEnabled::new,
                    EnsureBatteryOptimizationDisabled::new,
                    CreateSchedule::new
            );

    static InitializationHandler getHandler(List<HandlerFactory> handlerChain) {
        LinkedList<HandlerFactory> handlerChainQueue = new LinkedList<>(handlerChain);
        HandlerFactory firstFactory = Objects.requireNonNull(handlerChainQueue.poll());
        return firstFactory.create(handlerChainQueue);
    }

    void initialize(MainActivity activity);
}
