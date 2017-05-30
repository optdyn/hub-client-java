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
}
