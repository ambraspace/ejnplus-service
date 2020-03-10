package com.ambraspace.ejnplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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
	
	
	String token = null;
	Map<String, CPVCode> codes = null;
	
	public void collectAnnouncements() {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
		
		EntityManager em = emf.createEntityManager();
		
		try {
			
			Date fromDate = em.createQuery("select max(a.date) from Announcement a", Date.class).getSingleResult();
			if (fromDate == null)
			{
				fromDate = (new SimpleDateFormat("yyyy-MM-dd")).parse("2014-11-27");
			}
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
			

			/*
			 * First, get the token.
			 */
			
			Document doc = Jsoup.connect("https://www.ejn.gov.ba/Announcement/Search").get();

			Elements els = doc.getElementsByTag("script");
			
			Iterator<Element> it = els.iterator();
			Element el = null;
			
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
			 * Prepare CPVCode's Map
			 */
			
			codes = new HashMap<String, CPVCode>();
			
			List<CPVCode> codeList = em.createQuery("from CPVCode", CPVCode.class).getResultList();
			for (CPVCode c:codeList)
			{
				codes.put(c.getId(), c);
			}
			
			/*
			 * Now, collect all announcement from specific date.
			 */
			
			LocalDate dateFrom = LocalDate.of(
					calFrom.get(Calendar.YEAR), 
					calFrom.get(Calendar.MONTH)+1,
					calFrom.get(Calendar.DAY_OF_MONTH));
			
			LocalDate dateTo = LocalDate.of(
					calTo.get(Calendar.YEAR), 
					calTo.get(Calendar.MONTH)+1,
					calTo.get(Calendar.DAY_OF_MONTH));
			
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
			
			
			List<Announcement> anns = getAnnouncementsFromJSON(receivedData);
			
			em.getTransaction().begin();
			for (Announcement ann:anns)
			{
				em.persist(ann.getDetails());
				em.persist(ann);
			}
			em.getTransaction().commit();
			
			
			int pages;
			pages = total / 20;
			if (total>(pages*20)) pages++;
			
			for (int page=2; page<=pages; page++) {

				requestData.put("page", page);

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
				
				anns = getAnnouncementsFromJSON(receivedData);
				
				em.getTransaction().begin();
				for (Announcement ann:anns)
				{
					em.persist(ann.getDetails());
					em.persist(ann);
				}
				em.getTransaction().commit();

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
			emf.close();
		}

		
	}
	
	
	private List<Announcement> getAnnouncementsFromJSON(JSONObject jsonData)
			throws NoCPVCodesException, JSONException, ParseException, IOException
	{
		
		List<Announcement> anns = new ArrayList<Announcement>();

		for (int i=0; i<jsonData.getJSONArray("records").length(); i++) {
			Announcement ann = new Announcement();
			JSONObject jsonAnn = jsonData.getJSONArray("records").getJSONObject(i);
			ann.setId(jsonAnn.getLong("id"));
			ann.setNumber(jsonAnn.getString("number"));
			ann.setLegalEntity(jsonAnn.getString("legalEntity"));
			ann.setAnnouncementType(jsonAnn.getString("announcementType"));
			if (jsonAnn.get("name").getClass().equals(String.class)) {
				ann.setName(jsonAnn.getString("name"));
			}
			ann.setDate(new SimpleDateFormat("d.M.yyyy.").parse(
					jsonAnn.getString("date")));
			ann.setUrl(jsonAnn.getString("downloadUrl"));
			ann.setDetails(new AnnouncementDetails());
			anns.add(ann);
		}
		

		for (Announcement ann:anns) {
			
			URL url = null;
			HttpsURLConnection conn = null;
			BufferedReader in = null;
			StringBuffer content = null;
			String line = null;
			if (ann.getAnnouncementType().contains("Ispravka obavještenja")) {

				url = new URL("https://www.ejn.gov.ba/api/Announcement/GetRelatedAnnouncements?id=" + ann.getId());
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
				url = new URL("https://www.ejn.gov.ba/api/Announcement/GetAnnouncementUrl?id=" + ann.getId());
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
			
			JSONObject receivedData = new JSONObject(content.toString());
			
			// Preuzmi PDF
			
			URL dlPDF = new URL(receivedData.getString("url").replace(".html", ".pdf"));
			HttpsURLConnection connection = (HttpsURLConnection)dlPDF.openConnection();
			InputStream input = connection.getInputStream();
			PDDocument pdfDocument = PDDocument.load(input);
			input.close();
			connection.disconnect();
			
			// Razdvoji stranice
			
			Splitter splitter = new Splitter();
			List<PDDocument> pdfPages = splitter.split(pdfDocument);

			// Pretvori u TXT svaku stranicu
			
			List<String> pdfPageTexts = new ArrayList<String>();
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			for (PDDocument pdfPage:pdfPages)
			{
				pdfPageTexts.add(stripper.getText(pdfPage));
			}
			
			
			// Cleanup
			for (PDDocument d:pdfPages)
			{
				d.close();
			}
			pdfDocument.close();
			
			// Izbaci prazne linije iz svakog TXT fajla
			// i kombinuj sve TXT fajlove u jedan String
			String[] lines = null;
			List<String> newDocTxt = new ArrayList<String>();
			for (String pdfPageText:pdfPageTexts)
			{
				pdfPageText = pdfPageText.replaceAll("\r", "\n");
				lines = pdfPageText.split("\n");

				for (String l:lines)
				{
					l=l.trim();
					if (!l.equals(""))
					{
						newDocTxt.add(l);
					}
				}
				
				newDocTxt.remove(newDocTxt.size()-1);

			}
			
			// Izdvoji CPV kodove
			
			int start=0, stop=newDocTxt.size()-1;
			String l = null;
			for (int i=0; i<newDocTxt.size(); i++)
			{
				l=newDocTxt.get(i);
				if (l.startsWith("II 4.c") || l.startsWith("II 4.ц")) {
					start=i+1;
					break;
				} else {
					continue;
				}
			}
			
			for (int i=start; i<newDocTxt.size(); i++)
			{
				l=newDocTxt.get(i);
				if (l.startsWith("II")) {
					stop=i-1;
					break;
				} else {
					continue;
				}
			}
			
			Pattern cpvPattern = Pattern.compile("\\d{8,8}-\\d{1,1}");
			Matcher matcher = null;
			String cpvCode = null;
			for (int i=start; i<=stop; i++)
			{
				l = newDocTxt.get(i);
				matcher = cpvPattern.matcher(l);
				if (matcher.find())
				{
					cpvCode = l.substring(matcher.start(), matcher.end());
					ann.getCpvs().add(codes.get(cpvCode));
				} else {
					continue;
				}
				
			}
			
			if (ann.getCpvs().size()==0) {
				throw new NoCPVCodesException();
			}
			
			StringBuffer text = new StringBuffer();
			for (String row:newDocTxt)
			{
				text.append(row + "\n");
			}
			ann.getDetails().setText(text.toString());
			
		}
		
		return anns;
		

	}
	
	
	@SuppressWarnings("unchecked")
	public void distibuteMails() {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
		
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();
		
		List<User> users = em.createQuery("from User", User.class).getResultList();
		
		for (User user:users) {
			
			Date lastDateInformed = user.getLastDateInformed();
			
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
						"<td><a href=\"https://ejnplus.avantous.com/getdoc.php?type=announcement&id=" + an.getId() + "\">" + an.getNumber() + "</a></td></tr>");
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

	
	public static void main(String[] args) {
		
		Operator o = new Operator();
		
		o.collectAnnouncements();
		o.distibuteMails();
		
	}
	
}
