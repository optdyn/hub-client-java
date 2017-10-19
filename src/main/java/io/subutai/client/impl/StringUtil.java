package io.subutai.client.impl;


import com.google.common.base.Strings;


public class StringUtil
{

    public static boolean isBlank( String str )
    {
        return Strings.nullToEmpty( str ).trim().isEmpty();
    }
}
