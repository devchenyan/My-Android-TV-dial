package cn.caratel.voip.cells;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.caratech.voip.R;
import cn.caratel.voip.cells.model.ContactModel;

/**
 * Created by chenyan on 7/1/16.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsHolder> {

    private List<ContactModel> mContacts;
    private LayoutInflater mLayoutInflater;

    private OnItemClickLitener mOnItemClickLitener;

    public ContactsAdapter(Context context, List<ContactModel> contactModels) {
        mContacts = contactModels;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ContactsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.list_item_contact, parent, false);
        ContactsHolder holder = new ContactsHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ContactsHolder holder, int position) {

        if (position == 0) {
            holder.mTextView.setText("添加联系人");
            holder.mImageView.setImageResource(R.drawable.add);

            //TODO: 头像的设置
        }
        else {
            ContactModel contactModel = mContacts.get(position);
            holder.bindContact(contactModel);
        }

        /**
         * 如果 设置了监听,则 设置点击事件
         */
        if (mOnItemClickLitener != null) {
            /**
             * 判断holder.itemView是否已经设置过click监听事件
             * 防止重复创建监听事件对象
             */
            if (!holder.itemView.hasOnClickListeners()){
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(holder.itemView, pos);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    /**
     * ViewHolder 类
     */
    class ContactsHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ImageView mImageView;

        private ContactModel mContact;

        public ContactsHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.list_item_contact_img);
            mTextView = (TextView) itemView.findViewById(R.id.list_item_contact_text);
        }

        /**
         * 绑定 ViewHolder对应的itemView的 数据model
         * @param contactModel   数据model
         */
        public void bindContact(ContactModel contactModel) {
            mContact = contactModel;
            mTextView.setText(mContact.getName());
            mImageView.setImageResource(R.drawable.photo_head);
        }
    }

    /**
     * 处理item的点击事件
     */
    public interface OnItemClickLitener {
        public void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickLitener onItemClickListener) {
        this.mOnItemClickLitener = onItemClickListener;
    }

}
