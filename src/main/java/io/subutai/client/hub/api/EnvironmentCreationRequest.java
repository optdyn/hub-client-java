package io.subutai.client.hub.api;


public interface EnvironmentCreationRequest
{
    void addNode( String hostname, String templateId, Container.ContainerSize containerSize, String peerId,
                  String resourceHostId );
}
