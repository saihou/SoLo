package com.coldcoldnuts.solo;

import android.util.Log;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by JackOng on 1/16/16.
 */
public class Constants {
    public static final String CHAT_SERVER_URL = "http://192.168.1.96:5000/solo";
    public static final String MAIN_ROOM = "main_room";
    public static void testing() {
        Log.v("testing", "haha");
    }

    // helper function to populate view with history
    public static void populate(JSONObject data, ArrayList<NewsItem> mMessages, BaseAdapter mAdapter, String mRoomName, String mUsername) {
        String username;
        String room;
        JSONArray msgHistory;
        try {
            username = data.getString("username");
            room = data.getString("room");
            msgHistory = data.getJSONArray("messages");
        } catch (JSONException e) {
            return;
        }
        Log.v("test onJoinRoom", data.toString());

        if (!username.equals(mUsername)) {
            return;
        }

        int arrSize = msgHistory.length();
        for (int i = 0; i < arrSize; i++) {
            try {
                JSONObject post = msgHistory.getJSONObject(i);
                String message = post.getString("message");
                String user = post.getString("username");
                String currTime = post.getString("time");
                NewsItem newsData = new NewsItem();
                newsData.setHeadline(message);
                newsData.setReporterName(user);
                newsData.setDate(currTime);
                if (mRoomName.equals(MAIN_ROOM)) {
                    mMessages.add(0, newsData);
                } else {
                    mMessages.add(newsData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    // helper function to populate view with new message
    public static void addMsg(JSONObject data, ArrayList<NewsItem> mMessages, BaseAdapter mAdapter, String mRoomName) {
        String username;
        String message;
        String room;
        String currTime;
        try {
            username = data.getString("username");
            message = data.getString("message");
            room = data.getString("room");
            currTime = data.getString("time");
        } catch (JSONException e) {
            return;
        }

        if (!room.equals(mRoomName)) {
            Log.e("DetailsActivity", "Wrong Room!!");
            return;
        }

        JSONObject newMsg = new JSONObject();
        try {
            newMsg.put("username", username);
            newMsg.put("message", message);
            newMsg.put("time", currTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("test onNewMessage", newMsg.toString());

        NewsItem newsData = new NewsItem();
        try {
            newsData.setHeadline(newMsg.getString("message"));
            newsData.setReporterName(newMsg.getString("username"));
            newsData.setDate(newMsg.getString("time"));
            if (mRoomName.equals(MAIN_ROOM)) {
                mMessages.add(0, newsData);
            } else {
                mMessages.add(newsData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
    }
}
