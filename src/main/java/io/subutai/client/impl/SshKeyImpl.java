package io.subutai.client.impl;


import java.util.Date;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.SshKey;


public class SshKeyImpl implements SshKey
{
    private String name;
    @SerializedName( "userId" )
    private long hubUserId;
    private Date createDate;
    private String sshKey;


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public long getHubUserId()
    {
        return hubUserId;
    }


    @Override
    public Date getCreateDate()
    {
        return createDate;
    }


    public String getSshKey()
    {
        return sshKey;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
