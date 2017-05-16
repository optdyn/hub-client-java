package io.subutai.client.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.api.RhDiskData;


public class RhDiskDataImpl implements RhDiskData
{
    private double total;

    private double available;


    public double getTotal()
    {
        return total;
    }


    public double getAvailable()
    {
        return available;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
