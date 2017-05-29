package io.subutai.client.impl;


import io.subutai.client.api.HubClient;


public class HubClients
{

    private HubClients()
    {
    }


    public static HubClient getClient( HubClient.HubEnv hubEnv, String pgpKeyFilePath, String pgpKeyPassword )
    {
        return new HubClientImplementation( hubEnv, pgpKeyFilePath, pgpKeyPassword );
    }


    public static HubClient getClient( HubClient.HubEnv hubEnv )
    {
        return new HubClientImplementation( hubEnv );
    }
}
