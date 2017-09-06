package io.subutai.client.impl;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

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
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

import io.subutai.client.api.Container.ContainerSize;
import io.subutai.client.api.Domain;
import io.subutai.client.api.DomainAssignment;
import io.subutai.client.api.Environment;
import io.subutai.client.api.EnvironmentCreationRequest;
import io.subutai.client.api.EnvironmentModificationRequest;
import io.subutai.client.api.FriendsInfo;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.Organization;
import io.subutai.client.api.Peer;
import io.subutai.client.api.SshKey;
import io.subutai.client.api.Template;
import io.subutai.client.api.User;
import io.subutai.client.pgp.SignerTest;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HubClientImplementationTest
{

    private static final String USERNAME = "test-d";
    private static final String EMAIL = "test.d@mail.com";
    private static final String PASSWORD = "test";
    private static final String TEMPLATE_ID = "a697e70f3fc538b4f4763588a7868388";
    private static final String PEER_ID = "94E0A1C6EB6718A608D8754EBD3BD7BB1F2B36A1";
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


    @Test
    public void testLogin() throws Exception
    {
        hubClient.login( EMAIL, PASSWORD );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetEnvironments() throws Exception
    {
        doReturn( Lists.newArrayList( environment ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        List<Environment> environments = hubClient.getEnvironments();

        assertTrue( environments.contains( environment ) );
    }


    @Test
    public void testGetBalance() throws Exception
    {
        ResultDto resultDto = mock( ResultDto.class );
        doReturn( resultDto ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        Double balance = hubClient.getBalance();

        verify( resultDto ).getValue();
    }


    @Test
    public void testGetPeers() throws Exception
    {
        doReturn( Lists.newArrayList( peer ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        List<Peer> peers = hubClient.getPeers();

        assertTrue( peers.contains( peer ) );
    }


    @Test
    public void testGetSharedPeers() throws Exception
    {
        doReturn( Lists.newArrayList( peer ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        List<Peer> peers = hubClient.getSharedPeers();

        assertTrue( peers.contains( peer ) );
    }


    @Test
    public void testGetOwnPeers() throws Exception
    {
        doReturn( Lists.newArrayList( peer ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        List<Peer> peers = hubClient.getOwnPeers();

        assertTrue( peers.contains( peer ) );
    }


    @Test
    public void testGetFavoritePeers() throws Exception
    {
        doReturn( Lists.newArrayList( peer ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        List<Peer> peers = hubClient.getFavoritePeers();

        assertTrue( peers.contains( peer ) );
    }


    @Test
    public void testGetPublicPeers() throws Exception
    {
        doReturn( Lists.newArrayList( peer ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        List<Peer> peers = hubClient.getPublicPeers();

        assertTrue( peers.contains( peer ) );
    }


    @Test
    public void testStopContainer() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        hubClient.stopContainer( ENVIRONMENT_ID, CONTAINER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testStartContainer() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        hubClient.startContainer( ENVIRONMENT_ID, CONTAINER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        hubClient.destroyContainer( ENVIRONMENT_ID, CONTAINER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testDestroyEnvironment() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        hubClient.destroyEnvironment( ENVIRONMENT_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void tesGetSshKeys() throws Exception
    {
        doReturn( Lists.newArrayList( SSH_KEY ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getSshKeys( ENVIRONMENT_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testAddSshKey() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        hubClient.addSshKey( ENVIRONMENT_ID, SSH_KEY );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testRemoveSshKey() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        hubClient.removeSshKey( ENVIRONMENT_ID, SSH_KEY );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void tesGetTemplates() throws Exception
    {
        hubClient.getTemplates();

        verify( kurjunClient ).getTemplates( "" );
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        doReturn( Lists.newArrayList( template ) ).when( hubClient ).getTemplates();
        doReturn( "template" ).when( hubClient ).getTemplateNameById( anyList(), anyString() );
        EnvironmentCreationRequestImpl.Node node = mock( EnvironmentCreationRequestImpl.Node.class );
        doReturn( Lists.newArrayList( node ) ).when( createEnvironmentRequest ).getNodes();
        doReturn( "" ).when( hubClient ).toJson( createEnvironmentRequest );
        doReturn( "template" ).when( node ).getTemplateName();
        EnvironmentRefImpl user = mock( EnvironmentRefImpl.class );
        doReturn( user ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.createEnvironment( createEnvironmentRequest );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testModifyEnvironment() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );
        doReturn( Lists.newArrayList( template ) ).when( hubClient ).getTemplates();
        doReturn( "template" ).when( hubClient ).getTemplateNameById( anyList(), anyString() );
        EnvironmentCreationRequestImpl.Node createNode = mock( EnvironmentCreationRequestImpl.Node.class );
        doReturn( Lists.newArrayList( createNode ) ).when( modifyEnvironmentRequest ).getNodesToAdd();
        doReturn( "" ).when( hubClient ).toJson( modifyEnvironmentRequest );
        doReturn( "template" ).when( createNode ).getTemplateName();
        EnvironmentModificationRequestImpl.Node destroyNodeDto = mock( EnvironmentModificationRequestImpl.Node.class );
        doReturn( Lists.newArrayList( destroyNodeDto ) ).when( modifyEnvironmentRequest ).getNodesToRemove();

        hubClient.modifyEnvironment( modifyEnvironmentRequest );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testCreateHubClientWithKey() throws Exception
    {
        File keyFile = File.createTempFile( "test-keys", ".tmp" );
        Files.copy( SignerTest.getKeyFileAsStream(), keyFile.toPath(), StandardCopyOption.REPLACE_EXISTING );

        HubClients.getClient( HubClient.HubEnv.DEV, keyFile.getPath(), "" );
    }


    @Test
    public void testUpdatePeerScope() throws Exception
    {
        hubClient.updatePeerScope( PEER_ID, Peer.Scope.PRIVATE );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testUpdatePeerName() throws Exception
    {
        hubClient.updatePeerName( PEER_ID, NEW_PEER_NAME );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testSharePeer() throws Exception
    {
        hubClient.sharePeer( PEER_ID, USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testUnsharePeer() throws Exception
    {
        hubClient.unsharePeer( PEER_ID, USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testAddPeerToFavorites() throws Exception
    {
        hubClient.addPeerToFavorites( PEER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testRemovePeerFromFavorites() throws Exception
    {
        hubClient.removePeerFromFavorites( PEER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetUser() throws Exception
    {
        UserImpl user = mock( UserImpl.class );
        doReturn( user ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getUser( USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testFindUserByName() throws Exception
    {
        UserImpl user = mock( UserImpl.class );
        doReturn( user ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.findUserByName( EMAIL );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testFindUserByEmail() throws Exception
    {
        UserImpl user = mock( UserImpl.class );
        doReturn( user ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.findUserByName( EMAIL );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetUserPeers() throws Exception
    {
        doReturn( Lists.newArrayList( peer ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getUserPeers( USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetPeerUsers() throws Exception
    {
        UserImpl user = mock( UserImpl.class );
        doReturn( Lists.newArrayList( user ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getPeerUsers( PEER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetUserOrganizations() throws Exception
    {
        OrganizationImpl organization = mock( OrganizationImpl.class );
        doReturn( Lists.newArrayList( organization ) ).when( hubClient )
                                                      .parse( eq( response ), any( TypeToken.class ) );

        hubClient.getUserOrganizations( USER_ID, false );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetOrganizations() throws Exception
    {
        OrganizationImpl organization = mock( OrganizationImpl.class );
        doReturn( Lists.newArrayList( organization ) ).when( hubClient )
                                                      .parse( eq( response ), any( TypeToken.class ) );

        hubClient.getOrganizations( false );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetDomains() throws Exception
    {
        DomainImpl domain = mock( DomainImpl.class );
        doReturn( Lists.newArrayList( domain ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getDomains();

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetDomainAssignments() throws Exception
    {
        DomainAssignmentImpl assignment = mock( DomainAssignmentImpl.class );
        Map<String, List<DomainAssignment>> map = Maps.newHashMap();
        doReturn( map ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getDomainAssignments();

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testDeleteDomain() throws Exception
    {
        hubClient.deleteDomain( DOMAIN );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testReserveDomain() throws Exception
    {
        hubClient.reserveDomain( DOMAIN );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testGetFriendsInfo() throws Exception
    {
        FriendsInfoImpl friendsInfo = mock( FriendsInfoImpl.class );
        doReturn( friendsInfo ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getFriendsInfo();

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testRequestFriendship() throws Exception
    {
        hubClient.requestFriendship( USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testAcceptFriendship() throws Exception
    {
        hubClient.acceptFriendship( USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testRejectFriendshipRequest() throws Exception
    {
        hubClient.rejectFriendshipRequest( USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testCancelFriendshipRequest() throws Exception
    {
        hubClient.cancelFriendshipRequest( USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testBreakFriendship() throws Exception
    {
        hubClient.breakFriendship( USER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testCreateEnvironmentFromBlueprint() throws Exception
    {
        returnHttpCode( HttpStatus.SC_ACCEPTED );

        hubClient.createEnvironmentFromBlueprint( "blueprint" );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    /******* Real tests *******/

    private void prepare()
    {
        hubClient = ( HubClientImplementation ) HubClients.getClient( HubClient.HubEnv.DEV );
        hubClient.login( EMAIL, PASSWORD );
    }


    @Test
    @Ignore
    public void testRealBreakFriendship() throws Exception
    {
        prepare();

        hubClient.breakFriendship( USER_ID );
    }


    @Test
    @Ignore
    public void testRealCancelFriendshipRequest() throws Exception
    {
        prepare();

        hubClient.cancelFriendshipRequest( USER_ID );
    }


    @Test
    @Ignore
    public void testRealRejectFriendshipRequest() throws Exception
    {
        prepare();

        hubClient.rejectFriendshipRequest( USER_ID );
    }


    @Test
    @Ignore
    public void testRealAcceptFriendship() throws Exception
    {
        prepare();

        hubClient.acceptFriendship( USER_ID );
    }


    @Test
    @Ignore
    public void testRealRequestFriendship() throws Exception
    {
        prepare();

        hubClient.requestFriendship( USER_ID );
    }


    @Test
    @Ignore
    public void testRealGetFriendsInfo() throws Exception
    {
        prepare();

        FriendsInfo friendsInfo = hubClient.getFriendsInfo();

        System.out.println( friendsInfo );
    }


    @Test
    @Ignore
    public void testRealGetDomains() throws Exception
    {
        prepare();

        List<Domain> domains = hubClient.getDomains();

        System.out.println( domains );
    }


    @Test
    @Ignore
    public void testRealGetDomainAssignments() throws Exception
    {
        prepare();

        Map<String, List<DomainAssignment>> assignments = hubClient.getDomainAssignments();

        System.out.println( assignments );
    }


    @Test
    @Ignore
    public void testRealDeleteDomain() throws Exception
    {
        prepare();

        hubClient.deleteDomain( DOMAIN );
    }


    @Test
    @Ignore
    public void testRealReserveDomain() throws Exception
    {
        prepare();

        hubClient.reserveDomain( DOMAIN );
    }


    @Test
    @Ignore
    public void testRealGetUserOrganizations() throws Exception
    {
        prepare();

        List<Organization> organizations = hubClient.getUserOrganizations( USER_ID, false );

        System.out.println( organizations );
    }


    @Test
    @Ignore
    public void testRealGetOrganizations() throws Exception
    {
        prepare();

        List<Organization> organizations = hubClient.getOrganizations( false );

        System.out.println( organizations );
    }


    @Test
    @Ignore
    public void testRealGetUser() throws Exception
    {
        prepare();

        User user = hubClient.getUser( USER_ID );

        System.out.println( user );
    }


    @Test
    @Ignore
    public void testRealGetUserPeers() throws Exception
    {
        prepare();

        List<Peer> peers = hubClient.getUserPeers( USER_ID );

        System.out.println( peers );
    }


    @Test
    @Ignore
    public void testRealFindUserByEmail() throws Exception
    {
        prepare();

        User user = hubClient.findUserByEmail( EMAIL );

        System.out.println( user );
    }


    @Test
    @Ignore
    public void testRealFindUserByName() throws Exception
    {
        prepare();

        User user = hubClient.findUserByName( USERNAME );

        System.out.println( user );
    }


    @Test
    @Ignore
    public void testRealAddPeerToFavorites() throws Exception
    {
        prepare();

        hubClient.addPeerToFavorites( PEER_ID );
    }


    @Test
    @Ignore
    public void testRealRemovePeerFromFavorites() throws Exception
    {
        prepare();

        hubClient.removePeerFromFavorites( PEER_ID );
    }


    @Test
    @Ignore
    public void testRealSharePeer() throws Exception
    {
        prepare();

        hubClient.sharePeer( PEER_ID, USER_ID );
    }


    @Test
    @Ignore
    public void testRealUnsharePeer() throws Exception
    {
        prepare();

        hubClient.unsharePeer( PEER_ID, USER_ID );
    }


    @Test
    @Ignore
    public void testRealUpdatePeerScope() throws Exception
    {
        prepare();

        hubClient.updatePeerScope( PEER_ID, Peer.Scope.SHARED );
    }


    @Test
    @Ignore
    public void testRealUpdatePeerName() throws Exception
    {
        prepare();

        hubClient.updatePeerName( PEER_ID, NEW_PEER_NAME );
    }


    @Test
    @Ignore
    public void testRealGetBalance() throws Exception
    {
        prepare();

        System.out.println( hubClient.getBalance() );
    }


    @Test
    @Ignore
    public void testRealStartContainer() throws Exception
    {
        prepare();

        hubClient.startContainer( ENVIRONMENT_ID, CONTAINER_ID );
    }


    @Test
    @Ignore
    public void testRealStopContainer() throws Exception
    {
        prepare();

        hubClient.stopContainer( ENVIRONMENT_ID, CONTAINER_ID );
    }


    @Test
    @Ignore
    public void testRealDestroyContainer() throws Exception
    {
        prepare();

        hubClient.destroyContainer( ENVIRONMENT_ID, CONTAINER_ID );
    }


    @Test
    @Ignore
    public void testRealDestroyEnvironment() throws Exception
    {
        prepare();

        hubClient.destroyEnvironment( ENVIRONMENT_ID );
    }


    @Test
    @Ignore
    public void testRealGetSshKeys() throws Exception
    {
        prepare();

        List<SshKey> sshKeys = hubClient.getSshKeys( ENVIRONMENT_ID );

        System.out.println( sshKeys );
    }


    @Test
    @Ignore
    public void testRealAddSshKey() throws Exception
    {
        prepare();

        hubClient.addSshKey( ENVIRONMENT_ID, SSH_KEY );
    }


    @Test
    @Ignore
    public void testRealRemoveSshKey() throws Exception
    {
        prepare();

        hubClient.removeSshKey( ENVIRONMENT_ID, SSH_KEY );
    }


    @Test
    @Ignore
    public void testRealGetPeers() throws Exception
    {
        prepare();

        List<Peer> peers = hubClient.getOwnPeers();

        System.out.println( peers );
    }


    @Test
    @Ignore
    public void testRealGetPeerUsers() throws Exception
    {
        prepare();

        List<User> users = hubClient.getPeerUsers( PEER_ID );

        System.out.println( users );
    }


    @Test
    @Ignore
    public void testRealGetEnvironments() throws Exception
    {
        prepare();

        List<Environment> environments = hubClient.getEnvironments();

        System.out.println( environments );
    }


    @Test
    @Ignore
    public void testRealGetTemplates() throws Exception
    {
        hubClient = ( HubClientImplementation ) HubClients
                .getClient( HubClient.HubEnv.DEV, "C:\\Users\\Dilshat\\Desktop\\dilshat.aliev_all.asc", "" );

        List<Template> templates = hubClient.getTemplates();

        System.out.println( templates );
    }


    @Test
    @Ignore
    public void testRealCreateEnvironment() throws Exception
    {
        prepare();
        EnvironmentCreationRequest environmentCreationRequest = hubClient.createRequest( "test-env" );
        environmentCreationRequest.addNode( "test-container1", TEMPLATE_ID, ContainerSize.SMALL, PEER_ID, RH_ID );
        environmentCreationRequest.addNode( "test-container2", TEMPLATE_ID, ContainerSize.SMALL, PEER_ID, RH_ID );

        hubClient.createEnvironment( environmentCreationRequest );
    }


    @Test
    @Ignore
    public void testRealModifyEnvironment() throws Exception
    {
        prepare();
        EnvironmentModificationRequest modifyEnvironmentRequest = hubClient.modifyRequest( ENVIRONMENT_ID );
        modifyEnvironmentRequest.addNode( "test-container3", TEMPLATE_ID, ContainerSize.SMALL, PEER_ID, RH_ID );
        modifyEnvironmentRequest.removeNode( CONTAINER_ID );

        hubClient.modifyEnvironment( modifyEnvironmentRequest );
    }


    @Test
    @Ignore
    public void testRealCreateEnvironmentFromBlueprint() throws Exception
    {
        prepare();

        String blueprint =
                "{\n" + "  \"name\": \"test env\",\n" + "  \"description\": \"test blueprint\",\n" + "  \"nodes\": [\n"
                        + "    {\n" + "      \"name\": \"master\",\n" + "      \"template\": \"master\",\n"
                        + "      \"peer_group\": \"GROUP1\",\n" + "      \"size\": \"TINY\"\n" + "    }\n" + "  ],\n"
                        + "  \"peerGroups\": [\n" + "    {\n" + "      \"name\": \"GROUP1\",\n"
                        + "      \"pricingPref\": {\n" + "        \"maxPrice\": \"200\"\n" + "      },\n"
                        + "      \"hwPreferences\": {\n" + "        \"avgCpuLoad\": \"10\",\n"
                        + "        \"minFreeRam\": \"512MiB\",\n" + "        \"minFreeDiskSpace\": \"500MiB\"\n"
                        + "      },\n" + "      \"zonePreferences\": {},\n" + "      \"proximityPreferences\": {}\n"
                        + "    }\n" + "  ]\n" + "}";

        hubClient.createEnvironmentFromBlueprint( blueprint );
    }
}
