package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

//20000127.0135.maintext -s
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
	public static HashMap<String, Article> ans_templates = new HashMap<String, Article>();
	public static HashMap<String, Article> output_templates = new HashMap<String, Article>();
	public static HashMap<String, String> eventRules = new HashMap<String, String>();
	public static HashMap<String, String> containmentRules = new HashMap<String, String>();
	public static HashMap<String, String> statusRules = new HashMap<String, String>();
	public static HashMap<String, String> perpOrgRules = new HashMap<String, String>();
	public static HashMap<String, String> diseaseRules = new HashMap<String, String>();
	public static HashMap<String, String> victimRules = new HashMap<String, String>();
	public static ScoringProgram scoringProgram;
	public static Scanner scanner = new Scanner(System.in);
	boolean singleFile;

	public static HashSet<String> countriesList = new HashSet<String>();

	public static void main(String args[]) throws FileNotFoundException, IOException, InterruptedException {
		printPrompt();
	}

	public static void printPrompt() throws FileNotFoundException, IOException, InterruptedException {
		System.out.println(
				"Hello! Welcome to the Disease Domain Information Extraction System! \nPlease select an option:\n");
		System.out.println("1) Pass in a data folder (data/test-set-docs)");
		System.out.println("2) Pass in a single file");
		System.out.println("3) Edit an existing data file and print a template");

		int choice = scanner.nextInt();

		switch (choice) {
		case 1:
			System.out.println("Input received. Loading...");
			performResults(false, false);
			break;
		case 2:
			System.out.println("Which file would you like to evaluate?");
			performResults(true, false);
			break;
		case 3:
			System.out.println(
					"Which file do you want to edit?\n**Please note that templates for edited documents cannot be scored by our system due to a lack of an answer template\n");
			performResults(true, true);
			break;
		}

	}

	public static void performResults(boolean singleFile, boolean editingAFile)
			throws FileNotFoundException, IOException, InterruptedException {
		File dev_folder = new File("data/test-set-docs");
		File[] listOfDevFiles = dev_folder.listFiles();

		parseSeeds();
		instantiateRules();

		if (singleFile) {
			int count = 1;

			for (File file : listOfDevFiles) {
				if (file.isFile()) {
					System.out.println(count + ") " + file.getName());
					count++;
				}
			}

			int index = scanner.nextInt();
			File file = new File("data/test-set-docs/" + listOfDevFiles[index - 1].getName());

			if (!editingAFile) {
				System.out.println("Input received. Loading...");
				dev_files.add(file);
			} else {
				Process p = new ProcessBuilder("emacs", file.getName()).start();
				p.waitFor();
				File updatedFile = new File("data/test-set-docs/" + listOfDevFiles[index - 1].getName());
				dev_files.add(updatedFile);
				System.out.println("Evaluating edited file. Loading...");
			}
		} else {
			for (File file : listOfDevFiles) {
				if (file.isFile()) {
					dev_files.add(file);
				}
			}
		}

		// Change this to data/templates if working on dev
		// Change to data/test-set-templates if working on test
		File ans_folder = new File("data/test-set-templates");
		File[] list = ans_folder.listFiles();
		for (File file : list) {
			if (file.isFile()) {
				parseAnswerFile(file);
			}
		}

		scoringProgram = new ScoringProgram();

		if (!singleFile) {
			if (editingAFile) {
				generateTemplate(true);
			} else {
				generateTemplate(false);
			}
			scoringProgram.printTotals();
		} else {
			generateTemplate(false);
		}
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
		// Story: 20030416.0928
		// ID: 1
		// Date: May 16, 2003
		// Event: outbreak
		// Status: confirmed
		// Containment: quarantine
		// culling
		// Country: ERITREA
		// Disease: Contagious Bovine Pleuropneumonia / Mycoplasma mycoides subsp.
		// mycoides SC (MmmSC)
		// Victims: imported cattle
		//
		//
		// Bytespans (Template 1): 443-476 1743-1789 2001-2017
		Article a = new Article();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			String prev = "";
			while ((line = br.readLine()) != null) {
				if (line.contains(":")) {
					String[] split = line.split(":");
					if (split[0].equals("Story")) {
						a.story = line.substring(21);
						String split1[] = line.substring(21).split("\\.");
						a.story = split1[0] + "." + split1[1];
						// System.out.println(a.story);
						// System.out.println(line.substring(21, line.length()-1));
					}
					if (split[0].equals("ID")) {
						a.id = line.substring(21);
					}
					if (split[0].equals("Date")) {
						a.date = (line.substring(21));
					}
					if (split[0].equals("Event")) {
						a.event = line.substring(21);
					}
					if (split[0].equals("Country")) {
						a.country = line.substring(21);
					}
					if (split[0].equals("Status")) {
						a.status = line.substring(21);
					}
					if (split[0].equals("Containment")) {
						a.containment = new HashSet<String>();

						String[] v = line.substring(21).split("/");
						for (String s : v) {
							a.containment.add(s.trim());
						}
						prev = "Containment";
					}
					if (split[0].equals("Disease")) {
						a.disease = new HashSet<String>();
						String[] v = line.substring(21).split("/");
						for (String s : v) {
							a.disease.add(s.trim());
						}
						prev = "Disease";
					}
					if (split[0].equals("Victims")) {
						a.victim = new HashSet<String>();
						String[] v = line.substring(21).split("/");
						for (String s : v) {
							a.victim.add(s.trim());
						}
						prev = "Victim";
					}
				} else {
					if (prev.equals("Containment")) {
						a.containment.add(line.substring(21));
					} else if (prev.equals("Victim")) {
						if (line.length() != 0) {
							a.victim.add(line.substring(21));
						}
					} else if (prev.equals("Disease")) {
						a.disease.add(line.substring(21));
					}
				}
			}
		}
		ans_templates.put(a.story, a);
		return a;
	}

	public static String getStatus(String text) {
		if (text.contains("confirmed")) {
			return "confirmed";
		} else if (text.contains("suspected")) {
			return "suspected";
		} else if (text.contains("possible") || text.contains("possibly")) {
			return "possible";
		} else {
			return "confirmed";
		}
	}

	public static String getDate(String story) {
//		Story:               20040626.1701
//		Date:                July 26, 2004
		String year = story.substring(0,4);
		String month  = getMonth(story.substring(4,6));
		String day = story.substring(6,8);
		
		String date = month + " " + day + ", " + year;
		
		
		return date;
	}
	
	public static String getMonth(String month) {
		String monthString;
		switch (month) {
	        case "01":  monthString = "February";
	                 break;
	        case "02":  monthString = "March";
	                 break;
	        case "03":  monthString = "April";
	                 break;
	        case "04":  monthString = "May";
	                 break;
	        case "05":  monthString = "June";
	                 break;
	        case "06":  monthString = "July";
	                 break;
	        case "07":  monthString = "August";
	                 break;
	        case "08":  monthString = "September";
	                 break;
	        case "09":  monthString = "October";
	                 break;
	        case "10": monthString = "November";
	                 break;
	        case "11": monthString = "December";
	                 break;
	        case "12": monthString = "January";
	                 break;
	        default: monthString = "Invalid month";
	                 break;
		}
		
		return monthString;
	}

	public static String getCountry(String text) {
		String country = null;
		HashSet<String> c = countriesList;
		for (String s : c) {
			if (text.contains(s)) {
				return s.toUpperCase();
			}
		}

		if (country == null) {
			if (text.contains("USA")) {
				return "UNITED STATES";
			}
			if (text.contains("U.S.A")) {
				return "UNITED STATES";
			}
			if (text.contains("US")) {
				return "UNITED STATES";
			}
			if (text.contains("U.S.")) {
				return "UNITED STATES";
			}
			if (text.contains("U.S")) {
				return "UNITED STATES";
			}
			if (text.contains("UK")) {
				return "UNITED KINGDOM";
			}
			if (text.contains("U.K.")) {
				return "UNITED KINGDOM";
			}
			if (text.contains("U.K")) {
				return "UNITED KINGDOM";
			}
		}

		return "-----";
	}

	public static String getEvent() {
		return "outbreak";
	}

	public static void generateTemplate(boolean editingAFile) throws FileNotFoundException, IOException {
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
			} catch (FileNotFoundException e) {
				System.out.println("Please remove the '-s' flag on the command line if passing in a folder path!");
				System.exit(0);
			}

			Article a = new Article();
			String split[] = file.getName().split("\\.");
			a.story = split[0] + "." + split[1];
			a.id = "1";
			a.status = getStatus(text);
			a.country = getCountry(text);
			a.event = getEvent();
			a.date = getDate(a.story);

			HashSet<String> diseases = new HashSet<String>();
			HashSet<String> victims = new HashSet<String>();
			Document d = new Document(text);
			for (Sentence s : d.sentences()) {
				// // System.out.println(s.text());
				// for (String s1 : weaponGeneralRules.keySet()) {
				// // System.out.println(s1);
				// if (s.text().matches(".*\\b" + s1 + "\\b.*")) {
				// String w = parseWeaponRule(weaponGeneralRules.get(s1), s.text());
				// if (w != null) {
				// if(weapons.contains(w)) {
				// weaponsSet.add(w);
				// }
				// }
				// }
				// }
				//
				// for (String s1 : perpOrgRules.keySet()) {
				// // System.out.println(s1);
				// if (s.text().matches(".*\\b" + s1 + "\\b.*")) {
				// String w = parsePerpOrgRule(perpOrgRules.get(s1), s.text());
				// if (w != null) {
				// perpOrgs.add(w);
				// }
				// }
				// }
				//
				for (String s2 : diseaseRules.keySet()) {
					if (s.text().matches(".*\\b" + s2 + "\\b.*")) {
						HashSet<String> w = parseDiseaseRule(diseaseRules.get(s2), s.text());
						// System.out.println(w);
						if (w != null) {
							diseases.addAll(w);
						}
					}
				}
				for (String s2 : victimRules.keySet()) {
					if (s.text().matches(".*\\b" + s2 + "\\b.*")) {
						HashSet<String> w = parseDiseaseRule(victimRules.get(s2), s.text());
						// System.out.println(w);
						if (w != null) {

							victims.addAll(w);
						}
					}
				}
			}
			a.disease = diseases;
			a.containment = new HashSet<String>();
			a.containment.add("-----");
			a.victim = victims;
			output_templates.put(a.story, a);

			System.out.println(
					">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println();
			System.out.println("          SYSTEM OUTPUT\n");
			printTemplate(a.story, a.story, a.id, a.date, a.event, a.status, a.containment, a.country, a.disease,
					a.victim);

			if (!editingAFile) {
				Article goldAnswer = ans_templates.get(a.story);
				System.out.println("          ANSWER KEY\n");
				printTemplate(goldAnswer.story, goldAnswer.story, goldAnswer.id, goldAnswer.date, goldAnswer.event,
						goldAnswer.status, goldAnswer.containment, goldAnswer.country, goldAnswer.disease,
						goldAnswer.victim);
				scoringProgram.evaluateSingle(a, goldAnswer);
			}
		}
	}

	public static void printTemplate(String fileName, String story, String id, String date, String event, String status,
			HashSet<String> containment, String country, HashSet<String> disease, HashSet<String> victim)
			throws FileNotFoundException, UnsupportedEncodingException {
		// Story: 20000123.0120
		// ID: 1
		// Date: February 23, 2000
		// Event: outbreak
		// Status: confirmed
		// Containment: quarantine
		// culling
		// Country: UNITED STATES
		// Disease: Plum pox potyvirus (PPV- EPPO A2 quarantine pest) / PPV
		// Victims: a fruit farm

		String template = "";
		template += "Story:               " + story + "\n";
		template += "ID:                  " + id + "\n";
		template += "Date:                " + date + "\n";
		template += "Event:               " + event + "\n";
		template += "Status:              " + status + "\n";
		template += "Containment          ";
		int count = 0;
		if (containment.size() == 0) {
			template += "-----";
			template += "\n";
		}
		for (String s : containment) {
			if (count == 0) {
				template += s;
				template += "\n";
			} else {
				template += "                     " + s;
				template += "\n";
			}
			count++;
		}
		template += "Country:             " + country + "\n";
		template += "Disease:             ";
		int count2 = 0;
		if (disease.size() == 0) {
			template += "-----";
			template += "\n";
		}
		for (String s : disease) {
			if (count2 == 0) {
				template += s;
				template += "\n";
			} else {
				template += "                     " + s;
				template += "\n";
			}

			count2++;
		}

		template += "Victims:             ";
		int count3 = 0;
		if (victim.size() == 0) {
			template += "-----";
			template += "\n";
		}
		for (String s : victim) {
			if (count3 == 0) {
				template += s;
				template += "\n";
			} else {
				template += "                     " + s;
				template += "\n";
			}

			count3++;
		}
		System.out.println(template);
		// body += template + "\n";
		generateOutputFile(fileName, template);
	}

	public static void instantiateRules() {
		diseaseRules.put("REPORT OF", "REPORT OF <DISEASE>");
		diseaseRules.put("RECORD OF", "RECORD OF <DISEASE>");
		diseaseRules.put("REPORTS OF", "REPORTS OF <DISEASE>");
		diseaseRules.put("STRAIN OF", "STRAIN OF <DISEASE>");
		diseaseRules.put("OUTBREAK OF", "OUTBREAK OF <DISEASE>");
		// diseaseRules.put("OUTBREAK", "<DISEASE> OUTBREAK");
		// diseaseRules.put("CONTAIN THE", "CONTAIN THE <DISEASE>");
		diseaseRules.put("TEST FOR", "TEST FOR <DISEASE>");
		diseaseRules.put("TESTING FOR", "TESTING FOR <DISEASE>");
		diseaseRules.put("POSITIVE FOR", "POSITIVE FOR <DISEASE>");
		diseaseRules.put("TRANSMISSION OF", "TRANSMISSION OF <DISEASE>");
		diseaseRules.put("ERADICATION OF", "ERADICATION OF <DISEASE>");
		diseaseRules.put("ADMITTED WITH", "ADMITTED WITH <DISEASE>");
		// diseaseRules.put("PATIENTS", "<DISEASE> PATIENTS");
		// diseaseRules.put("ACCOMPANIED BY", "<DISEASE> ACCOMPANIED BY <DISEASE>");
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

		victimRules.put("diagnosed with", "<victim> diagnosed with");
		victimRules.put("died", "<victim> died");
		victimRules.put("infected with", "<victim> infected with");
		victimRules.put("were infected", "<victim> were infected");
		victimRules.put("illness among", "illness among <victim>");
		victimRules.put("die from", "<victim> die from");
		victimRules.put("died", "<victim> died");
		victimRules.put("admitted to", "<victim> admitted to");
		victimRules.put("discharged from", "<victim> discharged from");
		victimRules.put("killed", "killed <victim>");
		victimRules.put("with diagnosis", "<victim> with diagnosis");
		victimRules.put("tested positive", "<victim> tested positive");
		victimRules.put("testing positive", "<victim> testing positive");
		victimRules.put("killed", "<victim> have been killed");
		victimRules.put("killed", "<victim> has been killed");
		victimRules.put("contracted", "<victim> contracted");
		victimRules.put("reported", "reported <victim>");
		victimRules.put("reported", "<victim> reported");
		victimRules.put("total of", "total of <victim>");
		victimRules.put("suffering from", "<victim> suffering from");
		victimRules.put("suffered from", "<victim> suffered from");
		victimRules.put("detected in", "detected in <victim>");
		victimRules.put("infected", "<victim> was infected");
		victimRules.put("infected", "<victim> is infected");
		victimRules.put("taken to hospital", "<victim> taken to hospital");
		victimRules.put("in hospital", "<victim> in hospital");
		victimRules.put("affected", "affected <victim>");
		victimRules.put("affects", "affects <victim>");
		victimRules.put("virus in", "virus in <victim>");
	}

	public static void analyzeSentence(String s) {
		Sentence sent1 = new Sentence(s);
		Sentence sent = sent1;
		System.out.println(s);
		System.out.println(sent.parse());
		for (int i = 0; i < sent.words().size(); i++) {
			System.out.print(sent.word(i) + " (" + sent.nerTag(i) + ") " + "(" + sent.posTag(i) + ")");
		}

		System.out.println("");
	}

	public static HashSet<String> parseDiseaseRule(String rule, String s) {
		String[] rules = rule.split("\\s+");
		Sentence sentence = new Sentence(s);
		String[] split = sentence.words().stream().toArray(String[]::new);

		String disease = "";
		HashSet<String> diseases = new HashSet<String>();
		// analyzeSentence(s);
		boolean after = true;

		// List<String> posSplit = sentence.caseless().posTags();
		String[] posSplit = sentence.posTags().stream().toArray(String[]::new);

		// System.out.println(sentence.caseless().parse());
		int index = 0;
		int indexOfTriggerWord = 0;
		int indexOfDisease = 0;

		for (int i = 0; i < rules.length; i++) {
			if (!rules[i].contains("<")) {
				indexOfTriggerWord = i;
			} else {
				indexOfDisease = i;
			}
		}

		if (indexOfTriggerWord > indexOfDisease) {
			after = false;
		} else if (indexOfTriggerWord < indexOfDisease) {
			after = true;
		}

		String s1;
		for (int i = 0; i < sentence.words().size(); i++) {
			s1 = sentence.word(i).replaceAll("\\s*\\p{Punct}+\\s*$", "");
			if (s1.equals(rules[indexOfTriggerWord])) {
				index = i;
				break;
			}
		}
		List<String> wordSplit = sentence.words();
		if (after) {

			String subStr = "";
			for (int i = index + 1; i < split.length; i++) {
				subStr += split[i] + " ";
			}

			Sentence subSentence = new Sentence(subStr);
			boolean found = false;
			// System.out.println(subSentence.parse());

			for (Tree subtree : subSentence.parse()) {
				if (subtree.label().value().equals("NP") && !found) {
					for (Tree t : subtree.getChildrenAsList()) {
						if (t.label().value().equals("NP")) {
							for (Tree c : t.getLeaves()) {
								disease += c.value() + " ";
							}
						}

						found = true;
					}
				}
			}
		} else {
			String subStr = "";
			for (int i = 0; i < index; i++) {
				subStr += split[i] + " ";
			}
			if (subStr.length() > 0) {
				Sentence subSentence = new Sentence(subStr);
				// System.out.println(subSentence.caseless().parse());
				for (Tree subtree : subSentence.parse()) {
					if (subtree.label().value().equals("NP")) {
						disease = "";
						for (Tree t : subtree.getChildrenAsList()) {
							if (t.label().value().equals("NP")) {
								for (Tree c : t.getLeaves()) {
									disease += c.value() + " ";
								}
							}
						}
					}
				}
			}
		}
		// if (after) {
		// for (int i = index + 1; i < sentence.words().size(); i++) {
		// if (posSplit[i].contains("NN")) {
		// disease = sentence.word(i);
		// int k = i-1;
		// String pre = "";
		// while(posSplit[k].equals("JJ")) {
		// pre += sentence.word(k) + " ";
		// k--;
		// }
		//
		// disease = pre + sentence.word(i);
		//
		// break;
		// }
		// }
		// } else {
		// for (int i = index - 1; i > -1; i--) {
		//
		// if (posSplit[i].contains("NN")) {
		// int k = i-1;
		// String pre = "";
		// try {
		// while(posSplit[k].equals("JJ")) {
		// pre += sentence.word(k) + " ";
		// k--;
		// }
		// } catch(Exception e) {
		//
		// }
		//
		//
		// disease = pre + sentence.word(i);
		// break;
		// }
		// }
		// }
		if (disease.length() > 0) {
			diseases.add(disease.trim());
		}

		return diseases;
	}

	public static HashSet<String> checkDisease(HashSet<String> diseases) {
		HashSet<String> diseasesCopy = new HashSet<String>(diseases);
		for (String s : diseases) {
			if (s.length() > 0) {
				Sentence s1 = new Sentence(s);
				// for (String s2 : s1.posTags()) {
				// if (!s2.contains("NN") || !s) {
				// diseasesCopy.remove(s);
				// }
				// }
			} else {
				diseasesCopy.remove(s);
			}

		}
		return diseasesCopy;
	}

	public static void generateOutputFile(String fileName, String body)
			throws FileNotFoundException, UnsupportedEncodingException {
		// Change this to /dev-templates if working on dev
		// Change to /test-templates if working on test
		PrintWriter printWriter = new PrintWriter("dev-templates/" + fileName + ".templates", "UTF-8");
		printWriter.write(body);
		printWriter.close();
	}

	public static String getContainment() {
		return "culling";
	}
}
