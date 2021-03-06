package io.subutai.client.api;


import java.util.List;


public interface Environment
{
    enum EnvironmentStatus
    {
        PENDING, EMPTY, UNDER_MODIFICATION, HEALTHY, IMPORTING, UNHEALTHY
    }

    long getEnvironmentTtl();


    String getEnvironmentKey();


    String getEnvironmentId();


    String getEnvironmentStatusDescription();


    EnvironmentStatus getEnvironmentStatus();


    String getEnvironmentName();


    String getEnvironmentHash();


    List<Container> getContainers();


    String getEnvironmentP2pSubnet();


    String getEnvironmentSubnetCidr();


    long getEnvironmentOwnerHubId();


    String getEnvironmentOwner();


    long getEnvironmentVni();
}
