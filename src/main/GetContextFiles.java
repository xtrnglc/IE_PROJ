package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetContextFiles {
	static ArrayList<File> answer_files = new ArrayList<File>();
	static HashMap<String, File> text_files = new HashMap<String, File>();

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File answer_folder = new File("./data/templates");
		File text_folder = new File("./data/labeled-docs");
		File[] listOfAnswerFolders = answer_folder.listFiles();
		File[] listOfTextFolders = text_folder.listFiles();

		for (File file : listOfAnswerFolders) {
			if (file.isFile() && !file.getName().startsWith(".")) {
				answer_files.add(file);
			}
		}
		for (File file : listOfTextFolders) {
			if (file.isFile() && !file.getName().startsWith(".")) {
				text_files.put(file.getName(), file);
			}
		}

		generateStatusFile();
//		generateContainmentFile();
//		generateCountryFile();
//		generateDiseaseFile();
//		generateVictimsFile();
	}

	public static void generateStatusFile() throws FileNotFoundException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("context-files/status-context.txt"));
		String str = "";
		int count=0;

		for (File file : answer_files) {
			Scanner scanner = new Scanner(file);
			String answer = "";
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("Status:")) {
					String[] splitLine = line.split("\\s+");
					answer = splitLine[1];
				}
			}
			scanner.close();

			int index = file.getName().indexOf(".annot");
			String fileName = file.getName().substring(0, index);
			File textFile = text_files.get(fileName);
			Scanner scanner2 = new Scanner(textFile);
			if (count > 0)
			{
				str+= "\n";
			}
			str += "FILE: " + fileName + "\n";
			count++;
			str += "ANSWER: " + answer + "\n";
			String text = "";

			while (scanner2.hasNext()) {
				text += scanner2.nextLine();
			}

			Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)",
					Pattern.MULTILINE | Pattern.COMMENTS);
			Matcher reMatcher = re.matcher(text);
			while (reMatcher.find()) {
				String sentence = reMatcher.group();
				if (sentence.contains(answer)) {
					str += "CONTEXT: " + sentence + "\n\n";
				}
			}

			scanner2.close();
		}

		writer.write(str);
		writer.close();
	}

	public static void generateContainmentFile() throws FileNotFoundException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("context-files/containment-context.txt"));
		String str = "";
		int count=0;
		
		for (File file : answer_files) {
			Scanner scanner = new Scanner(file);
			String answer = "";
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("Containment:")) {
					String[] splitLine = line.split("\\s+");
					answer = splitLine[1];
				}
			}
			scanner.close();

			int index = file.getName().indexOf(".annot");
			String fileName = file.getName().substring(0, index);
			File textFile = text_files.get(fileName);
			Scanner scanner2 = new Scanner(textFile);
			if (count > 0)
			{
				str+= "\n";
			}
			str += "FILE: " + fileName + "\n";
			count++;
			str += "ANSWER: " + answer + "\n";
			String text = "";

			while (scanner2.hasNext()) {
				text += scanner2.nextLine();
			}

			Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)",
					Pattern.MULTILINE | Pattern.COMMENTS);
			Matcher reMatcher = re.matcher(text);
			while (reMatcher.find()) {
				String sentence = reMatcher.group();
				if (sentence.contains(answer)) {
					str += "CONTEXT: " + sentence + "\n\n";
				}
			}

			scanner2.close();
		}

		writer.write(str);
		writer.close();
	}

	public static void generateCountryFile() throws FileNotFoundException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("context-files/country-context.txt"));
		String str = "";
		ArrayList<String> answers = new ArrayList<String>();
		int count = 0;

		for (File file : answer_files) {
			answers.clear();
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("Country:")) {
					String answer = "";
					String[] splitLine = line.split("\\s+");
					int lengthOfAnswer = splitLine.length;
					for (int i = 1; i < lengthOfAnswer - 1; i++) {
						answer += splitLine[i] + " ";
					}
					answer += splitLine[lengthOfAnswer - 1];
					answers.add(answer);
				}
			}
			scanner.close();

			int index = file.getName().indexOf(".annot");
			String fileName = file.getName().substring(0, index);
			File textFile = text_files.get(fileName);
			Scanner scanner2 = new Scanner(textFile);
			if (count > 0)
			{
				str+= "\n";
			}
			str += "FILE: " + fileName + "\n";
			count++;
			String text = "";

			while (scanner2.hasNext()) {
				text += scanner2.nextLine();
			}

			for (String foundAnswer2 : answers) {
				str += "ANSWER: " + foundAnswer2 + "\n";

				Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)",
						Pattern.MULTILINE | Pattern.COMMENTS);
				Matcher reMatcher = re.matcher(text);
				while (reMatcher.find()) {
					String sentence = reMatcher.group();
					if (sentence.toLowerCase().contains(foundAnswer2.toLowerCase())) {
						str += "CONTEXT: " + sentence + "\n\n";
					}
				}
			}

			scanner2.close();
		}

		writer.write(str);
		writer.close();
	}

	public static void generateDiseaseFile() throws FileNotFoundException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("context-files/disease-context.txt"));
		String str = "";
		ArrayList<String> answers = new ArrayList<String>();
		int count = 0;

		for (File file : answer_files) {
			answers.clear();
			Scanner scanner = new Scanner(file);
			String answer = "";
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("Disease:")) {
					String[] splitLine = line.split("\\s+");
					int lengthOfAnswer = splitLine.length;
					for (int i = 1; i < lengthOfAnswer - 1; i++) {
						answer += splitLine[i] + " ";
					}
					answer += splitLine[lengthOfAnswer - 1];
					if (answer.contains(" / ")) {
						String[] splitAnswer = answer.split(" / ");
						for (String foundAnswer : splitAnswer) {
							answers.add(foundAnswer);
						}
					} else {
						answers.add(answer);
					}
				}
			}
			scanner.close();

			int index = file.getName().indexOf(".annot");
			String fileName = file.getName().substring(0, index);
			File textFile = text_files.get(fileName);
			Scanner scanner2 = new Scanner(textFile);
			if (count > 0)
			{
				str+= "\n";
			}
			str += "FILE: " + fileName + "\n";
			count++;
			String text = "";
			while (scanner2.hasNext()) {
				text += scanner2.nextLine();
			}

			for (String foundAnswer2 : answers) {
				str += "ANSWER: " + foundAnswer2 + "\n";

				Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)",
						Pattern.MULTILINE | Pattern.COMMENTS);
				Matcher reMatcher = re.matcher(text);
				while (reMatcher.find()) {
					String sentence = reMatcher.group();
					if (sentence.contains(foundAnswer2)) {
						str += "CONTEXT: " + sentence + "\n\n";
					}
				}
			}
			scanner2.close();
		}

		writer.write(str);
		writer.close();
	}

	public static void generateVictimsFile() throws FileNotFoundException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("context-files/victims-context.txt"));
		String str = "";
		ArrayList<String> answers = new ArrayList<String>();
		int count = 0;

		for (File file : answer_files) {
			answers.clear();
			Scanner scanner = new Scanner(file);
			String answer = "";
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("Victims:")) {
					String[] splitLine = line.split("\\s+");
					int lengthOfAnswer = splitLine.length;
					for (int i = 1; i < lengthOfAnswer - 1; i++) {
						answer += splitLine[i] + " ";
					}
					answer += splitLine[lengthOfAnswer - 1];
					if (answer.contains(" / ")) {
						String[] splitAnswer = answer.split(" / ");
						for (String foundAnswer : splitAnswer) {
							answers.add(foundAnswer);
						}
					} else {
						answers.add(answer);
					}
				}
			}
			scanner.close();

			int index = file.getName().indexOf(".annot");
			String fileName = file.getName().substring(0, index);
			File textFile = text_files.get(fileName);
			Scanner scanner2 = new Scanner(textFile);
			if (count > 0)
			{
				str+= "\n";
			}
			str += "FILE: " + fileName + "\n";
			count++;
			String text = "";
			while (scanner2.hasNext()) {
				text += scanner2.nextLine();
			}

			for (String foundAnswer2 : answers) {
				str += "ANSWER: " + foundAnswer2 + "\n";

				Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)",
						Pattern.MULTILINE | Pattern.COMMENTS);
				Matcher reMatcher = re.matcher(text);
				while (reMatcher.find()) {
					String sentence = reMatcher.group();
					if (sentence.contains(foundAnswer2)) {
						str += "CONTEXT: " + sentence + "\n\n";
					}
				}
			}
			scanner2.close();
		}

		writer.write(str);
		writer.close();
	}
}
