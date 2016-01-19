package com.coldcoldnuts.solo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class DetailsActivity extends AppCompatActivity {

    private DetailsCustomListAdapter mAdapter;

    
    private String mInitiator; // name of the user who posted the question
    private String mTime; // the timestamp at which the question was created
    private String mQuestion; // the actual question asked
    private String mRoomName; // the room for this question

    private String mUsername = Utils.getUsername();
    private ArrayList<NewsItem> mMessagesDetail;
    private JSONObject newData;
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

        getSupportActionBar().setTitle("Topic");

        // Get all the variables from Intent and set the variables above
        mInitiator = getIntent().getStringExtra("name");
        mQuestion = getIntent().getStringExtra("message");
        mTime = getIntent().getStringExtra("time");

        mMessagesDetail = new ArrayList<NewsItem>();

        ListView lv = (ListView) findViewById(R.id.details_custom_list);
        mAdapter = new DetailsCustomListAdapter(getApplicationContext(), mMessagesDetail);
        lv.setAdapter(mAdapter);

        // Gives the room a name
        mRoomName = mInitiator + mTime;

        newData = new JSONObject();
        try {
            newData.put("username", mUsername);
            newData.put("room", mRoomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Topic Question, always on top. Populate it first
        NewsItem question = new NewsItem();
        question.setHeadline(mQuestion);
        question.setReporterName(mInitiator);
        View topic = (View) findViewById(R.id.topic);
        TextView topicQuestion = (TextView) topic.findViewById(R.id.title);
        TextView topicChatBtn = (TextView) topic.findViewById(R.id.chatroom_btn);
        topicQuestion.setText(mQuestion);
        TextView asker = (TextView) topic.findViewById(R.id.reporter);
        if (mUsername.contains(mInitiator)) {
            topicChatBtn.setVisibility(View.INVISIBLE);
        } else {
            topicChatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("message", mQuestion);
                    intent.putExtra("reporter_name", mInitiator);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            });
        }
        asker.setText(mInitiator);

        ImageView profile = (ImageView) topic.findViewById(R.id.profile_picture);
        Resources res = getResources();

        int resourceIdMale = res.getIdentifier(
                "avatar_male", "drawable", getPackageName());
        int resourceIdFemale = res.getIdentifier(
                "avatar_female", "drawable", getPackageName() );
        Log.v("checkview", mInitiator);
        if (mInitiator.equals("Jack Ong")) {
            profile.setImageResource(resourceIdMale);
        } else {
            profile.setImageResource(resourceIdFemale);
        }

        final TextView post_reply = (TextView) findViewById(R.id.post_reply);
        final ImageButton sendReply = (ImageButton) findViewById(R.id.send_reply);
        sendReply.setOnClickListener(new View.OnClickListener() {
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
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(post_reply.getWindowToken(), 0);
                    post_reply.setText("");

                    // if network is not connected i.e. server down
                    if (!Utils.isConnected) {
                        mMessagesDetail.add(0, ContentFragment.makeDummyData(reply, mUsername, "0000011111"));
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("send room message", onNewMessage);
        mSocket.on("joined room", onJoinRoom);
        mSocket.on("left room", onLeftRoom);
        mSocket.connect();
        // join the Initiator's Question room in the socket
        mSocket.emit("join", newData);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // clean up mMessagesDetail
        mMessagesDetail = new ArrayList<NewsItem>();

        // Leave the room
        mSocket.emit("leave", newData);
        Log.v("test onPause detail", newData.toString());

        mSocket.emit("disconnect request");
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("send room message", onNewMessage);
        mSocket.off("joined room", onJoinRoom);
        mSocket.off("left room", onLeftRoom);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // disconnect and drop all subscription
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
                    Constants.addMsg(data, mMessagesDetail, mAdapter, mRoomName);
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
                    Constants.populate(data, mMessagesDetail, mAdapter, mRoomName, mUsername);
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
