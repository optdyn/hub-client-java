package io.subutai.client.hub.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.hub.api.RhCpuModel;


public class RhCpuModelImpl implements RhCpuModel
{
    private long frequency;

    private int core;

    private String name;


    public long getFrequency()
    {
        return frequency;
    }


    public int getCore()
    {
        return core;
    }


    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
