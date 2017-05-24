package io.subutai.client.impl;


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
import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.Peer;
import io.subutai.client.api.Template;
import io.subutai.client.api.dto.CreateEnvironmentDto;
import io.subutai.client.api.dto.CreateNodeDto;
import io.subutai.client.api.dto.DestroyNodeDto;
import io.subutai.client.api.dto.ModifyEnvironmentDto;

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
    private CreateEnvironmentDto createEnvironmentDto;
    @Mock
    private Template template;
    @Mock
    ModifyEnvironmentDto modifyEnvironmentDto;


    @Before
    public void setUp() throws Exception
    {
        hubClient = ( HubClientImplementation ) spy( HubClients.getClient( HubClient.HubEnv.DEV ) );
        doReturn( response ).when( hubClient ).execute( any( HttpRequestBase.class ) );
        returnHttpCode( HttpStatus.SC_OK );
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
        doReturn( Lists.newArrayList( template ) ).when( hubClient ).parse( eq( response ), any( TypeToken.class ) );

        hubClient.getTemplates();

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        returnHttpCode( HttpStatus.SC_CREATED );
        doReturn( Lists.newArrayList( template ) ).when( hubClient ).getTemplates();
        doReturn( "template" ).when( hubClient ).getTemplateNameById( anyList(), anyString() );
        CreateNodeDto node = mock( CreateNodeDto.class );
        doReturn( Lists.newArrayList( node ) ).when( createEnvironmentDto ).getNodes();
        doReturn( "" ).when( hubClient ).toJson( createEnvironmentDto );
        doReturn( "template" ).when( node ).getTemplateName();

        hubClient.createEnvironment( createEnvironmentDto );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    public void testModifyEnvironment() throws Exception
    {
        doReturn( Lists.newArrayList( template ) ).when( hubClient ).getTemplates();
        doReturn( "template" ).when( hubClient ).getTemplateNameById( anyList(), anyString() );
        CreateNodeDto createNodeDto = mock( CreateNodeDto.class );
        doReturn( Lists.newArrayList( createNodeDto ) ).when( modifyEnvironmentDto ).getNodesToAdd();
        doReturn( "" ).when( hubClient ).toJson( modifyEnvironmentDto );
        doReturn( "template" ).when( createNodeDto ).getTemplateName();
        DestroyNodeDto destroyNodeDto = new DestroyNodeDto( "ID" );
        doReturn( Lists.newArrayList( destroyNodeDto ) ).when( modifyEnvironmentDto ).getNodesToRemove();

        hubClient.modifyEnvironment( modifyEnvironmentDto );

        verify( hubClient ).execute( any( HttpRequestBase.class ) );
    }


    @Test
    @Ignore
    public void testRealCreateEnvironment() throws Exception
    {
        reset( hubClient );
        String templateId = "a697e70f3fc538b4f4763588a7868388";//master
        String peerId = "F56B2CB82E5D4B8A52F1642EB229CB4027DEFA20";
        String rhId = "0785A1DF7CB770F3C135481FEEB2B89BCBE2FEA9";

        hubClient.login( "test.d@mail.com", "test" );

        CreateEnvironmentDto createEnvironmentDto = new CreateEnvironmentDto( "test-env" );
        createEnvironmentDto.addNode( "test-container", templateId, ContainerSize.SMALL, peerId, rhId );

        hubClient.createEnvironment( createEnvironmentDto );
    }


    @Test
//    @Ignore
    public void testRealModifyEnvironment() throws Exception
    {
        reset( hubClient );
        String templateId = "a697e70f3fc538b4f4763588a7868388";//master
        String peerId = "F56B2CB82E5D4B8A52F1642EB229CB4027DEFA20";
        String rhId = "0785A1DF7CB770F3C135481FEEB2B89BCBE2FEA9";
        String envId = "dc183f2c-df1e-4056-a6b0-e750cfe1af63";
        String contIt = "14FF171AB9DA787CDBBFBC69BCE3F6FBEFB3FBBE";

        hubClient.login( "test.d@mail.com", "test" );

        ModifyEnvironmentDto modifyEnvironmentDto = new ModifyEnvironmentDto( envId );
        modifyEnvironmentDto.addNode( "test-container222", templateId, ContainerSize.SMALL, peerId, rhId );
        modifyEnvironmentDto.removeNode( contIt );

        hubClient.modifyEnvironment( modifyEnvironmentDto );
    }
}
