package io.subutai.client.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.client.hub.api.Container;
import io.subutai.client.hub.api.Environment;
import io.subutai.client.hub.api.EnvironmentCreationRequest;
import io.subutai.client.hub.api.HubClient;
import io.subutai.client.hub.impl.HubClients;

import static junit.framework.TestCase.assertEquals;


@Ignore
public class HubLoadTest2
{
    // https://devcdn.subutai.io:8338/kurjun/rest/template/info?id=ea886a22-2994-481e-8354-3a3032c598ba
    private static final String TEMPLATE_ID = "QmQZMYoPx6uQcRREfQWUt9CKubhy8bka3NyB9hshSi8NKZ";

    private static final String EMAIL = "a@od.com";
    private static final String PASSWORD = "zzzzzz";
    private static final int NUM_OF_PARALLEL_ENV_CREATIONS = 15;
    private static final int NUM_OF_CONTAINERS_PER_ENV = 1;
    private HubClient hubClient;
    private Set<String> envNames = Sets.newConcurrentHashSet();

    private static int envCounter = 1;
    private static int contCounter = 1;

    private static final String PEER1_ID = "8B3981FB0C9A2B78697CAB51B86140D6CF1685C8";
    private static final String PEER1_MH_ID = "002EA5726830A7C96EEFB70DB623EBCEE9AAF637";
    private static final String PEER1_RH1_ID = "002EA5726830A7C96EEFB70DB623EBCEE9AAF637";

//    private static final String PEER2_ID = "16BBC2C3A423A8C6823F0DCD37AA83A83CCCFA48";
//    private static final String PEER2_MH_ID = "D193527A9C023A4C1E5546F10AE8B579D23B75CE";
    private static final String PEER2_ID = "FE000F93E937E580D176BA6107804C96F03F48A2";
    private static final String PEER2_MH_ID = "F114E036F7822F5330F3D0BC31A950058400DBB6";

//    private static final String PEER3_ID = "F3998F379345CD87CD50EA4BE68532A390F85D19";
//    private static final String PEER3_MH_ID = "3DB1C8DAC31785C56F17609E5A7F299F76038AC9";
    private static final String PEER3_ID = "D32193A1BC697420F651FBE809E0134C12DC3354";
    private static final String PEER3_MH_ID = "4C374EF8898A43CEAFBE7BF55725738D3E43878B";


    @Before
    public void setUp() throws Exception
    {
        // create client for DEV Hub
//        hubClient = HubClients.getClient( HubClient.HubEnv.DEV );

        //login to Hub
//        hubClient.login( EMAIL, PASSWORD );
    }


    public void testCreateEnvironmentOnPeer1()
    {
        //create env creation request
        String envName = String.format( "env-%03d-peer1-%s", envCounter, UUID.randomUUID() );
        envCounter++;
        envNames.add( envName.toLowerCase() );
        EnvironmentCreationRequest EnvironmentCreationRequest = hubClient.createRequest( envName );

        //populate request with container orders
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            EnvironmentCreationRequest
                    .addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER1_ID, PEER1_MH_ID );
            contCounter++;
        }

        //initiate env creation
        try
        {
            hubClient.createEnvironment( EnvironmentCreationRequest );
        }
        catch ( Exception e )
        {
            System.out.println( "Failed to create env. with name: " + envName );
            e.printStackTrace();
        }
    }


    public void testCreateEnvironmentOnPeer2()
    {
        //create env creation request
        String envName = String.format( "env-%03d-peer2-%s", envCounter, UUID.randomUUID() );
        envCounter++;
        envNames.add( envName.toLowerCase() );
        EnvironmentCreationRequest EnvironmentCreationRequest = hubClient.createRequest( envName );

        //populate request with container orders
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            EnvironmentCreationRequest
                    .addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER2_ID, PEER2_MH_ID );
//            EnvironmentCreationRequest
//                    .addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER2_ID,
//                            PEER2_RH1_ID );
            contCounter++;
        }

        //initiate env creation
        try
        {
            hubClient.createEnvironment( EnvironmentCreationRequest );
        }
        catch ( Exception e )
        {
            System.out.println( "Failed to create env. with name: " + envName );
            e.printStackTrace();
        }
    }


    //    @Test
    public void testCreateEnvironmentOnBothPeers()
    {
        //create env creation request
        String envName = String.format( "env-%03d-mix-%s", envCounter, UUID.randomUUID() );
        envCounter++;
        envNames.add( envName.toLowerCase() );
        EnvironmentCreationRequest EnvironmentCreationRequest = hubClient.createRequest( envName );

        //populate request with container orders
        for ( int i = 1; i <= NUM_OF_CONTAINERS_PER_ENV; i++ )
        {
            EnvironmentCreationRequest
                    .addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER1_ID, PEER1_MH_ID );
            EnvironmentCreationRequest
                    .addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER1_ID, PEER1_RH1_ID );
            EnvironmentCreationRequest
                    .addNode( "cont_" + contCounter, TEMPLATE_ID, Container.ContainerSize.TINY, PEER2_ID, PEER2_MH_ID );
            contCounter++;
        }

        //initiate env creation
        try
        {
            hubClient.createEnvironment( EnvironmentCreationRequest );
        }
        catch ( Exception e )
        {
            System.out.println( "Failed to create env. with name: " + envName );
            e.printStackTrace();
        }
    }


//    @Test
    public void testCreateEnvironmentsInParallel() throws Exception
    {
        envNames.clear();

        List<CompletableFuture> futures = Lists.newArrayList();

        //run NUM_OF_PARALLEL_ENV_CREATIONS environment creations in parallel
        for ( int i = 1; i <= Math.round( 5 ); i++ )
        {
            futures.add( CompletableFuture.runAsync( this::testCreateEnvironmentOnPeer1 ) );
        }
        for ( int i = 1; i <= Math.round( 5 ); i++ )
        {
            futures.add( CompletableFuture.runAsync( this::testCreateEnvironmentOnPeer2 ) );
        }
        for ( int i = 1; i <= Math.round( 5 ); i++ )
        {
            futures.add( CompletableFuture.runAsync( this::testCreateEnvironmentOnBothPeers ) );
        }

        CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).get();

        //wait while there are still env-s under modification
        List<Environment> environments;
        do
        {
            Thread.sleep( 300 );
            environments = hubClient.getEnvironments();
        }
        while ( environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) ).anyMatch(
                e -> e.getEnvironmentStatus() == Environment.EnvironmentStatus.UNDER_MODIFICATION ) );

        //assert that the newly ordered  environments are HEALTHY
        environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) )
                    .forEach( e -> assertEquals( Environment.EnvironmentStatus.HEALTHY, e.getEnvironmentStatus() ) );
    }


    @Test
    public void testSeveralUsersCreateEnvInParallel() throws InterruptedException, ExecutionException
    {
        HubClient hubClient1 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient1.login( "a@od.com", "abc" );

        HubClient hubClient2 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient2.login( "abdisamat@mail.ru", "abc" );

        HubClient hubClient3 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient3.login( "samsonbek@gmail.com", "abc" );

        HubClient hubClient4 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient4.login( "abdysamat.mamutov@gmail.com", "abc" );

        HubClient hubClient5 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient5.login( "amamutov@optimal-dynamics.com", "abc" );


        List<CompletableFuture> futures = Lists.newArrayList();

        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient1, "one1", 1, PEER1_ID, PEER1_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient1, "one2", 1, PEER1_ID, PEER1_RH1_ID, PEER2_ID, PEER2_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient1, "one3", 1, PEER2_ID, PEER2_MH_ID, PEER3_ID, PEER3_MH_ID ) ) );

        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient2, "two1", 1, PEER2_ID, PEER2_MH_ID ) ) );
        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient2, "two2", 1, PEER3_ID, PEER3_MH_ID ) ) );
        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient2, "two3", 1, PEER1_ID, PEER1_RH1_ID ) ) );

        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient3, "three1", 1, PEER1_ID, PEER1_MH_ID ) ) );
        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient3, "three2", 1, PEER3_ID, PEER3_MH_ID ) ) );
        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient3, "three3", 1, PEER2_ID, PEER2_MH_ID ) ) );

        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient4, "four1", 1, PEER2_ID, PEER2_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient4, "four2", 1, PEER1_ID, PEER1_RH1_ID, PEER3_ID, PEER3_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient4, "four3", 1, PEER1_ID, PEER1_MH_ID, PEER2_ID, PEER2_MH_ID ) ) );

        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient5, "five1", 1, PEER1_ID, PEER1_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient5, "five2", 1, PEER1_ID, PEER1_RH1_ID, PEER3_ID, PEER3_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient5, "five3", 1, PEER2_ID, PEER2_MH_ID, PEER3_ID, PEER3_MH_ID ) ) );

        CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).get();


        //wait while there are still env-s under modification
        List<Environment> environments = new ArrayList<>();
        do
        {
            environments.clear();
            Thread.sleep( 30000 );
            environments.addAll( hubClient1.getEnvironments() );
            environments.addAll( hubClient2.getEnvironments() );
            environments.addAll( hubClient3.getEnvironments() );
            environments.addAll( hubClient4.getEnvironments() );
        }
        while ( environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) ).anyMatch(
                e -> e.getEnvironmentStatus() == Environment.EnvironmentStatus.UNDER_MODIFICATION ) );

        //assert that the newly ordered  environments are HEALTHY
        environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) )
                    .forEach( e -> assertEquals( Environment.EnvironmentStatus.HEALTHY, e.getEnvironmentStatus() ) );
    }


    private void createEnvOnOnePeer( HubClient hubClient, String envName, int containerCount, String peerId,
                                     String rhId )
    {
        EnvironmentCreationRequest environmentCreationRequest = hubClient.createRequest( envName );

        for ( int i = 0; i < containerCount; i++ )
        {
            environmentCreationRequest
                    .addNode( envName + "-cont" + i, TEMPLATE_ID, Container.ContainerSize.TINY, peerId, rhId );
        }

        try
        {
            envNames.add( envName );
            hubClient.createEnvironment( environmentCreationRequest );
        }
        catch ( Exception e )
        {
            System.out.println( "Failed to create env. with name: " + envName );
            e.printStackTrace();
        }
    }


    private void createEnvOnTwoPeers( HubClient hubClient, String envName, int contCountOnEachPeer, String peer1Id,
                                      String peer1rhId, String peer2Id, String peer2rhId )
    {
        EnvironmentCreationRequest envCreationRequest = hubClient.createRequest( envName );

        for ( int i = 1; i <= contCountOnEachPeer; i++ )
        {
            envCreationRequest
                    .addNode( envName + "-p1-cont" + i, TEMPLATE_ID, Container.ContainerSize.TINY, peer1Id, peer1rhId );
        }

        for ( int i = 1; i <= contCountOnEachPeer; i++ )
        {
            envCreationRequest
                    .addNode( envName + "-p2-cont" + i, TEMPLATE_ID, Container.ContainerSize.TINY, peer2Id, peer2rhId );
        }

        try
        {
            envNames.add( envName );
            hubClient.createEnvironment( envCreationRequest );
        }
        catch ( Exception e )
        {
            System.out.println( "Failed to create env. with name: " + envName );
            e.printStackTrace();
        }
    }


    @Test
    public void testTwoUsersCreateEnvInParallel() throws InterruptedException, ExecutionException
    {
        HubClient hubClient1 = HubClients.getClient( HubClient.HubEnv.DEV );
        //hubClient1.login( "a@od.com", "abc" );
        hubClient1.login( "abdisamat@mail.ru", "abc" );

        HubClient hubClient2 = HubClients.getClient( HubClient.HubEnv.DEV );
        hubClient2.login( "samsonbek@gmail.com", "abc" );

        List<CompletableFuture> futures = Lists.newArrayList();

        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient1, "abc-111", 1, PEER3_ID, PEER3_MH_ID ) ) );
        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient1, "abc-222", 1, PEER2_ID, PEER2_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient1, "abd-333", 1, PEER3_ID, PEER3_MH_ID, PEER2_ID, PEER2_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient1, "abd-444", 1, PEER3_ID, PEER3_MH_ID, PEER2_ID, PEER2_MH_ID ) ) );

        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient2, "sam-111", 1, PEER3_ID, PEER3_MH_ID ) ) );
        futures.add( CompletableFuture
                .runAsync( () -> this.createEnvOnOnePeer( hubClient2, "sam-222", 1, PEER2_ID, PEER2_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient2, "sam-333", 1, PEER3_ID, PEER3_MH_ID, PEER2_ID, PEER2_MH_ID ) ) );
        futures.add( CompletableFuture.runAsync( () -> this
                .createEnvOnTwoPeers( hubClient2, "sam-444", 1, PEER3_ID, PEER3_MH_ID, PEER2_ID, PEER2_MH_ID ) ) );


        CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).get();


        //wait while there are still env-s under modification
        List<Environment> environments = new ArrayList<>();
        do
        {
            environments.clear();
            Thread.sleep( 30000 );
            environments.addAll( hubClient1.getEnvironments() );
            environments.addAll( hubClient2.getEnvironments() );
        }
        while ( environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) ).anyMatch(
                e -> e.getEnvironmentStatus() == Environment.EnvironmentStatus.UNDER_MODIFICATION ) );

        //assert that the newly ordered  environments are HEALTHY
        environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) )
                    .forEach( e -> assertEquals( Environment.EnvironmentStatus.HEALTHY, e.getEnvironmentStatus() ) );
    }
}
