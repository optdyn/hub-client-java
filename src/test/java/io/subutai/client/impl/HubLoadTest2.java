package io.subutai.client.impl;


import java.util.List;
import java.util.Set;
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


public class HubLoadTest2
{
    private static final String TEMPLATE_ID = "a697e70f3fc538b4f4763588a7868388";
    private static final String EMAIL = "amamutov@optimal-dynamics.com";
    private static final String PASSWORD = "abc";
    private static final int NUM_OF_PARALLEL_ENV_CREATIONS = 10;
    private static final int NUM_OF_CONTAINERS_PER_ENV = 3;
    private HubClient hubClient;
    private Set<String> envNames = Sets.newConcurrentHashSet();

    private static int envCounter = 1;
    private static int contCounter = 1;

    private static final String PEER1_ID = "A84661CAA2EF594905E2E22FB8E25948C01BCB6D";
    private static final String PEER1_MH_ID = "BA732D237A26FC1BC713C0EDFE522F10552988FB";

    private static final String PEER2_ID = "95F4964C00E245A47609A3CE25E1D4410C71B01D";
    private static final String PEER2_MH_ID = "CB468989F50E33C4A53D41C82FCA514CE98BFF4D";
    private static final String PEER2_RH1_ID = "76B31C9C730B3342A491470D40014D5E9D8E4087";


    @Before
    public void setUp() throws Exception
    {
        // create client for DEV Hub
        hubClient = HubClients.getClient( HubClient.HubEnv.DEV );

        //login to Hub
        hubClient.login( EMAIL, PASSWORD );
    }


//    @Test
    public void testCreateEnvironmentOnPeer1()
    {
        //create env creation request
        String envName = "peer1-env-" + envCounter;
        envCounter++;
        envNames.add( envName.toLowerCase() );
        CreateEnvironmentRequest createEnvironmentRequest = hubClient.createRequest( envName );

        //populate request with container orders
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            createEnvironmentRequest.addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER1_ID, PEER1_MH_ID );
            contCounter++;
        }

        //initiate env creation
        hubClient.createEnvironment( createEnvironmentRequest );
    }

//    @Test
    public void testCreateEnvironmentOnPeer2()
    {
        //create env creation request
        String envName = "peer2-env-" + envCounter;
        envCounter++;
        envNames.add( envName.toLowerCase() );
        CreateEnvironmentRequest createEnvironmentRequest = hubClient.createRequest( envName );

        //populate request with container orders
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            createEnvironmentRequest.addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER2_ID, PEER2_MH_ID );
            createEnvironmentRequest.addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER2_ID, PEER2_RH1_ID );
            contCounter++;
        }

        //initiate env creation
        hubClient.createEnvironment( createEnvironmentRequest );
    }


//    @Test
    public void testCreateEnvironmentOnBothPeers()
    {
        //create env creation request
        String envName = "test-env-" + envCounter;
        envCounter++;
        envNames.add( envName.toLowerCase() );
        CreateEnvironmentRequest createEnvironmentRequest = hubClient.createRequest( envName );

        //populate request with container orders
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            createEnvironmentRequest.addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER1_ID, PEER1_MH_ID );
            createEnvironmentRequest.addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER2_ID, PEER2_MH_ID );
            createEnvironmentRequest.addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER2_ID, PEER2_RH1_ID );
            contCounter++;
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
        for ( int i = 1; i <= Math.round( NUM_OF_PARALLEL_ENV_CREATIONS / 3 ); i++ )
        {
            futures.add( CompletableFuture.runAsync( this::testCreateEnvironmentOnPeer1 ) );
        }
        for ( int i = 1; i <= Math.round( NUM_OF_PARALLEL_ENV_CREATIONS / 3 ); i++ )
        {
            futures.add( CompletableFuture.runAsync( this::testCreateEnvironmentOnPeer2 ) );
        }
        for ( int i = 1; i <= Math.round( NUM_OF_PARALLEL_ENV_CREATIONS / 3 ); i++ )
        {
            futures.add( CompletableFuture.runAsync( this::testCreateEnvironmentOnBothPeers ) );
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
