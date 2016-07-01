package cn.caratel.voip.data.source;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by wurenhai on 2016/6/28.
 */
public interface DataSource<T> {

    interface LoadDataCallback<T> {

        void onDataLoaded(List<T> data);

        void onDataNotAvailable();

    }

    interface GetItemCallback<T> {

        void onItemLoaded(T item);

        void onItemNotAvailable();

    }

    void all(@NonNull LoadDataCallback<T> callback);

    void find(@NonNull String id, @NonNull GetItemCallback<T> callback);

    void save(@NonNull T model);

    void insert(@NonNull T model);

    void update(@NonNull T model);

    void delete(@NonNull String id);

    void deleteAll();

    void refresh();

}
