package io.subutai.client.api;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class Node
{
    private String hostname;
    private String templateId;
    //as a workaround template name is passed from client for now
    private String templateName;
    private ContainerSize containerSize;
    private String peerId;
    private String resourceHostId;


    public Node( final String hostname, final String templateId, final ContainerSize containerSize, final String peerId,
                 final String resourceHostId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( resourceHostId ) );
        Preconditions.checkNotNull( containerSize );
        this.hostname = hostname;
        this.templateId = templateId;
        this.containerSize = containerSize;
        this.peerId = peerId;
        this.resourceHostId = resourceHostId;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }
}
