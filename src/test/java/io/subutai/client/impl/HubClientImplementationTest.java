package io.subutai.client.impl;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;


@RunWith( MockitoJUnitRunner.class )
public class HubClientImplementationTest
{

    //TODO change before commit to Git
    private static final String username = "test.d@mail.com";
    private static final String password = "test";

    HubClient hubClient;


    @Before
    public void setUp() throws Exception
    {
        hubClient = HubClients.getClient( HubClient.HubEnv.DEV );
        hubClient.login( username, password );
    }


    @Test
    public void testGetEnvironments() throws Exception
    {
        List<Environment> environments = hubClient.getEnvironments();

        for ( Environment e : environments )
        {
            System.out.println( e );
        }
    }
}
