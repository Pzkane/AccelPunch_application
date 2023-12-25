package com.accelpunch.storage.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Glove.class, Bag.class, T3Vertebrae.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract GloveDao gloveDao();
    public abstract BagDao bagDao();
    public abstract T3VertebraeDao t3VertebraeDao();
}
