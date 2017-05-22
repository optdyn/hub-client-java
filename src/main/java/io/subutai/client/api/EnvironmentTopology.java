package io.subutai.client.api;


import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


public class EnvironmentTopology
{
    private String environmentName;
    private List<Node> nodes;


    public EnvironmentTopology( final String environmentName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentName ) );

        this.environmentName = environmentName;
        this.nodes = Lists.newArrayList();
    }


    public void addNode( String hostname, String templateId, ContainerSize containerSize, String peerId,
                         String resourceHostId )
    {
        nodes.add( new Node( hostname, templateId, containerSize, peerId, resourceHostId ) );
    }


    public List<Node> getNodes()
    {
        return nodes;
    }
}
