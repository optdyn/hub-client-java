package io.subutai.client.api.dto;


import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.ContainerSize;


public class ModifyEnvironmentDto
{
    private final String environmentId;

    @SerializedName( "add" )
    private final List<CreateNodeDto> nodesToAdd;

    @SerializedName( "remove" )
    private final List<DestroyNodeDto> nodesToRemove;


    public ModifyEnvironmentDto( final String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        this.environmentId = environmentId;
        this.nodesToAdd = Lists.newArrayList();
        this.nodesToRemove = Lists.newArrayList();
    }


    public void addNode( String hostname, String templateId, ContainerSize containerSize, String peerId,
                         String resourceHostId )
    {
        this.nodesToAdd.add( new CreateNodeDto( hostname, templateId, containerSize, peerId, resourceHostId ) );
    }


    public void removeNode( String containerId )
    {
        this.nodesToRemove.add( new DestroyNodeDto( containerId ) );
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public List<CreateNodeDto> getNodesToAdd()
    {
        return nodesToAdd;
    }


    public List<DestroyNodeDto> getNodesToRemove()
    {
        return nodesToRemove;
    }
}
