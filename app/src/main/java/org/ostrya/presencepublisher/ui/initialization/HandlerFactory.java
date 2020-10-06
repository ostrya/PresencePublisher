package org.ostrya.presencepublisher.ui.initialization;

import java.util.Queue;

@FunctionalInterface
interface HandlerFactory {
    InitializationHandler create(Queue<HandlerFactory> handlerChain);
}
