package io.subutai.client.impl;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
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

import io.subutai.client.api.Environment;
import io.subutai.client.api.EnvironmentTopology;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.Node;
import io.subutai.client.api.OperationFailedException;
import io.subutai.client.api.Peer;
import io.subutai.client.api.Template;


public class HubClientImplementation implements HubClient
{
    private static final String PARSE_ERROR_MSG = "Failed to parse response";
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private HttpContext httpContext = new BasicHttpContext();
    private Gson gson = new Gson();
    private final HubEnv hubEnv;


    HubClientImplementation( HubEnv hubEnv )
    {
        Preconditions.checkNotNull( hubEnv );

        this.hubEnv = hubEnv;
        this.httpContext.setAttribute( HttpClientContext.COOKIE_STORE, new BasicCookieStore() );
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
        catch ( Exception e )
        {
            throw new OperationFailedException( PARSE_ERROR_MSG, e );
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
        catch ( Exception e )
        {
            throw new OperationFailedException( PARSE_ERROR_MSG, e );
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


    public void createEnvironment( final EnvironmentTopology environmentTopology )
    {
        Preconditions.checkNotNull( environmentTopology );
        Preconditions.checkArgument( !environmentTopology.getNodes().isEmpty() );

        //WORKAROUND!!!
        List<Template> templates = getTemplates();
        for ( Node node : environmentTopology.getNodes() )
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

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "env-topology", toJson( environmentTopology ) ) );
        httpPost.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( "UTF-8" ) ) );

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


    private String getTemplateNameById( final List<Template> templates, final String templateId )
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
        List<Template> templates = Lists.newArrayList();

        HttpGet httpGet = new HttpGet( String.format( "https://%scdn.subut.ai:8338/kurjun/rest/template/info",
                hubEnv == HubEnv.PROD ? "" : hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "list templates" );

            List<Template> peerList = parse( response, new TypeToken<List<Template>>()
            {
            } );

            templates.addAll( peerList );
        }
        catch ( Exception e )
        {
            throw new OperationFailedException( PARSE_ERROR_MSG, e );
        }
        finally
        {
            close( response );
        }

        return templates;
    }


    String toJson( Object object )
    {
        return gson.toJson( object );
    }


    <T> T parse( CloseableHttpResponse response, TypeToken<T> typeToken ) throws IOException
    {
        return gson.fromJson( EntityUtils.toString( response.getEntity() ), typeToken.getType() );
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

        closeQuietly( response );
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


    private void closeQuietly( CloseableHttpResponse response )
    {
        try
        {
            response.close();
        }
        catch ( Exception e )
        {
            //ignore
        }
    }
}
