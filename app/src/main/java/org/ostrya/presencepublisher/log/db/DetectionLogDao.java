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
public interface DetectionLogDao extends LogDao<DetectionLog> {
    @Query("SELECT id, line FROM detectionlog ORDER BY timestamp, id")
    LiveData<List<LogItem>> getAllContinuously();

    @Query("SELECT * FROM detectionlog ORDER BY timestamp, id")
    ListenableFuture<List<DetectionLog>> getAll();

    @Query("SELECT * FROM detectionlog WHERE timestamp < :threshold ORDER BY timestamp, id")
    List<DetectionLog> getEntriesOlderThan(long threshold);

    @Insert
    ListenableFuture<Long> insert(DetectionLog detectionLog);

    @Delete
    int delete(List<DetectionLog> detectionLogs);
}
