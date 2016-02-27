package com.jingchen.timerpicker;

import android.content.Context;

/**
 * Created by hoyouly on 16/2/26.
 */
public enum UnitEmun {
    UNIT_KE(0, R.string.gram, 10, 10, 20),//克
    UNIT_BOWL(1, R.string.bowl, 1, 1, 20),//碗
    UNIT_SCOOP(2, R.string.scoop, 1, 2, 20),//勺
    UNIT_MINUTE(3, R.string.minute, 1, 5, 20),//分
    UNIT_SECOND(4, R.string.second, 1, 60, 20),//秒
    UNIT_GRANULE(5, R.string.granule, 1, 1, 20);//粒

    private int id;
    private int nameResId;//单位的资源id
    private int start;//起始位置
    private int increment;//单位间隔
    private int length;//长度

    UnitEmun(int id, int nameResId, int start, int increment, int length) {
        this.id = id;
        this.nameResId = nameResId;
        this.start = start;
        this.increment = increment;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getId() {
        return id;
    }

    public int getStart() {
        return start;
    }

    public int getIncrement() {
        return increment;
    }

    public int getNameResId() {
        return nameResId;
    }

    public static UnitEmun getUnitEmun(Context context, String name) {
        if (context.getResources().getString(UNIT_KE.getNameResId()).equals(name)) {
            return UNIT_KE;
        } else if (context.getResources().getString(UNIT_BOWL.getNameResId()).equals(name)) {
            return UNIT_BOWL;
        } else if (context.getResources().getString(UNIT_SCOOP.getNameResId()).equals(name)) {
            return UNIT_SCOOP;
        } else if (context.getResources().getString(UNIT_GRANULE.getNameResId()).equals(name)) {
            return UNIT_GRANULE;
        } else if (context.getResources().getString(UNIT_MINUTE.getNameResId()).equals(name)) {
            return UNIT_MINUTE;
        } else if (context.getResources().getString(UNIT_SECOND.getNameResId()).equals(name)) {
            return UNIT_SECOND;
        }
        return UNIT_BOWL;
    }
}
