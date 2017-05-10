package io.subutai.client.impl;


import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.Container;
import io.subutai.client.api.Environment;
import io.subutai.client.api.EnvironmentStatus;


public class EnvironmentImpl implements Environment
{
    @SerializedName( "environment_ttl" )
    private long environmentTtl;
    @SerializedName( "environment_key" )
    private String environmentKey;
    @SerializedName( "environment_id" )
    private String environmentId;
    @SerializedName( "environment_status_desc" )
    private String environmentStatusDescription;
    @SerializedName( "environment_status" )
    private EnvironmentStatus environmentStatus;
    @SerializedName( "environment_name" )
    private String environmentName;
    @SerializedName( "environment_hash" )
    private String environmentHash;
    @SerializedName( "environment_containers" )
    private List<ContainerImpl> containers;


    public long getEnvironmentTtl()
    {
        return environmentTtl;
    }


    public String getEnvironmentKey()
    {
        return environmentKey;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getEnvironmentStatusDescription()
    {
        return environmentStatusDescription;
    }


    public EnvironmentStatus getEnvironmentStatus()
    {
        return environmentStatus;
    }


    public String getEnvironmentName()
    {
        return environmentName;
    }


    public String getEnvironmentHash()
    {
        return environmentHash;
    }


    public List<Container> getContainers()
    {
        List<Container> containerList = Lists.newArrayList();
        containerList.addAll( containers );
        return containerList;
    }


    @Override
    public String toString()
    {
        return "EnvironmentImpl{" + "environmentTtl=" + environmentTtl + ", environmentKey='" + environmentKey + '\''
                + ", environmentId='" + environmentId + '\'' + ", environmentStatusDescription='"
                + environmentStatusDescription + '\'' + ", environmentStatus=" + environmentStatus
                + ", environmentName='" + environmentName + '\'' + ", environmentHash='" + environmentHash + '\''
                + ", containers=" + containers + '}';
    }
}
