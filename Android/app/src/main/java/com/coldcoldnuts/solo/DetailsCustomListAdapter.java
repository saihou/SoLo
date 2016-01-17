package com.coldcoldnuts.solo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Huiwen on 16/1/16.
 */
public class DetailsCustomListAdapter extends BaseAdapter {
    private ArrayList<NewsItem> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public DetailsCustomListAdapter(Context aContext, ArrayList<NewsItem> listData) {
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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.details_list_row_layout, null);
            holder = new ViewHolder();
            holder.messageView = (TextView) convertView.findViewById(R.id.details_title);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.details_reporter);
            holder.reportedChatroomBtn = (TextView) convertView.findViewById(R.id.details_chatroom_btn);
            holder.date = (TextView) convertView.findViewById(R.id.details_date);

            ImageView profile = (ImageView) convertView.findViewById(R.id.profile_picture);
            ImageView detail_profile = (ImageView) convertView.findViewById(R.id.detail_profile);
            Resources res = context.getResources();

            int resourceIdMale = res.getIdentifier(
                    "avatar_male", "drawable", context.getPackageName());
            int resourceIdFemale = res.getIdentifier(
                    "avatar_female", "drawable", context.getPackageName() );
            if (listData.get(position).getReporterName().equals("Jack Ong")) {
                //profile.setImageResource(resourceIdMale);
                detail_profile.setImageResource(resourceIdMale);
            } else {
                profile.setImageResource(resourceIdFemale);
                detail_profile.setImageResource(resourceIdFemale);
            }

            holder.reportedChatroomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("message", holder.messageView.getText());
                    intent.putExtra("time", holder.date.getText());
                    intent.putExtra("name", holder.reporterNameView.getText());
                    context.startActivity(intent);
                }
            });
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.messageView.setText(listData.get(position).getHeadline());
        holder.reporterNameView.setText("By, " + listData.get(position).getReporterName());
        holder.date.setText(listData.get(position).getDate());
        return convertView;
    }

    static class ViewHolder {
        TextView messageView;
        TextView reporterNameView;
        TextView reportedChatroomBtn;
        TextView date;
    }
}