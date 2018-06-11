package io.subutai.client.hub.api;


import java.util.Date;
import java.util.List;


public interface Organization
{
    String getName();

    String getDescription();

    String getType();

    boolean isVerified();

    Date getRegistrationDate();

    Member getOwner();

    List<Member> getMembers();
}
