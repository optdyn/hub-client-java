package io.subutai.client.impl;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.client.hub.api.Container;
import io.subutai.client.hub.api.Environment;
import io.subutai.client.hub.api.EnvironmentCreationRequest;
import io.subutai.client.hub.api.HubClient;
import io.subutai.client.hub.impl.HubClients;

import static junit.framework.TestCase.assertEquals;

@Ignore
public class HubLoadTest
{
    private static final Logger LOG = LoggerFactory.getLogger( HubLoadTest.class );

    //key = peer id, values = { rhs ids }
    private static final Map<String, Set<String>> PEERS = ImmutableMap.of( "EC17E3CDD93253E520069D0561D9002471CFC5A9",
            Sets.newHashSet( "B9B9E764999071816F4120A313B662798D84A26E" )/*, "0D3C09A2CE28AEB4F9331745417B6F8908D00B63",
            Sets.newHashSet( "B6551B9D133D21496F69BB6880BA0EADED375ED1" ), "EC312EAE10A67C53297560025E6223577473936B",
            Sets.newHashSet( "B9B9E764999071816F4120A313B662798D84A26E" ) */ );

    private static final String TEMPLATE_ID = "a697e70f3fc538b4f4763588a7868388";
    private static final String EMAIL = "test.d@mail.com";
    private static final String PASSWORD = "test";
    private static final int NUM_OF_PARALLEL_ENV_CREATIONS_PER_PEER = 10;
    private static final int NUM_OF_CONTAINERS_PER_ENV = 2;
    private static final long USER_ID = 224;
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


    private void testCreateEnvironment( String peerId )
    {
        //create env creation request
        String envName = "env-" + UUID.randomUUID();
        envNames.add( envName.toLowerCase() );
        EnvironmentCreationRequest createEnvironmentRequest = hubClient.createRequest( envName );

        //populate request with container orders
        Iterator<String> rhIdsIt = Iterables.cycle( PEERS.get( peerId ) ).iterator();
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            createEnvironmentRequest
                    .addNode( "cont_" + UUID.randomUUID(), TEMPLATE_ID, Container.ContainerSize.SMALL, peerId,
                            rhIdsIt.next() );
        }

        //initiate env creation
        hubClient.createEnvironment( createEnvironmentRequest );
    }


    @Test
    public void testGetUsers() throws ExecutionException, InterruptedException
    {
        int MAX_THREADS = 500;

        List<CompletableFuture> futures = Lists.newArrayList();

        ExecutorService executor = Executors.newFixedThreadPool( MAX_THREADS );

        for ( int i = 1; i <= MAX_THREADS * 3; i++ )
        {
            futures.add( CompletableFuture.runAsync( () -> {
                try
                {
                    List<Environment> environments = hubClient.getEnvironments();

                    System.out.println( "number of envs: " + environments.size() );
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage() );
                }
            }, executor ) );
        }

        CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).get();

        executor.shutdown();
    }


    @Test
    public void testCreateEnvironmentsInParallel() throws Exception
    {
        envNames.clear();

        List<CompletableFuture> futures = Lists.newArrayList();

        //run NUM_OF_PARALLEL_ENV_CREATIONS_PER_PEER environment creations in parallel
        for ( String peerId : PEERS.keySet() )
        {
            for ( int i = 1; i <= NUM_OF_PARALLEL_ENV_CREATIONS_PER_PEER; i++ )
            {
                futures.add( CompletableFuture.runAsync( () -> {
                    try
                    {
                        testCreateEnvironment( peerId );
                    }
                    catch ( Exception e )
                    {
                        LOG.error( e.getMessage() );
                    }
                } ) );
            }
        }

        CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).get();

        //wait while there are still env-s under modification
        List<Environment> environments;
        do
        {
            Thread.sleep( 3000 );
            environments = hubClient.getEnvironments();
        }
        while ( environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) ).anyMatch(
                e -> e.getEnvironmentStatus() == Environment.EnvironmentStatus.UNDER_MODIFICATION ) );

        //assert that the newly ordered  environments are HEALTHY
        environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) )
                    .forEach( e -> assertEquals( Environment.EnvironmentStatus.HEALTHY, e.getEnvironmentStatus() ) );
    }
}
