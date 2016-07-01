package cn.caratel.voip.data.model;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

import cn.caratel.voip.data.model.BaseModel;
import cn.caratel.voip.data.source.local.entity.ContactEntity;

/**
 * Created by wurenhai on 2016/6/28.
 */
public final class Contact extends BaseModel {

    @NonNull
    String voip_number;

    @NonNull
    String client_id;

    @Nullable
    String nickname;

    public Contact(@NonNull String voip_number, @NonNull String client_id, @Nullable String nickname) {
        this.voip_number = voip_number;
        this.client_id = client_id;
        this.nickname = nickname;
    }

    public String getVoipNumber() {
        return voip_number;
    }

    public String getClientId() {
        return client_id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact)o;
        return Objects.equals(voip_number, contact.voip_number) &&
                Objects.equals(client_id, contact.client_id) &&
                Objects.equals(nickname, contact.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voip_number, client_id, nickname);
    }

    @Override
    public String toString() {
        return "Contact: " + voip_number + ", " + client_id + ", " + nickname;
    }

    @Override
    public String idValue() {
        return voip_number;
    }

    @Override
    public ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(ContactEntity.COLUMN_NAME_VOIP_NUMBER, voip_number);
        values.put(ContactEntity.COLUMN_NAME_CLIENT_ID, client_id);
        values.put(ContactEntity.COLUMN_NAME_NICKNAME, nickname);
        return values;
    }
}
