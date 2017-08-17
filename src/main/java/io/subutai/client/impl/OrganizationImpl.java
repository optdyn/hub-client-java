package io.subutai.client.impl;


import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;

import io.subutai.client.api.Member;
import io.subutai.client.api.Organization;


public class OrganizationImpl implements Organization
{

    private String name;
    private String description;
    private String type;
    private boolean verified;
    private Date registerDate;
    private MemberImpl owner;
    private List<MemberImpl> members;


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public String getType()
    {
        return type;
    }


    @Override
    public boolean isVerified()
    {
        return verified;
    }


    public Date getRegistrationDate()
    {
        return registerDate;
    }


    @Override
    public Member getOwner()
    {
        return owner;
    }


    @Override
    public List<Member> getMembers()
    {
        List<Member> membersList = Lists.newArrayList();
        membersList.addAll( members );
        return membersList;
    }


    @Override
    public String toString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson( this );
    }
}
