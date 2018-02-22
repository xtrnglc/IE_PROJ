package main;

import java.io.File;
import java.util.ArrayList;

public class GetContextFiles {
	static ArrayList<File> answer_files = new ArrayList<File>();
	static ArrayList<File> text_files = new ArrayList<File>();
	
	public static void main(String[] args) {
		File answer_folder = new File(args[0]);
		File text_folder = new File(args[1]);
		File[] listOfAnswerFolders = answer_folder.listFiles();
		File[] listOfTextFolders = text_folder.listFiles();

		for (File file : listOfAnswerFolders) {
			if (file.isFile()) {
				answer_files.add(file);
			}
		}
		for (File file : listOfTextFolders) {
			if (file.isFile()) {
				text_files.add(file);
			}
		}
		
		
	}
}
