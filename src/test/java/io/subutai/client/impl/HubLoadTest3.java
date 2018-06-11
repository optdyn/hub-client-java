package io.subutai.client.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.client.hub.api.Container;
import io.subutai.client.hub.api.Environment;
import io.subutai.client.hub.api.EnvironmentCreationRequest;
import io.subutai.client.hub.api.HubClient;
import io.subutai.client.hub.impl.HubClients;

import static junit.framework.TestCase.assertEquals;


/*
test for masterbazaar
 */
public class HubLoadTest3
{
    private static final String TEMPLATE_ID = "82570b29-2ec6-4777-be6a-29e77262ef96"; // debian-stretch:0.4.1

    private static final int NUM_OF_PARALLEL_ENV_CREATIONS = 20;
    private static final int NUM_OF_CONTAINERS_PER_ENV = 1;

    private Set<String> envNames = Sets.newConcurrentHashSet();

    // 2698: qavms-kg0_mast-AF
    private static final String PEER1_ID = "AF5E99107114F82108BE3D0D5A1FE5236FA17BF5";
    private static final String PEER1_MH_ID = "0742F5D215D56E573D4ABE752442EB4D8DBA00F6";

    // 3001: qavms-kg2_mast-BD // cannot accommodate
    private static final String PEER2_ID = "BD07D037B2EF417CD2676FBE3354810F037827FD";
    private static final String PEER2_MH_ID = "7249E495C93A844CD9BD37F88FCAB8EF2E5DA798";

    // 2706: qavms-kg3_mast-B8 // cannot accommodate
    private static final String PEER3_ID = "B881B527A6E93674C84E4806B95CF514B2003B82";
    private static final String PEER3_MH_ID = "EC327880B06CEBDA2BF73428745E757A6DA391B5";

    // 2993: eu2
    private static final String PEER4_ID = "7EB3E3974A9F27B97F1DC99A93B5AF2973CAB446";
    private static final String PEER4_MH_ID = "BF51AD96B7D2D605C81170097BC0E57FEDB63B90";

    // 3008: master-peer-us-1
    private static final String PEER5_ID = "267A3E9423FD7E820FD54FBF027C73C4D5F3B6B3";
    private static final String PEER5_MH_ID = "4C37868E41D773617FF58E86F69D8CDCA56C0FE5";

    // 3000: master-peer-us-2
    private static final String PEER6_ID = "74F1854507B4D774F3EDA30F998477024A661A2E";
    private static final String PEER6_MH_ID = "48950F0B8504075CEC5D294DBCFF3A13EAEDDD4E";

    // 3005: master-peer-eu-1
    private static final String PEER7_ID = "8CF32767B21BE05D4189A819C7EB927955F71171";
    private static final String PEER7_MH_ID = "9EADD7962582F7ACFB7A3352D88EF8E084CB4449";

    // 2997: master-peer-eu-2
    private static final String PEER8_ID = "382207CDDBA41AC7BBEF66794634DBEF36A07903";
    private static final String PEER8_MH_ID = "5008420DE87EFF494CB3E56C5627A9EEF222F6B2";


    @Before
    public void setUp() throws Exception
    {
    }


    @Test
    public void testSeveralUsersCreateEnvInParallel() throws InterruptedException, ExecutionException
    {
        List<HubClient> clients = Lists.newArrayList();

        HubClient hubClient1 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient1.login( "a@od.com", "zzzzzzzzz" );
        clients.add( hubClient1 );

        HubClient hubClient2 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient2.login( "abdisamat@mail.ru", "zzzzzzzz" );
        clients.add( hubClient2 );

        HubClient hubClient3 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient3.login( "samsonbek@gmail.com", "zzzzzzzzzz" );
        clients.add( hubClient3 );

        HubClient hubClient4 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient4.login( "abdysamat.mamutov@gmail.com", "zzzzzzzzzzzzzz" );
        clients.add( hubClient4 );

        HubClient hubClient5 = HubClients.getClient( HubClient.HubEnv.STAGE );
        hubClient5.login( "amamutov@optimal-dynamics.com", "zzzzzzzzzzz" );
        clients.add( hubClient5 );


        List<Map<String, Set<String>>> envTopologies = Lists.newArrayList();

        // 2 PEERS

        envTopologies.add( ImmutableMap
                .of( PEER1_ID, Sets.newHashSet( PEER1_MH_ID ), PEER8_ID, Sets.newHashSet( PEER8_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER1_ID, Sets.newHashSet( PEER1_MH_ID ), PEER3_ID, Sets.newHashSet( PEER3_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER2_ID, Sets.newHashSet( PEER2_MH_ID ), PEER4_ID, Sets.newHashSet( PEER4_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER3_ID, Sets.newHashSet( PEER3_MH_ID ), PEER5_ID, Sets.newHashSet( PEER5_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER1_ID, Sets.newHashSet( PEER1_MH_ID ), PEER3_ID, Sets.newHashSet( PEER3_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER6_ID, Sets.newHashSet( PEER6_MH_ID ), PEER7_ID, Sets.newHashSet( PEER7_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER4_ID, Sets.newHashSet( PEER4_MH_ID ), PEER8_ID, Sets.newHashSet( PEER8_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER1_ID, Sets.newHashSet( PEER1_MH_ID ), PEER7_ID, Sets.newHashSet( PEER7_MH_ID ) ) );

        // 3 PEERS

        envTopologies.add( ImmutableMap
                .of( PEER1_ID, Sets.newHashSet( PEER1_MH_ID ), PEER2_ID, Sets.newHashSet( PEER2_MH_ID ),
                        PEER3_ID, Sets.newHashSet( PEER3_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER8_ID, Sets.newHashSet( PEER8_MH_ID ), PEER4_ID, Sets.newHashSet( PEER4_MH_ID ),
                        PEER5_ID, Sets.newHashSet( PEER5_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER3_ID, Sets.newHashSet( PEER3_MH_ID ), PEER6_ID, Sets.newHashSet( PEER6_MH_ID ),
                        PEER8_ID, Sets.newHashSet( PEER8_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER3_ID, Sets.newHashSet( PEER3_MH_ID ), PEER4_ID, Sets.newHashSet( PEER4_MH_ID ),
                        PEER1_ID, Sets.newHashSet( PEER1_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER3_ID, Sets.newHashSet( PEER3_MH_ID ), PEER4_ID, Sets.newHashSet( PEER4_MH_ID ),
                        PEER8_ID, Sets.newHashSet( PEER8_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER4_ID, Sets.newHashSet( PEER4_MH_ID ), PEER6_ID, Sets.newHashSet( PEER6_MH_ID ),
                        PEER8_ID, Sets.newHashSet( PEER8_MH_ID ) ) );
        envTopologies.add( ImmutableMap
                .of( PEER1_ID, Sets.newHashSet( PEER1_MH_ID ), PEER7_ID, Sets.newHashSet( PEER7_MH_ID ),
                        PEER8_ID, Sets.newHashSet( PEER8_MH_ID ) ) );



        List<CompletableFuture> futures = Lists.newArrayList();

        for ( int i = 0; i < envTopologies.size(); i++ )
        {
            for ( int c = 0; c < clients.size(); c++ )
            {
                String envName = "client" + c + "-env" + ( i + 1 );
                final int ii = i;
                HubClient client = clients.get( c );

                futures.add( CompletableFuture.runAsync(
                        () -> this.createEnvironmentOnPeers( client, envName, envTopologies.get( ii ) ) ) );
            }
        }

        // start threads
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
            environments.addAll( hubClient5.getEnvironments() );
        }
        while ( environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) ).anyMatch(
                e -> e.getEnvironmentStatus() == Environment.EnvironmentStatus.UNDER_MODIFICATION ) );

        //assert that the newly ordered  environments are HEALTHY
        environments.stream().filter( e -> envNames.contains( e.getEnvironmentName().toLowerCase() ) )
                    .forEach( e -> assertEquals( Environment.EnvironmentStatus.HEALTHY, e.getEnvironmentStatus() ) );
    }


    private void createEnvironmentOnPeers( HubClient hubClient, String envName, Map<String, Set<String>> peers )
    {
        EnvironmentCreationRequest envCreationRequest = hubClient.createRequest( envName );

        for ( final Map.Entry<String, Set<String>> peerEntry : peers.entrySet() )
        {
            for ( final String rh : peerEntry.getValue() )
            {
                envCreationRequest
                        .addNode( envName, TEMPLATE_ID, Container.ContainerSize.TINY, peerEntry.getKey(), rh );
            }
        }

        envNames.add( envName );

        try
        {
            hubClient.createEnvironment( envCreationRequest );
        }
        catch ( Exception e )
        {
            System.out.println( "Failed to create env. with name: " + envName );
            e.printStackTrace();
        }
    }
}
