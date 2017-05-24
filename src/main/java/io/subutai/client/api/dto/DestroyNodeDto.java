package io.subutai.client.api.dto;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class DestroyNodeDto
{
    private final String containerId;


    public DestroyNodeDto( final String containerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

        this.containerId = containerId;
    }
}
