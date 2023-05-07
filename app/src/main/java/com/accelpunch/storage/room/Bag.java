package com.accelpunch.storage.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Bag extends RoomEntity {
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    public long time;
    public Integer x;
    public Integer y;
    public Integer z;
    public Integer temp;

}
