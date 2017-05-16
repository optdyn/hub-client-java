package io.subutai.client.api;


import java.util.Date;


public interface Peer
{
    enum Status
    {
        ONLINE, OFFLINE
    }


    enum Scope
    {
        PRIVATE, PUBLIC, SHARED
    }

    String getPeerId();


    String getPeerName();


    String getPeerIp();


    Status getPeerStatus();


    long getPeerOwnerId();


    String getPeerOwnerName();


    String getPeerVersion();


    Date getPeerRegistrationDate();

    Scope getPeerScope();
}
