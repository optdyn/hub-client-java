package io.subutai.client.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.FailedLoginException;

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

import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;


public class HubClientImplementation implements HubClient
{
    CloseableHttpClient httpclient = HttpClients.createDefault();
    CookieStore cookieStore = new BasicCookieStore();
    HttpContext httpContext = new BasicHttpContext();
    private final HubEnv hubEnv;


    public enum HubEnv
    {
        DEV( "dev" ), STAGE( "stage" ), PROD( "hub" );

        private String urlPrefix;


        HubEnv( final String urlPrefix )
        {
            this.urlPrefix = urlPrefix;
        }


        public String getUrlPrefix()
        {
            return urlPrefix;
        }
    }


    public HubClientImplementation( HubEnv hubEnv )
    {
        Preconditions.checkNotNull( hubEnv );

        this.hubEnv = hubEnv;
        httpContext.setAttribute( HttpClientContext.COOKIE_STORE, cookieStore );
    }


    public void login( final String username, final String password ) throws IOException, FailedLoginException
    {
        //TODO add own REST endpoints, for now temp usage of tray routes
        HttpPost httpPost =
                new HttpPost( String.format( "https://%s.subut.ai/rest/v1/tray/login", hubEnv.getUrlPrefix() ) );

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "email", username ) );
        nvps.add( new BasicNameValuePair( "password", password ) );
        httpPost.setEntity( new UrlEncodedFormEntity( nvps ) );

        CloseableHttpResponse response = httpclient.execute( httpPost, httpContext );

        try
        {
            if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK )
            {
                throw new FailedLoginException();
            }

            System.out.println( response.getStatusLine() );
            HttpEntity entity = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume( entity );
        }
        finally
        {
            response.close();
        }
    }


    public List<Environment> getEnvironments() throws IOException
    {
        List<Environment> environments = Lists.newArrayList();

        HttpGet httpGet =
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/tray/environments", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = httpclient.execute( httpGet, httpContext );

        try
        {
            if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK )
            {
                throw new RuntimeException(
                        String.format( "Failed to obtain environments: %s", response.getStatusLine() ) );
            }

            HttpEntity entity = response.getEntity();
            //TODO fill environments
            System.out.println( EntityUtils.toString( entity ) );

            EntityUtils.consume( entity );
        }
        finally
        {
            response.close();
        }

        return environments;
    }
}
