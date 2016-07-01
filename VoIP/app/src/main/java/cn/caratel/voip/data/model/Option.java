package cn.caratel.voip.data.model;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

import cn.caratel.voip.data.source.local.entity.OptionEntity;

/**
 * Created by wurenhai on 2016/6/30.
 */
public class Option extends BaseModel {

    @NonNull
    String key;

    @Nullable
    String value;

    public Option(@NonNull String key, @Nullable String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String idValue() {
        return key;
    }

    @Override
    public ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(OptionEntity.COLUMN_NAME_KEY, key);
        values.put(OptionEntity.COLUMN_NAME_VALUE, value);
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option)o;
        return Objects.equals(key, option.key) &&
                Objects.equals(value, option.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Option: " + key + ", " + value;
    }
}
