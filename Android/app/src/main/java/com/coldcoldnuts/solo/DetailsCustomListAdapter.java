package com.coldcoldnuts.solo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
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
            holder.detailProfile = (ImageView) convertView.findViewById(R.id.detail_profile);

            ImageView background = (ImageView) convertView.findViewById(R.id.details_imageView);
            CustomListAdapter.setRandomBackground(background, position);

            holder.reportedChatroomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("message", holder.messageView.getText());
                    intent.putExtra("reporter_name", holder.reporterNameView.getText().toString().substring(4));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Resources res = context.getResources();

        holder.reportedChatroomBtn.setVisibility(View.VISIBLE);

        int resourceIdMale = res.getIdentifier(
                "avatar_male", "drawable", context.getPackageName());
        int resourceIdFemale = res.getIdentifier(
                "avatar_female", "drawable", context.getPackageName() );
        String reporter = listData.get(position).getReporterName();
        if (reporter.equals("Jack Ong")) {
            holder.detailProfile.setImageResource(resourceIdMale);
        } else {
            holder.detailProfile.setImageResource(resourceIdFemale);
        }
        if (reporter.contains(Utils.getUsername())) {
            holder.reportedChatroomBtn.setVisibility(View.INVISIBLE);
        } else {
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
        ImageView detailProfile;
    }
}