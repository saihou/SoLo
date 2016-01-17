package com.coldcoldnuts.solo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private OnFragmentInteractionListener mListener;

    public ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContentFragment newInstance(String param1, String param2) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMessages = new ArrayList<NewsItem>();
        mRoomName = Constants.MAIN_ROOM;

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("send room message", onNewMessage);
        mSocket.on("joined room", onJoinRoom);
        mSocket.on("left room", onLeftRoom);
        mSocket.connect();

        // join the room "main_room" in the socket
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        final ListView lv1 = (ListView) view.findViewById(R.id.custom_list);
        mAdapter = new CustomListAdapter(getContext(), mMessages);
        lv1.setAdapter(mAdapter);

        TextView sendNewPost = (TextView) view.findViewById(R.id.newpost_send);
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
    public void onDestroy() {
        super.onDestroy();

        mMessages = new ArrayList<NewsItem>();
        JSONObject newData = new JSONObject();
        try {
            newData.put("username", mUsername);
            newData.put("room", mRoomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("leave", newData);
        Log.v("test onDestroy", newData.toString());
        mSocket.emit("disconnect request");
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("send room message", onNewMessage);
        mSocket.off("joined room", onJoinRoom);
        mSocket.off("left room", onLeftRoom);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // helper function to populate view with history
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
        mAdapter.notifyDataSetChanged();
    }

    // helper function to populate view with new message
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
        mAdapter.notifyDataSetChanged();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
            getActivity().runOnUiThread(new Runnable() {
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
