package it.hci2020.smartlock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class DevicesFragment extends Fragment
{
    private ArrayList<String> devices;

    MainActivity mainActivity;

    SharedPreferences prefs;

    String myPrefs = "SmartLock";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        devices = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            if ((!entry.getKey().equals("token"))&&(!entry.getKey().equals("toggle"))&&(!entry.getKey().equals("item"))) {
                devices.add(entry.getKey());
            }
        }

        Activity activity = getActivity();
        if(activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        }

        MainActivity.isDeviceSelected = false;

        View view = inflater.inflate(R.layout.devices_fragment_layout, container, false);

        final ListView devicesList = (ListView) view.findViewById(R.id.devicesList);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.simple_list_item, devices);

        devicesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        devicesList.setAdapter(adapter);

        prefs = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        int checkedItem = prefs.getInt("item", -1);

        devicesList.setItemChecked(checkedItem, true);

        if (checkedItem < 0)
        {
            MainActivity.isDeviceSelected = false;
            MainActivity.deviceSelected = "";
        }
        else
        {
            MainActivity.isDeviceSelected = true;
            MainActivity.deviceSelected = devicesList.getItemAtPosition(checkedItem).toString().trim();
        }

        final TextView noDevices =  (TextView) view.findViewById(R.id.noDevices);
        final TextView selectDevice = (TextView) view.findViewById(R.id.selectDevice);

        final Button buttonCheckStatus = (Button) view.findViewById(R.id.checkStatus);
        final Button buttonRemoveDevice = (Button) view.findViewById(R.id.removeDevice);

        if(devicesList.getCount() < 1)
        {
            noDevices.setVisibility(View.VISIBLE);
            devicesList.setVisibility(View.GONE);
            selectDevice.setVisibility(View.GONE);
            buttonCheckStatus.setVisibility(View.GONE);
            buttonRemoveDevice.setVisibility(View.GONE);
        }
        else
        {
            noDevices.setVisibility(View.GONE);
            devicesList.setVisibility(View.VISIBLE);
            selectDevice.setVisibility(View.VISIBLE);
            buttonCheckStatus.setVisibility(View.VISIBLE);
            buttonRemoveDevice.setVisibility(View.VISIBLE);
        }

        buttonCheckStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mainActivity.isConnectedToNetwork())
                {
                    return;
                }
                if (devicesList.getCheckedItemCount() == 0)
                {
                    showToast("Please select a device");
                    MainActivity.isDeviceSelected = false;
                }
                else
                {
                    MainActivity.isDeviceSelected = true;
                    mainActivity.checkStatus();
                }
            }
        });

        buttonRemoveDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (devicesList.getCheckedItemPosition() < 0)
                {
                    showToast("Please select a device");
                    MainActivity.isDeviceSelected = false;
                }
                else
                {
                    prefs = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(devicesList.getItemAtPosition(devicesList.getCheckedItemPosition()).toString());
                    editor.putInt("item", -1);
                    editor.commit();
                    adapter.remove(adapter.getItem(devicesList.getCheckedItemPosition()));
                    devicesList.clearChoices();
                    adapter.notifyDataSetChanged();
                    MainActivity.isDeviceSelected = false;
                    StatusFragment.switchOff();
                    if(devicesList.getCount() < 1)
                    {
                        noDevices.setVisibility(View.VISIBLE);
                        devicesList.setVisibility(View.GONE);
                        selectDevice.setVisibility(View.GONE);
                        buttonCheckStatus.setVisibility(View.GONE);
                        buttonRemoveDevice.setVisibility(View.GONE);
                    }
                }
            }
        });

        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (devicesList.getCheckedItemCount() == 0)
                {
                    MainActivity.isDeviceSelected = false;
                }
                else
                {
                    MainActivity.deviceSelected = "";
                    MainActivity.isDeviceSelected = true;
                    MainActivity.deviceSelected = devicesList.getItemAtPosition(position).toString().trim();
                }
                prefs = getActivity().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("item", devicesList.getCheckedItemPosition());
                editor.apply();
            }
        });

        return view;
    }

    private void showToast (String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
