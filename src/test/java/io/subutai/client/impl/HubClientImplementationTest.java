package io.subutai.client.impl;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.client.api.Environment;
import io.subutai.client.api.HubClient;
import io.subutai.client.api.Peer;


@RunWith( MockitoJUnitRunner.class )
public class HubClientImplementationTest
{

    //TODO change before commit to Git
    private static final String username = "test.d@mail.com";
    private static final String password = "test";

    HubClient hubClient;


    @Before
    public void setUp() throws Exception
    {
        hubClient = HubClients.getClient( HubClient.HubEnv.DEV );
        hubClient.login( username, password );
    }


    @Test
    public void testGetEnvironments() throws Exception
    {
        List<Environment> environments = hubClient.getEnvironments();

        for ( Environment e : environments )
        {
            System.out.println( e );
        }
    }


    @Test
    public void testGetPeers() throws Exception
    {
        List<Peer> peers = hubClient.getPeers();

        for ( Peer p : peers )
        {
            System.out.println( p );
        }
    }


    @Test
    public void testStopContainer() throws Exception
    {
        hubClient.stopContainer( "bc8b8e43-0416-4ad4-a002-a4b8ad61b1f2", "33416CAEC7D07CABD7C73AB0FE1EF92DBA27FCB6" );
    }


    @Test
    public void testAddSshKey() throws Exception
    {
        hubClient.addSshKey( "bc8b8e43-0416-4ad4-a002-a4b8ad61b1f2",
                "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCjUo/8VklFC8cRyHE502tUXit15L8Qg2z/47c6PMpQThR0sjhURgoILms"
                        + "/IX180yGqgkpjdX08MIkmANhbXDmSFh6T4lUzqGGoC7lerePwkA2yJWlsP+7JKk9oDSaYJ3lkfvKZnz8ZG7JS1jg"
                        + "+sRiTsYYfyANHBJ8sDAK+eNDDms1oorrxk704r8oeNuRaE4BNKhVO4wpRJHEo4/uztLB0jkvG5OUFea5E0jCk"
                        + "+tUK4R7kJBecYQGkJj4ILt/cAGrY0sg8Ol+WBOq4ex3zCF1zJrdJCxW4t2NUyNfCxW7kV2uUhbWNuj+n"
                        +
                        "/I5a8CDrMJsJLqdgC3EQ17uRy41GHbTwBQs0q2gwfBpefHFXokWwxu06hk0jfwFHWm9xRT79a56hr101Fy4uNjzzVtrWDS4end9VC7bt7Xf/kDxx7FB9DW1wfaYMcCp6YD5O8ENpl35gK35ZXtT5BP2GBoxHGlPdF4PObMCNi5ATtO/gLD8kW1LutO2ldsaY4sHm/JG55UNrpQCpIYe6QfkHsO+fX9/WmjP+iTDdHs1untgurvk5KdhtQxecTvTk3M/ewzHZbEbzYJYzFOsy5f6FQ8U/ckw8PejBzGDUiMGTJXl+GjV9VV3BmkKKeqD5uKu+gta5dynbdfU4r7heAV6oxan2x/rg9iHpOklIRtu2chJYJUq7lQ== dilshat.aliev@gmail.com" );
    }


    @Test
    public void testRemoveSshKey() throws Exception
    {
        hubClient.removeSshKey( "bc8b8e43-0416-4ad4-a002-a4b8ad61b1f2",
                "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCjUo/8VklFC8cRyHE502tUXit15L8Qg2z/47c6PMpQThR0sjhURgoILms"
                        + "/IX180yGqgkpjdX08MIkmANhbXDmSFh6T4lUzqGGoC7lerePwkA2yJWlsP+7JKk9oDSaYJ3lkfvKZnz8ZG7JS1jg"
                        + "+sRiTsYYfyANHBJ8sDAK+eNDDms1oorrxk704r8oeNuRaE4BNKhVO4wpRJHEo4/uztLB0jkvG5OUFea5E0jCk"
                        + "+tUK4R7kJBecYQGkJj4ILt/cAGrY0sg8Ol+WBOq4ex3zCF1zJrdJCxW4t2NUyNfCxW7kV2uUhbWNuj+n"
                        +
                        "/I5a8CDrMJsJLqdgC3EQ17uRy41GHbTwBQs0q2gwfBpefHFXokWwxu06hk0jfwFHWm9xRT79a56hr101Fy4uNjzzVtrWDS4end9VC7bt7Xf/kDxx7FB9DW1wfaYMcCp6YD5O8ENpl35gK35ZXtT5BP2GBoxHGlPdF4PObMCNi5ATtO/gLD8kW1LutO2ldsaY4sHm/JG55UNrpQCpIYe6QfkHsO+fX9/WmjP+iTDdHs1untgurvk5KdhtQxecTvTk3M/ewzHZbEbzYJYzFOsy5f6FQ8U/ckw8PejBzGDUiMGTJXl+GjV9VV3BmkKKeqD5uKu+gta5dynbdfU4r7heAV6oxan2x/rg9iHpOklIRtu2chJYJUq7lQ== dilshat.aliev@gmail.com" );
    }
}
