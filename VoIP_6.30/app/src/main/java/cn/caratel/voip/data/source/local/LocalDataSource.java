package cn.caratel.voip.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.caratel.voip.data.model.BaseModel;
import cn.caratel.voip.data.source.DataSource;
import cn.caratel.voip.data.source.local.entity.BaseEntity;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class LocalDataSource<T extends BaseModel, E extends BaseEntity<T>> implements DataSource<T> {

    private final BaseEntity entity;

    DbHelper dbHelper;

    public LocalDataSource(@NonNull Context context, @NonNull E entity) {
        this.dbHelper = new DbHelper(context);
        this.entity = entity;
    }

    @Override
    public void all(@NonNull LoadDataCallback<T> callback) {
        List<T> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = entity.projection();

        Cursor c = db.query(entity.tableName(), projection, null, null, null, null, null);
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                Map<String, String> fields = new HashMap<>();
                for(int i=0; i<projection.length; i++) {
                    fields.put(projection[i], c.getString(c.getColumnIndexOrThrow(projection[i])));
                }
                T model = (T)entity.newModel(fields);
                items.add(model);
            }
        }
        if (c != null) {
            c.close();
        }

        db.close();

        if (items.isEmpty()) {
            callback.onDataNotAvailable();
        } else {
            callback.onDataLoaded(items);
        }
    }

    @Override
    public void find(@NonNull String id, @NonNull GetItemCallback<T> callback) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = entity.projection();

        String selection = entity.idField() + "=?";
        String[] selectionArgs = {id};

        Cursor c = db.query(entity.tableName(), projection, selection, selectionArgs, null, null, null);

        T item = null;

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            Map<String, String> fields = new HashMap<>();
            for(int i=0; i<projection.length; i++) {
                fields.put(projection[i], c.getString(c.getColumnIndexOrThrow(projection[i])));
            }
            item = (T)entity.newModel(fields);
        }
        if (c != null) {
            c.close();
        }

        db.close();

        if (item != null) {
            callback.onItemLoaded(item);
        } else {
            callback.onItemNotAvailable();
        }
    }

    @Override
    public void save(@NonNull T model) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues(model.contentValues());
        db.insert(entity.tableName(), null, values);
        db.close();
    }

    @Override
    public void insert(@NonNull T model) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues(model.contentValues());
        db.insert(entity.tableName(), null, values);
        db.close();
    }

    @Override
    public void update(@NonNull T model) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues(model.contentValues());
        values.remove(entity.idField());
        String selection = entity.idField() + "=?";
        String[] selectionArgs = { model.idValue() };
        db.update(entity.tableName(), values, selection, selectionArgs);
        db.close();
    }

    @Override
    public void delete(@NonNull String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = entity.idField() + "=?";
        String[] selectionArgs = {id};
        db.delete(entity.tableName(), selection, selectionArgs);
        db.close();
    }

    @Override
    public void deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(entity.tableName(), null, null);
        db.close();
    }

    @Override
    public void refresh() {

    }
}
