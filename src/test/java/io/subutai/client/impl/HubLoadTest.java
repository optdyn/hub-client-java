package io.subutai.client.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.client.api.Container;
import io.subutai.client.api.CreateEnvironmentRequest;
import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;

import static junit.framework.TestCase.assertEquals;


public class HubLoadTest
{
    private static final String PEER_ID = "E612D4E6FBB99864E16EB479416B55A8DEC7CD8B";
    private static final String RH_ID = "98E2BCF35E208D583E49998E169E99C92FAA7D08";
    private static final String TEMPLATE_ID = "a697e70f3fc538b4f4763588a7868388";
    private static final String EMAIL = "test.d@mail.com";
    private static final String PASSWORD = "test";
    private static final int NUM_OF_PARALLEL_ENV_CREATIONS = 5;
    private static final int NUM_OF_CONTAINERS_PER_ENV = 5;
    private HubClient hubClient;
    private Set<String> envNames = Sets.newConcurrentHashSet();


    @Before
    public void setUp() throws Exception
    {
        // create client for DEV Hub
        hubClient = HubClients.getClient( HubClient.HubEnv.DEV );

        //login to Hub
        hubClient.login( EMAIL, PASSWORD );
    }


    @Test
    public void testCreateEnvironment()
    {
        //create env creation request
        String envName = "test-env-" + UUID.randomUUID();
        envNames.add( envName.toLowerCase() );
        CreateEnvironmentRequest createEnvironmentRequest = hubClient.createRequest( envName );

        //populate request with container orders
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            createEnvironmentRequest
                    .addNode( "cont_" + UUID.randomUUID(), TEMPLATE_ID, Container.ContainerSize.SMALL, PEER_ID, RH_ID );
        }

        //initiate env creation
        hubClient.createEnvironment( createEnvironmentRequest );
    }


    @Test
    public void testCreateEnvironmentsInParallel() throws Exception
    {
        envNames.clear();

        List<CompletableFuture> futures = Lists.newArrayList();

        //run NUM_OF_PARALLEL_ENV_CREATIONS environment creations in parallel
        for ( int i = 1; i <= NUM_OF_PARALLEL_ENV_CREATIONS; i++ )
        {
            futures.add( CompletableFuture.runAsync( this::testCreateEnvironment ) );
        }

        CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).get();

        //wait while there are still env-s under modification
        List<Environment> environments;
        do
        {
            Thread.sleep( 1000 );
            environments = hubClient.getEnvironments();
        }
        while ( environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) ).anyMatch(
                e -> e.getEnvironmentStatus() == Environment.EnvironmentStatus.UNDER_MODIFICATION ) );

        //assert that the newly ordered  environments are HEALTHY
        environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) )
                    .forEach( e -> assertEquals( Environment.EnvironmentStatus.HEALTHY, e.getEnvironmentStatus() ) );
    }
}
