package io.subutai.client.hub.api;


public interface RhCpuData
{

    Double getSystem();


    Double getIdle();


    Double getIowait();


    Double getUser();


    Double getNice();


    Double getLoad();


    Double getUsed();
}
