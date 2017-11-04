package io.subutai.client.hub.impl;


import java.util.Map;


public class BlueprintDto
{
    private String blueprintSource;

    private Map<String, String> variables;


    public BlueprintDto( final String blueprintSource, final Map<String, String> variables )
    {
        this.blueprintSource = blueprintSource;
        this.variables = variables;
    }
}
