package io.subutai.client.hub.api;


public interface Container
{
    enum ContainerState
    {
        BUILDING, STARTING, RUNNING, STOPPING, STOPPED, ABORTING, FREEZING, FROZEN, UNKNOWN
    }


    enum ContainerSize
    {
        TINY, SMALL, MEDIUM, LARGE, HUGE
    }

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
