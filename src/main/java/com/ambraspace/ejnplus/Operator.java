package com.ambraspace.ejnplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.EmailPopulatingBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import com.ambraspace.ejnplus.exceptions.NoCPVCodesException;
import com.ambraspace.ejnplus.exceptions.TokenException;

public class Operator {
	
	private static EntityManagerFactory emf;
	
	public static void collectAnnouncements() {
		
		if (emf==null) {
			emf = Persistence.createEntityManagerFactory("default");
		}
		
		EntityManager em = emf.createEntityManager();
		
		List<Announcement> announcements = null;
		try {
			
			Date fromDate = em.createQuery("select max(a.date) from Announcement a", Date.class).getSingleResult();
			Calendar calFrom = Calendar.getInstance();
			Calendar calTo = Calendar.getInstance();
			calFrom.setTimeInMillis(fromDate.getTime());
			calTo.set(Calendar.AM_PM, Calendar.AM);
			calTo.set(Calendar.HOUR, 0);
			calTo.set(Calendar.MINUTE, 0);
			calTo.set(Calendar.SECOND, 0);
			calTo.set(Calendar.MILLISECOND, 0);
			calTo.add(Calendar.DATE, -1);
			calFrom.add(Calendar.DATE, 1);
			
			if (calFrom.after(calTo)) {
				System.out.println("Wait for a day.");
				return;
			}
			
			announcements = collectAnnouncementsByDate(
					LocalDate.of(
							calFrom.get(Calendar.YEAR), 
							calFrom.get(Calendar.MONTH)+1,
							calFrom.get(Calendar.DAY_OF_MONTH)),
					LocalDate.of(
							calTo.get(Calendar.YEAR), 
							calTo.get(Calendar.MONTH)+1,
							calTo.get(Calendar.DAY_OF_MONTH)));
			
			if (announcements.size()>0) {
				System.out.println("Saving data to the database...");
				em.getTransaction().begin();
	
				for (Announcement an:announcements) {
					em.persist(an);
				}
	
				em.getTransaction().commit();
				System.out.println("Saved.");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoCPVCodesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			em.close();
		}

		
	}
	
	
	private static List<Announcement> collectAnnouncementsByDate(LocalDate dateFrom, LocalDate dateTo) throws IOException, JSONException, TokenException, ParseException, NoCPVCodesException {
		
		List<Announcement> announcements = new ArrayList<Announcement>();
		
		/*
		 * First, get the token.
		 */
		
		Document doc = Jsoup.connect("https://www.ejn.gov.ba/Announcement/Search").get();

		Elements els = doc.getElementsByTag("script");
		
		Iterator<Element> it = els.iterator();
		Element el = null;
		
		String token = null;
		while (it.hasNext()) {
			el = it.next();
			if (el.data().matches("^var csrfToken\\s*=\\s*'\\S+';$")) {
				token = el.data();
				token = token.replaceAll("^var csrfToken\\s*=\\s*'", "");
				token = token.replaceAll("';$", "");
			}
		}
		
		if (token == null || token.equals("")) {
			throw new TokenException();
		} else {
			System.out.println("Token received.");
		}
		
		/*
		 * Now, collect all announcement from specific date.
		 */
		
		String sDateFrom = dateFrom.toString() + "T00:00:00.000Z";
		String sDateTo = dateTo.toString() + "T00:00:00.000Z";

		JSONObject requestData = new JSONObject();
		
		requestData.put("announcementDateFrom", sDateFrom);
		requestData.put("announcementDateTo", sDateTo);
		requestData.put("page", 1);
		requestData.put("rows", 20);
		requestData.put("sidx", "id");
		requestData.put("sord", "DESC");
		requestData.put("cAName", JSONObject.NULL);
		requestData.put("uniqueIdentificationNumber", JSONObject.NULL);
		requestData.put("procedureName", JSONObject.NULL);
		requestData.put("number", JSONObject.NULL);
		requestData.put("type", JSONObject.NULL);
		requestData.put("procedureStatus", JSONObject.NULL);
		requestData.put("contractType", JSONObject.NULL);
		requestData.put("cPVDivision", JSONObject.NULL);
		requestData.put("procedureCategoryId", JSONObject.NULL);
		requestData.put("isAuctionOnline", JSONObject.NULL);
		requestData.put("eProcurement", JSONObject.NULL);
		requestData.put("justValidProcedure", JSONObject.NULL);
		requestData.put("supplierName", JSONObject.NULL);
		requestData.put("supplierUniqueIdentificationNumber", JSONObject.NULL);
		requestData.put("isDocumentationOnline", JSONObject.NULL);
		requestData.put("isTenderDocumentationIconAllowed", JSONObject.NULL);
		requestData.put("isMasterAgreement", JSONObject.NULL);
		requestData.put("isAwardCriterionIconVisible", JSONObject.NULL);
		requestData.put("hasLots", JSONObject.NULL);
		requestData.put("isJointProcurement", JSONObject.NULL);
		requestData.put("isTdVisible", JSONObject.NULL);
		
		URL url = new URL("https://www.ejn.gov.ba/api/Announcement/Search");
		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
		conn.setRequestProperty("X-JsonRequestVerificationToken", token);
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setRequestMethod("POST");
		PrintStream out = new PrintStream(conn.getOutputStream());
		out.println(requestData.toString());
		out.close();
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		
		StringBuffer content = new StringBuffer();
		String line = null;
		while ((line=in.readLine())!=null) {
			content.append(line);
		}
		in.close();
		conn.disconnect();

		JSONObject receivedData = new JSONObject(content.toString());

		int total = receivedData.getInt("total");
		System.out.println("There are " + total + " announcemets.");

		Announcement an = null;
		for (int i=0; i<(total>20 ? 20 : total); i++) {
			an = new Announcement();
			an.setId(receivedData.getJSONArray("records").getJSONObject(i).getLong("id"));
			an.setNumber(receivedData.getJSONArray("records").getJSONObject(i).getString("number"));
			an.setLegalEntity(receivedData.getJSONArray("records").getJSONObject(i).getString("legalEntity"));
			an.setAnnouncementType(receivedData.getJSONArray("records").getJSONObject(i).getString("announcementType"));
			if (receivedData.getJSONArray("records").getJSONObject(i).get("name").getClass().equals(String.class)) {
				an.setName(receivedData.getJSONArray("records").getJSONObject(i).getString("name"));
			}
			an.setDate(new SimpleDateFormat("d.M.yyyy.").parse(
							receivedData.getJSONArray("records").getJSONObject(i).getString("date")));
			an.setUrl(receivedData.getJSONArray("records").getJSONObject(i).getString("downloadUrl"));
			announcements.add(an);
		}
		
		System.out.println("Collected " + announcements.size() + " announcements.");
		
		int pages;
		pages = total / 20;
		if (total>(pages*20))
			pages++;
		
		for (int page=1; page<pages; page++) {

			requestData.put("page", page+1);

			conn = (HttpsURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			conn.setRequestProperty("X-JsonRequestVerificationToken", token);
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestMethod("POST");
			out = new PrintStream(conn.getOutputStream());
			out.println(requestData.toString());
			out.close();
			
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			
			content = new StringBuffer();
			line = null;
			while ((line=in.readLine())!=null) {
				content.append(line);
			}
			in.close();
			conn.disconnect();

			receivedData = new JSONObject(content.toString());

			for (int i=0; i<(total-(page*20)>20 ? 20 : (total-(page*20))); i++) {
				an = new Announcement();
				an.setId(receivedData.getJSONArray("records").getJSONObject(i).getLong("id"));
				an.setNumber(receivedData.getJSONArray("records").getJSONObject(i).getString("number"));
				an.setLegalEntity(receivedData.getJSONArray("records").getJSONObject(i).getString("legalEntity"));
				an.setAnnouncementType(receivedData.getJSONArray("records").getJSONObject(i).getString("announcementType"));
				if (receivedData.getJSONArray("records").getJSONObject(i).get("name").getClass().equals(String.class)) {
					an.setName(receivedData.getJSONArray("records").getJSONObject(i).getString("name"));
				}
				an.setDate(new SimpleDateFormat("d.M.yyyy.").parse(
								receivedData.getJSONArray("records").getJSONObject(i).getString("date")));
				an.setUrl(receivedData.getJSONArray("records").getJSONObject(i).getString("downloadUrl"));
				announcements.add(an);
			}
			
			System.out.println("Collected " + announcements.size() + " announcements.");
		
		}
		
		if (announcements.size()==0) {
			return announcements;
		}

		
		/*
		 * Finally, get CPVs for each announcement
		 */

		EntityManager em = emf.createEntityManager();
		List<CPVCode> codes = em.createQuery("from CPVCode", CPVCode.class).getResultList();
		em.close();

		Map<String, CPVCode> codesMap = new HashMap<>();

		for (CPVCode code:codes) {
			codesMap.put(code.getId(), code);
		}

		for (Announcement announcement:announcements) {
			
			if (announcement.getAnnouncementType().contains("Ispravka obavještenja")) {

				url = new URL("https://www.ejn.gov.ba/api/Announcement/GetRelatedAnnouncements?id=" + announcement.getId());
				conn = (HttpsURLConnection)url.openConnection();
				conn.setRequestProperty("X-JsonRequestVerificationToken", token);
				conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
				conn.setRequestMethod("GET");
				
				in = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				
				content = new StringBuffer();
				line = null;
				while ((line=in.readLine())!=null) {
					content.append(line);
				}
				in.close();
				conn.disconnect();
				
				JSONArray array = new JSONArray(content.toString());

				url = new URL("https://www.ejn.gov.ba/api/Announcement/GetAnnouncementUrl?id=" + 
						array.getJSONObject(0).getInt("id"));
			} else {
				url = new URL("https://www.ejn.gov.ba/api/Announcement/GetAnnouncementUrl?id=" + announcement.getId());
			}
			
			conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestProperty("X-JsonRequestVerificationToken", token);
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestMethod("GET");
			
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			
			content = new StringBuffer();
			line = null;
			while ((line=in.readLine())!=null) {
				content.append(line);
			}
			in.close();
			conn.disconnect();
			
			receivedData = new JSONObject(content.toString());
			
			doc = Jsoup.connect(receivedData.getString("url")).get();
			
			els = doc.getElementsByTag("span");
			
			el = null;
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
					announcement.getCpvs().add(codesMap.get(text));
				}
			}
			
			if (announcement.getCpvs().size()==0) {
				throw new NoCPVCodesException();
			}
			
		}

		return announcements;
	}
	
	
	@SuppressWarnings("unchecked")
	public static void distibuteMails() {
		
		if (emf==null) {
			emf = Persistence.createEntityManagerFactory("default");
		}

		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();
		
		List<User> users = em.createQuery("from User", User.class).getResultList();
		
		for (User user:users) {
			
			Date lastDateInformed = em.createQuery("select u.lastDateInformed from User u where u.id=" + user.getId(), Date.class).getSingleResult();
			
			SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat localDateFormat = new SimpleDateFormat("d.M.yyyy.");
			
			List<Announcement> anns = null;
			if (lastDateInformed == null) {
				anns = em.createNativeQuery(
						"select * from announcements " + 
								"where id in " +
								"(select announcement_id from announcements_cpvs " + 
								"where cpvs_id in " + 
								"(select cpvs_id from users_cpvs " + 
								"where user_id=" + user.getId() + ")) and " +
								"DATEDIFF(CURRENT_DATE, announcement_date)<30 " +
								"order by announcement_date DESC, id DESC",
								Announcement.class).getResultList();
			} else {
				anns = em.createNativeQuery(
						"select * from announcements " + 
								"where id in " +
								"(select announcement_id from announcements_cpvs " + 
								"where cpvs_id in " + 
								"(select cpvs_id from users_cpvs " + 
								"where user_id=" + user.getId() + ")) and " +
								"announcement_date>'" + dbDateFormat.format(lastDateInformed) + "' and " +
								"DATEDIFF(CURRENT_DATE, announcement_date)<30 " +
								"order by announcement_date DESC, id DESC",
								Announcement.class).getResultList();
			}
			
			if (anns.size()==0) {
				continue;
			}
			
			
			EmailPopulatingBuilder emlBuilder = EmailBuilder.startingBlank()
					.to(user.getName() + " " + user.getSurname(), user.getEmail())
					.withSubject("EJNplus - nova obavještenja")
					.from("EJNplus", "noreply@avantous.com")
					.withHTMLText("<style>" +
							"body {background-color: white; font-family: \"Helvetica\", sans-serif; }" +
							"table {border-collapse: collapse; font-size: 0.8em; } " +
							"tr:nth-child(odd) {background-color: #f2f2f2; } " +
							"th {border-top: 1px solid #ddd; border-bottom: 1px solid #ddd; padding: 2px; background-color: black; color: white; } " +
							"td { border-bottom: 1px solid #ddd; padding: 2px; } " +
							"</style>")
					.appendTextHTML("<table><tr>" +
							"<th>Datum</th>" +
							"<th>Ugovorni organ</th>" +
							"<th>Predmet nabavke</th>" +
							"<th>Tip obavještenja</th>" +
							"<th>Broj obavještenja</th></tr>");
			for (Announcement an:anns) {
				if (lastDateInformed==null || an.getDate().after(lastDateInformed)) {
					lastDateInformed = an.getDate();
				}
				emlBuilder.appendTextHTML("<tr>" + 
						"<td>" + localDateFormat.format(an.getDate()) + "</td>" +
						"<td>" + an.getLegalEntity() + "</td>" +
						"<td>" + an.getName() + "</td>" +
						"<td>" + an.getAnnouncementType() + "</td>" +
						"<td><a href=\"" + an.getUrl() + "\">" + an.getNumber() + "</a></td></tr>");
			}
			
			emlBuilder.appendTextHTML("</table>");
			Email eml = emlBuilder.buildEmail();
			
			Mailer mailer = MailerBuilder
					.withSMTPServer("localhost", 25)
					.withTransportStrategy(TransportStrategy.SMTP)
					.buildMailer();
			
			mailer.sendMail(eml);
			
			user.setLastDateInformed(lastDateInformed);
			
		}

		
		
		em.getTransaction().commit();
		
		em.close();

		
	}
	
	
	public static void cleanUp() {
		
		if (emf!=null) {
			emf.close();
		}
		
		
	}
	

	
	public static void main(String[] args) {
		
		collectAnnouncements();
		
		distibuteMails();
		
		cleanUp();
	
	}
}
