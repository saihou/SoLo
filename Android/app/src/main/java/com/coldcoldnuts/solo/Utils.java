package com.coldcoldnuts.solo;

import android.net.Uri;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;

import org.json.JSONObject;

/**
 * Created by saihou on 1/16/16.
 *
 * Utility class to provide basic functions such as getting username
 */
public class Utils {

    public static String username = "no username yet";
    public static Uri picture = null;
    public static boolean isConnected = true; //current state of the network,  defaults to true

    public static boolean isFacebookLoggedIn() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return token != null;
    }

    public static String getUsername() {
        return username;
    }

    public static Uri getPicture() {
        return picture;
    }

    public static boolean setGuestDetails(String name) {
        username = name;
        return true;
    }
    public static boolean setFacebookDetails() {
        Profile.fetchProfileForCurrentAccessToken();
        // try to get from Profile
        if (Profile.getCurrentProfile() != null) {
            username = Profile.getCurrentProfile().getFirstName();
            picture = Profile.getCurrentProfile().getProfilePictureUri(200, 200);
        } else {
            //if Profile has not yet been updated, then
            //manually send a request to Graph API to get the name
            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            username = object.optString("name");
                            picture = (Uri) object.opt("picture");
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,picture");
            request.setParameters(parameters);
            request.executeAsync();
        }
        return true;
    }
}
