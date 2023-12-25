package com.accelpunch.storage.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface T3VertebraeDao {
    @Query("SELECT * FROM t3")
    public ListenableFuture<List<T3Vertebrae>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<Long> insert(T3Vertebrae t3);

    @Query("DELETE FROM t3")
    public ListenableFuture<Integer> purge();

    @Delete
    public ListenableFuture<Integer> delete(List<T3Vertebrae> t3Records);
}
