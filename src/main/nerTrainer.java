package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class nerTrainer {
	static String outputDisease = "";
	static String outputVictim = "";
	static String output = "";
	static String output2 = "";
	
	public static HashMap<String, Integer> features = new HashMap<String, Integer>();
	public static HashMap<String, Integer> labels = new HashMap<String,Integer>();
	static HashMap<String, File> answer_files = new HashMap<String, File>();
	static HashMap<String, File> text_files = new HashMap<String, File>();
	public static int f = 1;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		boolean newNer = false;
		
		init();
		
		
		
		if(!newNer) {
			for(Entry<String, File> s : text_files.entrySet()) {
				generateVictimTrainingFile(s.getValue(), answer_files.get(s.getKey()));
				PrintWriter printWriter = new PrintWriter("nerTrainingFiles/Train_Victim.tsv", "UTF-8");
				printWriter.write(outputVictim);
				printWriter.close();
			}
		} else {
			
			for(Entry<String, File> s : text_files.entrySet()) {
				generateTrainingFV(s.getValue(), answer_files.get(s.getKey()));
				
			}
			PrintWriter printWriter = new PrintWriter("nerTrainingFiles/Train_fv.tsv", "UTF-8");
			printWriter.write(output);
			printWriter.close();
			PrintWriter printWriter2 = new PrintWriter("nerTrainingFiles/Train_fv.words.tsv", "UTF-8");
			printWriter2.write(output2);
			printWriter2.close();
			
		}
	
		System.out.println("Done");
	}
	
	public static void init() throws FileNotFoundException, IOException {
		labels.put("O", 0);
		labels.put("B-VIC", 1);
		labels.put("I-VIC", 2);
		labels.put("B-DIS", 3);
		labels.put("I-DIS", 4);
		
		
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
		File featureFile = new File("nerTrainingFiles/features.tsv");
		if(featureFile.exists() && !featureFile.isDirectory()) { 
			populateFeatures(featureFile);
		} else {
			generateFeatures();
			for(Entry<String, File> s : text_files.entrySet()) {
				System.out.println("Getting features from " + s.getKey());
				getFeaturesFromFile(s.getValue());
				printFeatures();
			}
		}
		
	}
 	
	public static String generateTestingFV(File file) throws IOException {
		String outputString = "";
		String outputString2 = "";
		String text = "";
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				text += line +" ";
			}
		} catch (FileNotFoundException e) {
			System.exit(0);
		}
		
		Document d = new Document(text);
		for (Sentence s : d.sentences()) {
			if(s.length() > 3) {
				for(int i = 0; i < s.words().size(); i++) {
					ArrayList<Integer> fv = new ArrayList<Integer>();
					String word = s.word(i);
					outputString2 += word;
					//System.out.println(word);
					if(i == 0) {
						outputString += labels.get("O");
						
						//add previous and next word
						fv.add(features.get("prev-PHI"));
						try{
							if(features.containsKey(s.word(i+1))) {
								fv.add(features.get("next-" + s.word(i+1)));
							} else {
								fv.add(features.get("next-UNK"));
							}
						} catch(Exception e) {
							System.out.println("");
						}
						
						
						//add current word
						if(features.containsKey(s.word(i))) {
							fv.add(features.get(s.word(i)));
						} else {
							fv.add(features.get("UNK"));
						}
						
						//add current pos
						if(features.containsKey(s.posTag(i))) {
							fv.add(features.get(s.posTag(i)));
						} else {
							fv.add(features.get("UNKPOS"));
						}
						
						//add previous and next pos
						fv.add(features.get("prev-PHIPOS"));
						if(features.containsKey(s.posTag(i+1))) {
							fv.add(features.get("next-" + s.posTag(i+1)));
						} else {
							fv.add(features.get("next-UNKPOS"));
						}
						
						if(cap(s.word(i))) {
							fv.add(features.get("caps"));
						}
						
					}
					else if(i == s.words().size()-1) {
						outputString += labels.get("O");
						
						//add previous and next word
						fv.add(features.get("next-OMEGA"));
						if(features.containsKey(s.word(i-1))) {
							fv.add(features.get("prev-" + s.word(i-1)));
						} else {
							fv.add(features.get("prev-UNK"));
						}
						
						//add current word
						if(features.containsKey(s.word(i))) {
							fv.add(features.get(s.word(i)));
						} else {
							fv.add(features.get("UNK"));
						}
						
						//add current pos
						if(features.containsKey(s.posTag(i))) {
							fv.add(features.get(s.posTag(i)));
						} else {
							fv.add(features.get("UNKPOS"));
						}
						
						//add previous and next pos
						fv.add(features.get("next-OMEGAPOS"));
						if(features.containsKey(s.posTag(i-1))) {
							fv.add(features.get("prev-" + s.posTag(i-1)));
						} else {
							fv.add(features.get("prev-UNKPOS"));
						}
						
						if(cap(s.word(i))) {
							fv.add(features.get("caps"));
						}
					} else {
						String prevWord = s.word(i-1);
						String nextWord = s.word(i+1);
						
						outputString += labels.get("O");

						//add previous and next word
						if(features.containsKey(s.word(i+1))) {
							fv.add(features.get("next-" + s.word(i+1)));
						} else {
							fv.add(features.get("next-UNK"));
						}
						if(features.containsKey(s.word(i-1))) {
							fv.add(features.get("prev-" + s.word(i-1)));
						} else {
							fv.add(features.get("prev-UNK"));
						}
						
						//add current word
						if(features.containsKey(s.word(i))) {
							fv.add(features.get(s.word(i)));
						} else {
							fv.add(features.get("UNK"));
						}
						
						//add current pos
						if(features.containsKey(s.posTag(i))) {
							fv.add(features.get(s.posTag(i)));
						} else {
							fv.add(features.get("UNKPOS"));
						}
						
						//add previous and next pos
						if(features.containsKey(s.posTag(i+1))) {
							fv.add(features.get("next-" + s.posTag(i+1)));
						} else {
							fv.add(features.get("next-UNKPOS"));
						}
						if(features.containsKey(s.posTag(i-1))) {
							fv.add(features.get("prev-" + s.posTag(i-1)));
						} else {
							fv.add(features.get("prev-UNKPOS"));
						}
						
						if(cap(s.word(i))) {
							fv.add(features.get("caps"));
						}
					}
					
					Collections.sort(fv);
					List<Integer> sortedUniqueFV = fv.stream().distinct().collect(Collectors.toList());
					for(int j : sortedUniqueFV) {
						outputString += " " + j + ":1";
						outputString2 += " " + j + ":1";
					}
					outputString2 += "\n";
					outputString += "\n";
				}
			}
		}
		
		PrintWriter printWriter = new PrintWriter("nerTestingFiles/" + file.getName() + ".tsv", "UTF-8");
		printWriter.write(outputString);
		printWriter.close();
		
		PrintWriter printWriter2 = new PrintWriter("nerTestingFiles/" + file.getName() + "words.tsv", "UTF-8");
		printWriter2.write(outputString2);
		printWriter2.close();
		
		return outputString;
	}
	
	public static void generateTrainingFV(File file, File answerFile) throws IOException {
		String outputString = "";
		String outputString2 = "";
		String text = "";
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				text += line +" ";
			}
		} catch (FileNotFoundException e) {
			
		}
		
		Article answerArticle = Driver.parseAnswerFile(answerFile);
		
		HashSet<String> keyVictimWords = getKeyWords(answerArticle.victim);
		HashSet<String> keyDiseaseWords = getKeyWords(answerArticle.disease);
		System.out.println(file.getName());
		Document d = new Document(text);
		for (Sentence s : d.sentences()) {
			if(s.length() > 3) {
				for(int i = 0; i < s.words().size(); i++) {
					ArrayList<Integer> fv = new ArrayList<Integer>();
					String word = s.word(i);
					//System.out.println(word);
					if(i == 0) {
						if(victimContains(keyVictimWords, s.word(i))) {
							outputString += labels.get("B-VIC");
							outputString2 += "B-VIC";
						} else if(diseaseContains(keyDiseaseWords, s.word(i))) {
							outputString += labels.get("B-DIS");
							outputString2 += "B-DIS";
						} else {
							outputString += labels.get("O");
							outputString2 += "O";
						}
						
						//add previous and next word
						fv.add(features.get("prev-PHI"));
						try{
							if(features.containsKey(s.word(i+1))) {
								fv.add(features.get("next-" + s.word(i+1)));
							} else {
								fv.add(features.get("next-UNK"));
							}
						} catch(Exception e) {
							System.out.println("");
						}
						
						
						//add current word
						if(features.containsKey(s.word(i))) {
							fv.add(features.get(s.word(i)));
						} else {
							fv.add(features.get("UNK"));
						}
						
						//add current pos
						if(features.containsKey(s.posTag(i))) {
							fv.add(features.get(s.posTag(i)));
						} else {
							fv.add(features.get("UNKPOS"));
						}
						
						//add previous and next pos
						fv.add(features.get("prev-PHIPOS"));
						if(features.containsKey(s.posTag(i+1))) {
							fv.add(features.get("next-" + s.posTag(i+1)));
						} else {
							fv.add(features.get("next-UNKPOS"));
						}
						
						if(cap(s.word(i))) {
							fv.add(features.get("caps"));
						}
						
					}
					else if(i == s.words().size()-1) {
						if(victimContains(keyVictimWords, s.word(i))) {
							if(victimContains(keyVictimWords, s.word(i-1))) {
								outputString += labels.get("I-VIC");
								outputString2 += "I-VIC";
							} else {
								outputString += labels.get("B-VIC");
								outputString2 += "B-VIC";
							}
						} else if(diseaseContains(keyDiseaseWords, s.word(i))) {
							if(diseaseContains(keyDiseaseWords, s.word(i-1))) {
								outputString += labels.get("I-DIS");
								outputString2 += "I-VIC";
							} else {
								outputString += labels.get("B-DIS");
								outputString2 += "B-DIS";
							}
						} else {
							outputString += labels.get("O");
							outputString2 += "O";
						}
						
						
						//add previous and next word
						fv.add(features.get("next-OMEGA"));
						if(features.containsKey(s.word(i-1))) {
							fv.add(features.get("prev-" + s.word(i-1)));
						} else {
							fv.add(features.get("prev-UNK"));
						}
						
						//add current word
						if(features.containsKey(s.word(i))) {
							fv.add(features.get(s.word(i)));
						} else {
							fv.add(features.get("UNK"));
						}
						
						//add current pos
						if(features.containsKey(s.posTag(i))) {
							fv.add(features.get(s.posTag(i)));
						} else {
							fv.add(features.get("UNKPOS"));
						}
						
						//add previous and next pos
						fv.add(features.get("next-OMEGAPOS"));
						if(features.containsKey(s.posTag(i-1))) {
							fv.add(features.get("prev-" + s.posTag(i-1)));
						} else {
							fv.add(features.get("prev-UNKPOS"));
						}
						
						if(cap(s.word(i))) {
							fv.add(features.get("caps"));
						}
					} else {
						String prevWord = s.word(i-1);
						String nextWord = s.word(i+1);
						if(victimContains(keyVictimWords, s.word(i))) {
							if(victimContains(keyVictimWords, s.word(i+1))) {
								if(word.equals("and") || word.equals("or") || word.equals("at") || word.equals("other") || word.equals("a") || word.equals("an")){
									if(s.word(i+1).equals("other")) {
										outputString += labels.get("O");
										outputString2 += "O";
									} else {
										if(victimContains(keyVictimWords, s.word(i-1))) {
											outputString += labels.get("I-VIC");
											outputString2 += "I-VIC";	
										} else {
											outputString += labels.get("B-VIC");
											outputString2 += "B-VIC";
										}
									}
								} else {
									if(victimContains(keyVictimWords, s.word(i-1))) {
										outputString += labels.get("I-VIC");
										outputString2 += "I-VIC";	
									} else {
										outputString += labels.get("B-VIC");
										outputString2 += "B-VIC";
									}
								}
							} else if(victimContains(keyVictimWords, s.word(i-1))) {
								outputString += labels.get("I-VIC");
								outputString2 += "I-VIC";
							}  else {
								if(word.equals("the") || word.equals("and") || word.equals("or") || word.equals("at") || word.equals("other") || word.equals("a") || word.equals("an")){
									outputString += labels.get("O");
									outputString2 += "O";
								} else {
									outputString += labels.get("B-VIC");
									outputString2 += "B-VIC";
								}
								
							}
						} else if(diseaseContains(keyDiseaseWords, s.word(i))) {
							if(diseaseContains(keyDiseaseWords, s.word(i-1))) {
								if(word.equals("the") || word.equals("The") || word.equals("in")) {
									if(diseaseContains(keyDiseaseWords, s.word(i+1))) {
										outputString += labels.get("I-DIS");
										outputString2 += "I-DIS";
									} else {
										outputString += labels.get("O");
										outputString2 += "O";
									}
								} else {
									outputString += labels.get("I-DIS");
									outputString2 += "I-DIS";
								}
							} else {
								if(word.equals("the") || word.equals("The") || word.equals("in") || word.equals("a") || word.equals("an")) {
									if(diseaseContains(keyDiseaseWords, s.word(i+1))) {
										outputString += labels.get("B-DIS");
										outputString2 += "B-DIS";
									} else {
										outputString += labels.get("O");
										outputString2 += "O";
									}
								} else {
									outputString += labels.get("B-DIS");
									outputString2 += "B-DIS";
								}		
							}
						} else {
							outputString += labels.get("O");
							outputString2 += "O";
						}
						
						
						
						//add previous and next word
						if(features.containsKey(s.word(i+1))) {
							fv.add(features.get("next-" + s.word(i+1)));
						} else {
							fv.add(features.get("next-UNK"));
						}
						if(features.containsKey(s.word(i-1))) {
							fv.add(features.get("prev-" + s.word(i-1)));
						} else {
							fv.add(features.get("prev-UNK"));
						}
						
						//add current word
						if(features.containsKey(s.word(i))) {
							fv.add(features.get(s.word(i)));
						} else {
							fv.add(features.get("UNK"));
						}
						
						//add current pos
						if(features.containsKey(s.posTag(i))) {
							fv.add(features.get(s.posTag(i)));
						} else {
							fv.add(features.get("UNKPOS"));
						}
						
						//add previous and next pos
						if(features.containsKey(s.posTag(i+1))) {
							fv.add(features.get("next-" + s.posTag(i+1)));
						} else {
							fv.add(features.get("next-UNKPOS"));
						}
						if(features.containsKey(s.posTag(i-1))) {
							fv.add(features.get("prev-" + s.posTag(i-1)));
						} else {
							fv.add(features.get("prev-UNKPOS"));
						}
						
						if(cap(s.word(i))) {
							fv.add(features.get("caps"));
						}
					}
					
					Collections.sort(fv);
					List<Integer> sortedUniqueFV = fv.stream().distinct().collect(Collectors.toList());
					for(int j : sortedUniqueFV) {
						outputString += " " + j + ":1";
						outputString2 += " " + j + ":1";
					}
					outputString += "\n";
					outputString2 += " " +  word + "\n";
				}
			}
		}
		
		output += outputString;
		output2 += outputString2;

		
		PrintWriter printWriter = new PrintWriter("nerTrainingFiles/" + file.getName() + ".tsv", "UTF-8");
		printWriter.write(outputString);
		printWriter.close();
		PrintWriter printWriter2 = new PrintWriter("nerTrainingFiles/" + file.getName() + ".words.tsv", "UTF-8");
		printWriter2.write(outputString2);
		printWriter2.close();
		
	}
	
	public static boolean diseaseContains(HashSet<String> keyDiseaseWords, String word) {
		for(String s : keyDiseaseWords) {
			if(s.contains("/")) {
				String split[] = s.split("/");
				for(String s1 : split) {
					if(isContain(s1.trim(), word)) {
						return true;
					}
				}
			}
			else if(isContain(s, word)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static boolean victimContains(HashSet<String> keyVictimWords, String word) {
		for(String s : keyVictimWords) {
			if(isContain(s, word)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isContain(String source, String subItem) {
		if(StringUtils.isAlphanumeric(subItem)) {
			String pattern = "\\b" + subItem + "\\b";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(source);
			return m.find();
		}
		return false;
		
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
		
		features.put("caps", f++);
	}
	
	public static boolean cap(String w) {
		if(Character.isUpperCase(w.charAt(0))){
			return true;
		} else {
			return false;
		}
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
			System.exit(0);
		}
		
		Document d = new Document(text);
		
		HashSet<String> keyVictimWords = getKeyWords(answerArticle.victim);
		
		for (Sentence s : d.sentences()) {
			for(i = 0; i < s.words().size(); i++) {
				//first word of sentence
				if(i == 0) {
					if(keyVictimWords.contains(s.word(i))) {
						if(keyVictimWords.contains(s.word(i+1))) {
							outputString += s.word(i) + "	B-VIC\n";
						}
					} else {
						outputString += s.word(i) + "	" + s.posTag(i) + "\n";
					}
				//last word
				} else if (i == s.words().size()-1) {
					if(keyVictimWords.contains(s.word(i))) {
						if(keyVictimWords.contains(s.word(i-1))) {
							outputString += s.word(i) + "	I-VIC\n";
						}
					} else {
						outputString += s.word(i) + "	" + s.posTag(i) + "\n";
					}
				} 
				//all other words
				else {
					//i-1, i, i+1
					if(keyVictimWords.contains(s.word(i)) && keyVictimWords.contains(s.word(i-1))) {
						outputString += s.word(i) + "	I-VIC\n";
					} else if(keyVictimWords.contains(s.word(i)) && keyVictimWords.contains(s.word(i+1))) {
						outputString += s.word(i) + "	B-VIC\n";
					} else {
						outputString += s.word(i) + "	" + s.posTag(i) + "\n";
					}
				}
				
			}
		}
		PrintWriter printWriter = new PrintWriter("nerTrainingFiles/" + textFile.getName() +".victims.tsv", "UTF-8");
		printWriter.write(outputString);
		printWriter.close();
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
//
//for (Sentence s : d.sentences()) {
//	for(i = 0; i < s.words().size(); i++) {
//		if(keyVictimWords.contains(s.word(i))) {
//			if(s.word(i).equals("the")) {
//				if(keyVictimWords.contains(s.word(i+1))) {
//					outputString += s.word(i) + "	B-VIC\n";
//				}
//			} 
//			else if(s.word(i).equals("and")) {
//				if(keyVictimWords.contains(s.word(i+1)) && keyVictimWords.contains(s.word(i-1))) {
//					outputString += s.word(i) + "	I-VIC\n";
//				}
//			}
//			else if(s.word(i).equals("at")) {
//				if(keyVictimWords.contains(s.word(i+1))) {
//					outputString += s.word(i) + "	I-VIC\n";
//				}
//			}
//			else if(s.word(i).equals("at")) {
//				if(keyVictimWords.contains(s.word(i+1))) {
//					outputString += s.word(i) + "	I-VIC\n";
//				}
//			}
//			else {
//				outputString += s.word(i) + "	B-VIC\n";
//			}
//		} else {
//			outputString += s.word(i) + "	O\n";
//		}
//	}
//}
//outputVictim += outputString;
