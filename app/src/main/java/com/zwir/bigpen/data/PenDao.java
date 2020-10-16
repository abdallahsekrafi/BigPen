package com.zwir.bigpen.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


@Dao
public interface PenDao {
    @Query("SELECT * FROM Pen")
    LiveData<List<Pen>> getAllPen();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void savePen(Pen pen);
    @Delete
    void deletePen(Pen pen);
}