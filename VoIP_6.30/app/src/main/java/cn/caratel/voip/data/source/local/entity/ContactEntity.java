package cn.caratel.voip.data.source.local.entity;

import java.util.Map;

import cn.caratel.voip.data.model.Contact;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class ContactEntity extends BaseEntity<Contact> {

    public static final String TABLE_NAME = "contact";
    public static final String COLUMN_NAME_VOIP_NUMBER = "voip_number";
    public static final String COLUMN_NAME_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_NAME_ICON = "icon";
    public static final String COLUMN_NAME_NICKNAME = "nickname";
    public static final String COLUMN_NAME_FREQUENCY = "frequency";

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
                COLUMN_NAME_PHONE_NUMBER,
                COLUMN_NAME_ICON,
                COLUMN_NAME_NICKNAME,
                COLUMN_NAME_FREQUENCY,
        };
    }

    @Override
    public Contact newModel(Map<String, String> values) {
        return new Contact(values.get(COLUMN_NAME_VOIP_NUMBER),
                values.get(COLUMN_NAME_PHONE_NUMBER),
                values.get(COLUMN_NAME_ICON),
                values.get(COLUMN_NAME_NICKNAME),
                Integer.valueOf(values.get(COLUMN_NAME_FREQUENCY))
            );
    }

}
