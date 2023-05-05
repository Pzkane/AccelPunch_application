package com.accelpunch.storage.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Glove.class, Bag.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract GloveDao gloveDao();
    public abstract BagDao bagDao();
}
