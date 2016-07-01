package cn.caratel.voip.data.model;

import android.content.ContentValues;
import android.support.annotation.Nullable;

import java.util.Objects;

import cn.caratel.voip.data.model.BaseModel;
import cn.caratel.voip.data.source.local.entity.ContactEntity;

/**
 * Created by wurenhai on 2016/6/28.
 */
public final class Contact extends BaseModel {

    String voip_number;

    @Nullable
    String phone_number;

    @Nullable
    String icon;

    @Nullable
    String nickname;

    int frequency;

    public Contact(String voip_number, @Nullable String icon, @Nullable String phone_number, @Nullable String nickname) {
        this(voip_number, phone_number, icon, nickname, 0);
    }

    public Contact(String voip_number, @Nullable String phone_number, @Nullable String icon, @Nullable String nickname, int frequency) {
        this.voip_number = voip_number;
        this.phone_number = phone_number;
        this.icon = icon;
        this.nickname = nickname;
        this.frequency = frequency;
    }

    public String getVoipNumber() {
        return voip_number;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact)o;
        return Objects.equals(voip_number, contact.voip_number) &&
                Objects.equals(phone_number, contact.phone_number) &&
                Objects.equals(icon, contact.icon) &&
                Objects.equals(nickname, contact.nickname) &&
                Objects.equals(frequency, contact.frequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voip_number, phone_number, icon, nickname);
    }

    @Override
    public String toString() {
        return "Contact: " + voip_number + ", " + phone_number + ", " + icon + ", " + nickname + ", " + frequency;
    }

    @Override
    public String idValue() {
        return voip_number;
    }

    @Override
    public ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(ContactEntity.COLUMN_NAME_VOIP_NUMBER, voip_number);
        values.put(ContactEntity.COLUMN_NAME_PHONE_NUMBER, phone_number);
        values.put(ContactEntity.COLUMN_NAME_ICON, icon);
        values.put(ContactEntity.COLUMN_NAME_NICKNAME, nickname);
        values.put(ContactEntity.COLUMN_NAME_FREQUENCY, frequency);
        return values;
    }
}
