package org.ostrya.presencepublisher.log.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import org.ostrya.presencepublisher.log.LogItem;

import java.util.List;

@Dao
public interface DeveloperLogDao extends LogDao<DeveloperLog> {
    @Query("SELECT id, line FROM developerlog")
    LiveData<List<LogItem>> getAllContinuously();

    @Query("SELECT * FROM developerlog")
    ListenableFuture<List<DeveloperLog>> getAll();

    @Query("SELECT * FROM developerlog WHERE timestamp < :threshold")
    List<DeveloperLog> getEntriesOlderThan(long threshold);

    @Insert
    ListenableFuture<Long> insert(DeveloperLog developerLog);

    @Delete
    int delete(List<DeveloperLog> developerLogs);
}
