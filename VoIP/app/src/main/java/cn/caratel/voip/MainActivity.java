package cn.caratel.voip;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import cn.caratech.voip.R;

/**
 * Created by chenyan on 6/28/16.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private DialFragment dialFragment;

    private ContactsFragment contactsFragment;

    private TextView dialText;

    private TextView contactsText;

//    我的视讯号
    private TextView myNumberText;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initViews();

        fragmentManager = getFragmentManager();
        setTabSelection(0);
    }

    /**
     * 获取控件实例,并设置点击事件
     */
    private void initViews() {
        dialText = (TextView) findViewById(R.id.dial_text);
        contactsText = (TextView) findViewById(R.id.contacts_text);

        dialText.setOnClickListener(this);
        contactsText.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dial_text:
                setTabSelection(0);
                break;
            case R.id.contacts_text:
                setTabSelection(1);
                break;
            default:
                break;
        }
    }

    /**
     * 根据传入的index参数,设置选中的tab页
     *
     * @param index tab页对应下标: 0-拨号, 1-联系人, 2-设置(未)
     */
    private void setTabSelection(int index) {

        clearSelection();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);

        switch (index) {
            case 0:
                dialText.setTextColor(Color.parseColor("#fffefe"));
                if (dialFragment == null) {
                    dialFragment = new DialFragment();
                    transaction.add(R.id.content, dialFragment);
                } else {
                    transaction.show(dialFragment);
                }
                break;
            case 1:
                contactsText.setTextColor(Color.parseColor("#fffefe"));
                if (contactsFragment == null) {
                    contactsFragment = new ContactsFragment();
                    transaction.add(R.id.content, contactsFragment);
                } else {
                    transaction.show(contactsFragment);
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }

    /**
     * 清除所有选中状态
     */
    private void clearSelection() {
        dialText.setTextColor(Color.parseColor("#82858b"));
        contactsText.setTextColor(Color.parseColor("#82858b"));
    }

    /**
     * 将所有Fragment都设置为隐藏状态
     *
     * @param transaction   用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (dialFragment != null) {
            transaction.hide(dialFragment);
        }
        if (contactsFragment != null) {
            transaction.hide(contactsFragment);
        }
    }

    //TODO: 视讯号的 设置
}
