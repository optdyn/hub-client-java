package io.subutai.client.impl;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.subutai.client.api.CreateEnvironmentRequest;
import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.ModifyEnvironmentRequest;
import io.subutai.client.api.OperationFailedException;
import io.subutai.client.api.Peer;
import io.subutai.client.api.Template;


public class HubClientImplementation implements HubClient
{
    private static final String KURJUN_TOKEN_HEADER = "kurjun-token";
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private HttpContext httpContext = new BasicHttpContext();
    private Gson gson = new Gson();
    private final HubEnv hubEnv;
    protected KurjunClient kurjunClient;
    private String pgpKeyFilePath;
    private String pgpKeyPassword;


    HubClientImplementation( HubEnv hubEnv )
    {
        Preconditions.checkNotNull( hubEnv );

        this.hubEnv = hubEnv;
        this.kurjunClient = new KurjunClient( hubEnv );
        this.httpContext.setAttribute( HttpClientContext.COOKIE_STORE, new BasicCookieStore() );
    }


    HubClientImplementation( HubEnv hubEnv, String pgpKeyFilePath, String pgpKeyPassword )
    {
        this( hubEnv );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( pgpKeyFilePath ) );

        this.pgpKeyFilePath = pgpKeyFilePath;
        this.pgpKeyPassword = pgpKeyPassword;
    }


    public void login( final String username, final String password )
    {
        HttpPost httpPost =
                new HttpPost( String.format( "https://%s.subut.ai/rest/v1/client/login", hubEnv.getUrlPrefix() ) );

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "email", username ) );
        nvps.add( new BasicNameValuePair( "password", password ) );
        httpPost.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( "UTF-8" ) ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpPost );

            checkHttpStatus( response, HttpStatus.SC_OK, "login" );
        }
        finally
        {

            close( response );
        }
    }


    public List<Environment> getEnvironments()
    {
        List<Environment> environments = Lists.newArrayList();

        HttpGet httpGet = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/environments", hubEnv.getUrlPrefix() ) );


        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "list environments" );

            List<EnvironmentImpl> envList = parse( response, new TypeToken<List<EnvironmentImpl>>()
            {
            } );

            environments.addAll( envList );
        }
        finally
        {
            close( response );
        }

        return environments;
    }


    public List<Peer> getPeers()
    {
        List<Peer> peers = Lists.newArrayList();

        HttpGet httpGet =
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/peers", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "list peers" );

            List<PeerImpl> peerList = parse( response, new TypeToken<List<PeerImpl>>()
            {
            } );

            peers.addAll( peerList );
        }
        finally
        {
            close( response );
        }

        return peers;
    }


    public void addSshKey( final String envId, final String sshKey )
    {
        HttpPost httpPost = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/ssh-key/add", hubEnv.getUrlPrefix(),
                        envId ) );

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "ssh-key", sshKey ) );
        httpPost.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( "UTF-8" ) ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpPost );

            checkHttpStatus( response, HttpStatus.SC_CREATED, "add ssh key" );
        }
        finally
        {
            close( response );
        }
    }


    public void removeSshKey( final String envId, final String sshKey )
    {
        HttpPost httpPost = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/ssh-key/remove",
                        hubEnv.getUrlPrefix(), envId ) );

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "ssh-key", sshKey ) );
        httpPost.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( "UTF-8" ) ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpPost );

            checkHttpStatus( response, HttpStatus.SC_NO_CONTENT, "remove ssh key" );
        }
        finally
        {
            close( response );
        }
    }


    public void startContainer( final String envId, final String contId )
    {
        HttpPost httpPost = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/containers/%s/start",
                        hubEnv.getUrlPrefix(), envId, contId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpPost );

            checkHttpStatus( response, HttpStatus.SC_OK, "start container" );
        }
        finally
        {
            close( response );
        }
    }


    public void stopContainer( final String envId, final String contId )
    {
        HttpPost httpPost = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/containers/%s/stop",
                        hubEnv.getUrlPrefix(), envId, contId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpPost );

            checkHttpStatus( response, HttpStatus.SC_OK, "stop container" );
        }
        finally
        {
            close( response );
        }
    }


    public void destroyContainer( final String envId, final String contId )
    {
        HttpDelete httpDelete = new HttpDelete(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/containers/%s",
                        hubEnv.getUrlPrefix(), envId, contId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpDelete );

            checkHttpStatus( response, HttpStatus.SC_NO_CONTENT, "destroy container" );
        }
        finally
        {
            close( response );
        }
    }


    public CreateEnvironmentRequest createRequest( final String environmentName )
    {
        return new CreateEnvironmentRequestImpl( environmentName );
    }


    public void createEnvironment( final CreateEnvironmentRequest createEnvironmentRequest )
    {
        Preconditions.checkNotNull( createEnvironmentRequest );
        Preconditions.checkArgument( createEnvironmentRequest instanceof CreateEnvironmentRequestImpl );
        CreateEnvironmentRequestImpl createEnvironmentReq = ( CreateEnvironmentRequestImpl ) createEnvironmentRequest;
        Preconditions.checkArgument( !createEnvironmentReq.getNodes().isEmpty() );

        //WORKAROUND!!!
        List<Template> templates = getTemplates();
        for ( CreateEnvironmentRequestImpl.Node node : createEnvironmentReq.getNodes() )
        {
            node.setTemplateName( getTemplateNameById( templates, node.getTemplateId() ) );

            if ( Strings.isNullOrEmpty( node.getTemplateName() ) )
            {
                throw new OperationFailedException( "Template not found by id " + node.getTemplateId(), null );
            }
        }
        //WORKAROUND!!!

        HttpPost httpPost = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments", hubEnv.getUrlPrefix() ) );

        httpPost.setEntity( new StringEntity( toJson( createEnvironmentReq ), ContentType.APPLICATION_JSON ) );
        httpPost.addHeader( KURJUN_TOKEN_HEADER, getKurjunToken() );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpPost );

            checkHttpStatus( response, HttpStatus.SC_CREATED, "create environment" );
        }
        finally
        {
            close( response );
        }
    }


    public ModifyEnvironmentRequest modifyRequest( final String environmentId )
    {
        return new ModifyEnvironmentRequestImpl( environmentId );
    }


    public void modifyEnvironment( final ModifyEnvironmentRequest modifyEnvironmentRequest )
    {
        Preconditions.checkNotNull( modifyEnvironmentRequest );
        Preconditions.checkArgument( modifyEnvironmentRequest instanceof ModifyEnvironmentRequestImpl );
        ModifyEnvironmentRequestImpl modifyEnvironmentReq = ( ModifyEnvironmentRequestImpl ) modifyEnvironmentRequest;
        Preconditions.checkArgument(
                modifyEnvironmentReq.getNodesToAdd().size() > 0 || modifyEnvironmentReq.getNodesToRemove().size() > 0 );


        //WORKAROUND!!!
        List<Template> templates = getTemplates();
        for ( CreateEnvironmentRequestImpl.Node node : modifyEnvironmentReq.getNodesToAdd() )
        {
            node.setTemplateName( getTemplateNameById( templates, node.getTemplateId() ) );

            if ( Strings.isNullOrEmpty( node.getTemplateName() ) )
            {
                throw new OperationFailedException( "Template not found by id " + node.getTemplateId(), null );
            }
        }
        //WORKAROUND!!!

        HttpPut httpPut = new HttpPut(
                String.format( "https://%s.subut.ai/rest/v1/client/environments", hubEnv.getUrlPrefix() ) );

        httpPut.setEntity( new StringEntity( toJson( modifyEnvironmentReq ), ContentType.APPLICATION_JSON ) );
        httpPut.addHeader( KURJUN_TOKEN_HEADER, getKurjunToken() );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpPut );

            checkHttpStatus( response, HttpStatus.SC_OK, "modify environment" );

            System.out.println( readContent( response ) );
        }
        finally
        {
            close( response );
        }
    }


    String getTemplateNameById( final List<Template> templates, final String templateId )
    {
        for ( Template template : templates )
        {
            if ( template.getId().equalsIgnoreCase( templateId ) )
            {
                return template.getName();
            }
        }

        return null;
    }


    public void destroyEnvironment( final String envId )
    {
        HttpDelete httpDelete = new HttpDelete(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s", hubEnv.getUrlPrefix(), envId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpDelete );

            checkHttpStatus( response, HttpStatus.SC_NO_CONTENT, "destroy environment" );
        }
        finally
        {
            close( response );
        }
    }


    public List<Template> getTemplates()
    {
        return kurjunClient.getTemplates( getKurjunToken() );
    }


    private String getKurjunToken()
    {
        //TODO if pgpKey is set then return token otherwise return empty string
        //0) if token exists and not expired then return it, otherwise:
        //1) obtain auth id (use fingerprint as username)
        //2) sign with pgp key
        //3) obtain token
        //use mutual instance locking for methods createEnvironment, modifyEnvironment and getTemplates
        return "";
    }


    String toJson( Object object )
    {
        return gson.toJson( object );
    }


    <T> T parse( CloseableHttpResponse response, TypeToken<T> typeToken )
    {
        try
        {
            return gson.fromJson( EntityUtils.toString( response.getEntity() ), typeToken.getType() );
        }
        catch ( Exception e )
        {
            throw new OperationFailedException( "Failed to parse response", e );
        }
    }


    CloseableHttpResponse execute( HttpRequestBase httpRequest )
    {
        try
        {
            return httpclient.execute( httpRequest, httpContext );
        }
        catch ( Exception e )
        {
            throw new OperationFailedException( "Failed to execute http request", e );
        }
    }


    private void checkHttpStatus( CloseableHttpResponse response, int expectedStatus, String actionName )
    {
        if ( response.getStatusLine().getStatusCode() != expectedStatus )
        {
            throw new OperationFailedException(
                    String.format( "Failed to %s: %s, %s", actionName, response.getStatusLine(),
                            readContent( response ) ), null );
        }
    }


    private void close( CloseableHttpResponse response )
    {
        EntityUtils.consumeQuietly( response.getEntity() );

        IOUtils.closeQuietly( response );
    }


    private String readContent( CloseableHttpResponse response )
    {
        try
        {
            return EntityUtils.toString( response.getEntity() );
        }
        catch ( Exception e )
        {
            return null;
        }
    }
}
