package io.subutai.client.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.api.RhMemoryData;


public class RhMemoryDataImpl implements RhMemoryData
{
    private double active;

    private double cached;

    private double memFree;

    private double buffers;

    private double totalRam;

    private double availableRam;


    public double getActive()
    {
        return active;
    }


    public double getCached()
    {
        return cached;
    }


    public double getMemFree()
    {
        return memFree;
    }


    public double getBuffers()
    {
        return buffers;
    }


    public double getTotalRam()
    {
        return totalRam;
    }


    public double getAvailableRam()
    {
        return availableRam;
    }

    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
