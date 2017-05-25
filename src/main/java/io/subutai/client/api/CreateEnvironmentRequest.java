package io.subutai.client.api;


public interface CreateEnvironmentRequest
{
    void addNode( String hostname, String templateId, ContainerSize containerSize, String peerId,
                  String resourceHostId );
}
