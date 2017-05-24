package io.subutai.client.api;


import java.util.List;

import io.subutai.client.api.dto.CreateEnvironmentDto;
import io.subutai.client.api.dto.ModifyEnvironmentDto;


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

    void login( String username, String password );

    List<Environment> getEnvironments();

    void addSshKey( String envId, String sshKey );

    void removeSshKey( String envId, String sshKey );

    List<Peer> getPeers();

    void startContainer( String envId, String contId );

    void stopContainer( String envId, String contId );

    void destroyContainer( String envId, String contId );

    void destroyEnvironment( String envId );

    List<Template> getTemplates();

    void createEnvironment( CreateEnvironmentDto createEnvironmentDto );

    void modifyEnvironment( ModifyEnvironmentDto modifyEnvironmentDto );
}
