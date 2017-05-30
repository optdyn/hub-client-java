package io.subutai.client.impl;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
import com.google.gson.reflect.TypeToken;

import io.subutai.client.api.ContainerSize;
import io.subutai.client.api.CreateEnvironmentRequest;
import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.ModifyEnvironmentRequest;
import io.subutai.client.api.Peer;
import io.subutai.client.api.Template;
import io.subutai.client.pgp.SignerTest;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HubClientImplementationTest
{

    private static final String USERNAME = "dummy-user";
    private static final String PASSWORD = "dummy-pwd";
    private static final String ENVIRONMENT_ID = "bc8b8e43-0416-4ad4-a002-a4b8ad61b1f2";
    private static final String CONTAINER_ID = "33416CAEC7D07CABD7C73AB0FE1EF92DBA27FCB6";
    private static final String SSH_KEY =
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCjUo/8VklFC8cRyHE502tUXit15L8Qg2z/47c6PMpQThR0sjhURgoILms"
                    + "/IX180yGqgkpjdX08MIkmANhbXDmSFh6T4lUzqGGoC7lerePwkA2yJWlsP+7JKk9oDSaYJ3lkfvKZnz8ZG7JS1jg"
                    + "+sRiTsYYfyANHBJ8sDAK+eNDDms1oorrxk704r8oeNuRaE4BNKhVO4wpRJHEo4/uztLB0jkvG5OUFea5E0jCk"
                    + "+tUK4R7kJBecYQGkJj4ILt/cAGrY0sg8Ol+WBOq4ex3zCF1zJrdJCxW4t2NUyNfCxW7kV2uUhbWNuj+n"
                    +
                    "/I5a8CDrMJsJLqdgC3EQ17uRy41GHbTwBQs0q2gwfBpefHFXokWwxu06hk0jfwFHWm9xRT79a56hr101Fy4uNjzzVtrWDS4end9VC7bt7Xf/kDxx7FB9DW1wfaYMcCp6YD5O8ENpl35gK35ZXtT5BP2GBoxHGlPdF4PObMCNi5ATtO/gLD8kW1LutO2ldsaY4sHm/JG55UNrpQCpIYe6QfkHsO+fX9/WmjP+iTDdHs1untgurvk5KdhtQxecTvTk3M/ewzHZbEbzYJYzFOsy5f6FQ8U/ckw8PejBzGDUiMGTJXl+GjV9VV3BmkKKeqD5uKu+gta5dynbdfU4r7heAV6oxan2x/rg9iHpOklIRtu2chJYJUq7lQ== dilshat.aliev@gmail.com";

    private HubClientImplementation hubClient;

    @Mock
    private CloseableHttpResponse response;
    @Mock
    private PeerImpl peer;
    @Mock
    private EnvironmentImpl environment;
    @Mock
    private CreateEnvironmentRequestImpl createEnvironmentRequest;
    @Mock
    private Template template;
    @Mock
    private ModifyEnvironmentRequestImpl modifyEnvironmentRequest;
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
        hubClient.login( USERNAME, PASSWORD );

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
    public void testGetPeers() throws Exception
    {
        doReturn( Lists.newArrayList( peer ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        List<Peer> peers = hubClient.getPeers();

        assertTrue( peers.contains( peer ) );
    }


    @Test
    public void testStopContainer() throws Exception
    {
        hubClient.stopContainer( ENVIRONMENT_ID, CONTAINER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testStartContainer() throws Exception
    {
        hubClient.startContainer( ENVIRONMENT_ID, CONTAINER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        returnHttpCode( HttpStatus.SC_NO_CONTENT );

        hubClient.destroyContainer( ENVIRONMENT_ID, CONTAINER_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testDestroyEnvironment() throws Exception
    {
        returnHttpCode( HttpStatus.SC_NO_CONTENT );

        hubClient.destroyEnvironment( ENVIRONMENT_ID );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testAddSshKey() throws Exception
    {
        returnHttpCode( HttpStatus.SC_CREATED );

        hubClient.addSshKey( ENVIRONMENT_ID, SSH_KEY );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testRemoveSshKey() throws Exception
    {
        returnHttpCode( HttpStatus.SC_NO_CONTENT );

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
        returnHttpCode( HttpStatus.SC_CREATED );
        doReturn( Lists.newArrayList( template ) ).when( hubClient ).getTemplates();
        doReturn( "template" ).when( hubClient ).getTemplateNameById( anyList(), anyString() );
        CreateEnvironmentRequestImpl.Node node = mock( CreateEnvironmentRequestImpl.Node.class );
        doReturn( Lists.newArrayList( node ) ).when( createEnvironmentRequest ).getNodes();
        doReturn( "" ).when( hubClient ).toJson( createEnvironmentRequest );
        doReturn( "template" ).when( node ).getTemplateName();

        hubClient.createEnvironment( createEnvironmentRequest );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testModifyEnvironment() throws Exception
    {
        doReturn( Lists.newArrayList( template ) ).when( hubClient ).getTemplates();
        doReturn( "template" ).when( hubClient ).getTemplateNameById( anyList(), anyString() );
        CreateEnvironmentRequestImpl.Node createNode = mock( CreateEnvironmentRequestImpl.Node.class );
        doReturn( Lists.newArrayList( createNode ) ).when( modifyEnvironmentRequest ).getNodesToAdd();
        doReturn( "" ).when( hubClient ).toJson( modifyEnvironmentRequest );
        doReturn( "template" ).when( createNode ).getTemplateName();
        ModifyEnvironmentRequestImpl.Node destroyNodeDto = mock( ModifyEnvironmentRequestImpl.Node.class );
        doReturn( Lists.newArrayList( destroyNodeDto ) ).when( modifyEnvironmentRequest ).getNodesToRemove();

        hubClient.modifyEnvironment( modifyEnvironmentRequest );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    @Ignore
    public void testRealGetTemplates() throws Exception
    {
        hubClient = ( HubClientImplementation ) HubClients
                .getClient( HubClient.HubEnv.DEV, "C:\\Users\\Dilshat\\Desktop\\dilshat.aliev_all.asc", "" );
        List<Template> templates = hubClient.getTemplates();
        for ( Template template : templates )
        {
            System.out.println( template );
        }
    }


    @Test
    @Ignore
    public void testRealCreateEnvironment() throws Exception
    {
        reset( hubClient );
        String templateId = "a697e70f3fc538b4f4763588a7868388";//master
        String peerId = "8BC9E203393B29DECF485BF8934A1421E3ECB58A";
        String rhId = "B2E4DBC6200D6592298F7CE2D89CD0E8E61E6326";

        hubClient.login( "test.d@mail.com", "test" );

        CreateEnvironmentRequest createEnvironmentRequest = hubClient.createRequest( "test-env" );
        createEnvironmentRequest.addNode( "test-container", templateId, ContainerSize.SMALL, peerId, rhId );

        hubClient.createEnvironment( createEnvironmentRequest );
    }


    @Test
    @Ignore
    public void testRealModifyEnvironment() throws Exception
    {
        reset( hubClient );
        String templateId = "a697e70f3fc538b4f4763588a7868388";//master
        String peerId = "8BC9E203393B29DECF485BF8934A1421E3ECB58A";
        String rhId = "B2E4DBC6200D6592298F7CE2D89CD0E8E61E6326";
        String envId = "f0740e29-1519-4dcb-91d1-99c91bd0326b";
        String contIt = "06C6BE754504777A29F3F77EA2450082B7614323";

        hubClient.login( "test.d@mail.com", "test" );

        ModifyEnvironmentRequest modifyEnvironmentRequest = hubClient.modifyRequest( envId );
        modifyEnvironmentRequest.addNode( "test-container222", templateId, ContainerSize.SMALL, peerId, rhId );
        modifyEnvironmentRequest.removeNode( contIt );

        hubClient.modifyEnvironment( modifyEnvironmentRequest );
    }


    @Test
    public void testRealCreateHubClientWithKey() throws Exception
    {
        File keyFile = File.createTempFile( "test-keys", ".tmp" );
        Files.copy( SignerTest.getKeyFileAsStream(), keyFile.toPath(), StandardCopyOption.REPLACE_EXISTING );

        HubClients.getClient( HubClient.HubEnv.DEV, keyFile.getPath(), "" );
    }
}
