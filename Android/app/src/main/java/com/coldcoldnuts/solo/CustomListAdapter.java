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
public class CustomListAdapter extends BaseAdapter {
    private ArrayList<NewsItem> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapter(Context aContext, ArrayList<NewsItem> listData) {
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
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.headlineView = (TextView) convertView.findViewById(R.id.title);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.reporter);
            holder.reportedChatroomBtn = (TextView) convertView.findViewById(R.id.chatroom_btn);
            holder.date = (TextView) convertView.findViewById(R.id.date);

            ImageView background = (ImageView) convertView.findViewById(R.id.imageView);
            setRandomBackground(background);

            ImageView profile = (ImageView) convertView.findViewById(R.id.profile_picture);
            Resources res = context.getResources();

            int resourceIdMale = res.getIdentifier(
                    "avatar_male", "drawable", context.getPackageName());
            int resourceIdFemale = res.getIdentifier(
                    "avatar_female", "drawable", context.getPackageName() );
            if (listData.get(position).getReporterName().equals("Jack Ong")) {
                profile.setImageResource(resourceIdMale);
            } else {
                profile.setImageResource(resourceIdFemale);
            }

            holder.reportedChatroomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("message", holder.headlineView.getText());
                    intent.putExtra("time", holder.date.getText());
                    intent.putExtra("name", holder.reporterNameView.getText());
                    context.startActivity(intent);
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.headlineView.setText(listData.get(position).getHeadline());
        holder.reporterNameView.setText("By, " + listData.get(position).getReporterName());
        holder.date.setText(listData.get(position).getDate());
        return convertView;
    }

    public static void setRandomBackground(ImageView bg) {
        int rdm = (int) Math.round(Math.random() * 5.0f);
        switch (rdm) {
            case 0:
                bg.setBackgroundResource(R.drawable.cafe);
                break;
            case 1:
                bg.setBackgroundResource(R.drawable.golden_gate);
                break;
            case 2:
                bg.setBackgroundResource(R.drawable.palace_of_finearts);
                break;
            case 3:
                bg.setBackgroundResource(R.drawable.ferry_building);
                break;
            case 4:
                bg.setBackgroundResource(R.drawable.houses);
                break;
            case 5:
                bg.setBackgroundResource(R.drawable.chinatown);
                break;
        }
    }

    static class ViewHolder {
        TextView headlineView;
        TextView reporterNameView;
        TextView reportedChatroomBtn;
        TextView date;
    }
}