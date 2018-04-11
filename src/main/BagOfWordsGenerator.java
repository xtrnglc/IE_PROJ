package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class BagOfWordsGenerator {
	// static HashSet<String> uniqueWords = new HashSet<String>();
	static HashSet<Bigram> uniqueBigrams = new HashSet<Bigram>();
	static HashSet<String> uniqueLabels = new HashSet<String>();
	static List<String> containmentMapping = Arrays.asList("vaccine", "-----", "other", "culling", "pesticide",
			"facility closing", "quarantine", "disinfecting", "inspection");
	static List<String> statusMapping = Arrays.asList("suspected", "possible", "confirmed");
	// static HashMap<String, Integer> wordMapping = new HashMap<String, Integer>();
	static HashMap<Bigram, Integer> wordMapping = new HashMap<Bigram, Integer>();
	static ArrayList<File> documents = new ArrayList<File>();
	static ArrayList<String> documentsAdjusted = new ArrayList<String>();
	static ArrayList<File> templates = new ArrayList<File>();
	static int count = 0;

	public static void main(String args[]) throws IOException {
		init();
	}

	public static void init() throws IOException {

		File f = new File("train-word-vectors/wordmapping");
		if (f.exists() && !f.isDirectory()) {
			int i = 0;

			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						String bigram = line.split(":")[0];
						String value = line.split(":")[1];
						wordMapping.put(Bigram.getBigram(bigram), Integer.parseInt(value));
					}

				}
			} catch (FileNotFoundException e) {
				System.exit(0);
			}
		} else {
			File docFolder = new File("data/labeled-docs/");
			File templateFolder = new File("data/templates/");
			File[] listOfDocuments = docFolder.listFiles();
			for (File file : listOfDocuments) {
				if (file.getName().contains(".DS")) {
					file.delete();
				} else {
					documents.add(file);
				}
			}
			File[] listOfTemplates = templateFolder.listFiles();
			for (File file : listOfTemplates) {
				if (file.getName().contains(".DS")) {
					file.delete();
				} else {
					templates.add(file);
				}
			}

			Scanner scanner = null;

			for (File document : documents) {
				scanner = new Scanner(document);
				String file = "";

				while (scanner.hasNext()) {
					file += scanner.next().replaceAll("\\W", "");
					file += " ";
				}

				documentsAdjusted.add(file);
			}

			scanner.close();
			System.out.println("Generating word mappings");
			generateWordMappings();
			System.out.println("Generating vectors for status");
			generateBagOfWordsFile(false);
		}
	}

	public static void generateBagOfWordsFile(boolean containment)
			throws FileNotFoundException, UnsupportedEncodingException {
		Scanner scanner = null;
		Scanner scanner2 = null;
		Scanner scanner3 = null;
		String label = "";

		if (containment) {
			label = "Containment:";
		} else {
			label = "Status:";
		}

		String vectorLine = "";
		int count = 0;

		for (String file : documentsAdjusted) {
			scanner = new Scanner(file);
			System.out.println(file);
			scanner2 = new Scanner(file);
			scanner3 = new Scanner(templates.get(count));
			System.out.println(templates.get(count).toString());

			while (scanner3.hasNextLine()) {
				String line = scanner3.nextLine();
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

			scanner2.next();
			ArrayList<String> mapping = new ArrayList<String>();

			while (scanner2.hasNext()) {
				String word1 = scanner.next();
				String word2 = scanner2.next();
				Bigram bigram = new Bigram(word1.toLowerCase(), word2.toLowerCase());
				mapping.add(wordMapping.get(bigram) + ":1");
			}

			Collections.sort(mapping, new VectorComparator());
			for (String value : mapping) {
				vectorLine += value + " ";
			}
			vectorLine += "\n";
			count++;
		}

		String slot = "";
		if (containment) {
			slot = "containment.txt";
		} else {
			slot = "status.txt";
		}
		PrintWriter printWriter = new PrintWriter("train-word-vectors/" + slot, "UTF-8");
		printWriter.write(vectorLine);
		printWriter.close();
	}

	public static String generateWordVector(String input) {
		Scanner scanner3 = new Scanner(input);
		String text = "";
		while (scanner3.hasNext()) {
			text += scanner3.next().replaceAll("\\W", "");
			text += " ";
		}
		scanner3.close();
		HashMap<Integer, Integer> wordCounts = new HashMap<Integer, Integer>();
		ArrayList<String> mapping = new ArrayList<String>();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		Scanner scanner = new Scanner(text);
		Scanner scanner2 = new Scanner(text);
		scanner2.next();
		String vector = "";
		while (scanner2.hasNext()) {
			String word1 = scanner.next().toLowerCase();
			String word2 = scanner2.next().toLowerCase();
			Bigram bigram = new Bigram(word1, word2);

			if (bigram.getLength() > 0) {
				if (wordMapping.containsKey(bigram)) {
					int index = wordMapping.get(bigram);
					if (index != -1) {
						if (wordCounts.containsKey(index)) {
							int val = wordCounts.get(index) + 1;
							wordCounts.put(index, val);
						} else {
							wordCounts.put(index, 1);
						}
						indices.add(index);
					}
				}

			}

		}

		for (Integer index : indices) {
			mapping.add(index + ":" + wordCounts.get(index));
		}

		mapping.sort(new VectorComparator());
		for (String map : mapping) {
			vector += map + " ";
		}

		vector += "\n";

		scanner.close();
		scanner2.close();

		return vector;
	}

	public static void generateWordMappings() throws FileNotFoundException, UnsupportedEncodingException {
		Scanner scanner = null;
		Scanner scanner2 = null;

		for (String document : documentsAdjusted) {
			scanner = new Scanner(document);
			scanner2 = new Scanner(document);
			scanner2.next();
			while (scanner2.hasNext()) {
				String word1 = scanner.next();
				String word2 = scanner2.next();

				Bigram bigram = new Bigram(word1.toLowerCase(), word2.toLowerCase());
				uniqueBigrams.add(bigram);
			}
		}

		scanner.close();

		String output = "";
		for (Bigram bigram : uniqueBigrams) {
			if (bigram.getLength() > 0) {
				int index = ++count;
				wordMapping.put(bigram, index);
				output += bigram.toString() + ":" + index + "\n";
			}

		}

		PrintWriter printWriter = new PrintWriter("train-word-vectors/wordmapping", "UTF-8");
		printWriter.write(output);
		printWriter.close();
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

	public static class VectorComparator implements Comparator<String> {
		@Override
		public int compare(String a, String b) {
			Integer left = Integer.parseInt(a.split(":")[0]);
			Integer right = Integer.parseInt(b.split(":")[0]);

			return Integer.compare(left, right);
		}
	}
}
