package io.subutai.client.hub.api;


import java.util.Date;


public interface Member
{
    long getUserId();

    String getUsername();

    Date getJoinDate();
}
