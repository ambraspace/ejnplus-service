package com.ambraspace.ejnplus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

@Entity(name="User")
@Table(name="users")
public class User {
	
	private long id;
	private String email;
	private Date lastDateInformed;
	private String name;
	private String surname;
	private String gender;
	private List<CPVCode> cpvs = new ArrayList<CPVCode>();

	/*
	 * This entity is used only for reading users, so some fields are left out
	 */
	public User () {
		
	}
	
	@Id
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@NaturalId
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@Column(columnDefinition="DATE")
	public Date getLastDateInformed() {
		return lastDateInformed;
	}

	public void setLastDateInformed(Date lastDateInformed) {
		this.lastDateInformed = lastDateInformed;
	}
	
	public String getName()
	{
		return name;
	}

	
	public void setName(String name)
	{
		this.name = name;
	}

	
	public String getSurname()
	{
		return surname;
	}

	
	public void setSurname(String surname)
	{
		this.surname = surname;
	}

	
	public String getGender()
	{
		return gender;
	}

	
	public void setGender(String gender)
	{
		this.gender = gender;
	}

	@ManyToMany
	public List<CPVCode> getCpvs() {
		return cpvs;
	}

	public void setCpvs(List<CPVCode> cpvs) {
		this.cpvs = cpvs;
	}
	
	
}
