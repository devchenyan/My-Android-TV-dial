package cn.caratel.voip.data.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.caratel.voip.data.model.BaseModel;
import cn.caratel.voip.data.source.DataSource;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class BaseRepository<T extends BaseModel, T_DS extends DataSource<T>> implements DataSource<T> {

    T_DS remoteDataSource;
    T_DS localDataSource;

    Map<String, T> cached;

    boolean cacheIsDirty = false;

    protected BaseRepository(@NonNull T_DS remoteDataSource, @NonNull T_DS localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    private void getFromRemoteDataSource(@NonNull final LoadDataCallback<T> callback) {
        remoteDataSource.all(new LoadDataCallback<T>() {
            @Override
            public void onDataLoaded(List<T> data) {
                refreshCache(data);
                refreshLocalDataSource(data);
                callback.onDataLoaded(new ArrayList<>(cached.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<T> items) {
        if (cached == null) {
            cached = new LinkedHashMap<>();
        }
        cached.clear();
        for (T item : items) {
            cached.put(item.idValue(), item);
        }
        cacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<T> items) {
        localDataSource.deleteAll();
        for (T item : items) {
            localDataSource.save(item);
        }
    }

    @Nullable
    private T getWithKey(@NonNull String key) {
        if (cached == null || cached.isEmpty()) {
            return null;
        } else {
            return cached.get(key);
        }
    }

    @Override
    public void all(@NonNull final LoadDataCallback<T> callback) {
        if (cached != null && !cacheIsDirty) {
            callback.onDataLoaded(new ArrayList<>(cached.values()));
            return;
        }

        if (cacheIsDirty) {
            getFromRemoteDataSource(callback);
        } else {
            localDataSource.all(new LoadDataCallback<T>() {
                @Override
                public void onDataLoaded(List<T> data) {
                    refreshCache(data);
                    callback.onDataLoaded(new ArrayList<>(cached.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void find(@NonNull final String id, @NonNull final GetItemCallback<T> callback) {
        T cachedItem = getWithKey(id);

        // Respond immediately with cache if available
        if (cachedItem != null) {
            callback.onItemLoaded(cachedItem);
            return;
        }

        //TODO: Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        localDataSource.find(id, new GetItemCallback<T>() {
            @Override
            public void onItemLoaded(T item) {
                callback.onItemLoaded(item);
            }

            @Override
            public void onItemNotAvailable() {
                remoteDataSource.find(id, new GetItemCallback<T>() {
                    @Override
                    public void onItemLoaded(T item) {
                        callback.onItemLoaded(item);
                    }

                    @Override
                    public void onItemNotAvailable() {
                        callback.onItemNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void save(@NonNull T model) {
        remoteDataSource.save(model);
        localDataSource.save(model);

        if (cached == null) {
            cached = new LinkedHashMap<>();
        }
        cached.put(model.idValue(), model);
    }

    @Override
    public void insert(@NonNull T model) {
        remoteDataSource.insert(model);
        localDataSource.insert(model);

        if (cached == null) {
            cached = new LinkedHashMap<>();
        }
        cached.put(model.idValue(), model);
    }

    @Override
    public void update(@NonNull T model) {
        remoteDataSource.update(model);
        localDataSource.update(model);

        if (cached == null) {
            cached = new LinkedHashMap<>();
        }
        cached.put(model.idValue(), model);
    }

    @Override
    public void delete(@NonNull String id) {
        remoteDataSource.delete(id);
        localDataSource.delete(id);
        cached.remove(id);
    }

    @Override
    public void deleteAll() {
        remoteDataSource.deleteAll();
        localDataSource.deleteAll();
        if (cached == null) {
            cached = new LinkedHashMap<>();
        }
        cached.clear();
    }

    @Override
    public void refresh() {
        cacheIsDirty = true;
    }
}
