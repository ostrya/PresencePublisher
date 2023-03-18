package org.ostrya.presencepublisher.log.db;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.ostrya.presencepublisher.log.LogItem;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface LogDao<T extends DbLog> {
    LiveData<List<LogItem>> getAllContinuously();

    ListenableFuture<List<T>> getAll();

    List<T> getEntriesOlderThan(long threshold);

    ListenableFuture<Long> insert(T entity);

    int delete(List<T> entities);

    default Future<Integer> deleteAll(Executor executor) {
        ListenableFuture<List<T>> all = getAll();
        return Futures.transform(all, this::delete, executor);
    }
}
