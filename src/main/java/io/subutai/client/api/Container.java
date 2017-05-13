package io.subutai.client.api;


public interface Container
{
    String getContainerId();


    String getContainerIp();


    String getContainerName();


    String getContainerHostname();


    ContainerSize getContainerSize();


    ContainerState getContainerState();


    String getContainerPeerId();


    String getContainerTemplateId();


    String getContainerTemplateName();


    String getRhIp();
}
