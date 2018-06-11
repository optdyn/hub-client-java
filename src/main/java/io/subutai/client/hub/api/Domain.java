package io.subutai.client.hub.api;


public interface Domain
{
    enum State
    {
        CREATING, RESERVED, ASSIGNED, DESTROYING
    }

    /**
     * @return full domain name
     */
    String getName();

    State getState();
}
