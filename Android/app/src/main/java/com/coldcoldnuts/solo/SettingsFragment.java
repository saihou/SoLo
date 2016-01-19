package com.coldcoldnuts.solo;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;


public class SettingsFragment extends Fragment {

    private TextView radiusLabel;
    private SeekBar radiusSlider;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        radiusLabel = (TextView) view.findViewById(R.id.labelRadius);
        radiusSlider = (SeekBar) view.findViewById(R.id.sliderRadius);

        radiusSlider.setMax(50);
        radiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float progressF = progress / 10f;

                Resources res = getResources();
                String newLabel = String.format(res.getString(R.string.radius_label), Float.toString(progressF));
                radiusLabel.setText(newLabel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        radiusLabel.setText(Utils.getUsername());
        return view;
    }
}
