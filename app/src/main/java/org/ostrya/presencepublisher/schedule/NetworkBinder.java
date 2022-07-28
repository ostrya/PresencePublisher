package org.ostrya.presencepublisher.schedule;

import android.os.Build;

import androidx.work.ListenableWorker;

import java.io.Closeable;

public interface NetworkBinder extends Closeable {

    static NetworkBinder bindToNetwork(ListenableWorker worker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new WorkerNetworkBinder(worker);
        } else {
            return () -> {};
        }
    }

    @Override
    void close();
}
