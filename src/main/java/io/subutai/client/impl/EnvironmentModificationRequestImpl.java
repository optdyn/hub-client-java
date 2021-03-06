package io.subutai.client.impl;


import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.Container.ContainerSize;
import io.subutai.client.api.EnvironmentModificationRequest;


public class EnvironmentModificationRequestImpl implements EnvironmentModificationRequest
{
    private final String environmentId;
    @SerializedName( "add" )
    private final List<EnvironmentCreationRequestImpl.Node> nodesToAdd;

    @SerializedName( "remove" )
    private final List<Node> nodesToRemove;


    EnvironmentModificationRequestImpl( final String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        this.environmentId = environmentId;
        this.nodesToAdd = Lists.newArrayList();
        this.nodesToRemove = Lists.newArrayList();
    }


    public void addNode( final String hostname, final String templateId, final ContainerSize containerSize,
                         final String peerId, final String resourceHostId )
    {
        this.nodesToAdd.add( new EnvironmentCreationRequestImpl.Node( hostname, templateId, containerSize, peerId,
                resourceHostId ) );
    }


    public void removeNode( final String containerId )
    {
        this.nodesToRemove.add( new Node( containerId ) );
    }


    List<EnvironmentCreationRequestImpl.Node> getNodesToAdd()
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
