package io.subutai.client.api;


public interface ModifyEnvironmentRequest extends CreateEnvironmentRequest
{
    void removeNode( String containerId );
}
