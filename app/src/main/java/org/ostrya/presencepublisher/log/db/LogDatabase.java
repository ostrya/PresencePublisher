package org.ostrya.presencepublisher.log.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {DetectionLog.class, MessagesLog.class, DeveloperLog.class},
        version = 1)
public abstract class LogDatabase extends RoomDatabase {
    public abstract DetectionLogDao detectionLogDao();

    public abstract MessagesLogDao messagesLogDao();

    public abstract DeveloperLogDao developerLogDao();
}
