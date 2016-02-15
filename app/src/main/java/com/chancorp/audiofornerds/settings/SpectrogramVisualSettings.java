package com.chancorp.audiofornerds.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.chancorp.audiofornerds.R;
import com.chancorp.audiofornerds.helper.Log2;

/**
 * Created by Chan on 2/3/2016.
 */
public class SpectrogramVisualSettings extends BaseSetting implements AdapterView.OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener {
    private static final String LOG_TAG = "CS_AFN";
    private static final String PREF_IDENTIFIER = "com.chancorp.audiofornerds.settings.SpectrogramVisualSettings";

    //TODO Logaritmic Scale
    int fftSize = 2048;
    float startFreq = 20, endFreq = 1000;

    public int getFftSize() {
        return fftSize;
    }

    public void setFftSize(int fftSize) {
        Log2.log(2,this,"setFFTSize() Called",fftSize);
        this.fftSize = fftSize;

        if (fftSizeSpinner!=null) {
            for (int i = 0; i < fftSizes.length; i++) {
                if (Integer.parseInt(fftSizes[i]) == fftSize) {
                    fftSizeSpinner.setSelection(i, false);
                    return;
                }
            }
            Log.w(LOG_TAG, "SpectrogramVisualSettings>setFftSize(): fftSize NOT in fftSizes[]!!");
        }
    }

    public float getStartFreq() {
        return startFreq;
    }

    public void setStartFreq(float startFreq) {
        //if end and start order is reversed(or is very close), weird shit would happen. So we do this
        if (this.endFreq<startFreq+100){
            //don't do anything
        }else{
            this.startFreq = startFreq;
        }
        if (startFrqTV!=null && startFrqSeekbar!=null) {
            startFrqTV.setText(Float.toString(this.startFreq));
            startFrqSeekbar.setProgress((int) (this.startFreq / 10));
        }

    }


    public float getEndFreq() {
        return endFreq;
    }

    public void setEndFreq(float endFreq) {

        //if end and start order is reversed(or is very close), weird shit would happen. So we do this
        if (endFreq<this.startFreq+100){
            //don't do anything
        }else{
            this.endFreq = endFreq;
        }
        if (endFrqTV!=null && endFrqSeekbar!=null) {
            endFrqTV.setText(Float.toString(this.endFreq));
            endFrqSeekbar.setProgress((int) (this.endFreq / 10));
        }

    }


    public SpectrogramVisualSettings(SidebarSettings sbs, Context c) {
        super(sbs,c);

    }

    private static final String[] fftSizes = {"256", "512", "1024", "2048", "4096", "8192"};
    Spinner fftSizeSpinner;

    SeekBar startFrqSeekbar, endFrqSeekbar;
    TextView startFrqTV, endFrqTV;

    public View getSettingsView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.visuals_setting_spectrogram, container, false);

        fftSizeSpinner = (Spinner) v.findViewById(R.id.vis_spectrograph_setting_fftsize_selector);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_spinner_item, fftSizes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fftSizeSpinner.setAdapter(adapter);

        //fftSizeSpinner.setOnItemSelectedListener(this);
        //We do this because otherwise the initialization of the spinner would fire a callback
        //Which would be undesirable.
        fftSizeSpinner.post(new Runnable() {
            public void run() {
                fftSizeSpinner.setOnItemSelectedListener(SpectrogramVisualSettings.this);
            }
        });


        startFrqSeekbar = (SeekBar) v.findViewById(R.id.vis_spectrograph_setting_frq_start_seekbar);
        startFrqTV = (TextView) v.findViewById(R.id.vis_spectrograph_setting_frq_start_value);
        startFrqSeekbar.setOnSeekBarChangeListener(this);

        endFrqSeekbar = (SeekBar) v.findViewById(R.id.vis_spectrograph_setting_frq_end_seekbar);
        endFrqTV = (TextView) v.findViewById(R.id.vis_spectrograph_setting_frq_end_value);
        endFrqSeekbar.setOnSeekBarChangeListener(this);

        load();
        return v;
    }


    @Override
    public int getType() {
        return BaseSetting.SPECTRUM;
    }

    @Override
    protected void save() {
        //Log2.log(2,this,"Saving:",fftSize,bars,spacing,startFreq,endFreq);
        SharedPreferences.Editor editor=getSharedPreferences(PREF_IDENTIFIER).edit();
        editor.putInt("fftSize", fftSize);

        editor.putFloat("startFreq", startFreq);
        editor.putFloat("endFreq", endFreq);
        editor.apply();
    }

    @Override
    protected void load() {
        SharedPreferences pref=getSharedPreferences(PREF_IDENTIFIER);
        //Log2.log(2, this, "initial", fftSize, bars, spacing, startFreq, endFreq);
        setFftSize(pref.getInt("fftSize", fftSize));
        setStartFreq(pref.getFloat("startFreq", startFreq));
        setEndFreq(pref.getFloat("endFreq", endFreq));
        //Log2.log(2, this, "end", fftSize, bars, spacing, startFreq, endFreq);
        sbs.notifyUI(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.vis_spectrum_setting_fftsize_selector) {
            setFftSize(Integer.parseInt(fftSizes[position]));
        }
        save();
        sbs.notifyUI(this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.vis_spectrum_setting_frq_start_seekbar) {
            setStartFreq(progress * 10); //0~10000
        }else if (seekBar.getId() == R.id.vis_spectrum_setting_frq_end_seekbar) {
            setEndFreq(progress * 10); //0~10000
        } else {
            Log.w(LOG_TAG, "I think I'm not the only seekbar around here....");
        }
        if (fromUser) {
            save();
            sbs.notifyUI(this);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}