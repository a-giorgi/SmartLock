package it.hci2020.smartlock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InfoFragment extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.info_fragment_layout, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","edoardo.cagnes@stud.unifi.it", null));
                intent.putExtra(Intent.EXTRA_CC, new String[] { "andrea.giorgi2@stud.unifi.it" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "Smart Lock");
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });

        return view;
    }
}
