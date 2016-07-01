package cn.caratel.voip.cells;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.caratech.voip.R;

/**
 * Created by chenyan on 6/29/16.
 */
public class ContactCell extends FrameLayout {
    private ImageView imageView;
    private TextView textView;


    public ContactCell(Context context) {
        super(context);
        setupViews(context, null);
    }

    public ContactCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupViews(context, attrs);
    }

    public ContactCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupViews(context, attrs);
    }

    /**
     * 初始化View
     *
     * @param context
     * @param attributeSet
     */
    private void setupViews(Context context, AttributeSet attributeSet) {

//        将xml布局到ViewGroup中来
        View.inflate(context, R.layout.cell_contact, this);

        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.ContactCell);
        imageView = (ImageView) findViewById(R.id.cell_contact_image);
        textView = (TextView) findViewById(R.id.cell_contact_text);

//        头像
        int resId = ta.getResourceId(R.styleable.ContactCell_imgSrc, -1);
        if (resId != -1) {

            imageView.setImageResource(resId);
        }
//        名字
        String text = ta.getString(R.styleable.ContactCell_textby);
        textView.setText(text);

        ta.recycle();
    }

    /**
     * 点击事件接口
     */
    public interface OnContactCellClickListener {
        void onImageClick();
    }

    /**
     * 点击事件的回调
     * @param listener
     */
    public void setOnContactCellClickListener(final OnContactCellClickListener listener) {
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onImageClick();
            }
        });
    }

}
