package io.subutai.client.api;


public interface UserInfo
{
    long getId();

    String getName();

    String getFingerprint();

    boolean isBlocked();

    String getEmail();

    String getPublicKey();
}
