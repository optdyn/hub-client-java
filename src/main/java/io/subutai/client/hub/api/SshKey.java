package io.subutai.client.hub.api;


import java.util.Date;


public interface SshKey
{
    String getName();

    long getHubUserId();

    Date getCreationDate();

    String getSshKey();
}
