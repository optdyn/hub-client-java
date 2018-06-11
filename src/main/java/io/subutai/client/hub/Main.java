package io.subutai.client.hub;


import io.subutai.client.hub.api.HubClient;
import io.subutai.client.hub.api.Peer;
import io.subutai.client.hub.api.ResourceHost;
import io.subutai.client.hub.impl.HubClients;
import io.subutai.client.hub.impl.StringUtil;


public class Main
{
    public static void main( String[] args )
    {
        HubClient hubClient = HubClients.getClient( HubClient.HubEnv.DEV );
        hubClient.login( "test.d@mail.com", "test" );

        //if only own peers are needed, use hubClient.getOwnPeers()
        for ( Peer peer : hubClient.getPeers() )
        {
            for ( ResourceHost resourceHost : peer.getResourceHosts() )
            {

                if ( !StringUtil.isBlank( resourceHost.getRhIp() ) )
                {
                    System.out.printf( "Name: %s, Ip: %s%n", resourceHost.getRhName(), resourceHost.getRhIp() );
                }
            }
        }
    }
}
