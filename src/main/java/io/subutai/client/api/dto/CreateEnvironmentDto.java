package io.subutai.client.api.dto;


import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.client.api.ContainerSize;


public class CreateEnvironmentDto
{
    private final String environmentName;

    private final List<CreateNodeDto> nodes;

    private Boolean exchangeSshKeys = true;

    private Boolean registerHosts = true;


    public CreateEnvironmentDto( final String environmentName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentName ) );

        this.environmentName = environmentName;
        this.nodes = Lists.newArrayList();
    }


    public List<CreateNodeDto> getNodes()
    {
        return nodes;
    }


    public void addNode( String hostname, String templateId, ContainerSize containerSize, String peerId,
                         String resourceHostId )
    {
        this.nodes.add( new CreateNodeDto( hostname, templateId, containerSize, peerId, resourceHostId ) );
    }
}
