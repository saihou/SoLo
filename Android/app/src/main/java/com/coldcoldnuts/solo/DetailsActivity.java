package com.coldcoldnuts.solo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class DetailsActivity extends AppCompatActivity {

    // TODO: edit these variables and get the value from Intent
    private String mInitiator = "from_main"; // name of the user who posted the question
    private String mTime = "the timestamp"; // the timestamp at which the question was created
    private String mQuestion = "the question asked from main_room"; // the actual question asked
    private String mRoomName; // the room for this question

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
        setContentView(R.layout.activity_details);

        mInitiator = getIntent().getStringExtra("name");
        mQuestion = getIntent().getStringExtra("message");
        mTime = getIntent().getStringExtra("time");

        mMessages = new ArrayList<NewsItem>();

        // TODO: get all the variables from Intent and set the variables above

        // Gives the room a name
        mRoomName = mInitiator + mTime;

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("send room message", onNewMessage);
        mSocket.on("joined room", onJoinRoom);
        mSocket.on("left room", onLeftRoom);
        mSocket.connect();

        // Append the Question to mMessages
        NewsItem question = new NewsItem();
        question.setHeadline(mQuestion);
        question.setReporterName(mInitiator);
        mMessages.add(0, question);

        // join the Initiator's Question room in the socket
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
                mMessages.add(0, newsData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // TODO: implement Adapter
        //mAdapter.notifyDataSetChanged();
    }

    private void addMsg(JSONObject newMsg) {
        NewsItem newsData = new NewsItem();
        try {
            newsData.setHeadline(newMsg.getString("message"));
            newsData.setReporterName(newMsg.getString("username"));
            newsData.setDate(newMsg.getString("time"));
            mMessages.add(0, newsData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // TODO: implement Adapter
        //mAdapter.notifyDataSetChanged();
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
