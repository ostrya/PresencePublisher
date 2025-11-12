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
public interface MessagesLogDao extends LogDao<MessagesLog> {
    @Query("SELECT id, line FROM messageslog ORDER BY timestamp, id")
    LiveData<List<LogItem>> getAllContinuously();

    @Query("SELECT * FROM messageslog ORDER BY timestamp, id")
    ListenableFuture<List<MessagesLog>> getAll();

    @Query("SELECT * FROM messageslog WHERE timestamp < :threshold ORDER BY timestamp, id")
    List<MessagesLog> getEntriesOlderThan(long threshold);

    @Insert
    ListenableFuture<Long> insert(MessagesLog messagesLog);

    @Delete
    int delete(List<MessagesLog> messagesLogs);
}
