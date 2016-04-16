//Licensed under the MIT License.
//Include the license text thingy if you're gonna use this.
//Copyright (c) 2016 Chansol Yang

package com.thirtyseventhpercentile.nerdyaudio.visuals;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.thirtyseventhpercentile.nerdyaudio.exceptions.BufferNotPresentException;
import com.thirtyseventhpercentile.nerdyaudio.interfaces.SettingsUpdateListener;
import com.thirtyseventhpercentile.nerdyaudio.settings.BaseSetting;
import com.thirtyseventhpercentile.nerdyaudio.settings.SidebarSettings;
import com.thirtyseventhpercentile.nerdyaudio.settings.WaveformVisualSettings;

public class WaveformVisuals extends BaseRenderer{
    int range = 2048;
    int drawEvery = 1;
    boolean downmix=false;


    WaveformVisualSettings newSettings=null;


    Paint pt;



    public WaveformVisuals(float density) {
        super(density);
        pt = new Paint(Paint.ANTI_ALIAS_FLAG);

        updated(sbs.getSetting(BaseSetting.WAVEFORM));
    }



    private void syncChanges(){
        if (newSettings!=null){
            Log.d(LOG_TAG,"WaveformVisuals state changed. syncing.");
            range=newSettings.getRange();

            drawEvery=range/1024; //TODO more elegant way of optimizing
            if (drawEvery<1) drawEvery=1;

            downmix=newSettings.getDownmix();

            newSettings=null;
        }
    }

    @Override
    public void drawVisuals(Canvas c, int w, int h) {
        syncChanges();


            long currentFrame = getCurrentFrame();
            try {

                pt.setColor(Color.BLACK);

                short[] pcmL = getLSamples(currentFrame - range+1, currentFrame);
                short[] pcmR = getRSamples(currentFrame - range+1, currentFrame);
                deleteBefore(currentFrame  - range+1);

                int numberOfLinePoints = pcmL.length / drawEvery;
                float[] lines = new float[numberOfLinePoints * 4];
                //float[] points = new float[numberOfLinePoints*2];
                assert pcmL.length==pcmR.length;

                int pcmIndex;
                //TODO Performance Improvements.
                if (downmix) {
                    for (int i = 0; i < numberOfLinePoints - 1; i++) {
                        pcmIndex = i * drawEvery;
                        lines[i * 4] = i / (float) numberOfLinePoints * w;
                        lines[i * 4 + 1] = ((pcmL[pcmIndex]+pcmR[pcmIndex]) / 65534.0f + 1) * h / 2.0f;
                        lines[i * 4 + 2] = (i + 1) / (float) numberOfLinePoints * w;
                        lines[i * 4 + 3] = ((pcmL[pcmIndex + drawEvery]+pcmR[pcmIndex + drawEvery]) / 65534.0f + 1) * h / 2.0f;
                    }
                    c.drawLines(lines, pt);
                }else{
                    for (int i = 0; i < numberOfLinePoints - 1; i++) {
                        pcmIndex = i * drawEvery;
                        lines[i * 4] = i / (float) numberOfLinePoints * w;
                        lines[i * 4 + 1] = (pcmL[pcmIndex] / 32767.0f + 1) * h / 4.0f;
                        lines[i * 4 + 2] = (i + 1) / (float) numberOfLinePoints * w;
                        lines[i * 4 + 3] = (pcmL[pcmIndex + drawEvery] / 32767.0f + 1) * h / 4.0f;
                    }
                    c.drawLines(lines, pt);

                    for (int i = 0; i < numberOfLinePoints - 1; i++) {
                        pcmIndex = i * drawEvery;
                        lines[i * 4] = i / (float) numberOfLinePoints * w;
                        lines[i * 4 + 1] = (pcmR[pcmIndex] / 32767.0f + 1) * h / 4.0f+h/2.0f;
                        lines[i * 4 + 2] = (i + 1) / (float) numberOfLinePoints * w;
                        lines[i * 4 + 3] = (pcmR[pcmIndex + drawEvery] / 32767.0f + 1) * h / 4.0f+h/2.0f;

                    }
                    c.drawLines(lines, pt);
                }
                //c.drawPoints(points,pt);

            } catch (BufferNotPresentException e) {
                Log.d(LOG_TAG, "Buffer not present! Requested around " + currentFrame);
            }

    }


    @Override
    public void updated(BaseSetting setting) {
        if (setting instanceof WaveformVisualSettings){
            newSettings=(WaveformVisualSettings) setting;

        }
    }

    @Override
    public void dimensionsChanged(int w, int h) {

    }

}