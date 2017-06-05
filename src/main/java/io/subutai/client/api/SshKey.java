package io.subutai.client.api;


import java.util.Date;


public interface SshKey
{
    String getName();

    long getHubUserId();

    Date getCreateDate();

    String getSshKey();
}
