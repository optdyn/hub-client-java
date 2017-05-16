package io.subutai.client.api;


import java.util.Date;


public interface ResourceHost
{
    enum P2pStatus
    {
        WORKS, FAILS
    }

    String getRhId();


    String getRhName();


    Date getRhCreationDate();


    Double getRhUptime();

    RhCpuModel getRhCpuModel();

    RhCpuData getRhCpuData();

    RhMemoryData getRhMemoryData();

    RhMemoryModel getRhMemoryModel();

    RhDiskData getRhDiskData();

    RhDiskModel getRhDiskModel();

    RhNetData getRhNetData();

    String getRhP2pVersion();

    P2pStatus getRhP2pStatus();
}
