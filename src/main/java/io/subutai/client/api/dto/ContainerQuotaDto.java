package io.subutai.client.api.dto;


import com.google.common.base.Preconditions;

import io.subutai.client.api.ContainerSize;


public class ContainerQuotaDto
{
    private ContainerSize containerSize;


    public ContainerQuotaDto( final ContainerSize containerSize )
    {
        Preconditions.checkNotNull( containerSize );

        this.containerSize = containerSize;
    }
}
