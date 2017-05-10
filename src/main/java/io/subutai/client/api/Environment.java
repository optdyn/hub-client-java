package io.subutai.client.api;


import java.util.List;


public interface Environment
{
    long getEnvironmentTtl();


    String getEnvironmentKey();


    String getEnvironmentId();


    String getEnvironmentStatusDescription();


    EnvironmentStatus getEnvironmentStatus();


    String getEnvironmentName();


    String getEnvironmentHash();


    List<Container> getContainers();
}
