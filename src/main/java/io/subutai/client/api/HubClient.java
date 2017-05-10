package io.subutai.client.api;


import java.io.IOException;
import java.util.List;

import javax.security.auth.login.FailedLoginException;


public interface HubClient
{
    void login( String username, String password ) throws IOException, FailedLoginException;

    List<Environment> getEnvironments() throws IOException;
}
