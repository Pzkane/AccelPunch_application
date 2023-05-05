package com.accelpunch.storage.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface GloveDao {
    @Query("SELECT * FROM glove")
    public ListenableFuture<List<Glove>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<Long> insert(Glove glove);

    @Query("DELETE FROM glove")
    public ListenableFuture<Integer> purge();
}
