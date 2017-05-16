package io.subutai.client.impl;


import java.util.Date;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.subutai.client.api.Peer;


public class PeerImpl implements Peer
{
    @SerializedName( "peer_id" )
    private String peerId;
    @SerializedName( "peer_name" )
    private String peerName;
    @SerializedName( "peer_ip" )
    private String peerIp;
    @SerializedName( "peer_status" )
    private Status peerStatus;
    @SerializedName( "peer_owner_id" )
    private long peerOwnerId;
    @SerializedName( "peer_owner_name" )
    private String peerOwnerName;
    @SerializedName( "peer_version" )
    private String peerVersion;
    @SerializedName( "peer_registration_date" )
    private Date peerRegistrationDate;
    @SerializedName( "peer_scope" )
    private Scope peerScope;


    public String getPeerId()
    {
        return peerId;
    }


    public String getPeerName()
    {
        return peerName;
    }


    public String getPeerIp()
    {
        return peerIp;
    }


    public Status getPeerStatus()
    {
        return peerStatus;
    }


    public long getPeerOwnerId()
    {
        return peerOwnerId;
    }


    public String getPeerOwnerName()
    {
        return peerOwnerName;
    }


    public String getPeerVersion()
    {
        return peerVersion;
    }


    public Date getPeerRegistrationDate()
    {
        return peerRegistrationDate;
    }


    public Scope getPeerScope()
    {
        return peerScope;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
