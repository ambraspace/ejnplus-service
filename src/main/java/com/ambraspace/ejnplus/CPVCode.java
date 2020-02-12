package com.ambraspace.ejnplus;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name="CPVCode")
@Table(name="cpvs")
public class CPVCode {
	
	private String id;
	private String description;
	/*
	 * This is used only for reading CPV codes, so no need to
	 * specify parent field
	 */
	public CPVCode() {
		
	}
	
	public CPVCode(String id, String description) {
		this.id = id;
		this.description = description;
	}

	@Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
