package io.subutai.client.hub.api;


import java.util.Set;

import com.google.gson.annotations.SerializedName;


/**
 * Base class for Kurjun artifacts
 */
public class KurjunArtifact
{
    public static class Hash
    {
        private String md5;
        private String sha256;


        public String getMd5()
        {
            return md5;
        }


        public String getSha256()
        {
            return sha256;
        }
    }


    private String id;
    private String name;
    private long size;
    @SerializedName( "owner" )
    private Set<String> owners;
    private String version;
    private String filename;
    private Hash hash;


    public String getId()
    {
        return id;
    }


    public String getName()
    {
        return name;
    }


    public long getSize()
    {
        return size;
    }


    public Set<String> getOwners()
    {
        return owners;
    }


    public String getVersion()
    {
        return version;
    }


    public String getFilename()
    {
        return filename;
    }


    public Hash getHash()
    {
        return hash;
    }
}
