package com.coldcoldnuts.solo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ContentFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CustomListAdapter mAdapter;
    private ArrayList<NewsItem> mMessages;
    private String mUsername =  Utils.getUsername();
    private String mRoomName;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMessages = new ArrayList<NewsItem>();
        mRoomName = Constants.MAIN_ROOM;
        newData = new JSONObject();
        try {
            newData.put("username", mUsername);
            newData.put("room", mRoomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Log.v("mainFragment", "create");

    }

    @Override
    public void onStart() {
        super.onStart();

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("send room message", onNewMessage);
        mSocket.on("joined room", onJoinRoom);
        mSocket.on("left room", onLeftRoom);
        mSocket.connect();

        // join the room "main_room" in the socket
        mSocket.emit("join", newData);

        Log.v("mainFragment", "start");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        final SwipeRefreshLayout refreshView = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        refreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        refreshView.setRefreshing(false);
                    }
                }, 1000);
                }
        });

        final ListView lv1 = (ListView) refreshView.findViewById(R.id.custom_list);
        mAdapter = new CustomListAdapter(getContext(), mMessages);
        lv1.setAdapter(mAdapter);

        FloatingActionButton sendNewPost = (FloatingActionButton) view.findViewById(R.id.newpost_send);
        final EditText newPost = (EditText) view.findViewById(R.id.newpost_text);
        sendNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPostText = newPost.getText().toString();
                if (newPostText.equals("")) {
                    return;
                }
                JSONObject confirmPost = new JSONObject();
                try {
                    confirmPost.put("username", mUsername);
                    confirmPost.put("room", mRoomName);
                    confirmPost.put("message", newPostText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("room message", confirmPost);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(newPost.getWindowToken(), 0);
                newPost.setText("");
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMessages = new ArrayList<NewsItem>();
        mSocket.emit("leave", newData);
        Log.v("test onPause main", newData.toString());
        mSocket.emit("disconnect request");
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("send room message", onNewMessage);
        mSocket.off("joined room", onJoinRoom);
        mSocket.off("left room", onLeftRoom);

        Log.v("mainFragment", "paused");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v("mainFragment", "stop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("mainFragment", "destroyed");

    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Constants.addMsg(data, mMessages, mAdapter, mRoomName);
                }
            });
        }
    };

    private Emitter.Listener onJoinRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Constants.populate(data, mMessages, mAdapter, mRoomName, mUsername);
                }
            });
        }
    };

    private Emitter.Listener onLeftRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
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
