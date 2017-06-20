package io.subutai.client.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.api.Domain;


public class DomainImpl implements Domain
{
    private String name;
    private State state;


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public State getState()
    {
        return state;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
