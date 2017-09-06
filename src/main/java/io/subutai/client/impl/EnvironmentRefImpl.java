package io.subutai.client.impl;


import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.EnvironmentRef;


public class EnvironmentRefImpl implements EnvironmentRef
{
    @SerializedName( "subutaiId" )
    private String environmentId;
    @SerializedName( "hubId" )
    private long hubId;


    @Override
    public String getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public long getHubId()
    {
        return hubId;
    }


    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
