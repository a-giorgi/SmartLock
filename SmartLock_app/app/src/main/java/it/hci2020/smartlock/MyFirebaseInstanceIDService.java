package it.hci2020.smartlock;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Objects;

@SuppressWarnings("ALL")
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{

    private static SharedPreferences prefs;

    private static final String myPrefs = "SmartLock";

    private static final String token = "token";

    @Override
    public void onTokenRefresh() {

        if (FirebaseInstanceId.getInstance().getToken() != null)
        {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();

            if (!(Objects.requireNonNull(refreshedToken).isEmpty()))
            {
                prefs = this.getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(token, refreshedToken);
                editor.apply();
            }
        }
    }
}



