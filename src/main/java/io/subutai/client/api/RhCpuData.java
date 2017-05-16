package io.subutai.client.api;


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
