package com.accelpunch.storage.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "t3")
public class T3Vertebrae extends RoomEntity {
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    public long time;
    public float w;
    public Float x;
    public Float y;
    public Float z;
    public Float xg;
    public Float yg;
    public Float zg;
}
