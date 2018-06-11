package io.subutai.client.hub.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import com.google.common.collect.Lists;

import io.subutai.client.hub.api.HubClient;
import io.subutai.client.hub.api.Peer;
import io.subutai.client.hub.api.Template;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HubClientImplementationTest2
{

    private static final String USERNAME = "abdysamat";
    private static final String EMAIL = "a@od.com";
    private static final String PASSWORD = "zzzzzzzz";
    private static final String TEMPLATE_ID = "a697e70f3fc538b4f4763588a7868388";
    private static final String PEER_ID = "487F38E4775ABB36BA202EF0B22F24CD159BF9FD";
    private static final String RH_ID = "25127DD45F45417738248549BCEF91DF28AC3854";
    private static final String ENVIRONMENT_ID = "3bd12be7-e2f7-4884-9a71-20e21381e2e9";
    private static final String CONTAINER_ID = "1648E5166B5160DCB47CDF60D269772CC3337E1E";
    private static final String SSH_KEY =
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCjUo/8VklFC8cRyHE502tUXit15L8Qg2z/47c6PMpQThR0sjhURgoILms"
                    + "/IX180yGqgkpjdX08MIkmANhbXDmSFh6T4lUzqGGoC7lerePwkA2yJWlsP+7JKk9oDSaYJ3lkfvKZnz8ZG7JS1jg"
                    + "+sRiTsYYfyANHBJ8sDAK+eNDDms1oorrxk704r8oeNuRaE4BNKhVO4wpRJHEo4/uztLB0jkvG5OUFea5E0jCk"
                    + "+tUK4R7kJBecYQGkJj4ILt/cAGrY0sg8Ol+WBOq4ex3zCF1zJrdJCxW4t2NUyNfCxW7kV2uUhbWNuj+n"
                    +
                    "/I5a8CDrMJsJLqdgC3EQ17uRy41GHbTwBQs0q2gwfBpefHFXokWwxu06hk0jfwFHWm9xRT79a56hr101Fy4uNjzzVtrWDS4end9VC7bt7Xf/kDxx7FB9DW1wfaYMcCp6YD5O8ENpl35gK35ZXtT5BP2GBoxHGlPdF4PObMCNi5ATtO/gLD8kW1LutO2ldsaY4sHm/JG55UNrpQCpIYe6QfkHsO+fX9/WmjP+iTDdHs1untgurvk5KdhtQxecTvTk3M/ewzHZbEbzYJYzFOsy5f6FQ8U/ckw8PejBzGDUiMGTJXl+GjV9VV3BmkKKeqD5uKu+gta5dynbdfU4r7heAV6oxan2x/rg9iHpOklIRtu2chJYJUq7lQ== dilshat.aliev@gmail.com";
    private static final long USER_ID = 164;
    private static final String NEW_PEER_NAME = "New Peer-Name";
    private static final String DOMAIN = "domain";

    private HubClientImplementation hubClient;

    @Mock
    private CloseableHttpResponse response;
    @Mock
    private PeerImpl peer;
    @Mock
    private EnvironmentImpl environment;
    @Mock
    private EnvironmentCreationRequestImpl createEnvironmentRequest;
    @Mock
    private Template template;
    @Mock
    private EnvironmentModificationRequestImpl modifyEnvironmentRequest;
    @Mock
    private KurjunClient kurjunClient;


    @Before
    public void setUp() throws Exception
    {
        hubClient = ( HubClientImplementation ) spy( HubClients.getClient( HubClient.HubEnv.DEV ) );
        doReturn( response ).when( hubClient ).execute( any( HttpRequestBase.class ) );
        returnHttpCode( HttpStatus.SC_OK );
        hubClient.kurjunClient = kurjunClient;
    }


    private void returnHttpCode( int httpCode )
    {
        StatusLine statusLine = mock( StatusLine.class );
        doReturn( httpCode ).when( statusLine ).getStatusCode();
        doReturn( statusLine ).when( response ).getStatusLine();
    }


    @Ignore
    @Test
    public void testUpdatePeerScope() throws Exception
    {
        hubClient.updatePeerScope( PEER_ID, Peer.Scope.PRIVATE );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testUpdatePeerName() throws Exception
    {
        List<HubClient> clients = Lists.newArrayList();

        HubClient hubClient1 = HubClients.getClient( HubClient.HubEnv.HUB1 );
        hubClient1.login( "a@od.com", "abc" );
        clients.add( hubClient1 );

        HubClient hubClient2 = HubClients.getClient( HubClient.HubEnv.HUB2 );
        hubClient2.login( "a@od.com", "abc" );
        clients.add( hubClient2 );

        HubClient hubClient3 = HubClients.getClient( HubClient.HubEnv.HUB3 );
        hubClient3.login( "a@od.com", "abc" );
        clients.add( hubClient3 );

//        HubClient hubClient4 = HubClients.getClient( HubClient.HubEnv.HUB4 );
//        hubClient4.login( "a@od.com", "abc" );
//        clients.add( hubClient4 );

        List<CompletableFuture> futures = Lists.newArrayList();

        for ( int i = 0; i < 10; i++ )
        {
//            final int ii = i + 1;
            futures.add( CompletableFuture.runAsync( () -> hubClient1.updatePeerName( PEER_ID, "one-"+ UUID.randomUUID() ) ) );
            futures.add( CompletableFuture.runAsync( () -> hubClient2.updatePeerName( PEER_ID, "two-"+UUID.randomUUID() ) ) );
            futures.add( CompletableFuture.runAsync( () -> hubClient3.updatePeerName( PEER_ID, "three-"+UUID.randomUUID() ) ) );
//            futures.add( CompletableFuture.runAsync( () -> hubClient4.updatePeerName( PEER_ID, "four-"+UUID.randomUUID() ) ) );
        }

        // start threads
        CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).get();

//        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }
}
