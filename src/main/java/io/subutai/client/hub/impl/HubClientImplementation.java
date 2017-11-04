package io.subutai.client.hub.impl;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.subutai.client.hub.api.Domain;
import io.subutai.client.hub.api.DomainAssignment;
import io.subutai.client.hub.api.Environment;
import io.subutai.client.hub.api.EnvironmentCreationRequest;
import io.subutai.client.hub.api.EnvironmentModificationRequest;
import io.subutai.client.hub.api.EnvironmentRef;
import io.subutai.client.hub.api.FriendsInfo;
import io.subutai.client.hub.api.HubClient;
import io.subutai.client.hub.api.KurjunQuota;
import io.subutai.client.hub.api.OperationFailedException;
import io.subutai.client.hub.api.Organization;
import io.subutai.client.hub.api.Peer;
import io.subutai.client.hub.api.RawFile;
import io.subutai.client.hub.api.SshKey;
import io.subutai.client.hub.api.Template;
import io.subutai.client.hub.api.User;
import io.subutai.client.hub.pgp.Signer;


//TODO login method should return current user to avoid second request
//TODO rename HubClient to Client (also HubClients, HubClientImplementation)
public class HubClientImplementation implements HubClient
{
    private static final Logger LOG = LoggerFactory.getLogger( HubClientImplementation.class );

    private static final String KURJUN_TOKEN_HEADER = "kurjun-token";
    private static final String UTF8 = "UTF-8";
    private static final String LIST_PEERS = "list peers";
    private static final String SEARCH_USER_INFO = "search user";
    private static final String GET_USER_ORGANIZATIONS = "get user organizations";
    private static final String ERROR_ENCODING_PARAMETER = "Error encoding parameter";
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private HttpContext httpContext = new BasicHttpContext();
    private Gson gson = new GsonBuilder().registerTypeAdapter( Date.class, new DateDeserializer() ).create();

    private final HubEnv hubEnv;
    KurjunClient kurjunClient;
    private String pgpKeyPassword;
    private PGPSecretKey secretKey;
    private long kurjunTokenSetTime;
    private String kurjunToken = "";
    User currentUser;


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

        Preconditions.checkArgument( !StringUtil.isBlank( pgpKeyFilePath ) );

        this.pgpKeyPassword = StringUtil.isBlank( pgpKeyPassword ) ? "" : pgpKeyPassword;

        loadSecretKey( pgpKeyFilePath );
    }


    public void login( final String username, final String password )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( username ) );
        Preconditions.checkArgument( !StringUtil.isBlank( password ) );

        HttpPost request =
                new HttpPost( String.format( "https://%s.subut.ai/rest/v1/client/login", hubEnv.getUrlPrefix() ) );

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add( new BasicNameValuePair( "email", username ) );
        nvps.add( new BasicNameValuePair( "password", password ) );
        request.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( UTF8 ) ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "login" );

            currentUser = findUserByEmail( username );
        }
        finally
        {

            close( response );
        }
    }


    public List<Environment> getEnvironments()
    {
        List<Environment> environments = Lists.newArrayList();

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/environments", hubEnv.getUrlPrefix() ) );


        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

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

        HttpGet request =
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/peers/all", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

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

        HttpGet request =
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/peers/own", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

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

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/shared", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

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

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/favorite", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

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

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/public", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

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
    public void sharePeer( final String peerId, final long userId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( peerId ) );
        Preconditions.checkArgument( userId > 0 );

        HttpPut request = new HttpPut(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/%s/share/%s", hubEnv.getUrlPrefix(), peerId,
                        userId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "share peer" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void unsharePeer( final String peerId, final long userId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( peerId ) );
        Preconditions.checkArgument( userId > 0 );

        HttpDelete request = new HttpDelete(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/%s/share/%s", hubEnv.getUrlPrefix(), peerId,
                        userId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "unshare peer" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void addPeerToFavorites( final String peerId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( peerId ) );

        HttpPut request = new HttpPut(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/favorite/%s", hubEnv.getUrlPrefix(),
                        peerId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "add peer to favorites" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void removePeerFromFavorites( final String peerId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( peerId ) );

        HttpDelete request = new HttpDelete(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/favorite/%s", hubEnv.getUrlPrefix(),
                        peerId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "remove peer from favorites" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void updatePeerScope( final String peerId, final Peer.Scope scope )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( peerId ) );
        Preconditions.checkNotNull( scope );

        HttpPut request = new HttpPut(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/%s/scope/%s", hubEnv.getUrlPrefix(), peerId,
                        scope.name() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "update peer scope" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void updatePeerName( final String peerId, final String name )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( peerId ) );
        Preconditions.checkArgument( !StringUtil.isBlank( name ) );

        CloseableHttpResponse response = null;
        try
        {
            HttpPut request = new HttpPut(
                    String.format( "https://%s.subut.ai/rest/v1/client/peers/%s/name/%s", hubEnv.getUrlPrefix(), peerId,
                            URLEncoder.encode( name, UTF8 ) ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "update peer name" );
        }
        catch ( UnsupportedEncodingException e )
        {
            LOG.error( ERROR_ENCODING_PARAMETER, e );

            throw new OperationFailedException( ERROR_ENCODING_PARAMETER, e );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public List<SshKey> getSshKeys( final String envId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );

        List<SshKey> sshKeys = Lists.newArrayList();

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/ssh-keys", hubEnv.getUrlPrefix(),
                        envId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "Get ssh keys" );

            sshKeys.addAll( parse( response, new TypeToken<List<SshKeyImpl>>()
            {
            } ) );
        }
        finally
        {
            close( response );
        }

        return sshKeys;
    }


    public void addSshKey( final String envId, final String sshKey )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );
        Preconditions.checkArgument( !StringUtil.isBlank( sshKey ) );

        HttpPost request = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/ssh-keys/add", hubEnv.getUrlPrefix(),
                        envId ) );

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add( new BasicNameValuePair( "ssh-key", sshKey ) );
        request.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( UTF8 ) ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "add ssh key" );
        }
        finally
        {
            close( response );
        }
    }


    public void removeSshKey( final String envId, final String sshKey )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );
        Preconditions.checkArgument( !StringUtil.isBlank( sshKey ) );

        HttpPost request = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/ssh-keys/remove",
                        hubEnv.getUrlPrefix(), envId ) );

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add( new BasicNameValuePair( "ssh-key", sshKey ) );
        request.setEntity( new UrlEncodedFormEntity( nvps, Charset.forName( UTF8 ) ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "remove ssh key" );
        }
        finally
        {
            close( response );
        }
    }


    public void startContainer( final String envId, final String contId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );
        Preconditions.checkArgument( !StringUtil.isBlank( contId ) );

        HttpPut request = new HttpPut(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/containers/%s/start",
                        hubEnv.getUrlPrefix(), envId, contId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "start container" );
        }
        finally
        {
            close( response );
        }
    }


    public void stopContainer( final String envId, final String contId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );
        Preconditions.checkArgument( !StringUtil.isBlank( contId ) );

        HttpPut request = new HttpPut(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/containers/%s/stop",
                        hubEnv.getUrlPrefix(), envId, contId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "stop container" );
        }
        finally
        {
            close( response );
        }
    }


    public void destroyContainer( final String envId, final String contId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );
        Preconditions.checkArgument( !StringUtil.isBlank( contId ) );

        HttpDelete request = new HttpDelete(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s/containers/%s",
                        hubEnv.getUrlPrefix(), envId, contId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "destroy container" );
        }
        finally
        {
            close( response );
        }
    }


    public EnvironmentCreationRequest createRequest( final String environmentName )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( environmentName ) );

        return new EnvironmentCreationRequestImpl( environmentName );
    }


    public EnvironmentRef createEnvironment( final EnvironmentCreationRequest environmentCreationRequest )
    {
        Preconditions.checkNotNull( environmentCreationRequest );
        Preconditions.checkArgument( environmentCreationRequest instanceof EnvironmentCreationRequestImpl );
        EnvironmentCreationRequestImpl createEnvironmentReq =
                ( EnvironmentCreationRequestImpl ) environmentCreationRequest;
        Preconditions.checkNotNull( createEnvironmentReq.getNodes() );
        Preconditions.checkArgument( !createEnvironmentReq.getNodes().isEmpty() );

        //WORKAROUND!!!
        List<Template> templates = getTemplates();
        for ( EnvironmentCreationRequestImpl.Node node : createEnvironmentReq.getNodes() )
        {
            node.setTemplateName( getTemplateNameById( templates, node.getTemplateId() ) );

            Preconditions.checkArgument( !StringUtil.isBlank( node.getTemplateName() ),
                    "Template not found by id " + node.getTemplateId() );
        }
        //WORKAROUND!!!


        HttpPost request = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments", hubEnv.getUrlPrefix() ) );

        request.setEntity( new StringEntity( toJson( createEnvironmentReq ), ContentType.APPLICATION_JSON ) );
        request.addHeader( KURJUN_TOKEN_HEADER, getKurjunToken() );

        EnvironmentRefImpl environmentRef;
        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "create environment" );

            environmentRef = parse( response, new TypeToken<EnvironmentRefImpl>()
            {
            } );
        }
        finally
        {
            close( response );
        }

        return environmentRef;
    }


    public EnvironmentModificationRequest modifyRequest( final String envId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );

        return new EnvironmentModificationRequestImpl( envId );
    }


    public void modifyEnvironment( final EnvironmentModificationRequest environmentModificationRequest )
    {
        Preconditions.checkNotNull( environmentModificationRequest );
        Preconditions.checkArgument( environmentModificationRequest instanceof EnvironmentModificationRequestImpl );
        EnvironmentModificationRequestImpl modifyEnvironmentReq =
                ( EnvironmentModificationRequestImpl ) environmentModificationRequest;
        Preconditions.checkArgument(
                ( modifyEnvironmentReq.getNodesToAdd() != null && !modifyEnvironmentReq.getNodesToAdd().isEmpty() ) || (
                        modifyEnvironmentReq.getNodesToRemove() != null && !modifyEnvironmentReq.getNodesToRemove()
                                                                                                .isEmpty() ) );


        //WORKAROUND!!!
        List<Template> templates = getTemplates();
        for ( EnvironmentCreationRequestImpl.Node node : modifyEnvironmentReq.getNodesToAdd() )
        {
            node.setTemplateName( getTemplateNameById( templates, node.getTemplateId() ) );

            if ( StringUtil.isBlank( node.getTemplateName() ) )
            {
                throw new OperationFailedException( "Template not found by id " + node.getTemplateId(), null );
            }
        }
        //WORKAROUND!!!

        HttpPut request = new HttpPut(
                String.format( "https://%s.subut.ai/rest/v1/client/environments", hubEnv.getUrlPrefix() ) );

        request.setEntity( new StringEntity( toJson( modifyEnvironmentReq ), ContentType.APPLICATION_JSON ) );
        request.addHeader( KURJUN_TOKEN_HEADER, getKurjunToken() );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "modify environment" );

            System.out.println( readContent( response ) );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void createEnvironmentFromBlueprint( final String blueprint, final Map<String, String> blueprintVariables )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( blueprint ) );

        HttpPost request = new HttpPost(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/wizard", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            String json = toJson( new BlueprintDto( blueprint,
                    blueprintVariables == null ? Maps.newHashMap() : blueprintVariables ) );
            request.setEntity( new StringEntity( json, ContentType.APPLICATION_JSON ) );
            request.addHeader( KURJUN_TOKEN_HEADER, getKurjunToken() );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "create environment from blueprint" );
        }
        finally
        {
            close( response );
        }
    }


    public void destroyEnvironment( final String envId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( envId ) );

        HttpDelete request = new HttpDelete(
                String.format( "https://%s.subut.ai/rest/v1/client/environments/%s", hubEnv.getUrlPrefix(), envId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_ACCEPTED, "destroy environment" );
        }
        finally
        {
            close( response );
        }
    }


    public Double getBalance()
    {
        HttpGet request =
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/balance", hubEnv.getUrlPrefix() ) );

        ResultDto result;
        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "get balance" );

            result = parse( response, new TypeToken<ResultDto>()
            {
            } );
        }
        finally
        {
            close( response );
        }

        return ( Double ) result.getValue();
    }


    @Override
    public User getUser( final long userId )
    {
        Preconditions.checkArgument( userId > 0 );

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/users/%s", hubEnv.getUrlPrefix(), userId ) );

        UserImpl user;
        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "get user" );

            user = parse( response, new TypeToken<UserImpl>()
            {
            } );
        }
        finally
        {
            close( response );
        }

        return user;
    }


    @Override
    public User findUserByName( final String name )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( name ) );

        UserImpl user;
        CloseableHttpResponse response = null;
        try
        {
            HttpGet request = new HttpGet(
                    String.format( "https://%s.subut.ai/rest/v1/client/users/search?name=%s", hubEnv.getUrlPrefix(),
                            URLEncoder.encode( name, UTF8 ) ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, SEARCH_USER_INFO );

            user = parse( response, new TypeToken<UserImpl>()
            {
            } );
        }
        catch ( UnsupportedEncodingException e )
        {
            LOG.error( ERROR_ENCODING_PARAMETER, e );

            throw new OperationFailedException( ERROR_ENCODING_PARAMETER, e );
        }
        finally
        {
            close( response );
        }

        return user;
    }


    @Override
    public User findUserByEmail( final String email )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( email ) );

        UserImpl user;
        CloseableHttpResponse response = null;
        try
        {
            HttpGet request = new HttpGet(
                    String.format( "https://%s.subut.ai/rest/v1/client/users/search?email=%s", hubEnv.getUrlPrefix(),
                            URLEncoder.encode( email, UTF8 ) ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, SEARCH_USER_INFO );

            user = parse( response, new TypeToken<UserImpl>()
            {
            } );
        }
        catch ( UnsupportedEncodingException e )
        {
            LOG.error( ERROR_ENCODING_PARAMETER, e );

            throw new OperationFailedException( ERROR_ENCODING_PARAMETER, e );
        }
        finally
        {
            close( response );
        }

        return user;
    }


    @Override
    public List<Peer> getUserPeers( final long userId )
    {
        Preconditions.checkArgument( userId > 0 );

        List<Peer> peers = Lists.newArrayList();

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/users/%s/peers", hubEnv.getUrlPrefix(), userId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "get user peers" );

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
    public List<User> getPeerUsers( final String peerId )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( peerId ) );

        List<User> users = Lists.newArrayList();

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/peers/%s/users", hubEnv.getUrlPrefix(), peerId ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "get peer users" );

            List<UserImpl> userList = parse( response, new TypeToken<List<UserImpl>>()
            {
            } );

            users.addAll( userList );
        }
        finally
        {
            close( response );
        }

        return users;
    }


    @Override
    public List<Organization> getUserOrganizations( final long userId, boolean own )
    {
        Preconditions.checkArgument( userId > 0 );

        List<Organization> organizations = Lists.newArrayList();

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/users/%s/organizations?own=%s",
                        hubEnv.getUrlPrefix(), userId, own ? "true" : "false" ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, GET_USER_ORGANIZATIONS );

            List<OrganizationImpl> organizationList = parse( response, new TypeToken<List<OrganizationImpl>>()
            {
            } );

            organizations.addAll( organizationList );
        }
        finally
        {
            close( response );
        }

        return organizations;
    }


    @Override
    public List<Organization> getOrganizations( final boolean own )
    {
        List<Organization> organizations = Lists.newArrayList();

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/organizations?own=%s", hubEnv.getUrlPrefix(),
                        own ? "true" : "false" ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, GET_USER_ORGANIZATIONS );

            List<OrganizationImpl> organizationList = parse( response, new TypeToken<List<OrganizationImpl>>()
            {
            } );

            organizations.addAll( organizationList );
        }
        finally
        {
            close( response );
        }

        return organizations;
    }


    // >>>>> DOMAIN MGMT


    @Override
    public List<Domain> getDomains()
    {
        List<Domain> domains = Lists.newArrayList();

        HttpGet request =
                new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/domains", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "list domains" );

            List<DomainImpl> domainList = parse( response, new TypeToken<List<DomainImpl>>()
            {
            } );

            domains.addAll( domainList );
        }
        finally
        {
            close( response );
        }

        return domains;
    }


    @Override
    public void reserveDomain( final String domainName )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( domainName ) );

        CloseableHttpResponse response = null;
        try
        {
            HttpPut request = new HttpPut(
                    String.format( "https://%s.subut.ai/rest/v1/client/domains/%s", hubEnv.getUrlPrefix(),
                            URLEncoder.encode( domainName, UTF8 ) ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "reserve domain" );
        }
        catch ( UnsupportedEncodingException e )
        {
            LOG.error( ERROR_ENCODING_PARAMETER, e );

            throw new OperationFailedException( ERROR_ENCODING_PARAMETER, e );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void deleteDomain( final String domainName )
    {
        Preconditions.checkArgument( !StringUtil.isBlank( domainName ) );

        CloseableHttpResponse response = null;
        try
        {
            HttpDelete request = new HttpDelete(
                    String.format( "https://%s.subut.ai/rest/v1/client/domains/%s", hubEnv.getUrlPrefix(),
                            URLEncoder.encode( domainName, UTF8 ) ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "delete domain" );
        }
        catch ( UnsupportedEncodingException e )
        {
            LOG.error( ERROR_ENCODING_PARAMETER, e );

            throw new OperationFailedException( ERROR_ENCODING_PARAMETER, e );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public Map<String, List<DomainAssignment>> getDomainAssignments()
    {
        Map<String, List<DomainAssignment>> assignments = Maps.newHashMap();

        HttpGet request = new HttpGet(
                String.format( "https://%s.subut.ai/rest/v1/client/domains/assignments", hubEnv.getUrlPrefix() ) );

        CloseableHttpResponse response = null;
        try
        {
            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "list domain assignments" );

            Map<String, List<DomainAssignmentImpl>> assignmentsList =
                    parse( response, new TypeToken<Map<String, List<DomainAssignmentImpl>>>()
                    {
                    } );

            assignmentsList.forEach( ( k, v ) -> assignments.put( k, Lists.newArrayList( v ) ) );
        }
        finally
        {
            close( response );
        }

        return assignments;
    }

    // <<<<< DOMAIN MGMT

    // FRIENDS MGMT >>>>>


    @Override
    public FriendsInfo getFriendsInfo()
    {
        FriendsInfoImpl friendsInfo;
        CloseableHttpResponse response = null;
        try
        {
            HttpGet request =
                    new HttpGet( String.format( "https://%s.subut.ai/rest/v1/client/friends", hubEnv.getUrlPrefix() ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "get friends info" );

            friendsInfo = parse( response, new TypeToken<FriendsInfoImpl>()
            {
            } );
        }
        finally
        {
            close( response );
        }

        return friendsInfo;
    }


    @Override
    public void requestFriendship( final long userId )
    {
        Preconditions.checkArgument( userId > 0 );

        CloseableHttpResponse response = null;
        try
        {
            HttpPut request = new HttpPut(
                    String.format( "https://%s.subut.ai/rest/v1/client/friends/%s/request", hubEnv.getUrlPrefix(),
                            userId ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "send friendship request" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void acceptFriendship( final long userId )
    {
        Preconditions.checkArgument( userId > 0 );

        CloseableHttpResponse response = null;
        try
        {
            HttpPut request = new HttpPut(
                    String.format( "https://%s.subut.ai/rest/v1/client/friends/%s/accept", hubEnv.getUrlPrefix(),
                            userId ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "accept friendship request" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void rejectFriendshipRequest( final long userId )
    {
        Preconditions.checkArgument( userId > 0 );

        CloseableHttpResponse response = null;
        try
        {
            HttpPut request = new HttpPut(
                    String.format( "https://%s.subut.ai/rest/v1/client/friends/%s/reject", hubEnv.getUrlPrefix(),
                            userId ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "reject friendship request" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void cancelFriendshipRequest( final long userId )
    {
        Preconditions.checkArgument( userId > 0 );

        CloseableHttpResponse response = null;
        try
        {
            HttpPut request = new HttpPut(
                    String.format( "https://%s.subut.ai/rest/v1/client/friends/%s/cancel", hubEnv.getUrlPrefix(),
                            userId ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "cancel friendship request" );
        }
        finally
        {
            close( response );
        }
    }


    @Override
    public void breakFriendship( final long userId )
    {
        Preconditions.checkArgument( userId > 0 );

        CloseableHttpResponse response = null;
        try
        {
            HttpDelete request = new HttpDelete(
                    String.format( "https://%s.subut.ai/rest/v1/client/friends/%s", hubEnv.getUrlPrefix(), userId ) );

            response = execute( request );

            checkHttpStatus( response, HttpStatus.SC_OK, "break friendship" );
        }
        finally
        {
            close( response );
        }
    }
    // <<<<< FRIENDS MGMT

    // KURJUN >>>>>


    public List<Template> getTemplates()
    {
        return kurjunClient.getTemplates( getKurjunToken() );
    }


    @Override
    public List<RawFile> getRawFiles()
    {
        return kurjunClient.getRawFiles( getKurjunToken() );
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


    public String uploadFile( String filename, String version )
    {
        String fileId = kurjunClient.uploadFile( filename, version, getKurjunToken() );

        if ( secretKey != null )
        {
            try
            {
                byte[] signedAuthId =
                        Signer.clearSign( ( fileId.trim() + "\n" ).getBytes(), secretKey, pgpKeyPassword.toCharArray(),
                                "" );

                kurjunClient.signFile( new String( signedAuthId, UTF8 ), getKurjunToken() );
            }
            catch ( Exception e )
            {
                throw new OperationFailedException( "Failed to send file signature to Kurjun", e );
            }
        }

        return fileId;
    }


    @Override
    public void downloadFile( final String fileId, final String outputDirectory )
    {
        kurjunClient.downloadFile( fileId, outputDirectory, getKurjunToken() );
    }


    @Override
    public void removeFile( final String fileId )
    {
        kurjunClient.removeFile( fileId, getKurjunToken() );
    }


    @Override
    public void shareFile( final String fileId, final String userFingerprint )
    {
        kurjunClient.shareFile( fileId, userFingerprint, getKurjunToken() );
    }


    @Override
    public void unshareFile( final String fileId, final String userFingerprint )
    {
        kurjunClient.unshareFile( fileId, userFingerprint, getKurjunToken() );
    }


    @Override
    public List<String> getSharedUsers( final String fileId )
    {
        return kurjunClient.getSharedUsers( fileId, getKurjunToken() );
    }


    @Override
    public KurjunQuota getKurjunQuota()
    {
        return kurjunClient.getQuota( currentUser.getFingerprint(), getKurjunToken() );
    }

    // <<<<< KURJUN

    //**************


    private void loadSecretKey( String pgpKeyFilePath ) throws PGPException, IOException
    {
        String theKeys = Files.toString( new File( pgpKeyFilePath ), Charset.forName( UTF8 ) );

        InputStream secretKeyStream = new ByteArrayInputStream( Signer.getKeyBlock( theKeys, true ).getBytes( UTF8 ) );

        PGPSecretKeyRingCollection secretKeyRingCollection =
                new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream( secretKeyStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPSecretKeyRing secretKeyRing = secretKeyRingCollection
                .getSecretKeyRing( secretKeyRingCollection.iterator().next().getPublicKey().getKeyID() );

        secretKey = secretKeyRing.getSecretKey();
    }


    @Override
    public synchronized String getKurjunToken()
    {
        if ( secretKey == null )
        {
            return "";
        }
        else
        {
            try
            {
                if ( System.currentTimeMillis() - kurjunTokenSetTime > TimeUnit.MINUTES
                        .toMillis( KURJUN_TOKEN_TTL_MIN ) )
                {
                    String username = Signer.getFingerprint( secretKey );

                    String authId = kurjunClient.getAuthId( username );

                    byte[] signedAuthId = Signer.clearSign( ( authId.trim() + "\n" ).getBytes(), secretKey,
                            pgpKeyPassword.toCharArray(), "" );

                    String token = kurjunClient.getToken( username, new String( signedAuthId, UTF8 ) );

                    kurjunToken = StringUtil.isBlank( token ) ? "" : token;

                    kurjunTokenSetTime = System.currentTimeMillis();
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error obtaining Kurjun token", e );

                throw new OperationFailedException( "Error obtaining Kurjun token", e );
            }

            return kurjunToken;
        }
    }


    public User getCurrentUser()
    {
        return currentUser;
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
            LOG.error( "Error parsing response", e );

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
            LOG.error( "Error executing http request", e );

            throw new OperationFailedException( "Failed to execute http request", e );
        }
    }


    private void checkHttpStatus( CloseableHttpResponse response, int expectedStatus, String actionName )
    {
        int actualStatus = response.getStatusLine().getStatusCode();

        if ( actualStatus != expectedStatus )
        {
            LOG.warn( "Http status code expectation failed: expected {},  actual {}", expectedStatus, actualStatus );

            throw new OperationFailedException(
                    String.format( "Failed to %s: %s, %s", actionName, response.getStatusLine(),
                            readContent( response ) ), null );
        }
    }


    private void close( CloseableHttpResponse response )
    {
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
            LOG.error( "Error reading entity content", e );

            return null;
        }
    }
}
