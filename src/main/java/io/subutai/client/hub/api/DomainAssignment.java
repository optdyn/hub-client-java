package io.subutai.client.hub.api;


public interface DomainAssignment
{
    String getEnvironmentId();

    String getEnvironmentName();

    String getContainerId();

    String getContainerName();

    long getInternalPort();

    long getExternalPort();
}
