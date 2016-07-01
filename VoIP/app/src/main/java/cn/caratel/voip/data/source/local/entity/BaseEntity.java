package cn.caratel.voip.data.source.local.entity;

import android.provider.BaseColumns;

import java.util.Map;

import cn.caratel.voip.data.model.BaseModel;

/**
 * Created by wurenhai on 2016/6/28.
 */
public abstract class BaseEntity<T extends BaseModel> implements BaseColumns {

    protected static final String TEXT_TYPE = " TEXT";
    protected static final String INT_TYPE = " INTEGER";
    protected static final String BOOLEAN_TYPE = " INTEGER";
    protected static final String COMMA_SEP = ",";

    public abstract String tableName();

    public abstract String idField();

    public abstract String[] projection();

    public abstract T newModel(Map<String, String> values);

}
