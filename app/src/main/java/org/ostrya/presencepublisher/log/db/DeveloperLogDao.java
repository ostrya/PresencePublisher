package org.ostrya.presencepublisher.log.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface DeveloperLogDao extends DbLogDao<DeveloperLog> {
    @Query("SELECT * FROM developerlog")
    LiveData<List<DeveloperLog>> getAllContinuously();

    @Query("SELECT * FROM developerlog")
    ListenableFuture<List<DeveloperLog>> getAll();

    @Query("SELECT * FROM developerlog WHERE timestamp < :threshold")
    List<DeveloperLog> getEntriesOlderThan(long threshold);

    @Insert
    ListenableFuture<Long> insert(DeveloperLog developerLog);

    @Delete
    int delete(List<DeveloperLog> developerLogs);
}
