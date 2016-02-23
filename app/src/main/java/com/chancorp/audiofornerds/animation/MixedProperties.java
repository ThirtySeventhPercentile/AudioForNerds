package com.chancorp.audiofornerds.animation;

import android.util.Log;

import com.chancorp.audiofornerds.exceptions.PropertySetException;
import com.chancorp.audiofornerds.helper.ErrorLogger;
import com.chancorp.audiofornerds.helper.Log2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created by Chan on 2/21/2016.
 */
public class MixedProperties {
    public static final String LOG_TAG="CS_AFN";

    PropertySet basis;
    ArrayList<MixedProperties> set;

    private String name;
    private AnimatableValue influence;

    public MixedProperties(String name, PropertySet basis){
        this.name=name;
        influence=new AnimatableValue(1);
        this.basis=basis;
    }

    public MixedProperties(String name){
        this.name=name;
        influence=new AnimatableValue(1);
        set=new ArrayList<>();
    }


    public AnimatableValue getInfluence(){
        return this.influence;
    }

    public String getName(){
        return this.name;
    }

    public PropertySet getBasis(){
        if (basis==null) Log.w(LOG_TAG,"getBasis() called to a non-basic instance!");
        return basis;
    }

    public PropertySet update(long time){

        if (basis!=null) return basis;

        PropertySet res=new PropertySet();
        //float influenceSum=0;
        float influence;
        int size=-1;

        HashMap<String,Float> influences=new HashMap<>();

        for (MixedProperties mp : this.set) {

            PropertySet ps=mp.update(time);


            if (size>0 && ps.getNumKeys()!=size){
                //throw new PropertySetException("Property Set Size Mismatch!");
                //Log.w(LOG_TAG, "Property Set Size Mismatch!");
            }else size=ps.getNumKeys();

            //Log2.log(2,this,ps.toString());

            influence=mp.getInfluence().getValue(time);

            //influenceSum+=influence;
            for (Object k:ps.getIter()){
                String key=(String)k;

                if (influences.get(key)==null) influences.put(key,0.0f);

                influences.put(key,influences.get(key)+influence);
                //Log2.log(2,this,key,k,ps.getName());
                res.setValue(key, res.getValue(key,0)+ps.getValue(key)*influence);

            }

        }
        for (Object k:res.getIter()){
            String key=(String)k;
            if (influences.get(key)==0) Log.w(LOG_TAG,"Influence of "+key+" is ZERO. Expect Animation errors.");
            res.setValue(key, res.getValue(key)/influences.get(key));

        }

        return res;

    }
    public void addProperty(MixedProperties mp){
        this.set.add(mp);
    }
    @Deprecated
    public MixedProperties getProperty(String name){
        for (MixedProperties mp:this.set){
            if (mp.getName().equals(name)) {
                return mp;
            }
        }
        return null;
    }
}
