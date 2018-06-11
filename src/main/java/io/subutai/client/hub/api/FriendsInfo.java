package io.subutai.client.hub.api;


import java.util.List;


public interface FriendsInfo
{
    List<Friendship> getFriends();

    List<Friendship> getIncomingRequests();

    List<Friendship> getOutgoingRequests();
}
