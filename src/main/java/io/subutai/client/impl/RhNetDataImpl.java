package io.subutai.client.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.api.RhNetData;


public class RhNetDataImpl implements RhNetData
{
    private double netIn;

    private double netOut;


    public double getNetIn()
    {
        return netIn;
    }


    public double getNetOut()
    {
        return netOut;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
