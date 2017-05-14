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

    void login( String username, String password );

    List<Environment> getEnvironments();

    void addSshKey( String envId, String sshKey );

    void removeSshKey( String envId, String sshKey );
}
