package org.ostrya.presencepublisher.initialization;

import org.ostrya.presencepublisher.MainActivity;

import java.util.Queue;

@FunctionalInterface
public interface HandlerFactory {
    InitializationHandler create(MainActivity activity, Queue<HandlerFactory> handlerChain);
}
