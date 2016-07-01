package cn.caratel.voip.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.caratel.voip.data.source.local.entity.ContactEntity;

/**
 * Created by wurenhai on 2016/6/27.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;

    public static final String DB_NAME = "local1.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String INT_TYPE = " INTEGER";

    private static final String BOOLEAN_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CONTACTS =
            "CREATE TABLE " + ContactEntity.TABLE_NAME + " (" +
                    ContactEntity._ID + TEXT_TYPE + " PRIMARY KEY," +
                    ContactEntity.COLUMN_NAME_VOIP_NUMBER + TEXT_TYPE + COMMA_SEP +
                    ContactEntity.COLUMN_NAME_PHONE_NUMBER + TEXT_TYPE + COMMA_SEP +
                    ContactEntity.COLUMN_NAME_ICON + TEXT_TYPE + COMMA_SEP +
                    ContactEntity.COLUMN_NAME_NICKNAME + TEXT_TYPE + COMMA_SEP +
                    ContactEntity.COLUMN_NAME_FREQUENCY + INT_TYPE +
                    " )";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACTS);
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
