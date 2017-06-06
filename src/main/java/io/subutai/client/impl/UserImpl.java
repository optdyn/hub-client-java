package io.subutai.client.impl;


import com.google.gson.GsonBuilder;

import io.subutai.client.api.User;


public class UserImpl implements User
{
    private long id;
    private String fingerprint;
    private String name;
    private String email;
    private String publicKey;
    private boolean blocked;


    public long getId()
    {
        return id;
    }


    public String getFingerprint()
    {
        return fingerprint;
    }


    public String getName()
    {
        return name;
    }


    public String getEmail()
    {
        return email;
    }


    public String getPublicKey()
    {
        return publicKey;
    }


    public boolean isBlocked()
    {
        return blocked;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
