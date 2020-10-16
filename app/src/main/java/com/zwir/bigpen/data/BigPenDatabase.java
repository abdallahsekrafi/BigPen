package com.zwir.bigpen.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Pen.class}, version = 2)
public abstract class BigPenDatabase extends RoomDatabase {

    private static volatile BigPenDatabase INSTANCE;

    static BigPenDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BigPenDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BigPenDatabase.class, "bigPen_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public abstract PenDao getPenDao();
}

