package io.subutai.client.hub.api;


import java.util.Set;

import com.google.gson.GsonBuilder;


public class Template extends KurjunArtifact
{
    public enum HostArchitecture
    {
        I386, I686, X86_64, IA64, ALPHA, AMD64, ARM, ARMEB, ARMEL, HPPA, M32R, M68K, MIPS, MIPSEL, POWERPC, PPC64, S390,
        S390X, SH3, SH3EB, SH4, SH4EB, SPARC, ARMHF, ARMV7, UNKNOWN
    }


    private Set<String> tags;
    private String parent;
    private HostArchitecture architecture;


    public Set<String> getTags()
    {
        return tags;
    }


    public String getParent()
    {
        return parent;
    }


    public HostArchitecture getArchitecture()
    {
        return architecture;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
