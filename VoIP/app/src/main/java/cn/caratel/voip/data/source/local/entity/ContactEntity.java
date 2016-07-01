package cn.caratel.voip.data.source.local.entity;

import java.util.Map;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.source.local.DbHelper;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class ContactEntity extends BaseEntity<Contact> {

    public static final String TABLE_NAME = "contact";
    public static final String COLUMN_NAME_VOIP_NUMBER = "voip_number";
    public static final String COLUMN_NAME_CLIENT_ID = "client_id";
    public static final String COLUMN_NAME_NICKNAME = "nickname";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + TEXT_TYPE + " PRIMARY KEY," +
                    COLUMN_NAME_VOIP_NUMBER + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_CLIENT_ID + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_NICKNAME + TEXT_TYPE +
            " )";

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String idField() {
        return COLUMN_NAME_VOIP_NUMBER;
    }

    @Override
    public String[] projection() {
        return new String[] {
                COLUMN_NAME_VOIP_NUMBER,
                COLUMN_NAME_CLIENT_ID,
                COLUMN_NAME_NICKNAME,
        };
    }

    @Override
    public Contact newModel(Map<String, String> values) {
        return new Contact(values.get(COLUMN_NAME_VOIP_NUMBER),
                values.get(COLUMN_NAME_CLIENT_ID),
                values.get(COLUMN_NAME_NICKNAME)
            );
    }

}
