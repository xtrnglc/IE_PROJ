package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class BagOfWordsGenerator {
	static HashSet<String> uniqueWords = new HashSet<String>();
	static HashSet<String> uniqueLabels = new HashSet<String>();
	static List<String> containmentMapping = Arrays.asList("vaccine", "-----", "other", "culling", "pesticide",
			"facility closing", "quarantine", "disinfecting", "inspection");
	static List<String> statusMapping = Arrays.asList("suspected", "possible", "confirmed");
	static ArrayList<String> wordMapping = new ArrayList<String>();
	static ArrayList<File> documents = new ArrayList<File>();
	static ArrayList<File> templates = new ArrayList<File>();

	public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
		File docFolder = new File("data/labeled-docs/");
		File templateFolder = new File("data/templates/");

		File[] listOfDocuments = docFolder.listFiles();
		File[] listOfTemplates = templateFolder.listFiles();

		for (File file : listOfDocuments) {
			if (file.isFile()) {
				if (!file.getName().contains("DS")) {
					documents.add(file);
				}
			}
		}

		for (File file : listOfTemplates) {
			if (file.isFile()) {
				if (!file.getName().contains("DS")) {
					templates.add(file);
				}
			}
		}

		generateWordMappings();
		generateBagOfWordsFile(true);
		generateBagOfWordsFile(false);
	}

	public static void generateBagOfWordsFile(boolean containment)
			throws FileNotFoundException, UnsupportedEncodingException {
		Scanner scanner = null;
		Scanner scanner2 = null;
		String label = "";

		if (containment) {
			label = "Containment:";
		} else {
			label = "Status:";
		}

		int count = 0;
		String vectorLine = "";

		while (count < documents.size()) {
			scanner = new Scanner(templates.get(count));
			scanner2 = new Scanner(documents.get(count));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith(label)) {
					String answer = line.split(":")[1];
					if (containment) {
						vectorLine += containmentMapping.indexOf(answer.trim()) + " ";
					} else {
						vectorLine += statusMapping.indexOf(answer.trim()) + " ";
					}
					break;
				}
			}

			while (scanner2.hasNext()) {
				String word = scanner2.next().replaceAll("\\W", "");
				int index = wordMapping.indexOf(word);
				if (index != -1) {
					vectorLine += index + ":1 ";
				}
			}
			vectorLine += "\n";
			count++;
		}
		scanner.close();

		String slot = "";
		if (containment) {
			slot = "containment.txt";
		} else {
			slot = "status.txt";
		}
		PrintWriter printWriter = new PrintWriter("word-vectors/" + slot, "UTF-8");
		printWriter.write(vectorLine);
		printWriter.close();
	}

	public static String generateWordVector(String input) {
		Scanner scanner = new Scanner(input);
		String vector = "";
		while (scanner.hasNext()) {
			String word = scanner.next().replaceAll("\\W", "");
			int index = wordMapping.indexOf(word);
			if (index != -1) {
				vector += index + ":1 ";
			}
		}
		scanner.close();
		return vector;
	}

	public static void generateWordMappings() throws FileNotFoundException {
		Scanner scanner = null;

		for (File document : documents) {
			scanner = new Scanner(document);
			while (scanner.hasNext()) {
				String word = scanner.next();
				word = word.replaceAll("\\W", "");
				uniqueWords.add(word);
			}
		}

		scanner.close();
		wordMapping = new ArrayList<String>(uniqueWords);
	}

	public static void generateLabelMapping(boolean containment) throws FileNotFoundException {
		uniqueLabels.clear();
		Scanner scanner = null;
		String label = "";
		if (containment) {
			label = "Containment:";
		} else {
			label = "Status:";
		}

		for (File template : templates) {
			scanner = new Scanner(template);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith(label)) {
					String answer = line.split(":")[1];
					uniqueLabels.add(answer.trim());
				}
			}
		}

		scanner.close();
		if (containment) {
			containmentMapping = new ArrayList<String>(uniqueLabels);
		} else {
			statusMapping = new ArrayList<String>(uniqueLabels);
		}
	}
}
