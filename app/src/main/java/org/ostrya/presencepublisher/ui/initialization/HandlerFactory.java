package org.ostrya.presencepublisher.ui.initialization;

import org.ostrya.presencepublisher.MainActivity;

import java.util.Queue;

@FunctionalInterface
interface HandlerFactory {
    InitializationHandler create(MainActivity activity, Queue<HandlerFactory> handlerChain);
}
