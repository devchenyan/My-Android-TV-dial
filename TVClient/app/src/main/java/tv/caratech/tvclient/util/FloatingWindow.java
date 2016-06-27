package tv.caratech.tvclient.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import tv.caratech.tvclient.R;

/**
 * Created by wurenhai on 2016/6/14.
 * 需要系统权限: android.permission.SYSTEM_ALERT_WINDOW
 * 在AndroidManifest.xml中添加:
 *   <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
 */
public class FloatingWindow {

    //private static final String TAG = "FloatingWindow";

    private static final int START_ALPHA = 80;

    private WindowManager windowManager;
    private View view;
    WindowManager.LayoutParams params;
    private boolean isVisible = false;

    public FloatingWindow(Context context, View view) {
        this.view = view;

        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.alpha = START_ALPHA;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.LEFT|Gravity.TOP;

        //设置全屏
        params.x = 0;
        params.y = 0;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    public void show() {
        if (!isVisible) {
            isVisible = true;
            windowManager.addView(view, params);
        }
    }

    public void hide() {
        if (isVisible) {
            isVisible = false;
            windowManager.removeView(this.view);
        }
    }

    public void update(int x, int y) {
        if (params.x != x || params.y != y) {
            params.x = x;
            params.y = y;
            if (isVisible) {
                windowManager.updateViewLayout(view, params);
            }
        }
    }

    public FloatingWindow setTouchMoveView(View view, final boolean eatActions) {
        view.setOnTouchListener(new View.OnTouchListener(){

            private float startX = 0f;
            private float startY = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float touchX = event.getX();
                final float touchY = event.getY();
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startX = touchX;
                        startY = touchY;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_MOVE:
                        update(params.x + (int)(touchX - startX), params.y + (int)(touchY - startY));
                        break;
                }
                return eatActions;
            }
        });
        return this;
    }

    public FloatingWindow setClickCloseView(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        return this;
    }

}
