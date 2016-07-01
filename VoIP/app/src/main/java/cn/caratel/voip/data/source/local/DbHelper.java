package cn.caratel.voip.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.caratel.voip.data.model.Option;
import cn.caratel.voip.data.source.local.entity.ContactEntity;
import cn.caratel.voip.data.source.local.entity.OptionEntity;

/**
 * Created by wurenhai on 2016/6/27.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;

    public static final String DB_NAME = "local3.db";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ContactEntity.SQL_CREATE_TABLE);
        db.execSQL(OptionEntity.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
