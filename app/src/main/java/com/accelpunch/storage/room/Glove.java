package com.accelpunch.storage.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Glove extends RoomEntity {
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    public long time;

    // Either 'L' or 'R'
    public char glove;
    public Integer x;
    public Integer y;
    public Integer z;
}
