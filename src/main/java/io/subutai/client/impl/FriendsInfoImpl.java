package io.subutai.client.impl;


import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;

import io.subutai.client.api.FriendsInfo;
import io.subutai.client.api.Friendship;


public class FriendsInfoImpl implements FriendsInfo
{
    private List<FriendshipImpl> friends;
    private List<FriendshipImpl> outgoingRequests;
    private List<FriendshipImpl> incomingRequests;


    @Override
    public List<Friendship> getFriends()
    {
        return Lists.newArrayList( friends );
    }


    @Override
    public List<Friendship> getOutgoingRequests()
    {
        return Lists.newArrayList( outgoingRequests );
    }


    @Override
    public List<Friendship> getIncomingRequests()
    {
        return Lists.newArrayList( incomingRequests );
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
