package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class nerTrainer {
	static String output = "";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		HashMap<String, File> answer_files = new HashMap<String, File>();
		HashMap<String, File> text_files = new HashMap<String, File>();
		File answer_folder = new File("./data/templates");
		File text_folder = new File("./data/labeled-docs");
		File[] listOfAnswerFolders = answer_folder.listFiles();
		File[] listOfTextFolders = text_folder.listFiles();

		for (File file : listOfAnswerFolders) {
			if (file.isFile() && !file.getName().startsWith(".")) {
				if(file.getName().contains("20020516.4232.maintext")) {
					System.out.println("here");
				}
				answer_files.put(file.getName().substring(0,22), file);
			}
		}
		for (File file : listOfTextFolders) {
			if (file.isFile() && !file.getName().startsWith(".")) {
				if(file.getName().contains("20020516.4232.maintext")) {
					System.out.println("here");
				}
				text_files.put(file.getName(), file);
			}
		}
		
		for(Entry<String, File> s : text_files.entrySet()) {
			generateDiseaseTrainingFile(s.getValue(), answer_files.get(s.getKey()));
		}
		
		//generateTrainingFile(text_files.get("20020415.3958.maintext"), answer_files.get("20020415.3958.maintext"));
		
		
		PrintWriter printWriter = new PrintWriter("diseaseTrain.tsv", "UTF-8");
		printWriter.write(output);
		printWriter.close();

		System.out.println("Done");
	}
	
	public static HashSet<String> getKeyWords (HashSet<String> set) {
		HashSet<String> words = new HashSet<String>();
		
		for(String s : set) {
			String[] split = s.split("\\s+");
			for(String s1 : split) {
				words.add(s1);
			}
		}
		
		return words;
	}
	
	public static void generateDiseaseTrainingFile(File textFile, File answerFile) throws FileNotFoundException, IOException {
		Article answerArticle = Driver.parseAnswerFile(answerFile);
		String text = "";
		int i = 0;
		String outputString = "";

		try (BufferedReader br = new BufferedReader(new FileReader(textFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (i == 0) {
				} else {
					text += " " + line;
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Please remove the '-s' flag on the command line if passing in a folder path!");
			System.exit(0);
		}
		
		Document d = new Document(text);
		
		HashSet<String> keyWords = getKeyWords(answerArticle.disease);
		for (Sentence s : d.sentences()) {
			for(i = 0; i < s.words().size(); i++) {
				if(keyWords.contains(s.word(i))) {
					if(s.word(i).equals("the")) {
						if(keyWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	DIS\n";
						}
					} else {
						outputString += s.word(i) + "	DIS\n";
					}
				} else {
					outputString += s.word(i) + "	0\n";
				}
			}
			
		}
		output += outputString;
	}
}
