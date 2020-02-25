package it.hci2020.smartlock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class StatusFragment extends Fragment
{
    static MainActivity mainActivity;

    ImageView led;
    GifImageView padlock;
    TextView lockStatus;
    SharedPreferences prefs;
    String myPrefs = "SmartLock";
    static Switch notificationsSwitch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Activity activity = getActivity();
        if(activity instanceof MainActivity)
        {
            mainActivity = (MainActivity) activity;
        }

        View view = inflater.inflate(R.layout.status_fragment_layout, container, false);
        try
        {
            Button updateStatusButton = (Button) view.findViewById(R.id.update_status);
            notificationsSwitch = (Switch) view.findViewById(R.id.notifications);
            led = (ImageView) view.findViewById(R.id.led);
            padlock = (GifImageView) view.findViewById(R.id.padlock);
            lockStatus = (TextView) view.findViewById(R.id.lockStatus);
            led.setImageResource(R.drawable.gray_led);
            final GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.device_offline);
            gifDrawable.setLoopCount(1);
            padlock.setImageDrawable(gifDrawable);
            lockStatus.setText("Device is offline");

            prefs = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
            boolean notificationToggle = prefs.getBoolean("toggle", false);

            if ((MainActivity.isDeviceSelected) && (notificationToggle))
            {
                notificationsSwitch.setChecked(notificationToggle);
                mainActivity.notifications("S");
            }
            else if (!notificationToggle)
            {
                notificationsSwitch.setChecked(notificationToggle);
                mainActivity.notifications("N");
            }

            updateStatusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!mainActivity.isConnectedToNetwork())
                    {
                        return;
                    }
                    mainActivity.checkStatus();
                }
            });

            notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton switchView, boolean isChecked) {

                    if (!mainActivity.isConnectedToNetwork())
                    {
                        notificationsSwitch.setChecked(false);
                        return;
                    }
                    if (!MainActivity.isDeviceSelected)
                    {
                        showToast("Please select a device");
                        notificationsSwitch.setChecked(false);
                        if (MainActivity.viewPager.getCurrentItem() != 0)
                        {
                            MainActivity.viewPager.setCurrentItem(0);
                        }
                        return;
                    }
                    if(isChecked) {
                        mainActivity.notifications("S");
                        prefs = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("toggle", true);
                        editor.apply();
                    }
                    else
                    {
                        mainActivity.notifications("N");
                        prefs = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("toggle", false);
                        editor.apply();
                    }
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        mainActivity.statusFragment = this;

        return view;
    }

    public static void switchOff()
    {
       notificationsSwitch.setChecked(false);
        mainActivity.notifications("N");
    }

    public void updateFragment(String pStatusResult)
    {
        //try {
        if (!MainActivity.isDeviceSelected)
        {
            showToast("Please select a device");
            if (MainActivity.viewPager.getCurrentItem() != 0)
            {
                MainActivity.viewPager.setCurrentItem(0);
            }
            return;
        }

        if (pStatusResult.equals("OPEN"))
        {
            setLed(R.drawable.green_led, "Lock is: open", R.drawable.padlock_open);
            showToast("Status updated");
        }
        else if (pStatusResult.equals("LOCKED"))
        {
            setLed(R.drawable.red_led, "Lock is: closed", R.drawable.padlock_closed);
            showToast("Status updated");
        }
        else if ((pStatusResult.equals("OFFLINE")) || (pStatusResult.equals("ERROR")) || (pStatusResult.equals("NODEVICE")))
        {
            setLed(R.drawable.gray_led, "Device is offline", R.drawable.device_offline);
            if (pStatusResult.equals("ERROR"))
            {
                showToast("Server error!\nTry again later");
            }
            else if (pStatusResult.equals("NODEVICE"))
            {
                showToast("Unknown device!");
            }
            else
            {
                showToast("Status updated");
            }
        }
    }

    private void setLed(int ledIcon, String deviceStatus, int padlockStatus)
    {
        try
        {
            led.setImageResource(ledIcon);
            lockStatus.setText(deviceStatus);
            GifDrawable gifDrawable = new GifDrawable(getResources(), padlockStatus);
            gifDrawable.setLoopCount(1);
            padlock.setImageDrawable(gifDrawable);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void showToast (String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
