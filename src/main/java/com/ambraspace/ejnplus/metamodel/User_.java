package com.ambraspace.ejnplus.metamodel;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.ambraspace.ejnplus.CPVCode;
import com.ambraspace.ejnplus.User;

@StaticMetamodel(User.class)
public class User_ {

    public static volatile SingularAttribute<User, Integer> id;
    public static volatile SingularAttribute<User, String> username;
    public static volatile ListAttribute<User, CPVCode> cpvs;

}
