package io.subutai.client.hub.api;


public class OperationFailedException extends RuntimeException
{
    public OperationFailedException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
