package com.coldcoldnuts.solo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {

    private ChatCustomListAdapter mAdapter;

    // TODO: edit these variables and get the value from Intent
    private String mOtherUser = "from_detail"; // name of the other user of this private chat
    private String mRoomName; // the room for this private chat

    // TODO: change mUsername to facebook user name
    private String mUsername = Utils.getUsername();
    private ArrayList<NewsItem> mMessages;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().hide();

        mUsername = Utils.getUsername();
        mOtherUser = getIntent().getStringExtra("reporter_name");
        String topic = getIntent().getStringExtra("message");
        TextView intro = (TextView) findViewById(R.id.intro_to_chat);
        intro.setText("Welcome to the private chat! Your last topic is \"" + topic + "\".");
        TextView title = (TextView) findViewById(R.id.chat_other_person);
        title.setText(mOtherUser);

        mMessages = new ArrayList<NewsItem>();

        ListView lv = (ListView) findViewById(R.id.custom_list_chat);
        mAdapter = new ChatCustomListAdapter(getApplicationContext(), mMessages);
        lv.setAdapter(mAdapter);

        // Gives the room a name
        // To ensure the room name between 2 users is always the same
        // the name that is lexicographically greater is used first
        int compareNames = mUsername.compareTo(mOtherUser);
        if (compareNames > 0) {
            mRoomName = mUsername + "_" + mOtherUser;
        } else {
            mRoomName = mOtherUser + "_" + mUsername;
        }

        final EditText post_reply = (EditText) findViewById(R.id.chat_text);
        FloatingActionButton sendBtn = (FloatingActionButton) findViewById(R.id.chat_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reply = post_reply.getText().toString();
                if (reply.equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter a longer reply! Do not spam short replies", Toast.LENGTH_LONG);
                } else {
                    JSONObject confirmPost = new JSONObject();
                    try {
                        confirmPost.put("username", mUsername);
                        confirmPost.put("room", mRoomName);
                        confirmPost.put("message", reply);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("room message", confirmPost);
                    post_reply.setText("");
                }
            }
        });

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("send room message", onNewMessage);
        mSocket.on("joined room", onJoinRoom);
        mSocket.on("left room", onLeftRoom);
        mSocket.connect();

        // join the private chat room in the socket
        JSONObject newData = new JSONObject();
        try {
            newData.put("username", mUsername);
            newData.put("room", mRoomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("join", newData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // clean up mMessages
        mMessages = new ArrayList<NewsItem>();

        // Leave the room
        JSONObject newData = new JSONObject();
        try {
            newData.put("username", mUsername);
            newData.put("room", mRoomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("leave", newData);
        Log.v("test onDestroy", newData.toString());

        // disconnect and drop all subscription
        mSocket.emit("disconnect request");
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("send room message", onNewMessage);
        mSocket.off("joined room", onJoinRoom);
        mSocket.off("left room", onLeftRoom);
    }

    private void populate(JSONArray msgHistory) {
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
                mMessages.add(newsData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // TODO: implement Adapter
        mAdapter.notifyDataSetChanged();
    }

    private void addMsg(JSONObject newMsg) {
        NewsItem newsData = new NewsItem();
        try {
            newsData.setHeadline(newMsg.getString("message"));
            newsData.setReporterName(newMsg.getString("username"));
            newsData.setDate(newMsg.getString("time"));
            mMessages.add(newsData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // TODO: implement Adapter
        mAdapter.notifyDataSetChanged();
    }






    // The various Listener functions

    // Handler of connection error, i.e. server not available
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    //handler of new messages
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
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
                    addMsg(newMsg);
                }
            });
        }
    };

    private Emitter.Listener onJoinRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
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
                    Log.v("test onJoinRoom", msgHistory.toString());
                    if (username.equals(mUsername)) {
                        populate(msgHistory);
                    }
                }
            });
        }
    };

    private Emitter.Listener onLeftRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String room;
                    try {
                        username = data.getString("username");
                        room = data.getString("room");
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };
}
