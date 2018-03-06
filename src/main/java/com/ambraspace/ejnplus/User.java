package com.ambraspace.ejnplus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

@Entity(name="User")
@Table(name="users")
public class User {
	
	private long id;
	private String username;
	private String md5password;
	private String email;
	private Date lastDateInformed;
	private List<CPVCode> cpvs = new ArrayList<CPVCode>();
	
	public User () {
		
	}
	
	public User (String username, String md5password, String email) {
		
		this.username = username;
		this.md5password = md5password;
		this.email = email;
		
	}

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@NaturalId
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMd5password() {
		return md5password;
	}
	public void setMd5password(String md5password) {
		this.md5password = md5password;
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

	@ManyToMany
	public List<CPVCode> getCpvs() {
		return cpvs;
	}

	public void setCpvs(List<CPVCode> cpvs) {
		this.cpvs = cpvs;
	}
	
}
