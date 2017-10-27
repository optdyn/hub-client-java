package io.subutai.client.hub.impl;


import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


public class DateDeserializer implements JsonDeserializer<Date>
{

    private static final Logger LOG = LoggerFactory.getLogger( DateDeserializer.class );


    @Override
    public Date deserialize( JsonElement element, Type arg1, JsonDeserializationContext arg2 ) throws JsonParseException
    {
        String date = element.getAsString();

        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
        formatter.setTimeZone( tryToGetLocalTZ() );

        try
        {
            return formatter.parse( date );
        }
        catch ( ParseException e )
        {
            LOG.error( "Failed to parse Date " + date, e );
            return null;
        }
    }


    private TimeZone tryToGetLocalTZ()
    {
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        return TimeZone.getTimeZone( timeZone.getDisplayName( false, TimeZone.SHORT ) );
    }
}