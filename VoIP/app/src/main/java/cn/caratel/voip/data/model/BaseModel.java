package cn.caratel.voip.data.model;

import android.content.ContentValues;

import java.util.HashMap;

/**
 * Created by wurenhai on 2016/6/28.
 */
public abstract class BaseModel {

    public abstract String idValue();

    public abstract ContentValues contentValues();

}
