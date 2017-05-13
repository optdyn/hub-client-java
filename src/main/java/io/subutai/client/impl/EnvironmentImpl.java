package io.subutai.client.impl;


import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
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
    @SerializedName( "environment_p2p_subnet" )
    private String environmentP2pSubnet;
    @SerializedName( "environment_subnet_cidr" )
    private String environmentSubnetCidr;
    @SerializedName( "environment_owner_hub_id" )
    private long environmentOwnerHubId;
    @SerializedName( "environment_owner" )
    private String environmentOwner;
    @SerializedName( "environment_vni" )
    private long environmentVni;


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


    public String getEnvironmentP2pSubnet()
    {
        return environmentP2pSubnet;
    }


    public String getEnvironmentSubnetCidr()
    {
        return environmentSubnetCidr;
    }


    public long getEnvironmentOwnerHubId()
    {
        return environmentOwnerHubId;
    }


    public String getEnvironmentOwner()
    {
        return environmentOwner;
    }


    public long getEnvironmentVni()
    {
        return environmentVni;
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
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
