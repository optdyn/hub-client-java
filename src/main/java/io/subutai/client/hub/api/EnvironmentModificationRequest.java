package io.subutai.client.hub.api;


public interface EnvironmentModificationRequest extends EnvironmentCreationRequest
{
    void removeNode( String containerId );
}
