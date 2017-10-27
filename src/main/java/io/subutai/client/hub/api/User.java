package io.subutai.client.hub.api;


public interface User
{
    long getId();

    String getName();

    String getFingerprint();

    boolean isBlocked();

    String getEmail();

    String getPublicKey();

    String getCountry();
}
