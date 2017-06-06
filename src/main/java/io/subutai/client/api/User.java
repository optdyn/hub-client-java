package io.subutai.client.api;


public interface User
{
    long getId();

    String getName();

    String getFingerprint();

    boolean isBlocked();

    String getEmail();

    String getPublicKey();
}
