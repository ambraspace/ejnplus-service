package com.ambraspace.ejnplus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity (name="Announcement")
@Table (name="announcements")
public class Announcement {
	
	private long id;
	private String number;
	private String legalEntity;
	private String name;
	private String announcementType;
	private Date date;
	private String url;
	private List<CPVCode> cpvs = new ArrayList<CPVCode>();
	private AnnouncementDetails details;
	
	
	public Announcement() {
		
	}
	
	public Announcement(long id, String number,
			String legalEntity, String name, String announcementType,
			Date date, String url, AnnouncementDetails details) {
		
		this.id = id;
		this.number = number;
		this.legalEntity = legalEntity;
		this.name = name;
		this.announcementType = announcementType;
		this.date = date;
		this.url = url;
		this.details = details;
		
	}

	@Id
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "announcement_number")
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getLegalEntity() {
		return legalEntity;
	}
	public void setLegalEntity(String legalEntity) {
		this.legalEntity = legalEntity;
	}
	
	@Column(columnDefinition="TEXT")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAnnouncementType() {
		return announcementType;
	}
	public void setAnnouncementType(String announcementType) {
		this.announcementType = announcementType;
	}
	
	@Column(name = "announcement_date", columnDefinition="DATE")
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@ManyToMany
	public List<CPVCode> getCpvs() {
		return cpvs;
	}
	public void setCpvs(List<CPVCode> cpvs) {
		this.cpvs = cpvs;
	}

	@OneToOne (fetch=FetchType.LAZY)
	@JoinColumn (name = "details_id")
	public AnnouncementDetails getDetails()
	{
		return details;
	}

	
	public void setDetails(AnnouncementDetails details)
	{
		this.details = details;
	}
	
}
