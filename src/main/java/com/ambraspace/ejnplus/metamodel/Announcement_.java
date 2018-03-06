package com.ambraspace.ejnplus.metamodel;

import java.util.Date;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.ambraspace.ejnplus.Announcement;
import com.ambraspace.ejnplus.CPVCode;

@StaticMetamodel(Announcement.class)
public class Announcement_ {
	
	public static volatile SingularAttribute<Announcement, Integer> id;
	public static volatile SingularAttribute<Announcement, Date> date;
	public static volatile ListAttribute<Announcement, CPVCode> cpvs;
	
}
