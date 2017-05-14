package io.subutai.client.api;


public class LoginFailedException extends RuntimeException
{
    public LoginFailedException( final String message )
    {
        super( message );
    }
}
