package io.subutai.client.impl;


import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.Friendship;


public class FriendshipImpl implements Friendship
{
    @SerializedName( "id" )
    private long userId;

    @SerializedName( "name" )
    private String username;

    private Status status;


    @Override
    public long getUserId()
    {
        return userId;
    }


    @Override
    public String getUsername()
    {
        return username;
    }


    @Override
    public Status getStatus()
    {
        return status;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
