package io.subutai.client.impl;


import io.subutai.client.api.HubClient;


//TODO add factory method with possibility to specify GPG private key for getting authorized with Kurjun
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
