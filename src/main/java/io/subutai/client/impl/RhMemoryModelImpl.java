package io.subutai.client.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.api.RhMemoryModel;


public class RhMemoryModelImpl implements RhMemoryModel
{

    private double total;


    public double getTotal()
    {
        return total;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
