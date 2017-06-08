package io.subutai.client.impl;


import java.io.IOException;

import org.bouncycastle.openpgp.PGPException;

import io.subutai.client.api.HubClient;


public class HubClients
{

    private HubClients()
    {
    }


    /**
     * This ctr for development only
     */
    public static HubClient getClient( HubClient.HubEnv hubEnv, String pgpKeyFilePath, String pgpKeyPassword )
            throws PGPException, IOException
    {
        return new HubClientImplementation( hubEnv, pgpKeyFilePath, pgpKeyPassword );
    }


    /**
     * This ctr for development only
     */
    public static HubClient getClient( HubClient.HubEnv hubEnv )
    {
        return new HubClientImplementation( hubEnv );
    }


    /**
     * Returns client instance set up to use PGP private key. This allows usage of private templates
     *
     * @param pgpKeyFilePath - path to PGP key file (exported from SS browser plugin in armored format)
     * @param pgpKeyPassword - password for PGP key, pass null if key does not have password
     */
    public static HubClient getClient( String pgpKeyFilePath, String pgpKeyPassword ) throws PGPException, IOException
    {
        return getClient( HubClient.HubEnv.PROD, pgpKeyFilePath, pgpKeyPassword );
    }


    /**
     * Returns client instance
     */
    public static HubClient getClient()
    {
        return getClient( HubClient.HubEnv.PROD );
    }
}
