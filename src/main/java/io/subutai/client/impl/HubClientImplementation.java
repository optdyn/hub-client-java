package io.subutai.client.impl;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.OperationFailedException;
import io.subutai.client.api.Peer;


public class HubClientImplementation implements HubClient
{
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

            checkHttpStatus( response, HttpStatus.SC_OK, "obtain environments" );

            HttpEntity entity = response.getEntity();

            List<EnvironmentImpl> envList = parse( response, new TypeToken<List<EnvironmentImpl>>()
            {
            } );

            environments.addAll( envList );
        }
        catch ( Exception e )
        {
            throw new OperationFailedException( "Failed to parse response", e );
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

            checkHttpStatus( response, HttpStatus.SC_OK, "obtain peers" );

            List<PeerImpl> peerList = parse( response, new TypeToken<List<PeerImpl>>()
            {
            } );

            peers.addAll( peerList );
        }
        catch ( Exception e )
        {
            throw new OperationFailedException( "Failed to parse response", e );
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


    protected <T> T parse( CloseableHttpResponse response, TypeToken<T> typeToken ) throws IOException
    {
        return gson.fromJson( EntityUtils.toString( response.getEntity() ), typeToken.getType() );
    }


    protected CloseableHttpResponse execute( HttpRequestBase httpRequest )
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


    protected void checkHttpStatus( CloseableHttpResponse response, int expectedStatus, String actionName )
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
