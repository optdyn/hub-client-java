package io.subutai.client.impl;


import java.util.Date;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.ResourceHost;
import io.subutai.client.api.RhDiskData;
import io.subutai.client.api.RhDiskModel;
import io.subutai.client.api.RhMemoryData;
import io.subutai.client.api.RhMemoryModel;
import io.subutai.client.api.RhNetData;


public class ResourceHostImpl implements ResourceHost
{
    @SerializedName( "rh_id" )
    private String rhId;
    @SerializedName( "rh_name" )
    private String rhName;
    @SerializedName( "rh_creation_date" )
    private Date rhCreationDate;
    @SerializedName( "rh_uptime" )
    private Double rhUptime;
    @SerializedName( "rh_cpu_model" )
    private RhCpuModelImpl rhCpuModel;
    @SerializedName( "rh_cpu_data" )
    private RhCpuDataImpl rhCpuData;
    @SerializedName( "rh_memory_data" )
    private RhMemoryDataImpl rhMemoryData;
    @SerializedName( "rh_memory_model" )
    private RhMemoryModelImpl rhMemoryModel;
    @SerializedName( "rh_disk_data" )
    private RhDiskDataImpl rhDiskData;
    @SerializedName( "rh_disk_model" )
    private RhDiskModelImpl rhDiskModel;
    @SerializedName( "rh_net_data" )
    private RhNetDataImpl rhNetData;
    //TODO


    public String getRhId()
    {
        return rhId;
    }


    public String getRhName()
    {
        return rhName;
    }


    public Date getRhCreationDate()
    {
        return rhCreationDate;
    }


    public Double getRhUptime()
    {
        return rhUptime;
    }


    public RhCpuModelImpl getRhCpuModel()
    {
        return rhCpuModel;
    }


    public RhCpuDataImpl getRhCpuData()
    {
        return rhCpuData;
    }


    public RhMemoryData getRhMemoryData()
    {
        return rhMemoryData;
    }


    public RhMemoryModel getRhMemoryModel()
    {
        return rhMemoryModel;
    }


    public RhDiskData getRhDiskData()
    {
        return rhDiskData;
    }


    public RhDiskModel getRhDiskModel()
    {
        return rhDiskModel;
    }


    public RhNetData getRhNetData()
    {
        return rhNetData;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
