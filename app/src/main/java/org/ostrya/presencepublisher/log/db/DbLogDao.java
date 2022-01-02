package org.ostrya.presencepublisher.log.db;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public interface DbLogDao<T extends DbLog> {
    LiveData<List<T>> getAllContinuously();

    ListenableFuture<List<T>> getAll();

    List<T> getEntriesOlderThan(long threshold);

    ListenableFuture<Long> insert(T entity);

    int delete(List<T> entities);

    default Future<Integer> deleteAll(Executor executor) {
        ListenableFuture<List<T>> all = getAll();
        FutureTask<Integer> future =
                new FutureTask<>(
                        () -> {
                            try {
                                List<T> entities = all.get();
                                return delete(entities);
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e.getCause());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
        all.addListener(future, executor);
        return future;
    }
}
