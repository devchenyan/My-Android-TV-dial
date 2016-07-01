package cn.caratel.voip.data.source.local.entity;

import java.util.Map;

import cn.caratel.voip.data.model.Option;

/**
 * Created by wurenhai on 2016/6/30.
 */
public class OptionEntity extends BaseEntity<Option> {

    public static final String TABLE_NAME = "option";
    public static final String COLUMN_NAME_KEY = "key";
    public static final String COLUMN_NAME_VALUE = "value";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + TEXT_TYPE + " PRIMARY KEY," +
                    COLUMN_NAME_KEY + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_VALUE + TEXT_TYPE +
                    " )";

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String idField() {
        return COLUMN_NAME_KEY;
    }

    @Override
    public String[] projection() {
        return new String[]{
                COLUMN_NAME_KEY,
                COLUMN_NAME_VALUE,
        };
    }

    @Override
    public Option newModel(Map<String, String> values) {
        return new Option(
                values.get(COLUMN_NAME_KEY),
                values.get(COLUMN_NAME_VALUE)
        );
    }

}
