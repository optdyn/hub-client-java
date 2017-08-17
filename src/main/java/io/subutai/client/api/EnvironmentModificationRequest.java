package io.subutai.client.api;


public interface EnvironmentModificationRequest extends EnvironmentCreationRequest
{
    void removeNode( String containerId );
}
