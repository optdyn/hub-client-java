package io.subutai.client.impl;


import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.ContainerSize;
import io.subutai.client.api.ModifyEnvironmentRequest;


public class ModifyEnvironmentRequestImpl implements ModifyEnvironmentRequest
{
    private final String environmentId;
    @SerializedName( "add" )
    private final List<CreateEnvironmentRequestImpl.Node> nodesToAdd;

    @SerializedName( "remove" )
    private final List<Node> nodesToRemove;


    ModifyEnvironmentRequestImpl( final String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        this.environmentId = environmentId;
        this.nodesToAdd = Lists.newArrayList();
        this.nodesToRemove = Lists.newArrayList();
    }


    public void addNode( final String hostname, final String templateId, final ContainerSize containerSize,
                         final String peerId, final String resourceHostId )
    {
        this.nodesToAdd.add( new CreateEnvironmentRequestImpl.Node( hostname, templateId, containerSize, peerId,
                resourceHostId ) );
    }


    public void removeNode( final String containerId )
    {
        this.nodesToRemove.add( new Node( containerId ) );
    }


    List<CreateEnvironmentRequestImpl.Node> getNodesToAdd()
    {
        return nodesToAdd;
    }


    List<Node> getNodesToRemove()
    {
        return nodesToRemove;
    }


    static class Node
    {
        private final String containerId;


        Node( final String containerId )
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            this.containerId = containerId;
        }
    }
}
