package io.subutai.client.api;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


public class BlueprintDto
{
    @JsonProperty( value = "blueprintSource" )
    private String blueprintSource;

    @JsonProperty( value = "variables" )
    private Map<String, String> variables;


    public BlueprintDto( final String blueprintSource, final Map<String, String> variables )
    {
        this.blueprintSource = blueprintSource;
        this.variables = variables;
    }


    public String getBlueprintSource()
    {
        return blueprintSource;
    }


    public void setBlueprintSource( final String blueprintSource )
    {
        this.blueprintSource = blueprintSource;
    }


    public Map<String, String> getVariables()
    {
        return variables;
    }


    public void setVariables( final Map<String, String> variables )
    {
        this.variables = variables;
    }
}
