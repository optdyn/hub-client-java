package io.subutai.client.api;


public enum ContainerSize
{
    TINY( "Tiny" ), SMALL( "Small" ), MEDIUM( "Medium" ), LARGE( "Large" ), HUGE( "Huge" ), CUSTOM( "Custom" );

    private String envContType;


    ContainerSize( final String type )
    {
        envContType = type;
    }


    public String getEnvContType()
    {
        return envContType;
    }
}