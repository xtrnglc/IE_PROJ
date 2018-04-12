package main;

public class Bigram {
	String word1;
	String word2;

	public Bigram(String word1, String word2) {
		this.word1 = word1;
		this.word2 = word2;
	}

	public int hashCode() {
		return word1.hashCode() * 31 + word2.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		Bigram bigram = (Bigram) o;
		return (this.word1.equals(bigram.word1) && this.word2.equals(bigram.word2));
	}

	@Override
	public String toString() {
		return this.word1 + " " + this.word2;
	}

	public int getLength() {
		return this.toString().length();
	}

	public String getWord1() {
		return word1;
	}

	public void setWord1(String word1) {
		this.word1 = word1;
	}

	public String getWord2() {
		return word2;
	}

	public void setWord2(String word2) {
		this.word2 = word2;
	}

	public static Bigram getBigram(String input) {
		String word1 = input.split(" ")[0];
		String word2 = input.split(" ")[1];
		Bigram bigram = new Bigram(word1, word2);
		return bigram;
	}
}
