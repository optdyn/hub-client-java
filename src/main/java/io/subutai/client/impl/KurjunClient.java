package io.subutai.client.impl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.subutai.client.api.HubClient;
import io.subutai.client.api.KurjunQuota;
import io.subutai.client.api.OperationFailedException;
import io.subutai.client.api.RawFile;
import io.subutai.client.api.Template;


//TODO add precondition checks for token where applicable
class KurjunClient
{
    private static final String UTF8 = "UTF-8";

    private final HubClient.HubEnv hubEnv;

    private Gson gson = new Gson();


    KurjunClient( final HubClient.HubEnv hubEnv )
    {
        Preconditions.checkNotNull( hubEnv );

        this.hubEnv = hubEnv;
    }


    String getAuthId( String username )
    {
        return getKurjunAuthId( username );
    }


    String getToken( String username, String signedAuthId )
    {
        return getKurjunToken( username, signedAuthId );
    }


    List<Template> getTemplates( String token )
    {
        return getKurjunTemplates( token );
    }


    List<RawFile> getRawFiles( final String token )
    {
        return getKurjunRawFiles( token );
    }


    private List<Template> getKurjunTemplates( String token )
    {
        List<Template> templates = Lists.newArrayList();

        HttpGet httpGet = new HttpGet( String.format( "%s/template/info?token=%s", getKurjunBaseUrl(),
                StringUtil.isBlank( token ) ? "" : token ) );

        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            CloseableHttpResponse response = execute( client, httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "list templates" );

            List<Template> templateList = parse( response, new TypeToken<List<Template>>()
            {
            } );

            templates.addAll( templateList );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return templates;
    }


    private List<RawFile> getKurjunRawFiles( String token )
    {
        List<RawFile> rawFiles = Lists.newArrayList();

        HttpGet httpGet = new HttpGet(
                String.format( "%s/raw/info?token=%s", getKurjunBaseUrl(), StringUtil.isBlank( token ) ? "" : token ) );

        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            CloseableHttpResponse response = execute( client, httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "list raw files" );

            List<RawFile> fileList = parse( response, new TypeToken<List<RawFile>>()
            {
            } );

            rawFiles.addAll( fileList );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return rawFiles;
    }


    String uploadFile( final String filename, final String version, final String token )
    {
        HttpPost post = new HttpPost( String.format( "%s/raw/upload", getKurjunBaseUrl() ) );
        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            File file = new File( filename );
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode( HttpMultipartMode.BROWSER_COMPATIBLE );
            builder.addBinaryBody( "file", file, ContentType.DEFAULT_BINARY, file.getName() );
            builder.addTextBody( "token", token, ContentType.DEFAULT_BINARY );
            builder.addTextBody( "private", "true", ContentType.DEFAULT_BINARY );
            if ( !StringUtil.isBlank( version ) )
            {
                builder.addTextBody( "version", version, ContentType.DEFAULT_BINARY );
            }
            HttpEntity entity = builder.build();
            post.setEntity( entity );

            CloseableHttpResponse response = execute( client, post );

            checkHttpStatus( response, HttpStatus.SC_OK, "upload file" );

            return readContent( response );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    void downloadFile( final String fileId, final String outputDirectory, final String token )
    {
        try ( CloseableHttpClient client = HttpClients.createDefault() )
        {
            HttpGet httpGet = new HttpGet( String.format( "%s/raw/download?id=%s&token=%s", getKurjunBaseUrl(),
                    URLEncoder.encode( fileId, UTF8 ), token ) );

            CloseableHttpResponse response = execute( client, httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "download file" );

            HttpEntity entity = response.getEntity();
            if ( entity != null )
            {
                String disposition = response.getFirstHeader( "Content-Disposition" ).getValue();
                String fileName = disposition.replaceFirst( "(?i)^.*filename=\"([^\"]+)\".*$", "$1" );
                try ( FileOutputStream fos = new FileOutputStream( Paths.get( outputDirectory, fileName ).toString() ) )
                {
                    entity.writeTo( fos );
                }
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new OperationFailedException( "Failed to encode request", e );
        }
        catch ( FileNotFoundException e )
        {
            throw new OperationFailedException( "Output file not found", e );
        }
        catch ( IOException e )
        {
            throw new OperationFailedException( "Error writing file", e );
        }
    }


    void removeFile( final String fileId, final String token )
    {
        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            String url = String.format( "%s/raw/delete?id=%s&token=%s", getKurjunBaseUrl(),
                    URLEncoder.encode( fileId, UTF8 ), token );

            HttpDelete httpDelete = new HttpDelete( url );

            CloseableHttpResponse response = execute( client, httpDelete );

            checkHttpStatus( response, HttpStatus.SC_OK, "remove file" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new OperationFailedException( "Failed to encode request", e );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    void shareFile( final String fileId, final String userFingerprint, final String token )
    {
        manageFileSharing( fileId, userFingerprint, token, true );
    }


    void unshareFile( final String fileId, final String userFingerprint, final String token )
    {
        manageFileSharing( fileId, userFingerprint, token, false );
    }


    private void manageFileSharing( final String fileId, final String userFingerprint, final String token,
                                    final boolean share )
    {
        HttpPost post = new HttpPost( String.format( "%s/share", getKurjunBaseUrl() ) );

        Map<String, Object> permissionMap = Maps.newHashMap();
        permissionMap.put( "token", token );
        permissionMap.put( "id", fileId );
        permissionMap.put( share ? "add" : "remove", Lists.newArrayList( userFingerprint ) );
        permissionMap.put( share ? "remove" : "add", Lists.newArrayList() );
        permissionMap.put( "repo", "raw" );
        System.out.println( gson.toJson( permissionMap ) );
        List<NameValuePair> params = new ArrayList<>();
        params.add( new BasicNameValuePair( "json", gson.toJson( permissionMap ) ) );

        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            post.setEntity( new UrlEncodedFormEntity( params ) );

            CloseableHttpResponse response = execute( client, post );

            checkHttpStatus( response, HttpStatus.SC_OK, ( share ? "share" : "unshare" ) + " file" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new OperationFailedException( "Failed to encode request", e );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    List<String> getSharedUsers( final String fileId, final String token )
    {
        List<String> users = Lists.newArrayList();

        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            String url = String.format( "%s/share?id=%s&token=%s&repo=raw", getKurjunBaseUrl(),
                    URLEncoder.encode( fileId, UTF8 ), StringUtil.isBlank( token ) ? "" : token );

            HttpGet httpGet = new HttpGet( url );

            CloseableHttpResponse response = execute( client, httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "list shared users" );

            List<String> userList = parse( response, new TypeToken<List<String>>()
            {
            } );

            users.addAll( userList );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new OperationFailedException( "Failed to encode request", e );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return users;
    }


    KurjunQuota getQuota( final String userFingerprint, final String token )
    {
        KurjunQuota quota;

        HttpGet httpGet = new HttpGet( String.format( "%s/quota?user=%s&token=%s", getKurjunBaseUrl(), userFingerprint,
                StringUtil.isBlank( token ) ? "" : token ) );

        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            CloseableHttpResponse response = execute( client, httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, "get quota" );

            quota = parse( response, new TypeToken<KurjunQuota>()
            {
            } );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return quota;
    }


    private String getKurjunToken( String username, String signedAuthId )
    {
        HttpPost post = new HttpPost( String.format( "%s/auth/token", getKurjunBaseUrl() ) );

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode( HttpMultipartMode.BROWSER_COMPATIBLE );
        entityBuilder.addTextBody( "user", username );
        entityBuilder.addTextBody( "message", signedAuthId );
        HttpEntity httpEntity = entityBuilder.build();
        post.setEntity( httpEntity );

        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            CloseableHttpResponse response = execute( client, post );

            checkHttpStatus( response, HttpStatus.SC_OK, "obtain token" );

            return readContent( response );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    private String getKurjunAuthId( String username )
    {

        if ( isRegisteredWithKurjun( username ) )
        {
            HttpGet httpGet = new HttpGet( String.format( "%s/auth/token?user=%s", getKurjunBaseUrl(), username ) );

            CloseableHttpClient client = HttpClients.createDefault();
            try
            {
                CloseableHttpResponse response = execute( client, httpGet );

                checkHttpStatus( response, HttpStatus.SC_OK, "obtain auth id" );

                return readContent( response );
            }
            finally
            {
                IOUtils.closeQuietly( client );
            }
        }
        else
        {
            throw new IllegalStateException( "User is not registered wih Kurjun" );
        }
    }


    private boolean isRegisteredWithKurjun( String username )
    {
        HttpGet httpGet = new HttpGet( String.format( "%s/auth/key?user=%s", getKurjunBaseUrl(), username ) );

        CloseableHttpClient client = HttpClients.createDefault();
        try
        {
            CloseableHttpResponse response = execute( client, httpGet );

            return response.getStatusLine().getStatusCode() == 200;
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    private String getKurjunBaseUrl()
    {
        return String.format( "https://%scdn.subut.ai:8338/kurjun/rest",
                hubEnv == HubClient.HubEnv.PROD ? "" : hubEnv.getCdnPrefix() );
    }


    private <T> T parse( CloseableHttpResponse response, TypeToken<T> typeToken )
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


    private CloseableHttpResponse execute( CloseableHttpClient httpClient, HttpRequestBase httpRequest )
    {
        try
        {
            return httpClient.execute( httpRequest );
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
