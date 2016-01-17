package com.coldcoldnuts.solo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Huiwen on 16/1/16.
 */
public class ChatCustomListAdapter extends DetailsCustomListAdapter {
    private ArrayList<NewsItem> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public ChatCustomListAdapter(Context aContext, ArrayList<NewsItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
        context = aContext;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.chat_single_message, null);

            holder = new ChatViewHolder();
            holder.messageView = (TextView) convertView.findViewById(R.id.message_text);
            holder.nameView = (TextView) convertView.findViewById(R.id.details_reporter);

            convertView.setTag(holder);
        } else {
            holder = (ChatViewHolder) convertView.getTag();
        }

        holder.messageView.setText(listData.get(position).getHeadline());
        holder.nameView.setText("By, " + listData.get(position).getReporterName());
        return convertView;
    }

    static class ChatViewHolder {
        TextView messageView;
        TextView nameView;
        TextView dateView;
    }
}