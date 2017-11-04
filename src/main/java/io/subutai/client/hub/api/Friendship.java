package io.subutai.client.hub.api;


public interface Friendship
{

    enum Status
    {
        REQUEST, FRIEND
    }

    long getUserId();

    String getUsername();

    Status getStatus();
}
