package com.chancorp.audiofornerds.draw;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.chancorp.audiofornerds.animation.MixedProperties;

/**
 * Created by Chan on 2/26/2016.
 */
public abstract class Animatable {
    MixedProperties mixedProperties;
    public Animatable(MixedProperties basis){
        this.mixedProperties=basis;
    }
    public MixedProperties getMixedProperties(){
        return this.mixedProperties;
    }
    public abstract void draw(Canvas c, Paint pt);
}
