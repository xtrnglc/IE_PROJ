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
import java.util.Map.Entry;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class nerTrainer {
	static String outputDisease = "";
	static String outputVictim = "";
	static String output = "";
	
	public static HashMap<String, Integer> features = new HashMap<String, Integer>();
	public static int f = 1;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		boolean newNer = true;
		
		HashMap<String, File> answer_files = new HashMap<String, File>();
		HashMap<String, File> text_files = new HashMap<String, File>();
		File answer_folder = new File("./data/templates");
		File text_folder = new File("./data/labeled-docs");
		File[] listOfAnswerFolders = answer_folder.listFiles();
		File[] listOfTextFolders = text_folder.listFiles();

		for (File file : listOfAnswerFolders) {
			if (file.isFile() && !file.getName().startsWith(".")) {
				answer_files.put(file.getName().substring(0,22), file);
			}
		}
		for (File file : listOfTextFolders) {
			if (file.isFile() && !file.getName().startsWith(".")) {
				text_files.put(file.getName(), file);
			}
		}
		
		
		
		if(!newNer) {
			for(Entry<String, File> s : text_files.entrySet()) {
				//generateTrainingFile(text_files.get("20020415.3958.maintext"), answer_files.get("20020415.3958.maintext"));
				generateTrainingFile(s.getValue(), answer_files.get(s.getKey()));
				PrintWriter printWriter = new PrintWriter("nerTrainingFiles/Train.tsv", "UTF-8");
				printWriter.write(output);
				printWriter.close();
			}
		} else {
			File featureFile = new File("nerTrainingFiles/features.tsv");
			if(featureFile.exists() && !featureFile.isDirectory()) { 
				populateFeatures(featureFile);
			} else {
				generateFeatures();
				for(Entry<String, File> s : text_files.entrySet()) {
					System.out.println(s.getKey());
					getFeaturesFromFile(s.getValue());
					printFeatures();
				}
			}
			
		}
	
		System.out.println("Done");
	}
	
	public static void generateTrainingFV(File file, File answerFile) {
		
	}
	
	public static void populateFeatures(File file) throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				String split[] = line.split("\t");
				features.put(split[0], Integer.parseInt(split[1]));
			}
		} catch (FileNotFoundException e) {
			
		}
	}
	
	public static void getFeaturesFromFile(File file) throws FileNotFoundException, IOException {
		String text = "";
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				text += line +" ";
			}
		} catch (FileNotFoundException e) {
			
		}
		
		Document d = new Document(text);
		for (Sentence s : d.sentences()) {
			for(int i = 0; i < s.words().size(); i++) {
				String wordPrev = "prev-" + s.word(i);
				String wordNext = "next-" + s.word(i);
				String word = s.word(i);
				
				if(!features.containsKey(word)) {
					features.put(word, f++);
				}
				if(!features.containsKey(wordPrev)) {
					features.put(wordPrev, f++);
				}
				if(!features.containsKey(wordNext)) {
					features.put(wordNext, f++);
				}
				
				String posPrev = "prev-" + s.posTag(i);
				String posNext = "next-" + s.posTag(i);
				String pos = s.posTag(i);
				
				if(!features.containsKey(pos)) {
					features.put(pos, f++);
				}
				if(!features.containsKey(posPrev)) {
					features.put(posPrev, f++);
				}
				if(!features.containsKey(posNext)) {
					features.put(posNext, f++);
				}
			}
		}
	}
	
	public static void printFeatures() throws FileNotFoundException, UnsupportedEncodingException {
		String text = "";
		for(Entry<String, Integer> s : features.entrySet()){
			text += s.getKey() + "\t" + s.getValue() + "\n";
		}
		
		PrintWriter printWriter = new PrintWriter("nerTrainingFiles/features.tsv", "UTF-8");
		printWriter.write(text);
		printWriter.close();
		
	}
	
	public static void generateFeatures() {
		features.put("UNKPOS", f++);
		features.put("UNK", f++);	
		
		features.put("prev-PHIPOS", f++);
		features.put("next-OMEGAPOS", f++);
		features.put("prev-UNKPOS", f++);
		features.put("next-UNKPOS", f++);
		
		features.put("prev-PHI", f++);
		features.put("next-OMEGA", f++);
		features.put("prev-UNK", f++);
		features.put("next-UNK", f++);
	}
	
	public static HashSet<String> getKeyWords (HashSet<String> set) {
		HashSet<String> words = new HashSet<String>();
		
		for(String s : set) {
			String[] split = s.split("\\s+");
			for(String s1 : split) {
				words.add(s1.replaceAll("\\s*\\p{Punct}+\\s*$", "").replaceAll("[()]", ""));
			}
		}
		
		return words;
	}
	
	public static void generateTrainingFile(File textFile, File answerFile) throws FileNotFoundException, IOException {
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
		
		HashSet<String> keyVictimWords = getKeyWords(answerArticle.victim);
		HashSet<String> keyDiseaseWords = getKeyWords(answerArticle.disease);
		
		for (Sentence s : d.sentences()) {
			for(i = 0; i < s.words().size(); i++) {
				//System.out.println(s.word(i));
				if(keyVictimWords.contains(s.word(i))) {
					if(s.word(i).equals("the")) {
						if(keyVictimWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	B-VIC\n";
						}
					} 
					else if(s.word(i).equals("and")) {
						if(keyVictimWords.contains(s.word(i+1)) && keyVictimWords.contains(s.word(i-1))) {
							outputString += s.word(i) + "	I-VIC\n";
						}
					}
					else if(s.word(i).equals("at")) {
						if(keyVictimWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	I-VIC\n";
						}
					}
					else if(s.word(i).equals("other")) {
						if(keyVictimWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	I-VIC\n";
						}
					}
					else {
						try{
							if(keyVictimWords.contains(s.word(i-1))) {
								outputString += s.word(i) + "	I-VIC\n";
							} else {
								outputString += s.word(i) + "	B-VIC\n";
							}
						} catch(Exception e) {
							outputString += s.word(i) + "	B-VIC\n";
						}
						
					}
				} 
				else if(s.word(i).contains("polio") || s.word(i).contains("Polio") ) {
					outputString += s.word(i) + "	B-DIS\n";
				}
				else if(s.word(i).contains("Herpes") || s.word(i).contains("herpes") ) {
					outputString += s.word(i) + "	B-DIS\n";
				}
				
				else if(keyDiseaseWords.contains(s.word(i))) {
					if(s.word(i).equals("the")) {
						if(keyDiseaseWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	B-DIS\n";
						}
					} else {
						try{
							if(keyDiseaseWords.contains(s.word(i-1))) {
								outputString += s.word(i) + "	I-DIS\n";
							} else {
								outputString += s.word(i) + "	B-DIS\n";
							}
						} catch(Exception e) {
							outputString += s.word(i) + "	B-DIS\n";
						}
					}
				} else {
//					if(s.word(i).equals("-LRB-")) {
//						if(keyDiseaseWords.contains(s.word(i+1))) {
//							outputString += s.word(i) + "	DIS\n";
//						}
//					}
//					else if(s.word(i).equals("-RRB-")) {
//						if(keyDiseaseWords.contains(s.word(i-1))) {
//							outputString += s.word(i) + "	DIS\n";
//						}
//					} else {
					try {
						if(s.word(i-1).contains("herpes") || s.word(i-1).contains("Herpes")) {
							outputString += s.word(i) + "	I-DIS\n";
						} else {
							outputString += s.word(i) + "	O"  +"\n";
						}
						
					} catch(Exception e) {
						outputString += s.word(i) + "	O"  +"\n";
					}
					//}
				}
			}
			
		}
		PrintWriter printWriter = new PrintWriter("nerTrainingFiles/" + textFile.getName() +".tsv", "UTF-8");
		printWriter.write(output);
		printWriter.close();
		
		output += outputString;
	}
	
	public static void generateVictimTrainingFile(File textFile, File answerFile) throws FileNotFoundException, IOException {
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
		
		HashSet<String> keyVictimWords = getKeyWords(answerArticle.victim);
		HashSet<String> keyDiseaseWords = getKeyWords(answerArticle.disease);
		
		for (Sentence s : d.sentences()) {
			for(i = 0; i < s.words().size(); i++) {
				if(keyVictimWords.contains(s.word(i))) {
					if(s.word(i).equals("the")) {
						if(keyVictimWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	VIC\n";
						}
					} 
					else if(s.word(i).equals("and")) {
						if(keyVictimWords.contains(s.word(i+1)) && keyVictimWords.contains(s.word(i-1))) {
							outputString += s.word(i) + "	VIC\n";
						}
					}
					else if(s.word(i).equals("at")) {
						if(keyVictimWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	VIC\n";
						}
					}
					else {
						outputString += s.word(i) + "	VIC\n";
					}
				} else {
					outputString += s.word(i) + "	O\n";
				}
				
				if(keyDiseaseWords.contains(s.word(i))) {
					if(s.word(i).equals("the")) {
						if(keyDiseaseWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	DIS\n";
						}
					} else {
						outputString += s.word(i) + "	DIS\n";
					}
				} else {
					
					outputString += s.word(i) + "	O\n";
				}
			}
			
		}
		outputVictim += outputString;
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
					
					outputString += s.word(i) + "	O\n";
				}
			}
			
		}
		outputDisease += outputString;
	}
}
