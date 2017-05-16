package io.subutai.client.api;


public interface RhMemoryData
{
    double getActive();


    double getCached();


    double getMemFree();


    double getBuffers();


    double getTotalRam();


    double getAvailableRam();
}
