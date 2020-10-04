package org.ostrya.presencepublisher.initialization;

import java.util.Queue;

@FunctionalInterface
interface HandlerFactory {
    InitializationHandler create(Queue<HandlerFactory> handlerChain);
}
