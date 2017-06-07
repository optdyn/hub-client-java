package io.subutai.client.impl;


import java.util.Date;

import com.google.gson.GsonBuilder;

import io.subutai.client.api.Member;



public class MemberImpl implements Member
{
    private long userId;
    private String username;
    private Date joinDate;


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
    public Date getJoinDate()
    {
        return joinDate;
    }

    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }

}
