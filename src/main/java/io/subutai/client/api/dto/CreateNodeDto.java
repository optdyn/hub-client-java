package io.subutai.client.api.dto;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.client.api.ContainerSize;


public class CreateNodeDto
{
    private final String hostname;

    private final String templateId;

    private String templateName;//workaround

    private final ContainerQuotaDto quota;

    private final String peerId;

    private final String resourceHostId;


    public CreateNodeDto( final String hostname, final String templateId, final ContainerSize containerSize,
                          final String peerId, final String resourceHostId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( resourceHostId ) );
        Preconditions.checkNotNull( containerSize );

        this.hostname = hostname;
        this.templateId = templateId;
        this.quota = new ContainerQuotaDto( containerSize );
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
