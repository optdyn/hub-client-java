package io.subutai.client.impl;


import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.Container;


public class ContainerImpl implements Container
{
    @SerializedName( "container_id" )
    private String containerId;
    @SerializedName( "container_ip" )
    private String containerIp;
    @SerializedName( "container_name" )
    private String containerName;
    @SerializedName( "container_hostname" )
    private String containerHostname;
    @SerializedName( "container_size" )
    private ContainerSize containerSize;
    @SerializedName( "container_state" )
    private ContainerState containerState;
    @SerializedName( "container_peer_id" )
    private String containerPeerId;
    @SerializedName( "container_template_id" )
    private String containerTemplateId;
    @SerializedName( "container_template_name" )
    private String containerTemplateName;
    @SerializedName( "rh_ip" )
    private String rhIp;


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


    public String getContainerHostname()
    {
        return containerHostname;
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    public ContainerState getContainerState()
    {
        return containerState;
    }


    public String getContainerPeerId()
    {
        return containerPeerId;
    }


    public String getContainerTemplateId()
    {
        return containerTemplateId;
    }


    public String getContainerTemplateName()
    {
        return containerTemplateName;
    }


    public String getRhIp()
    {
        return rhIp;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
