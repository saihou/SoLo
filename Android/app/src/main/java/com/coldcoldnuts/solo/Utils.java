package com.coldcoldnuts.solo;

import android.net.Uri;

import com.facebook.AccessToken;
import com.facebook.Profile;

/**
 * Created by saihou on 1/16/16.
 *
 * Utility class to provide basic functions such as getting username
 */
public class Utils {

    public static String username = "no username yet";
    public static String picture = "no picture yet";

    public static boolean isFacebookLoggedIn() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return token != null;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPicture() {
        return picture;
    }

    public static boolean setGuestDetails(String name) {
        username = name;
        return true;
    }
    public static boolean setFacebookDetails() {
        username = Profile.getCurrentProfile().getFirstName();
        Uri pictureUri = Profile.getCurrentProfile().getProfilePictureUri(200,200);
        return true;
    }
}
