package io.subutai.client.api.dto;


import io.subutai.client.api.ContainerSize;


public class ContainerQuotaDto
{
    private ContainerSize containerSize;


    public ContainerQuotaDto( final ContainerSize containerSize )
    {
        this.containerSize = containerSize;
    }
}
