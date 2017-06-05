package io.subutai.client.api;


import java.util.List;


public interface HubClient
{
    enum HubEnv
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

    /**
     * Authorizes client with Hub
     */
    void login( String username, String password );

    /**
     * Returns list of user environments
     */
    List<Environment> getEnvironments();


    /**
     * Returns all ssh keys assigned to environment
     *
     * @param envId environment id
     */
    List<SshKey> getSshKeys( String envId );


    /**
     * Adds SSH key to specified environment
     *
     * @param envId environment id
     * @param sshKey ssh key
     */
    void addSshKey( String envId, String sshKey );

    /**
     * Removes SSH key from specified environment
     *
     * @param envId environment id
     * @param sshKey ssh key
     */
    void removeSshKey( String envId, String sshKey );

    /**
     * Returns list of peers to which user has access permission (public , own or shared peers)
     */
    List<Peer> getPeers();

    /**
     * Returns list of own peers
     */
    List<Peer> getOwnPeers();

    /**
     * Returns list of peers shared with the current user
     */
    List<Peer> getSharedPeers();

    /**
     * Returns list of favorite peers
     */
    List<Peer> getFavoritePeers();

    /**
     * Returns list of all public peers
     */
    List<Peer> getPublicPeers();

    /**
     * Stops container
     *
     * @param envId environment id
     * @param contId container id
     */
    void startContainer( String envId, String contId );

    /**
     * Stops container
     *
     * @param envId environment id
     * @param contId container id
     */
    void stopContainer( String envId, String contId );

    /**
     * Destroys container
     *
     * @param envId environment id
     * @param contId container id
     */
    void destroyContainer( String envId, String contId );

    /**
     * Destroys environment
     *
     * @param envId environment id
     */
    void destroyEnvironment( String envId );

    /**
     * Returns list of templates accessible to user
     */
    List<Template> getTemplates();

    /**
     * Creates environment creation request object that should be populated further by calling party.
     *
     * @param environmentName - name of future environment
     */
    CreateEnvironmentRequest createRequest( String environmentName );

    /**
     * Creates environment modification request object that should be populated further by calling party.
     *
     * @param environmentId environment id
     */
    ModifyEnvironmentRequest modifyRequest( String environmentId );

    /**
     * Allows to create environment
     *
     * @param createEnvironmentRequest create environment request object returned by call to HubClient#createRequest
     */
    void createEnvironment( CreateEnvironmentRequest createEnvironmentRequest );

    /**
     * Allows to modify environment
     *
     * @param modifyEnvironmentRequest modify environment request object returned by call to HubClient#modifyRequest
     */
    void modifyEnvironment( ModifyEnvironmentRequest modifyEnvironmentRequest );

    /**
     * Returns user balance
     */
    Double getBalance();

    /**
     * Shares peer with user
     *
     * @param peerId peer id to share
     * @param userId user id to share with
     */
    void sharePeer( String peerId, String userId );

    /**
     * Unshares shared peer with user
     *
     * @param peerId peer id to unshare
     * @param userId user id to unshare with
     */
    void unsharePeer( String peerId, String userId );

    /**
     * Update peer scope
     *
     * @param peerId peer id
     * @param scope new scope
     */
    void updatePeerScope( String peerId, Peer.Scope scope );

    /**
     * Update peer name
     *
     * @param peerId peer id
     * @param name new name
     */
    void updatePeerName( String peerId, String name );

    /**
     * Adds peer to favorites
     *
     * @param peerId peer id
     */
    void addPeerToFavorites( String peerId );

    /**
     * Removes peer from favorites
     *
     * @param peerId peer id
     */
    void removePeerFromFavorites( String peerId );
}
