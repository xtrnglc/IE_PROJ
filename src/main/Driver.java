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
	
	public static HashMap<String, String> eventRules = new HashMap<String, String>();
	public static HashMap<String, String> containmentRules = new HashMap<String, String>();
	public static HashMap<String, String> statusRules = new HashMap<String, String>();
	public static HashMap<String, String> perpOrgRules = new HashMap<String, String>();
	public static HashMap<String, String> diseaseRules = new HashMap<String, String>();
	public static HashMap<String, String> victimRules = new HashMap<String, String>();
	public static String body = "";

	public static void main(String args[]) throws FileNotFoundException, IOException {
		
//		File dev_folder = new File(args[1]);
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
						a.story = split[1];
					}
					if(split[0].equals("ID")) {
						a.id = split[1];
					}
					if(split[0].equals("Date")) {
						a.date = split[1];
					}
					if(split[0].equals("Event")) {
						a.event = split[1];
					}
					if(split[0].equals("Country")) {
						a.country = split[1];
					}
					if(split[0].equals("Status")) {
						a.status = split[1];
					}
					if(split[0].equals("Containment")) {
						a.containment = new HashSet<String>();
						a.containment.add(split[1]);
						prev = "Containment";
					}
					if(split[0].equals("Disease")) {
						a.disease = new HashSet<String>();
						a.disease.add(split[1]);
						prev = "Disease";
					}
					if(split[0].equals("Victims")) {
						a.victim = new HashSet<String>();
						a.victim.add(split[1]);
						prev = "Victim";
					}
				} else {
					if(prev.equals("Containment")) {
						a.containment.add(line.substring(21, line.length()-1));
					} else if (prev.equals("Victim")) {
						a.victim.add(line.substring(21, line.length()-1));
					} else if (prev.equals("Disease")) {
						a.disease.add(line.substring(21, line.length()-1));
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
			if(output.status.equals("-")) {
				result.put("status", "0.00 (0/0)");
			} else {
				result.put("status", "0.00 (0/1)");
			}
		}
		
		if(output.country.equals(answer.country)) {
			result.put("country", "1.00 (1/1)");
		} else {
			if(output.country.equals("-")) {
				result.put("country", "0.00 (0/0)");
			} else {
				result.put("country", "0.00 (0/1)");
			}
		}
		
		if(output.date.equals(answer.date)) {
			result.put("date", "1.00 (1/1)");
		} else {
			if(output.date.equals("-")) {
				result.put("date", "0.00 (0/0)");
			} else {
				result.put("date", "0.00 (0/1)");
			}
		}
		
		if(output.event.equals(answer.event)) {
			result.put("event", "1.00 (1/1)");
		} else {
			if(output.event.equals("-")) {
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
//			text = text.replaceAll("\\s*\\p{Punct}+\\s*$", "");
//			text = text.replaceAll("\"", "");
//			text = text.replaceAll(",", "");
//			text = text.replaceAll("\\[", "").replaceAll("\\]", "");
//			text = text.replaceAll("\\(", "").replaceAll("\\)", "");
//			text = text.replaceAll("\\{", "").replaceAll("\\}", "");
//			text = text.replaceAll("\\$", "").replaceAll("\\$", "");
//			text = text.replaceAll("--", "");

//			String incident = getIncident(text);
//			oursIncident.put(id, incident);

			// oursPerpOrg.put(id, po);
			// HashSet<String> perpetrator_orgs = getAnswers(perp_orgs, text);

			// DEV-MUC3-0126, DEV-MUC3-0231, DEV-MUC3-0253, DEV-MUC3-0277, DEV-MUC3-031

//			HashSet<String> weaponsSet = parseWeaponsSpecificRules(text);
			HashSet<String> perpOrgs = new HashSet<String>();
			HashSet<String> victims = new HashSet<String>();

			Document d = new Document(text);
			// System.out.println(id);
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
		template += "Disease:             ";
		for (String s : disease) {
			template += " " + s;
		}
		template += "\n";
		template += "PERP ORG: ";
		int count2 = 0;
		if (disease.size() == 0) {
			template += "-";
			template += "\n";
		}
		for (String s : disease) {
			if (count2 == 0) {
				template += s;
				template += "\n";
			} else {
				template += "        " + s;
				template += "\n";
			}

			count2++;
		}

		template += "VICTIM: ";
		int count3 = 0;
		if (victim.size() == 0) {
			template += "-";
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

		body += template + "\n";
		generateOutputFile(fileName, body);
	}

	public static void generateOutputFile(String fileName, String body) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter printWriter = new PrintWriter(fileName + ".templates", "UTF-8");
		printWriter.write(body);
		printWriter.close();
	}
	
	
}
