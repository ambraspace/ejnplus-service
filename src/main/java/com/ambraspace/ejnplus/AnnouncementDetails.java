package com.ambraspace.ejnplus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity (name="AnnouncementDetails")
@Table (name="announcement_details")
public class AnnouncementDetails
{

	private long id = -1;
	private String text = null;
	
	public AnnouncementDetails()
	{
		
	}
	
	public AnnouncementDetails(long id, String text)
	{
		this.id = id;
		this.text = text;
	}

	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	public long getId()
	{
		return id;
	}

	
	public void setId(long id)
	{
		this.id = id;
	}

	@Column (columnDefinition = "TEXT")
	public String getText()
	{
		return text;
	}

	
	public void setText(String text)
	{
		this.text = text;
	}
	
}
