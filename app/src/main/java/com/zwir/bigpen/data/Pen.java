package com.zwir.bigpen.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Pen {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private int colorPen;
    public Pen(@NonNull int colorPen) {
        this.colorPen = colorPen;
    }

    public int getColorPen() {
        return colorPen;
    }
}
