package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

class Article {
	public String id;
	public String story;
	public String event;
	public String country;
	public String date;
	public String status;
	public HashSet<String> containment;
	public HashSet<String> disease;
	public HashSet<String> victim;
}
public class Driver {
	
	public static List<File> dev_files = new ArrayList<File>();
	public static List<Article> ans_templates = new ArrayList<Article>();
	public static List<Article> output_templates = new ArrayList<Article>();
	
	public static HashMap<String, String> eventRules = new HashMap<String, String>();
	public static HashMap<String, String> containmentRules = new HashMap<String, String>();
	public static HashMap<String, String> statusRules = new HashMap<String, String>();
	public static HashMap<String, String> perpOrgRules = new HashMap<String, String>();
	public static HashMap<String, String> diseaseRules = new HashMap<String, String>();
	public static HashMap<String, String> victimRules = new HashMap<String, String>();
	
	public static HashSet<String> countriesList = new HashSet<String>();

	public static void main(String args[]) throws FileNotFoundException, IOException {
		
//		File dev_folder = new File(args[1]);
		parseSeeds();
		File dev_folder = new File("data/labeled-docs");
		
		File[] listOfDevFiles = dev_folder.listFiles();

		for (File file : listOfDevFiles) {
			if (file.isFile()) {
				dev_files.add(file);
			}
		}
		
		File ans_folder = new File("data/templates");
		File[] list = ans_folder.listFiles();
		
		for (File file : list) {
			if (file.isFile()) {
				ans_templates.add(parseAnswerFile(file));
			}
		}
		
		generateTemplate();
	}
	
	public static void parseSeeds() throws FileNotFoundException, IOException {
		File countriesFile = new File("countries.txt");
		
		try (BufferedReader br = new BufferedReader(new FileReader(countriesFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				countriesList.add(line);
			}
		}
	}
	
	public static Article parseAnswerFile(File file) throws FileNotFoundException, IOException {
//		Story:               20030416.0928
//		ID:                  1
//		Date:                May 16, 2003
//		Event:               outbreak
//		Status:              confirmed
//		Containment:         quarantine
//		                     culling
//		Country:             ERITREA
//		Disease:             Contagious Bovine Pleuropneumonia / Mycoplasma mycoides subsp. mycoides SC (MmmSC)
//		Victims:             imported  cattle
//
//
//		Bytespans (Template 1): 443-476 1743-1789 2001-2017 
		Article a = new Article();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			String prev = "";
			while ((line = br.readLine()) != null) {
				if(line.contains(":")) {
					String[] split = line.split(":");
					if(split[0].equals("Story")) {
						a.story = line.substring(21);
						//System.out.println(a.story);
						//System.out.println(line.substring(21, line.length()-1));
					}
					if(split[0].equals("ID")) {
						a.id = line.substring(21);
					}
					if(split[0].equals("Date")) {
						a.date = (line.substring(21));
					}
					if(split[0].equals("Event")) {
						a.event = line.substring(21);
					}
					if(split[0].equals("Country")) {
						a.country = line.substring(21);
					}
					if(split[0].equals("Status")) {
						a.status = line.substring(21);
					}
					if(split[0].equals("Containment")) {
						a.containment = new HashSet<String>();
						a.containment.add(line.substring(21));
						prev = "Containment";
					}
					if(split[0].equals("Disease")) {
						a.disease = new HashSet<String>();
						a.disease.add(line.substring(21));
						prev = "Disease";
					}
					if(split[0].equals("Victims")) {
						a.victim = new HashSet<String>();
						a.victim.add(line.substring(21));
						prev = "Victim";
					}
				} else {
					if(prev.equals("Containment")) {
						a.containment.add(line.substring(21));
					} else if (prev.equals("Victim")) {
						if(line.length() != 0){
							a.victim.add(line.substring(21));
						}
					} else if (prev.equals("Disease")) {
						a.disease.add(line.substring(21));
					}
				}
			}
		}
		return a;
	}
	
	public static void evaluate(HashMap<String, Article> outputs,HashMap<String, Article> answers ) {

//		   RECALL          PRECISION       F-MEASURE
//Incident        1.00 (1/1)	   1.00 (1/1)         1.00
//Weapons         0.00 (0/0)	   0.00 (0/1)         0.00
//Perp_Ind        0.00 (0/1)	   0.00 (0/0)         0.00
//Perp_Org        0.00 (0/0)	   0.00 (0/0)         0.00
//Targets         0.00 (0/0)	   0.00 (0/0)         0.00
//Victims         0.00 (0/1)	   0.00 (0/2)         0.00
//--------        --------------     --------------     ----
//TOTAL           0.33 (1/3)	   0.25 (1/4)         0.29
//Story:               20000123.0120
//ID:                  1
//Date:                February 23, 2000
//Event:               outbreak
//Status:              confirmed
//Containment:         quarantine
//              culling
//Country:             UNITED STATES
//Disease:             Plum pox potyvirus (PPV- EPPO A2 quarantine pest) / PPV
//Victims:             a fruit farm
		for(Entry<String, Article>  s :outputs.entrySet()) {
			Article output = s.getValue();
			Article answer = answers.get(s.getKey());
			
			HashMap<String, String> recall = calculateRecall(output, answer);
			HashMap<String, String> precision = calculatePrecision(output, answer);
			HashMap<String, String> f1 = calculateF1(output, answer);
			printEvaluation(output.story, output.id, recall, precision, f1);
			
		}
	}
	
	public static void printEvaluation(String story, String id, HashMap<String, String> recall, HashMap<String, String> precision, HashMap<String, String> f1) {
		System.out.println("Story: " + story);
		System.out.println("ID: " + id);
		System.out.format("%25s%20s%20s", "RECALL", "PRECISION", "F-MEASURE");
		System.out.format("%1s%25s%20s%20s","Status:" , recall.get("status"), precision.get("status"), f1.get("status"));
		System.out.format("%1s%25s%20s%20s","Date:" , recall.get("date"), precision.get("date"), f1.get("date"));
		System.out.format("%1s%25s%20s%20s","Event:" , recall.get("event"), precision.get("event"), f1.get("event"));
		System.out.format("%1s%25s%20s%20s","Country:" , recall.get("country"), precision.get("country"), f1.get("country"));
		System.out.format("%1s%25s%20s%20s","Containment:" , recall.get("containment"), precision.get("containment"), f1.get("containment"));
		System.out.format("%1s%25s%20s%20s","Disease:" , recall.get("disease"), precision.get("disease"), f1.get("disease"));
		System.out.format("%1s%25s%20s%20s","Victim:" , recall.get("victim"), precision.get("victim"), f1.get("victim"));
	}

	public static HashMap<String, String> calculateRecall(Article output, Article answer) {
		HashMap<String, String> result = new HashMap<String, String>();
		DecimalFormat dec = new DecimalFormat("#0.00");
		
		if(output.status.equals(answer.status)) {
			result.put("status", "1.00 (1/1)");
		} else {
			result.put("status", "0.00 (0/1)");
		}
		
		if(output.country.equals(answer.country)) {
			result.put("country", "1.00 (1/1)");
		} else {
			result.put("country", "0.00 (0/1)");
		}
		
		if(output.date.equals(answer.date)) {
			result.put("date", "1.00 (1/1)");
		} else {
			result.put("date", "0.00 (0/1)");
		}
		
		if(output.event.equals(answer.event)) {
			result.put("event", "1.00 (1/1)");
		} else {
			result.put("event", "0.00 (0/1)");
		}
		
		int containmentTrueCount = answer.containment.size();
		int containmentLabeledCount = 0;
		for(String s : output.containment) {
			if(answer.containment.contains(s)) {
				containmentLabeledCount++;
			}
		}
		double containmentRecall = containmentLabeledCount / containmentTrueCount;
		result.put("containment", dec.format(containmentRecall).toString() + "(" + containmentLabeledCount + "/" + containmentTrueCount);
		
		int victimTrueCount = answer.victim.size();
		int victimLabeledCount = 0;
		for(String s : output.victim) {
			if(answer.victim.contains(s)) {
				victimLabeledCount++;
			}
		}
		double victimRecall = victimLabeledCount / victimTrueCount;
		result.put("victim", dec.format(victimRecall).toString() + "(" + victimLabeledCount + "/" + victimTrueCount);
		
		int diseaseTrueCount = answer.disease.size();
		int diseaseLabeledCount = 0;
		for(String s : output.disease) {
			if(answer.disease.contains(s)) {
				diseaseLabeledCount++;
			}
		}
		double diseaseRecall = diseaseLabeledCount / diseaseTrueCount;
		result.put("disease", dec.format(diseaseRecall).toString() + "(" + diseaseLabeledCount + "/" + diseaseTrueCount);
		
		return result;
	}
	
	public static HashMap<String, String> calculatePrecision(Article output, Article answer) {
		HashMap<String, String> result = new HashMap<String, String>();
		DecimalFormat dec = new DecimalFormat("#0.00");
		
		if(output.status.equals(answer.status)) {
			result.put("status", "1.00 (1/1)");
		} else {
			if(output.status.equals("-----")) {
				result.put("status", "0.00 (0/0)");
			} else {
				result.put("status", "0.00 (0/1)");
			}
		}
		
		if(output.country.equals(answer.country)) {
			result.put("country", "1.00 (1/1)");
		} else {
			if(output.country.equals("-----")) {
				result.put("country", "0.00 (0/0)");
			} else {
				result.put("country", "0.00 (0/1)");
			}
		}
		
		if(output.date.equals(answer.date)) {
			result.put("date", "1.00 (1/1)");
		} else {
			if(output.date.equals("-----")) {
				result.put("date", "0.00 (0/0)");
			} else {
				result.put("date", "0.00 (0/1)");
			}
		}
		
		if(output.event.equals(answer.event)) {
			result.put("event", "1.00 (1/1)");
		} else {
			if(output.event.equals("-----")) {
				result.put("event", "0.00 (0/0)");
			} else {
				result.put("event", "0.00 (0/1)");
			}
		}
		
		int containmentLabeledCount = 0;
		int containmentCorrectlyLabeledCount = 0;
		for(String s : output.containment) {
			if(answer.containment.contains(s)) {
				containmentCorrectlyLabeledCount++;
			}
			containmentLabeledCount++;
		}
		double containmentPrecision = 0;
		if(containmentLabeledCount > 0 ) {
			containmentPrecision = containmentCorrectlyLabeledCount / containmentLabeledCount;
		}
		result.put("containment", dec.format(containmentPrecision).toString() + "(" + containmentCorrectlyLabeledCount + "/" + containmentLabeledCount);
		
		int victimLabeledCount = 0;
		int victimCorrectlyLabeledCount = 0;
		for(String s : output.victim) {
			if(answer.victim.contains(s)) {
				victimCorrectlyLabeledCount++;
			}
			victimLabeledCount++;
		}
		double victimPrecision = 0;
		if(victimLabeledCount > 0 ) {
			victimPrecision = victimCorrectlyLabeledCount / victimLabeledCount;
		}
		result.put("victim", dec.format(victimPrecision).toString() + "(" + victimCorrectlyLabeledCount + "/" + victimLabeledCount);
		
		int diseaseLabeledCount = 0;
		int diseaseCorrectlyLabeledCount = 0;
		for(String s : output.disease) {
			if(answer.disease.contains(s)) {
				diseaseCorrectlyLabeledCount++;
			}
			diseaseLabeledCount++;
		}
		double diseasePrecision = 0;
		if(diseaseLabeledCount > 0 ) {
			diseasePrecision = diseaseCorrectlyLabeledCount / diseaseLabeledCount;
		}
		result.put("disease", dec.format(diseasePrecision).toString() + "(" + diseaseCorrectlyLabeledCount + "/" + diseaseLabeledCount);
		
		return result;
	}
	
	public static HashMap<String, String> calculateF1(Article output, Article answer) {
		DecimalFormat dec = new DecimalFormat("#0.00");
		HashMap<String, String> recall = calculateRecall(output, answer);
		HashMap<String, String> precision = calculatePrecision(output, answer);
		HashMap<String, String> result = new HashMap<String, String>();
		
		double statusRecall = Double.parseDouble(recall.get("status").split(" ")[1]);
		double eventRecall = Double.parseDouble(recall.get("event").split(" ")[1]);
		double countryRecall = Double.parseDouble(recall.get("country").split(" ")[1]);
		double dateRecall = Double.parseDouble(recall.get("date").split(" ")[1]);
		double containmentRecall = Double.parseDouble(recall.get("containment").split(" ")[1]);
		double diseaseRecall = Double.parseDouble(recall.get("disease").split(" ")[1]);
		double victimRecall = Double.parseDouble(recall.get("victim").split(" ")[1]);
		
		double statusPrecision = Double.parseDouble(precision.get("status").split(" ")[1]);
		double eventPrecision = Double.parseDouble(precision.get("event").split(" ")[1]);
		double countryPrecision = Double.parseDouble(precision.get("country").split(" ")[1]);
		double datePrecision = Double.parseDouble(precision.get("date").split(" ")[1]);
		double containmentPrecision = Double.parseDouble(precision.get("containment").split(" ")[1]);
		double diseasePrecision = Double.parseDouble(precision.get("disease").split(" ")[1]);
		double victimPrecision = Double.parseDouble(precision.get("victim").split(" ")[1]);
	
		result.put("status", dec.format(2*statusRecall*statusPrecision/(statusRecall + statusPrecision)));
		result.put("event", dec.format(2*eventRecall*eventPrecision/(eventRecall + eventPrecision)));
		result.put("country", dec.format(2*countryRecall*countryPrecision/(countryRecall + countryPrecision)));
		result.put("date", dec.format(2*dateRecall*datePrecision/(dateRecall + datePrecision)));
		result.put("containment", dec.format(2*containmentRecall*containmentPrecision/(containmentRecall + containmentPrecision)));
		result.put("disease", dec.format(2*diseaseRecall*diseasePrecision/(diseaseRecall + diseasePrecision)));
		result.put("victim", dec.format(2*victimRecall*victimPrecision/(victimRecall + victimPrecision)));
		
		return result;
		
	}
	
	public static String getStatus(String text) {
		if(text.contains("confirmed")) {
			return "confirmed";
		}
		else if(text.contains("suspected")) {
			return "suspected";
		}
		else if(text.contains("possible") || text.contains("possibly")) {
			return "possible";
		} else {
			return "confirmed";
		}
	}
	
	public static String getCountry(String text) {
		String country = null;
		for(String s : countriesList) {
			if(text.contains(s)) {
				return s.toUpperCase();
			}
		}
		
		if(country == null) {
			if(text.contains("USA")) {
				return "UNITED STATES";
			}
			if(text.contains("U.S.A")) {
				return "UNITED STATES";
			}
			if(text.contains("US")) {
				return "UNITED STATES";
			}
			if(text.contains("U.S.")) {
				return "UNITED STATES";
			}
			if(text.contains("U.S")) {
				return "UNITED STATES";
			}
			if(text.contains("USA")) {
				return "UNITED STATES";
			}
			if(text.contains("UK")) {
				return "UNITED KINGDOM";
			}
			if(text.contains("U.K.")) {
				return "UNITED KINGDOM";
			}
			if(text.contains("U.K")) {
				return "UNITED KINGDOM";
			}
		}
		
		return "----";
	}
	
	public static String getEvent() {
		return "outbreak";
	}
	
	public static void generateTemplate() throws FileNotFoundException, IOException {
		for (File file : dev_files) {
			String id = "";
			String text = "";
			int i = 0;
			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (i == 0) {
						id = line.split("\\s+")[0];
					} else {
						text += " " + line;
					}
					i++;
				}
			}
			
		    Article a = new Article();
		    a.story = file.getName();
		    a.id = "1";
		    a.status = getStatus(text);
		    a.country = getCountry(text);
		    a.event = getEvent();
		    
			Document d = new Document(text);
//			for (Sentence s : d.sentences()) {
//				// System.out.println(s.text());
//				for (String s1 : weaponGeneralRules.keySet()) {
//					// System.out.println(s1);
//					if (s.text().matches(".*\\b" + s1 + "\\b.*")) {
//						String w = parseWeaponRule(weaponGeneralRules.get(s1), s.text());
//						if (w != null) {
//							if(weapons.contains(w)) {
//								weaponsSet.add(w);
//							}
//						}
//					}
//				}
//
//				for (String s1 : perpOrgRules.keySet()) {
//					// System.out.println(s1);
//					if (s.text().matches(".*\\b" + s1 + "\\b.*")) {
//						String w = parsePerpOrgRule(perpOrgRules.get(s1), s.text());
//						if (w != null) {
//							perpOrgs.add(w);
//						}
//					}
//				}
//
//				for (String s2 : victimRules.keySet()) {
//					if (s.text().matches(".*\\b" + s2 + "\\b.*")) {
//						HashSet<String> w = parseVictimRule(victimRules.get(s2), s.text());
//						// System.out.println(w);
//						if (w != null) {
//							victims.addAll(w);
//						}
//					}
//				}
//
//			}
//			 System.out.print(id + " ");
//			 for(String s : perpOrgs) {
//			 System.out.print(s + " ");
//			 }
//			 System.out.println();

//			if (id.startsWith("DEV") || id.startsWith("TST")) {
//				System.out.println(printTemplate(id, "date", "event", "status", "country", new HashSet<String>(), new HashSet<String>()));
//				// System.out.println();
//			}
			
			ans_templates.add(a);
		}
	}

	public static void printTemplate(String fileName, String story, String id, String date, String event, String status, HashSet<String> containment, String country, HashSet<String> disease, HashSet<String> victim) throws FileNotFoundException, UnsupportedEncodingException {
//		Story:               20000123.0120
//		ID:                  1
//		Date:                February 23, 2000
//		Event:               outbreak
//		Status:              confirmed
//		Containment:         quarantine
//		                     culling
//		Country:             UNITED STATES
//		Disease:             Plum pox potyvirus (PPV- EPPO A2 quarantine pest) / PPV
//		Victims:             a fruit farm
		
		String template = "";
		template += "Story:               " + story + "\n";
		template += "ID:                  " + id + "\n";
		template += "Date:                ";
		template += "Event:               " + event + "\n";
		template += "Status:              " + status + "\n";
		template += "Containment          ";
		int count = 0;
		if (containment.size() == 0) {
			template += "-";
			template += "\n";
		}
		for (String s : containment) {
			if (count == 0) {
				template += s;
				template += "\n";
			} else {
				template += "        " + s;
				template += "\n";
			}
			count++;
		}
		template += "Country:             " + country + "\n";
		
		template += "\n";
		template += "Disease:             ";
		int count2 = 0;
		if (disease.size() == 0) {
			template += "----";
			template += "\n";
		}
		for (String s : disease) {
			if (count2 == 0) {
				template += s;
				template += "\n";
			} else {
				template += "             " + s;
				template += "\n";
			}

			count2++;
		}

		template += "VICTIM:              ";
		int count3 = 0;
		if (victim.size() == 0) {
			template += "----";
			template += "\n";
		}
		for (String s : victim) {
			if (count3 == 0) {
				template += s;
				template += "\n";
			} else {
				template += "        " + s;
				template += "\n";
			}

			count3++;
		}

		//body += template + "\n";
		generateOutputFile(fileName, template);
	}
	
	public static void instantiateRules() {
		diseaseRules.put("REPORT OF", "REPORT OF <DISEASE>");
		diseaseRules.put("RECORD OF", "REPORT OF <DISEASE>");
		diseaseRules.put("REPORTS OF", "REPORTS OF <DISEASE>");
		diseaseRules.put("STRAIN OF", "STRAIN OF <DISEASE>");
		diseaseRules.put("OUTBREAK OF", "OUTBREAK OF <DISEASE>");
		diseaseRules.put("OUTBREAK", "<DISEASE> OUTBREAK");
//		diseaseRules.put("CONTAIN THE", "CONTAIN THE <DISEASE>");
		diseaseRules.put("TEST FOR", "TEST FOR <DISEASE>");
		diseaseRules.put("TESTING FOR", "TESTING FOR <DISEASE>");
		diseaseRules.put("POSITIVE FOR", "POSITIVE FOR <DISEASE>");
		diseaseRules.put("TRANSMISSION OF", "TRANSMISSION OF <DISEASE>");
		diseaseRules.put("ERADICATION OF", "ERADICATION OF <DISEASE>");
		diseaseRules.put("ADMITTED WITH", "ADMITTED WITH <DISEASE>");
//		diseaseRules.put("PATIENTS", "<DISEASE> PATIENTS");
//		diseaseRules.put("ACCOMPANIED BY", "<DISEASE> ACCOMPANIED BY <DISEASE>");
		diseaseRules.put("SUFFERING WITH", "SUFFERING WITH <DISEASE>");
		diseaseRules.put("SUFFERING FROM", "SUFFERING FROM <DISEASE>");
		diseaseRules.put("DIAGNOSED AS HAVING", "DIAGNOSED AS HAVING <DISEASE>");
		diseaseRules.put("DIAGNOSED WITH HAVING", "DIAGNOSED WITH HAVING <DISEASE>");
		diseaseRules.put("DIAGNOSED WITH", "DIAGNOSED WITH <DISEASE>");
		diseaseRules.put("CASES OF", "CASES OF <DISEASE>");
		diseaseRules.put("CASE OF", "CASE OF <DISEASE>");
		diseaseRules.put("STAGES OF", "STAGES OF <DISEASE>");
		diseaseRules.put("EXPOSURE TO", "EXPOSURE TO <DISEASE>");
		diseaseRules.put("SPREAD OF", "SPREAD OF <DISEASE>");
		diseaseRules.put("SPREADING OF", "SPREADING OF <DISEASE>");
		diseaseRules.put("DETECTION OF", "DETECTION OF <DISEASE>");
		diseaseRules.put("CHARACTERIZED BY", "<DISEASE> IS CHARACTERIZED BY");
		diseaseRules.put("TRANSMITS", "TRANSMITS <DISEASE>");
		diseaseRules.put("DEVELOPED", "DEVELOPED <DISEASE>");
		diseaseRules.put("IS SPREAD BY", "<DISEASE> IS SPREAD BY");
		diseaseRules.put("EPIDEMIC", "<DISEASE> EPIDEMIC");
		diseaseRules.put("DEATH FROM", "DEATH FROM <DISEASE>");
		diseaseRules.put("DEATH BY", "DEATH BY <DISEASE>");
		diseaseRules.put("DISTRIBUTION OF", "DISTRIBUTION OF <DISEASE>");
		diseaseRules.put("POSITIVE CASE", "<DISEASE> POSITIVE CASE");
		diseaseRules.put("POSITIVE CASES", "<DISEASE> POSITIVE CASES");
		diseaseRules.put("VACCINE", "<DISEASE> VACCINE");
		diseaseRules.put("STRAIN", "<DISEASE> STRAIN");
		diseaseRules.put("WERE REPORTED", "<DISEASE> WERE REPORTED");
		diseaseRules.put("INFECTED BY", "INFECTED BY <DISEASE>");
		diseaseRules.put("DIED FROM", "DIED FROM <DISEASE>");
		diseaseRules.put("CONTAMINATED BY", "CONTAMINATED BY <DISEASE>");
		diseaseRules.put("EPIDEMIC OF", "EPIDEMIC OF <DISEASE>");
		diseaseRules.put("DOCUMENTED EPISODE OF", "DOCUMENTED EPISODE OF <DISEASE>");




	}

	public static void generateOutputFile(String fileName, String body) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter printWriter = new PrintWriter(fileName + ".templates", "UTF-8");
		printWriter.write(body);
		printWriter.close();
	}

}
