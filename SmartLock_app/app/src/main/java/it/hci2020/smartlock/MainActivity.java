package it.hci2020.smartlock;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import it.hci2020.smartlock.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity
{
    private static final String mainUrl = "https://www.brigataotaku.it/";
    private static final String monitorUrl = mainUrl+"hci2020/monitor.php";
    private static final String notificationUrl = mainUrl+"hci2020/notification.php";

    public static boolean isDeviceSelected = false;

    public static String deviceSelected = "";

    public static ViewPager viewPager;

    public static StatusFragment statusFragment;

    SharedPreferences prefs;

    private static final String myPrefs = "SmartLock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
        {
            // Stop here, we definitely need NFC
            showToast("This device doesn't support NFC");
            finish();
        }
        readFromIntent(getIntent());
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);

        isConnectedToNetwork();

        String menuFragment = getIntent().getStringExtra("menuFragment");
        if (menuFragment != null && isConnectedToNetwork())
        {
            if (menuFragment.equals("statusFragment"))
            {
                viewPager.setCurrentItem(1);
                this.checkStatus();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        isConnectedToNetwork();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private boolean readFromIntent(Intent intent) {

        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            writeDevice(buildTagViews(msgs));
            Log.println(Log.INFO,"NFC",buildTagViews(msgs));
            return true;
        }
        else
        {
            return false;
        }
    }

    private String buildTagViews(NdefMessage[] msgs)
    {
        if (msgs == null || msgs.length == 0)
        {
            return null;
        }
        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"

        try
        {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("UnsupportedEncoding", e.toString());
        }

       return text;
    }

    public void writeDevice(String deviceSerialCode) {

        try
        {
            if ((!deviceSerialCode.equals("")))
            {
                prefs = MainActivity.this.getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
                String savedSerial = prefs.getString(deviceSerialCode, "");
                Log.e("NFC",savedSerial);
                if(savedSerial.equals(""))
                {
                    SharedPreferences.Editor editorSharedPreferencesOrders = prefs.edit();
                    editorSharedPreferencesOrders.putString(deviceSerialCode, "1");
                    editorSharedPreferencesOrders.commit();
                    showToast("Device successfully loaded!");
                }
                else
                {
                    showToast("Device already loaded!");
                }
            }
            else
            {
                throw new Exception("not_all_parameters_are_set");
            }
        }
        catch (Exception e)
        {
            //do nothing
        }
    }

    private class AsynchTaskStatus extends AsyncTask<Void,Void,String>
    {
        @Override
        protected String doInBackground(Void... params)
        {
            String path = monitorUrl;
            HttpURLConnection urlConnection = null;
            BufferedReader reader=null;

            try
            {
                URL url = new URL(path + "?code=" + deviceSelected);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) !=null)
                {
                    buffer.append(line);
                }

                return buffer.toString();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return "exception";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            if (viewPager.getCurrentItem() != 1)
            {
                viewPager.setCurrentItem(1);
            }
            statusFragment.updateFragment(result);
        }
    }

    public void checkStatus()
    {
        AsynchTaskStatus statusUpdater = new AsynchTaskStatus();
        statusUpdater.execute();
    }

    public boolean isConnectedToNetwork()
    {
        ConnectivityManager myConnectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo myNetworkInfo = myConnectivity.getActiveNetworkInfo();
        if (myNetworkInfo != null && myNetworkInfo.isAvailable() && myNetworkInfo.isConnected())
        {
            return true;
        }
        showToast("Your smartphone is offline.\nPlease enable your internet connection");
        return false;
    }

    private class AsynchTaskNotifications extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            String path = notificationUrl;

            HttpURLConnection urlConnection = null;
            BufferedReader reader=null;

            try
            {
                URL url = new URL(path + "?device=" + deviceSelected + "&token=" + params[1] + "&toggle=" + params[2]);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) !=null)
                {
                    buffer.append(line);
                }

                return buffer.toString();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return "exception";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
        }

        private String getPostData(JSONObject params) throws Exception {

            StringBuilder sb = new StringBuilder();

            Iterator<String> itr = params.keys();

            while (itr.hasNext())
            {
                String key = itr.next();

                Object value = params.get(key);

                sb.append(URLEncoder.encode(key, "UTF-8"));
                sb.append("=");
                sb.append(URLEncoder.encode(value.toString(), "UTF-8"));
            }
            return sb.toString();
        }
    }

    public void notifications(String enabled)
    {
        prefs = MainActivity.this.getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        AsynchTaskNotifications notificationsUpdater = new AsynchTaskNotifications();
        notificationsUpdater.execute(deviceSelected, prefs.getString("token", ""), enabled);
    }

    private void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}