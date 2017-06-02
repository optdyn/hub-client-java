package io.subutai.client.impl;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.subutai.client.api.CreateEnvironmentRequest;
import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.ModifyEnvironmentRequest;
import io.subutai.client.api.OperationFailedException;
import io.subutai.client.api.Peer;
import io.subutai.client.api.Template;
import io.subutai.client.pgp.Signer;


public class HubClientImplementation implements HubClient
{
    private static final Logger LOG = LoggerFactory.getLogger( HubClientImplementation.class );

    private static final String KURJUN_TOKEN_HEADER = "kurjun-token";
    private static final String UTF8 = "UTF-8";
    public static final String LIST_PEERS = "list peers";
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private HttpContext httpContext = new BasicHttpContext();
    private Gson gson = new GsonBuilder().registerTypeAdapter( Date.class, new DateDeserializer() ).create();

    private final HubEnv hubEnv;
    KurjunClient kurjunClient;
    private String pgpKeyPassword;
    private PGPSecretKey secretKey;
    private long kurjunTokenSetTime;
    private String kurjunToken = "";


    HubClientImplementation( HubEnv hubEnv )
    {
        Preconditions.checkNotNull( hubEnv );

        this.hubEnv = hubEnv;
        this.kurjunClient = new KurjunClient( hubEnv );
        this.httpContext.setAttribute( HttpClientContext.COOKIE_STORE, new BasicCookieStore() );
    }


    HubClientImplementation( HubEnv hubEnv, String pgpKeyFilePath, String pgpKeyPassword )
            throws PGPException, IOException
    {
        this( hubEnv );

        Preconditions.checkArgument( !Strings.isNullOrEmpty( pgpKeyFilePath ) );

        this.pgpKeyPassword = Strings.isNullOrEmpty( pgpKeyPassword ) ? "" : pgpKeyPassword;

        loadSecretKey( pgpKeyFilePath );
    }


    public void login( final String username, final String password )
    {
        HttpPost httpPost =
                new HttpPost( String.format( "https://%s.subut.ai/rest/v1/client/login", hubEnv.getUrlPrefix() ) );

        List<NameValuePair> nvps = new ArrayList<>();
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
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/peers/all", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, LIST_PEERS );

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


    @Override
    public List<Peer> getOwnPeers()
    {
        List<Peer> peers = Lists.newArrayList();

        HttpGet httpGet =
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/peers/own", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, LIST_PEERS );

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


    @Override
    public List<Peer> getSharedPeers()
    {
        List<Peer> peers = Lists.newArrayList();

        HttpGet httpGet = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/shared", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, LIST_PEERS );

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


    @Override
    public List<Peer> getFavoritePeers()
    {
        List<Peer> peers = Lists.newArrayList();

        HttpGet httpGet = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/favorite", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, LIST_PEERS );

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


    @Override
    public List<Peer> getPublicPeers()
    {
        List<Peer> peers = Lists.newArrayList();

        HttpGet httpGet = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/public", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( httpGet );

            checkHttpStatus( response, HttpStatus.SC_OK, LIST_PEERS );

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

        List<NameValuePair> nvps = new ArrayList<>();
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

        List<NameValuePair> nvps = new ArrayList<>();
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


    //**************


    private void loadSecretKey( String pgpKeyFilePath ) throws PGPException, IOException
    {
        String theKeys = Files.toString( new File( pgpKeyFilePath ), Charset.forName( UTF8 ) );

        InputStream secretKeyStream = new ByteArrayInputStream( getPrivateKeyBlock( theKeys ).getBytes( UTF8 ) );

        PGPSecretKeyRingCollection secretKeyRingCollection =
                new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream( secretKeyStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPSecretKeyRing secretKeyRing = secretKeyRingCollection
                .getSecretKeyRing( secretKeyRingCollection.iterator().next().getPublicKey().getKeyID() );

        secretKey = secretKeyRing.getSecretKey();
    }


    private String getPrivateKeyBlock( String keys )
    {
        StringTokenizer lineSplitter = new StringTokenizer( keys, "\n" );

        StringBuilder keyBuffer = new StringBuilder();
        boolean append = false;

        while ( lineSplitter.hasMoreTokens() )
        {
            String nextLine = lineSplitter.nextToken();

            if ( nextLine.contains( String.format( "-----BEGIN PGP %s KEY BLOCK-----", "PRIVATE" ) ) )
            {
                append = true;
            }

            if ( append )
            {
                keyBuffer.append( nextLine );
            }

            if ( nextLine.contains( String.format( "-----END PGP %s KEY BLOCK-----", "PRIVATE" ) ) )
            {
                break;
            }
        }

        return keyBuffer.toString();
    }


    private synchronized String getKurjunToken()
    {
        if ( secretKey == null )
        {
            return "";
        }
        else
        {
            try
            {
                if ( System.currentTimeMillis() - kurjunTokenSetTime > TimeUnit.MINUTES.toMillis( 30 ) )
                {
                    String username = Signer.getFingerprint( secretKey );

                    String authId = kurjunClient.getAuthId( username );

                    byte[] signedAuthId = Signer.clearSign( ( authId.trim() + "\n" ).getBytes(), secretKey,
                            pgpKeyPassword.toCharArray(), "" );

                    String token = kurjunClient.getToken( username, new String( signedAuthId, UTF8 ) );

                    kurjunToken = Strings.isNullOrEmpty( token ) ? "" : token;

                    kurjunTokenSetTime = System.currentTimeMillis();
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error obtaining Kurjun token", e );
            }

            return kurjunToken;
        }
    }


    String toJson( Object object )
    {
        return gson.toJson( object );
    }


    <T> T parse( CloseableHttpResponse response, TypeToken<T> typeToken )
    {
        try
        {
            String responseContent = EntityUtils.toString( response.getEntity() );
            LOG.info( "Response: {} {}", response.getEntity().getContentType(), responseContent );
            return gson.fromJson( responseContent, typeToken.getType() );
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
