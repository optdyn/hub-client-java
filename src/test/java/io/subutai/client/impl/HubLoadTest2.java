package io.subutai.client.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.client.api.Container;
import io.subutai.client.api.Environment;
import io.subutai.client.api.EnvironmentCreationRequest;
import io.subutai.client.api.HubClient;

import static junit.framework.TestCase.assertEquals;


public class HubLoadTest2
{
    // https://devcdn.subutai.io:8338/kurjun/rest/template/info?id=ea886a22-2994-481e-8354-3a3032c598ba
    private static final String TEMPLATE_ID = "12805646-9684-48af-986c-07ae9ff69d12";

    private static final String EMAIL = "a@od.com";
    private static final String PASSWORD = "zzzzzz";
    private static final int NUM_OF_PARALLEL_ENV_CREATIONS = 15;
    private static final int NUM_OF_CONTAINERS_PER_ENV = 1;
    private HubClient hubClient;
    private Set<String> envNames = Sets.newConcurrentHashSet();

    private static int envCounter = 1;
    private static int contCounter = 1;

//    private static final String PEER1_ID = "2A4B0F3B155B3E5675DDA3981011973D049A4E3E";
//    private static final String PEER1_MH_ID = "8FDF91832F35DA11FC7031B881DBEF5AB864559E";
    private static final String PEER1_ID = "759C7B59821AC08BE565DB6B4A5D6D64050E4713";
    private static final String PEER1_MH_ID = "43DE9CB5F92900D4D12F6CF45150F0555B71D080";
    private static final String PEER1_RH1_ID = "BAE02769D355F2C6100267654708F1A8376FBA9B";

//    private static final String PEER2_ID = "B987E720E63F4D09CD0908952FB7D2DF608630A5";
//    private static final String PEER2_MH_ID = "14FE85B4AF6A47D250A576EA3E565AAA22B09C2A";
//    private static final String PEER2_RH1_ID = "14FE85B4AF6A47D250A576EA3E565AAA22B09C2A";
    // master-peer-eu-1
    private static final String PEER2_ID = "EB02A6415B956E212AD275C749600C49FA63CDAD";
    private static final String PEER2_MH_ID = "46487165CE7BEA0114AB71EE7FC38BDD5957F4BB";

//    private static final String PEER3_ID = "5DCC6D98F08E9F10513342DAE0067A8EC8FC93B7";
//    private static final String PEER3_MH_ID = "5A2D10AE0A1DD383C54B7CE1407B435073CE177F";
    private static final String PEER3_ID = "50DA020D572FF3C8EB1BDF919E60D3EF70BB80C4";
    private static final String PEER3_MH_ID = "14B399E7F4C815FC5F574A5972C4EE65678965F5";

    // master-peer-eu-2
//    private static final String PEER4_ID = "08C0510B8F25CFBE0F7DA0AA68BD65E2C8A6313C";
//    private static final String PEER4_MH_ID = "BFDF6A62CB78FEE79AFD806DBD00261C4246E247";

    // master-peer-us-2
//    private static final String PEER5_ID = "22326577364CD867623480D6C8A17DE83B37F22E";
//    private static final String PEER5_MH_ID = "003B7D4CF50F3FB1B2300002DC8FC8051A438D10";


    @Before
    public void setUp() throws Exception
    {
        // create client for DEV Hub
        hubClient = HubClients.getClient( HubClient.HubEnv.DEV );

        //login to Hub
        hubClient.login( EMAIL, PASSWORD );
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
}
