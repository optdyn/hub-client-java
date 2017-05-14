package io.subutai.client.impl;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import io.subutai.client.api.LoginFailedException;
import io.subutai.client.api.OperationFailedException;


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
        final CookieStore cookieStore = new BasicCookieStore();
        httpContext.setAttribute( HttpClientContext.COOKIE_STORE, cookieStore );
    }


    public void login( final String username, final String password )
    {
        HttpPost httpPost =
                new HttpPost( String.format( "https://%s.subut.ai/rest/v1/client/login", hubEnv.getUrlPrefix() ) );

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "email", username ) );
        nvps.add( new BasicNameValuePair( "password", password ) );
        httpPost.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( "UTF-8" ) ) );

        CloseableHttpResponse response;
        try
        {
            response = httpclient.execute( httpPost, httpContext );
        }
        catch ( IOException e )
        {
            throw new OperationFailedException( "Failed to execute web request", e );
        }

        try
        {
            if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK )
            {
                throw new LoginFailedException( String.format( "Failed to login: %s", response.getStatusLine() ) );
            }

            EntityUtils.consumeQuietly( response.getEntity() );
        }
        finally
        {
            closeQuietly( response );
        }
    }


    public List<Environment> getEnvironments()
    {
        List<Environment> environments = Lists.newArrayList();

        HttpGet httpGet = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/environments", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response;
        try
        {
            response = httpclient.execute( httpGet, httpContext );
        }
        catch ( IOException e )
        {
            throw new OperationFailedException( "Failed to execute web request", e );
        }

        try
        {
            if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK )
            {
                throwOperationFailedException(
                        String.format( "Failed to obtain environments: %s", response.getStatusLine() ), null );
            }

            HttpEntity entity = response.getEntity();

            List<Environment> envList =
                    gson.fromJson( EntityUtils.toString( entity ), new TypeToken<ArrayList<EnvironmentImpl>>()
                    {
                    }.getType() );

            environments.addAll( envList );

            EntityUtils.consumeQuietly( entity );
        }
        catch ( IOException e )
        {
            throwOperationFailedException( "Failed to parse response", e );
        }
        finally
        {
            closeQuietly( response );
        }

        return environments;
    }


    public void addSshKey( final String envId, final String sshKey )
    {
        HttpPost httpPost = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/ssh-key/add", hubEnv.getUrlPrefix(),
                        envId ) );

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "ssh-key", sshKey ) );
        httpPost.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( "UTF-8" ) ) );

        CloseableHttpResponse response;
        try
        {
            response = httpclient.execute( httpPost, httpContext );
        }
        catch ( IOException e )
        {
            throw new OperationFailedException( "Failed to execute web request", e );
        }

        try
        {
            if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT )
            {
                throw new LoginFailedException(
                        String.format( "Failed to add ssh key: %s", response.getStatusLine() ) );
            }

            EntityUtils.consumeQuietly( response.getEntity() );
        }
        finally
        {
            closeQuietly( response );
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

        CloseableHttpResponse response;
        try
        {
            response = httpclient.execute( httpPost, httpContext );
        }
        catch ( IOException e )
        {
            throw new OperationFailedException( "Failed to execute web request", e );
        }

        try
        {
            if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT )
            {
                throw new LoginFailedException(
                        String.format( "Failed to remove ssh key: %s", response.getStatusLine() ) );
            }

            EntityUtils.consumeQuietly( response.getEntity() );
        }
        finally
        {
            closeQuietly( response );
        }
    }


    private void throwOperationFailedException( String message, Throwable cause )
    {
        throw new OperationFailedException( message, cause );
    }


    private void closeQuietly( CloseableHttpResponse response )
    {
        try
        {
            response.close();
        }
        catch ( IOException e )
        {
            //ignore
        }
    }
}
