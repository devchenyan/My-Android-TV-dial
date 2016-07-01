package cn.caratel.voip.data.source.local;

import android.content.Context;
import android.support.annotation.NonNull;

import cn.caratel.voip.data.model.Option;
import cn.caratel.voip.data.source.OptionsDataSource;
import cn.caratel.voip.data.source.local.entity.OptionEntity;

/**
 * Created by wurenhai on 2016/6/30.
 */
public class OptionsLoaclDataSource extends LocalDataSource<Option, OptionEntity> implements OptionsDataSource {

    private static OptionsLoaclDataSource instance = null;

    public static OptionsLoaclDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new OptionsLoaclDataSource(context);
        }
        return instance;
    }

    public OptionsLoaclDataSource(@NonNull Context context) {
        super(context, new OptionEntity());
    }

}
