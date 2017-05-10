package io.subutai.client.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.client.api.HubClient;


@RunWith( MockitoJUnitRunner.class )
public class HubClientImplementationTest
{

    HubClient hubClient;


    @Before
    public void setUp() throws Exception
    {
        hubClient = HubClients.getClient( HubClientImplementation.HubEnv.DEV );
    }


    @Test
    public void testLogin() throws Exception
    {
        hubClient.login( "dilshat.aliev@gmail.com", "sadilya" );
    }


    @Test
    public void testGetEnvironments() throws Exception
    {
        //call to initiate session
        hubClient.login( "dilshat.aliev@gmail.com", "sadilya" );

        hubClient.getEnvironments();
    }
}
