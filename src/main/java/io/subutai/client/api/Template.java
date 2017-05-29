package io.subutai.client.api;


import java.util.Set;

import com.google.gson.annotations.SerializedName;


public class Template
{
    public enum HostArchitecture
    {
        I386, I686, X86_64, IA64, ALPHA, AMD64, ARM, ARMEB, ARMEL, HPPA, M32R, M68K, MIPS, MIPSEL, POWERPC, PPC64, S390,
        S390X, SH3, SH3EB, SH4, SH4EB, SPARC, ARMHF, ARMV7, UNKNOWN
    }


    private String id;
    private String name;
    private long size;
    @SerializedName( "owner" )
    private Set<String> owners;
    private Set<String> tags;
    private String parent;
    private String version;
    private String filename;
    private HostArchitecture architecture;


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


    public Set<String> getTags()
    {
        return tags;
    }


    public String getParent()
    {
        return parent;
    }


    public String getVersion()
    {
        return version;
    }


    public String getFilename()
    {
        return filename;
    }


    public HostArchitecture getArchitecture()
    {
        return architecture;
    }


    @Override
    public String toString()
    {
        return "Template{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", size=" + size + ", owners=" + owners
                + ", tags=" + tags + ", parent='" + parent + '\'' + ", version='" + version + '\'' + ", filename='"
                + filename + '\'' + ", architecture=" + architecture + '}';
    }
}
