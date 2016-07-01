package cn.caratel.voip.data.model;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class JsonResponse {

    int code;
    String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "" + code + "(" + message + ")";
    }
}
