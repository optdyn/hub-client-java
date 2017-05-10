package io.subutai.client.impl;


import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.Container;


public class ContainerImpl implements Container
{
    @SerializedName( "container_ip" )
    private String containerIp;
    @SerializedName( "container_name" )
    private String containerName;
    @SerializedName( "container_id" )
    private String containerId;
    @SerializedName( "rh_ip" )
    private String resourceHostIp;


    public String getContainerIp()
    {
        return containerIp;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public String getResourceHostIp()
    {
        return resourceHostIp;
    }


    @Override
    public String toString()
    {
        return "ContainerImpl{" + "containerIp='" + containerIp + '\'' + ", containerName='" + containerName + '\''
                + ", containerId='" + containerId + '\'' + ", resourceHostIp='" + resourceHostIp + '\'' + '}';
    }
}
