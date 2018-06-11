package io.subutai.client.hub.impl;


import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.hub.api.DomainAssignment;


public class DomainAssignmentImpl implements DomainAssignment
{
    @SerializedName( "environmentSubutaiId" )
    private String environmentId;
    private String environmentName;
    @SerializedName( "containerSubutaiId" )
    private String containerId;
    private String containerName;
    private long internalPort;
    private long externalPort;


    @Override
    public String getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public String getEnvironmentName()
    {
        return environmentName;
    }


    @Override
    public String getContainerId()
    {
        return containerId;
    }


    @Override
    public String getContainerName()
    {
        return containerName;
    }


    @Override
    public long getInternalPort()
    {
        return internalPort;
    }


    @Override
    public long getExternalPort()
    {
        return externalPort;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
