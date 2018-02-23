package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class Driver {
	
	public static List<File> dev_files = new ArrayList<File>();
	
	public static HashMap<String, String> eventRules = new HashMap<String, String>();
	public static HashMap<String, String> containmentRules = new HashMap<String, String>();
	public static HashMap<String, String> statusRules = new HashMap<String, String>();
	public static HashMap<String, String> perpOrgRules = new HashMap<String, String>();
	public static HashMap<String, String> diseaseRules = new HashMap<String, String>();
	public static HashMap<String, String> victimRules = new HashMap<String, String>();
	public static String body = "";
	
//	Story:               20000123.0120
//	ID:                  1
//	Date:                February 23, 2000
//	Event:               outbreak
//	Status:              confirmed
//	Containment:         quarantine
//	                     culling
//	Country:             UNITED STATES
//	Disease:             Plum pox potyvirus (PPV- EPPO A2 quarantine pest) / PPV
//	Victims:             a fruit farm

	public static void main(String args[]) throws FileNotFoundException, IOException {
		
//		File dev_folder = new File(args[1]);
		File dev_folder = new File("data/labeled-docs");
		
		File[] listOfDevFiles = dev_folder.listFiles();

		for (File file : listOfDevFiles) {
			if (file.isFile()) {
				dev_files.add(file);
			}
		}
		
		System.out.println();

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

			// DEV-MUC3-0126, DEV-MUC3-0231, DEV-MUC3-0253, DEV-MUC3-0277, DEV-MUC3-0316

			if (id.equals("DEV-MUC3-0102")) {
				System.out.print("");
			}

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

	
	public static String printTemplate(String id, String date, String event, String status, HashSet<String> containment, String country, HashSet<String> disease, HashSet<String> victim) {
		String template = "";
//		template += "ID: " + id + "\n";
//		template += "INCIDENT: " + incident + "\n";
//		template += "WEAPON: ";
//		int count = 0;
//		if (weapon.size() == 0) {
//			template += "-";
//			template += "\n";
//		}
//		for (String s : weapon) {
//			if (count == 0) {
//				template += s;
//				template += "\n";
//			} else {
//				template += "        " + s;
//				template += "\n";
//			}
//
//			count++;
//		}
//		template += "PERP INDIV: ";
//		for (String s : perpIndiv) {
//			template += " " + s;
//		}
//		template += "\n";
//		template += "PERP ORG: ";
//		int count2 = 0;
//		if (perpOrg.size() == 0) {
//			template += "-";
//			template += "\n";
//		}
//		for (String s : perpOrg) {
//			if (count2 == 0) {
//				template += s;
//				template += "\n";
//			} else {
//				template += "        " + s;
//				template += "\n";
//			}
//
//			count2++;
//		}
//		template += "TARGET: ";
//		for (String s : target) {
//			template += s;
//			template += "\n";
//		}
//		template += "VICTIM: ";
//		int count3 = 0;
//		if (victim.size() == 0) {
//			template += "-";
//			template += "\n";
//		}
//		for (String s : victim) {
//			if (count3 == 0) {
//				template += s;
//				template += "\n";
//			} else {
//				template += "        " + s;
//				template += "\n";
//			}
//
//			count3++;
//		}
//
//		body += template + "\n";
		return template;
	}
	
	public static void instantiateRules() {
		containmentRules.put("BLASTS", "<WEAPON> BLASTS");
		containmentRules.put("BLASTS", "<WEAPON> BLASTS");

	}
}
