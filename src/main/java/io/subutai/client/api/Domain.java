package io.subutai.client.api;


public interface Domain
{
    enum State
    {
        CREATING, RESERVED, ASSIGNED, DESTROYING
    }

    String getName();

    State getState();
}
