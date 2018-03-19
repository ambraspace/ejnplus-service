package com.ambraspace.ejnplus;

import java.io.File;
import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Entity(name="CPVCode")
@Table(name="cpvs")
public class CPVCode {
	
	private String id;
	private String description;
	private CPVCode parent;
	
	public CPVCode() {
		
	}
	
	public CPVCode(String id, String description, CPVCode parent) {
		this.id = id;
		this.description = description;
		this.parent = parent;
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
	
	@ManyToOne
	public CPVCode getParent() {
		return parent;
	}

	public void setParent(CPVCode parent) {
		this.parent = parent;
	}

	

	public static void main(String[] args) throws IOException {
	
		Document doc = Jsoup.parse(new File("/home/ambra/anndetail.html"), null);
		
		Elements els = doc.getElementsByTag("span");
		
		Element el = null;
		String text = null;
		int pos = 0;
		for (int i=0; i<els.size(); i++) {
			el = els.get(i);
			text = el.text();
			if (text.startsWith("II 4.ц.") || text.startsWith("II 4.c.")) {
				pos = i;
				break;
			}
		}
		
		for (int i=pos+1; i<els.size(); i++) {
			el = els.get(i);
			text = el.text().trim();
			if (text.startsWith("II 5")) {
				break;
			}
			if (text.matches("^\\d{8}-\\d")) {
				System.out.println(text);
			}
		}
	}
	

}
