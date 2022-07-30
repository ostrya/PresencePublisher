package org.ostrya.presencepublisher.log.db;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface DbLogDao<T extends DbLog> {
    LiveData<List<T>> getAllContinuously();

    ListenableFuture<List<T>> getAll();

    List<T> getEntriesOlderThan(long threshold);

    ListenableFuture<Long> insert(T entity);

    int delete(List<T> entities);

    default Future<Integer> deleteAll(Executor executor) {
        ListenableFuture<List<T>> all = getAll();
        return Futures.transform(all, this::delete, executor);
    }
}
