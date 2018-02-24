package main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ScoringProgram {
	public ScoringProgram() {
		instantiateTotals();
	}

	public static HashMap<String, Integer> recallTotalNumerator = new HashMap<String, Integer>();
	public static HashMap<String, Integer> recallTotalDenominator = new HashMap<String, Integer>();
	public static HashMap<String, Integer> precisionTotalNumerator = new HashMap<String, Integer>();
	public static HashMap<String, Integer> precisionTotalDenominator = new HashMap<String, Integer>();
	public static HashMap<String, List<Double>> f1Total = new HashMap<String, List<Double>>();

	public static void evaluateSingle(Article output, Article answer) {
		HashMap<String, String> recall = calculateRecall(output, answer);
		HashMap<String, String> precision = calculatePrecision(output, answer);
		HashMap<String, String> f1 = calculateF1(output, answer);
		appendTotals(recall, precision, f1);
		printEvaluation(output.story, output.id, recall, precision, f1);

	}

	public static void evaluate(HashMap<String, Article> outputs, HashMap<String, Article> answers) {

		for (Entry<String, Article> s : outputs.entrySet()) {
			Article output = s.getValue();
			Article answer = answers.get(s.getKey());

			HashMap<String, String> recall = calculateRecall(output, answer);
			HashMap<String, String> precision = calculatePrecision(output, answer);
			HashMap<String, String> f1 = calculateF1(output, answer);
			appendTotals(recall, precision, f1);
			printEvaluation(output.story, output.id, recall, precision, f1);
		}
		printTotals();
	}

	public static void printTotals() {

		System.out.println("====================================================================================\n");
		System.out.println("          SCORES for ALL Templates\n");
		DecimalFormat dec = new DecimalFormat("#0.00");
		System.out.format("%-20s%-30s%-30s%-30s", "Label", "RECALL", "PRECISION", "F-MEASURE");
		System.out.println();

		System.out.format("%-20s%-30s%-30s%-30s", "Status:",
				dec.format((double) recallTotalNumerator.get("status") / (double) recallTotalDenominator.get("status"))
						+ " (" + recallTotalNumerator.get("status") + "/" + recallTotalDenominator.get("status") + ")",
				dec.format((double) precisionTotalNumerator.get("status")
						/ (double) precisionTotalDenominator.get("status")) + " ("
						+ precisionTotalNumerator.get("status") + "/" + precisionTotalDenominator.get("status") + ")",
				dec.format(f1Total.get("status").stream().mapToDouble(val -> val).average().getAsDouble()));
		System.out.println();

		System.out.format("%-20s%-30s%-30s%-30s", "Date:",
				dec.format((double) recallTotalNumerator.get("date") / (double) recallTotalDenominator.get("date"))
						+ " (" + recallTotalNumerator.get("date") + "/" + recallTotalDenominator.get("date") + ")",
				dec.format(
						(double) precisionTotalNumerator.get("date") / (double) precisionTotalDenominator.get("date"))
						+ " (" + precisionTotalNumerator.get("date") + "/" + precisionTotalDenominator.get("date")
						+ ")",
				dec.format(f1Total.get("date").stream().mapToDouble(val -> val).average().getAsDouble()));
		System.out.println();

		System.out.format("%-20s%-30s%-30s%-30s", "Event:",
				dec.format((double) recallTotalNumerator.get("event") / (double) recallTotalDenominator.get("event"))
						+ " (" + recallTotalNumerator.get("event") + "/" + recallTotalDenominator.get("event") + ")",
				dec.format(
						(double) precisionTotalNumerator.get("event") / (double) precisionTotalDenominator.get("event"))
						+ " (" + precisionTotalNumerator.get("event") + "/" + precisionTotalDenominator.get("event")
						+ ")",
				dec.format(getFScore(
						(double) recallTotalNumerator.get("event") / (double) recallTotalDenominator.get("event"),
						(double) precisionTotalNumerator.get("event")
								/ (double) precisionTotalDenominator.get("event"))));
		System.out.println();

		System.out.format("%-20s%-30s%-30s%-30s", "Country:",
				dec.format(
						(double) recallTotalNumerator.get("country") / (double) recallTotalDenominator.get("country"))
						+ " (" + recallTotalNumerator.get("country") + "/" + recallTotalDenominator.get("country")
						+ ")",
				dec.format((double) precisionTotalNumerator.get("country")
						/ (double) precisionTotalDenominator.get("country")) + " ("
						+ precisionTotalNumerator.get("country") + "/" + precisionTotalDenominator.get("country") + ")",
				dec.format(getFScore(
						(double) recallTotalNumerator.get("country") / (double) recallTotalDenominator.get("country"),
						(double) precisionTotalNumerator.get("country")
								/ (double) precisionTotalDenominator.get("country"))));
		System.out.println();

		if ((double) precisionTotalDenominator.get("containment") == 0.0) {
			System.out.format("%-20s%-30s%-30s%-30s", "Containment:",
					dec.format((double) recallTotalNumerator.get("containment")
							/ (double) recallTotalDenominator.get("containment")) + " ("
							+ recallTotalNumerator.get("containment") + "/" + recallTotalDenominator.get("containment")
							+ ")",
					"0.00 (" + precisionTotalNumerator.get("containment") + "/"
							+ precisionTotalDenominator.get("containment") + ")",
					dec.format(f1Total.get("containment").stream().mapToDouble(val -> val).average().getAsDouble()));
			System.out.println();
		} else {
			System.out.format("%-20s%-30s%-30s%-30s", "Containment:",
					dec.format((double) recallTotalNumerator.get("containment")
							/ (double) recallTotalDenominator.get("containment")) + " ("
							+ recallTotalNumerator.get("containment") + "/" + recallTotalDenominator.get("containment")
							+ ")",
					dec.format((double) precisionTotalNumerator.get("containment")
							/ (double) precisionTotalDenominator.get("containment")) + " ("
							+ precisionTotalNumerator.get("containment") + "/"
							+ precisionTotalDenominator.get("containment") + ")",
					dec.format(f1Total.get("containment").stream().mapToDouble(val -> val).average().getAsDouble()));
			System.out.println();
		}

		System.out.format("%-20s%-30s%-30s%-30s", "Disease:",
				dec.format(
						(double) recallTotalNumerator.get("disease") / (double) recallTotalDenominator.get("disease"))
						+ " (" + recallTotalNumerator.get("disease") + "/" + recallTotalDenominator.get("disease")
						+ ")",
				dec.format((double) precisionTotalNumerator.get("disease")
						/ (double) precisionTotalDenominator.get("disease")) + " ("
						+ precisionTotalNumerator.get("disease") + "/" + precisionTotalDenominator.get("disease") + ")",
				dec.format(f1Total.get("disease").stream().mapToDouble(val -> val).average().getAsDouble()));
		System.out.println();

		if ((double) precisionTotalDenominator.get("victim") == 0.0) {
			System.out.format("%-20s%-30s%-30s%-30s", "Victim:",
					dec.format(
							(double) recallTotalNumerator.get("victim") / (double) recallTotalDenominator.get("victim"))
							+ " (" + recallTotalNumerator.get("victim") + "/" + recallTotalDenominator.get("victim")
							+ ")",
					"0.00 (" + precisionTotalNumerator.get("victim") + "/" + precisionTotalDenominator.get("victim")
							+ ")",
					dec.format(f1Total.get("victim").stream().mapToDouble(val -> val).average().getAsDouble()));
			System.out.println();
		} else {
			System.out.format("%-20s%-30s%-30s%-30s", "Victim:",
					dec.format(
							(double) recallTotalNumerator.get("victim") / (double) recallTotalDenominator.get("victim"))
							+ " (" + recallTotalNumerator.get("victim") + "/" + recallTotalDenominator.get("victim")
							+ ")",
					dec.format((double) precisionTotalNumerator.get("victim")
							/ (double) precisionTotalDenominator.get("victim")) + " ("
							+ precisionTotalNumerator.get("victim") + "/" + precisionTotalDenominator.get("victim")
							+ ")",
					dec.format(f1Total.get("victim").stream().mapToDouble(val -> val).average().getAsDouble()));
			System.out.println();
		}

		System.out.format("%-20s%-30s%-30s%-30s", "--------", "--------------", "--------------", "----");
		System.out.println();
		int recallNumerator = recallTotalNumerator.get("status") + recallTotalNumerator.get("date")
				+ recallTotalNumerator.get("event") + recallTotalNumerator.get("country")
				+ recallTotalNumerator.get("containment") + recallTotalNumerator.get("disease")
				+ recallTotalNumerator.get("victim");
		int recallDenominator = recallTotalDenominator.get("status") + recallTotalDenominator.get("date")
				+ recallTotalDenominator.get("event") + recallTotalDenominator.get("country")
				+ recallTotalDenominator.get("containment") + recallTotalDenominator.get("disease")
				+ recallTotalDenominator.get("victim");
		int precisionNumerator = precisionTotalNumerator.get("status") + precisionTotalNumerator.get("date")
				+ precisionTotalNumerator.get("event") + precisionTotalNumerator.get("country")
				+ precisionTotalNumerator.get("containment") + precisionTotalNumerator.get("disease")
				+ precisionTotalNumerator.get("victim");
		int precisionDenominator = precisionTotalDenominator.get("status") + precisionTotalDenominator.get("date")
				+ precisionTotalDenominator.get("event") + precisionTotalDenominator.get("country")
				+ precisionTotalDenominator.get("containment") + precisionTotalDenominator.get("disease")
				+ precisionTotalDenominator.get("victim");

		System.out.format("%-20s%-30s%-30s%-30s", "TOTAL",
				dec.format((double) recallNumerator / (double) recallDenominator) + " (" + recallNumerator + "/"
						+ recallDenominator + ")",
				dec.format((double) precisionNumerator / (double) precisionDenominator) + " (" + precisionNumerator
						+ "/" + precisionDenominator + ")",
				dec.format(getFScore((double) recallNumerator / (double) recallDenominator,
						(double) precisionNumerator / (double) precisionDenominator)));
		System.out.println();
	}

	public static double getFScore(Double r, Double p) {
		return 2 * ((r * p) / (r + p));
	}

	public static void appendTotals(HashMap<String, String> recall, HashMap<String, String> precision,
			HashMap<String, String> f1) {
		recallTotalNumerator.put("status", recallTotalNumerator.get("status")
				+ Integer.parseInt(recall.get("status").split(" ")[1].substring(1, 2)));
		recallTotalNumerator.put("event", recallTotalNumerator.get("event")
				+ Integer.parseInt(recall.get("event").split(" ")[1].substring(1, 2)));
		recallTotalNumerator.put("country", recallTotalNumerator.get("country")
				+ Integer.parseInt(recall.get("country").split(" ")[1].substring(1, 2)));
		recallTotalNumerator.put("containment", recallTotalNumerator.get("containment")
				+ Integer.parseInt(recall.get("containment").split(" ")[1].substring(1, 2)));
		recallTotalNumerator.put("disease", recallTotalNumerator.get("disease")
				+ Integer.parseInt(recall.get("disease").split(" ")[1].substring(1, 2)));
		recallTotalNumerator.put("victim", recallTotalNumerator.get("victim")
				+ Integer.parseInt(recall.get("victim").split(" ")[1].substring(1, 2)));
		recallTotalNumerator.put("date",
				recallTotalNumerator.get("date") + Integer.parseInt(recall.get("date").split(" ")[1].substring(1, 2)));

		recallTotalDenominator.put("status", recallTotalDenominator.get("status")
				+ Integer.parseInt(recall.get("status").split(" ")[1].substring(3, 4)));
		recallTotalDenominator.put("event", recallTotalDenominator.get("event")
				+ Integer.parseInt(recall.get("event").split(" ")[1].substring(3, 4)));
		recallTotalDenominator.put("country", recallTotalDenominator.get("country")
				+ Integer.parseInt(recall.get("country").split(" ")[1].substring(3, 4)));
		recallTotalDenominator.put("containment", recallTotalDenominator.get("containment")
				+ Integer.parseInt(recall.get("containment").split(" ")[1].substring(3, 4)));
		recallTotalDenominator.put("disease", recallTotalDenominator.get("disease")
				+ Integer.parseInt(recall.get("disease").split(" ")[1].substring(3, 4)));
		recallTotalDenominator.put("victim", recallTotalDenominator.get("victim")
				+ Integer.parseInt(recall.get("victim").split(" ")[1].substring(3, 4)));
		recallTotalDenominator.put("date", recallTotalDenominator.get("date")
				+ Integer.parseInt(recall.get("date").split(" ")[1].substring(3, 4)));

		precisionTotalNumerator.put("status", precisionTotalNumerator.get("status")
				+ Integer.parseInt(precision.get("status").split(" ")[1].substring(1, 2)));
		precisionTotalNumerator.put("event", precisionTotalNumerator.get("event")
				+ Integer.parseInt(precision.get("event").split(" ")[1].substring(1, 2)));
		precisionTotalNumerator.put("country", precisionTotalNumerator.get("country")
				+ Integer.parseInt(precision.get("country").split(" ")[1].substring(1, 2)));
		precisionTotalNumerator.put("containment", precisionTotalNumerator.get("containment")
				+ Integer.parseInt(precision.get("containment").split(" ")[1].substring(1, 2)));
		precisionTotalNumerator.put("disease", precisionTotalNumerator.get("disease")
				+ Integer.parseInt(precision.get("disease").split(" ")[1].substring(1, 2)));
		precisionTotalNumerator.put("victim", precisionTotalNumerator.get("victim")
				+ Integer.parseInt(precision.get("victim").split(" ")[1].substring(1, 2)));
		precisionTotalNumerator.put("date", precisionTotalNumerator.get("date")
				+ Integer.parseInt(precision.get("date").split(" ")[1].substring(1, 2)));

		precisionTotalDenominator.put("status", precisionTotalDenominator.get("status")
				+ Integer.parseInt(precision.get("status").split(" ")[1].substring(3, 4)));
		precisionTotalDenominator.put("event", precisionTotalDenominator.get("event")
				+ Integer.parseInt(precision.get("event").split(" ")[1].substring(3, 4)));
		precisionTotalDenominator.put("country", precisionTotalDenominator.get("country")
				+ Integer.parseInt(precision.get("country").split(" ")[1].substring(3, 4)));
		precisionTotalDenominator.put("containment", precisionTotalDenominator.get("containment")
				+ Integer.parseInt(precision.get("containment").split(" ")[1].substring(3, 4)));
		precisionTotalDenominator.put("disease", precisionTotalDenominator.get("disease")
				+ Integer.parseInt(precision.get("disease").split(" ")[1].substring(3, 4)));
		precisionTotalDenominator.put("victim", precisionTotalDenominator.get("victim")
				+ Integer.parseInt(precision.get("victim").split(" ")[1].substring(3, 4)));
		precisionTotalDenominator.put("date", precisionTotalDenominator.get("date")
				+ Integer.parseInt(precision.get("date").split(" ")[1].substring(3, 4)));

		List<Double> s = f1Total.get("status");
		s.add(Double.parseDouble(f1.get("status")));
		f1Total.put("status", s);

		List<Double> e = f1Total.get("event");
		e.add(Double.parseDouble(f1.get("event")));
		f1Total.put("event", e);

		List<Double> coun = f1Total.get("country");
		coun.add(Double.parseDouble(f1.get("country")));
		f1Total.put("country", e);

		List<Double> cont = f1Total.get("containment");
		cont.add(Double.parseDouble(f1.get("containment")));
		f1Total.put("containment", cont);

		List<Double> d = f1Total.get("date");
		d.add(Double.parseDouble(f1.get("date")));
		f1Total.put("date", d);

		List<Double> dis = f1Total.get("disease");
		dis.add(Double.parseDouble(f1.get("disease")));
		f1Total.put("disease", dis);

		List<Double> vic = f1Total.get("victim");
		vic.add(Double.parseDouble(f1.get("victim")));
		f1Total.put("victim", vic);

	}

	public static void instantiateTotals() {
		recallTotalNumerator.put("status", 0);
		recallTotalNumerator.put("event", 0);
		recallTotalNumerator.put("date", 0);
		recallTotalNumerator.put("country", 0);
		recallTotalNumerator.put("containment", 0);
		recallTotalNumerator.put("disease", 0);
		recallTotalNumerator.put("victim", 0);

		recallTotalDenominator.put("status", 0);
		recallTotalDenominator.put("event", 0);
		recallTotalDenominator.put("date", 0);
		recallTotalDenominator.put("country", 0);
		recallTotalDenominator.put("containment", 0);
		recallTotalDenominator.put("disease", 0);
		recallTotalDenominator.put("victim", 0);

		precisionTotalNumerator.put("status", 0);
		precisionTotalNumerator.put("event", 0);
		precisionTotalNumerator.put("date", 0);
		precisionTotalNumerator.put("country", 0);
		precisionTotalNumerator.put("containment", 0);
		precisionTotalNumerator.put("disease", 0);
		precisionTotalNumerator.put("victim", 0);

		precisionTotalDenominator.put("status", 0);
		precisionTotalDenominator.put("event", 0);
		precisionTotalDenominator.put("date", 0);
		precisionTotalDenominator.put("country", 0);
		precisionTotalDenominator.put("containment", 0);
		precisionTotalDenominator.put("disease", 0);
		precisionTotalDenominator.put("victim", 0);

		f1Total.put("status", new ArrayList<Double>());
		f1Total.put("event", new ArrayList<Double>());
		f1Total.put("date", new ArrayList<Double>());
		f1Total.put("country", new ArrayList<Double>());
		f1Total.put("containment", new ArrayList<Double>());
		f1Total.put("disease", new ArrayList<Double>());
		f1Total.put("victim", new ArrayList<Double>());

	}

	public static void printEvaluation(String story, String id, HashMap<String, String> recall,
			HashMap<String, String> precision, HashMap<String, String> f1) {
		System.out.println("Story: " + story);
		System.out.format("%-20s%-30s%-30s%-30s", "Label", "RECALL", "PRECISION", "F-MEASURE");
		System.out.println();
		System.out.format("%-20s%-30s%-30s%-30s", "Status:", recall.get("status"), precision.get("status"),
				f1.get("status"));
		System.out.println();
		System.out.format("%-20s%-30s%-30s%-30s", "Date:", recall.get("date"), precision.get("date"), f1.get("date"));
		System.out.println();
		System.out.format("%-20s%-30s%-30s%-30s", "Event:", recall.get("event"), precision.get("event"),
				f1.get("event"));
		System.out.println();
		System.out.format("%-20s%-30s%-30s%-30s", "Country:", recall.get("country"), precision.get("country"),
				f1.get("country"));
		System.out.println();
		System.out.format("%-20s%-30s%-30s%-30s", "Containment:", recall.get("containment"),
				precision.get("containment"), f1.get("containment"));
		System.out.println();
		System.out.format("%-20s%-30s%-30s%-30s", "Disease:", recall.get("disease"), precision.get("disease"),
				f1.get("disease"));
		System.out.println();
		System.out.format("%-20s%-30s%-30s%-30s", "Victim:", recall.get("victim"), precision.get("victim"),
				f1.get("victim"));
		System.out.println("\n");
	}

	public static HashMap<String, String> calculateRecall(Article output, Article answer) {
		HashMap<String, String> result = new HashMap<String, String>();
		DecimalFormat dec = new DecimalFormat("#0.00");

		if (output.status.equals(answer.status)) {
			result.put("status", "1.00 (1/1)");
		} else {
			result.put("status", "0.00 (0/1)");
		}

		if (output.country.equals(answer.country)) {
			result.put("country", "1.00 (1/1)");
		} else {
			result.put("country", "0.00 (0/1)");
		}

		if (output.date.equals(answer.date)) {
			result.put("date", "1.00 (1/1)");
		} else {
			result.put("date", "0.00 (0/1)");
		}

		if (output.event.equals(answer.event)) {
			result.put("event", "1.00 (1/1)");
		} else {
			result.put("event", "0.00 (0/1)");
		}

		int containmentTrueCount = answer.containment.size();
		int containmentLabeledCount = 0;
		for (String s : output.containment) {
			if (answer.containment.contains(s)) {
				containmentLabeledCount++;
			}
		}
		double containmentRecall = containmentLabeledCount / containmentTrueCount;
		result.put("containment", dec.format(containmentRecall).toString() + " (" + containmentLabeledCount + "/"
				+ containmentTrueCount + ")");

		int victimTrueCount = answer.victim.size();
		int victimLabeledCount = 0;
		for (String s : output.victim) {
			if (answer.victim.contains(s)) {
				victimLabeledCount++;
			}
		}
		double victimRecall = victimLabeledCount / victimTrueCount;
		result.put("victim",
				dec.format(victimRecall).toString() + " (" + victimLabeledCount + "/" + victimTrueCount + ")");

		int diseaseTrueCount = answer.disease.size();
		int diseaseLabeledCount = 0;
		for (String s : output.disease) {
			if (answer.disease.contains(s)) {
				diseaseLabeledCount++;
			}
		}
		double diseaseRecall = diseaseLabeledCount / diseaseTrueCount;
		result.put("disease",
				dec.format(diseaseRecall).toString() + " (" + diseaseLabeledCount + "/" + diseaseTrueCount + ")");

		return result;
	}

	public static HashMap<String, String> calculatePrecision(Article output, Article answer) {
		HashMap<String, String> result = new HashMap<String, String>();
		DecimalFormat dec = new DecimalFormat("#0.00");

		if (output.status.equals(answer.status)) {
			result.put("status", "1.00 (1/1)");
		} else {
			if (output.status.equals("-----")) {
				result.put("status", "0.00 (0/0)");
			} else {
				result.put("status", "0.00 (0/1)");
			}
		}

		if (output.country.equals(answer.country)) {
			result.put("country", "1.00 (1/1)");
		} else {
			if (output.country.equals("-----")) {
				result.put("country", "0.00 (0/0)");
			} else {
				result.put("country", "0.00 (0/1)");
			}
		}

		if (output.date.equals(answer.date)) {
			result.put("date", "1.00 (1/1)");
		} else {
			if (output.date.equals("-----")) {
				result.put("date", "0.00 (0/0)");
			} else {
				result.put("date", "0.00 (0/1)");
			}
		}

		if (output.event.equals(answer.event)) {
			result.put("event", "1.00 (1/1)");
		} else {
			if (output.event.equals("-----")) {
				result.put("event", "0.00 (0/0)");
			} else {
				result.put("event", "0.00 (0/1)");
			}
		}

		int containmentLabeledCount = 0;
		int containmentCorrectlyLabeledCount = 0;
		for (String s : output.containment) {
			if (answer.containment.contains(s)) {
				containmentCorrectlyLabeledCount++;
			}
			containmentLabeledCount++;
		}
		double containmentPrecision = 0;
		if (containmentLabeledCount > 0) {
			containmentPrecision = containmentCorrectlyLabeledCount / containmentLabeledCount;
		}
		result.put("containment", dec.format(containmentPrecision).toString() + " (" + containmentCorrectlyLabeledCount
				+ "/" + containmentLabeledCount + ")");

		int victimLabeledCount = 0;
		int victimCorrectlyLabeledCount = 0;
		for (String s : output.victim) {
			if (answer.victim.contains(s)) {
				victimCorrectlyLabeledCount++;
			}
			victimLabeledCount++;
		}
		double victimPrecision = 0;
		if (victimLabeledCount > 0) {
			victimPrecision = victimCorrectlyLabeledCount / victimLabeledCount;
		}
		result.put("victim", dec.format(victimPrecision).toString() + " (" + victimCorrectlyLabeledCount + "/"
				+ victimLabeledCount + ")");

		int diseaseLabeledCount = 0;
		int diseaseCorrectlyLabeledCount = 0;
		for (String s : output.disease) {
			if (answer.disease.contains(s)) {
				diseaseCorrectlyLabeledCount++;
			}
			diseaseLabeledCount++;
		}
		double diseasePrecision = 0;
		if (diseaseLabeledCount > 0) {
			diseasePrecision = diseaseCorrectlyLabeledCount / diseaseLabeledCount;
		}
		result.put("disease", dec.format(diseasePrecision).toString() + " (" + diseaseCorrectlyLabeledCount + "/"
				+ diseaseLabeledCount + ")");

		return result;
	}

	public static HashMap<String, String> calculateF1(Article output, Article answer) {
		DecimalFormat dec = new DecimalFormat("#0.00");
		HashMap<String, String> recall = calculateRecall(output, answer);
		HashMap<String, String> precision = calculatePrecision(output, answer);
		HashMap<String, String> result = new HashMap<String, String>();
		// System.out.println((recall.get("status").split(" ")[0]));
		// (1/1)

		double statusRecall = Double.parseDouble(recall.get("status").split(" ")[0]);
		double eventRecall = Double.parseDouble(recall.get("event").split(" ")[0]);
		double countryRecall = Double.parseDouble(recall.get("country").split(" ")[0]);
		double dateRecall = Double.parseDouble(recall.get("date").split(" ")[0]);
		double containmentRecall = Double.parseDouble(recall.get("containment").split(" ")[0]);
		double diseaseRecall = Double.parseDouble(recall.get("disease").split(" ")[0]);
		double victimRecall = Double.parseDouble(recall.get("victim").split(" ")[0]);

		double statusPrecision = Double.parseDouble(precision.get("status").split(" ")[0]);
		double eventPrecision = Double.parseDouble(precision.get("event").split(" ")[0]);
		double countryPrecision = Double.parseDouble(precision.get("country").split(" ")[0]);
		double datePrecision = Double.parseDouble(precision.get("date").split(" ")[0]);
		double containmentPrecision = Double.parseDouble(precision.get("containment").split(" ")[0]);
		double diseasePrecision = Double.parseDouble(precision.get("disease").split(" ")[0]);
		double victimPrecision = Double.parseDouble(precision.get("victim").split(" ")[0]);

		if (statusRecall + statusPrecision == 0) {
			result.put("status", "0.00");

		} else {
			result.put("status", dec.format(2 * statusRecall * statusPrecision / (statusRecall + statusPrecision)));
		}

		if (eventRecall + eventPrecision == 0) {
			result.put("event", "0.00");
		} else {
			result.put("event", dec.format(2 * eventRecall * eventPrecision / (eventRecall + eventPrecision)));
		}

		if (dateRecall + datePrecision == 0) {
			result.put("date", "0.00");

		} else {
			result.put("date", dec.format(2 * dateRecall * datePrecision / (dateRecall + datePrecision)));
		}

		if (countryRecall + countryPrecision == 0) {
			result.put("country", "0.00");

		} else {
			result.put("country",
					dec.format(2 * countryRecall * countryPrecision / (countryRecall + countryPrecision)));
		}

		if (containmentRecall + containmentPrecision == 0) {
			result.put("containment", "0.00");

		} else {
			result.put("containment", dec
					.format(2 * containmentRecall * containmentPrecision / (containmentRecall + containmentPrecision)));
		}

		if (diseaseRecall + diseasePrecision == 0) {
			result.put("disease", "0.00");

		} else {
			result.put("disease",
					dec.format(2 * diseaseRecall * diseasePrecision / (diseaseRecall + diseasePrecision)));
		}

		if (victimRecall + victimPrecision == 0) {
			result.put("victim", "0.00");

		} else {
			result.put("victim", dec.format(2 * victimRecall * victimPrecision / (victimRecall + victimPrecision)));
		}

		return result;
	}
}
