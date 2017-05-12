package io.subutai.client.impl;


import io.subutai.client.api.HubClient;


public class HubClients
{

    private HubClients()
    {
    }


    public static HubClient getClient( HubClient.HubEnv hubEnv )
    {
        return new HubClientImplementation( hubEnv );
    }
}
