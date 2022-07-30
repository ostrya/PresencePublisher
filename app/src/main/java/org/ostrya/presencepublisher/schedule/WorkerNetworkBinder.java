package org.ostrya.presencepublisher.schedule;

import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.work.ListenableWorker;

@RequiresApi(api = Build.VERSION_CODES.P)
public class WorkerNetworkBinder implements NetworkBinder {
    private final ConnectivityManager connectivityManager;

    WorkerNetworkBinder(ListenableWorker worker) {
        connectivityManager =
                worker.getApplicationContext().getSystemService(ConnectivityManager.class);
        connectivityManager.bindProcessToNetwork(worker.getNetwork());
    }

    @Override
    public void close() {
        connectivityManager.bindProcessToNetwork(null);
    }
}
