package io.subutai.client.hub.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.hub.api.RhCpuData;


public class RhCpuDataImpl implements RhCpuData
{
    private Double system;

    private Double idle;

    private Double iowait;

    private Double user;

    private Double nice;

    private Double load;

    private Double used;


    public Double getSystem()
    {
        return system;
    }


    public Double getIdle()
    {
        return idle;
    }


    public Double getIowait()
    {
        return iowait;
    }


    public Double getUser()
    {
        return user;
    }


    public Double getNice()
    {
        return nice;
    }


    public Double getLoad()
    {
        return load;
    }


    public Double getUsed()
    {
        return used;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
