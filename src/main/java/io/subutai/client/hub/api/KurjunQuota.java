package io.subutai.client.api;


import com.google.gson.GsonBuilder;


/**
 * Represents user quota on Kurjun<br/> <b>left</b> - number of bytes unused<br/> <b>used</b> - number of bytes
 * used<br/> <b>quota</b> - total number of bytes in quota
 */
public class KurjunQuota
{
    private Long left;
    private Long quota;
    private Long used;


    public Long getLeft()
    {
        return left;
    }


    public Long getQuota()
    {
        return quota;
    }


    public Long getUsed()
    {
        return used;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
