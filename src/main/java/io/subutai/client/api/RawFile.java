package io.subutai.client.api;


import com.google.gson.GsonBuilder;


public class RawFile extends KurjunArtifact
{
    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
