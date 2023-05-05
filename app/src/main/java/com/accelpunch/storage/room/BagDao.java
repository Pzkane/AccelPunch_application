package com.accelpunch.storage.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface BagDao {
    @Query("SELECT * FROM bag")
    public ListenableFuture<List<Bag>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<Long> insert(Bag bag);

    @Query("DELETE FROM bag")
    public ListenableFuture<Integer> purge();
}
