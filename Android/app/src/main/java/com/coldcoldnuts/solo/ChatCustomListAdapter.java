package com.coldcoldnuts.solo;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Huiwen on 16/1/16.
 */
public class ChatCustomListAdapter extends BaseAdapter {
    private ArrayList<NewsItem> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public ChatCustomListAdapter(Context aContext, ArrayList<NewsItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
        context = aContext;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.chat_single_message, null);

            holder = new ChatViewHolder();
            holder.messageView = (TextView) convertView.findViewById(R.id.message_text);
            holder.nameView = (TextView) convertView.findViewById(R.id.chat_name);

        } else {
            holder = (ChatViewHolder) convertView.getTag();
        }

        holder.messageView.setText(listData.get(position).getHeadline());
        holder.nameView.setText(listData.get(position).getReporterName());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
        if (isMine(holder.nameView.getText().toString().trim())) {
            params.gravity = Gravity.RIGHT;
            holder.messageView.setBackgroundResource(R.drawable.speech_bubble_from_me);
        } else {
            params.gravity = Gravity.LEFT;
            holder.messageView.setBackgroundResource(R.drawable.speech_bubble_from_others);
        }
        holder.messageView.setLayoutParams(params);

        convertView.setTag(holder);
        return convertView;
    }

    private boolean isMine(String username) {
        return Utils.getUsername().equals(username);
    }

    static class ChatViewHolder {
        TextView messageView;
        TextView nameView;
        TextView dateView;
    }
}