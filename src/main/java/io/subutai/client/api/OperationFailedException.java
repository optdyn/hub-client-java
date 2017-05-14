package io.subutai.client.api;


public class OperationFailedException extends RuntimeException
{
    public OperationFailedException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
