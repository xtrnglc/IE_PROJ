package main;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

public class ScoringProgram {
	public ScoringProgram() {
	}

	public void evaluate(HashMap<String, Article> outputs, HashMap<String, Article> answers) {

		// RECALL PRECISION F-MEASURE
		// Incident 1.00 (1/1) 1.00 (1/1) 1.00
		// Weapons 0.00 (0/0) 0.00 (0/1) 0.00
		// Perp_Ind 0.00 (0/1) 0.00 (0/0) 0.00
		// Perp_Org 0.00 (0/0) 0.00 (0/0) 0.00
		// Targets 0.00 (0/0) 0.00 (0/0) 0.00
		// Victims 0.00 (0/1) 0.00 (0/2) 0.00
		// -------- -------------- -------------- ----
		// TOTAL 0.33 (1/3) 0.25 (1/4) 0.29
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
		for (Entry<String, Article> s : outputs.entrySet()) {
			Article output = s.getValue();
			Article answer = answers.get(s.getKey());

			HashMap<String, String> recall = calculateRecall(output, answer);
			HashMap<String, String> precision = calculatePrecision(output, answer);
			HashMap<String, String> f1 = calculateF1(output, answer);
			printEvaluation(output.story, output.id, recall, precision, f1);
		}
	}

	public static void printEvaluation(String story, String id, HashMap<String, String> recall,
			HashMap<String, String> precision, HashMap<String, String> f1) {
		System.out.println("Story: " + story);
		System.out.format("%25s%20s%20s", "RECALL", "PRECISION", "F-MEASURE");
		System.out.println();
		System.out.format("%1s%22s%17s%14s", "Status:", recall.get("status"), precision.get("status"),
				f1.get("status"));
		System.out.println();
		System.out.format("%1s%24s%17s%14s", "Date:", recall.get("date"), precision.get("date"), f1.get("date"));
		System.out.println();
		System.out.format("%1s%23s%17s%14s", "Event:", recall.get("event"), precision.get("event"), f1.get("event"));
		System.out.println();
		System.out.format("%1s%21s%17s%14s", "Country:", recall.get("country"), precision.get("country"),
				f1.get("country"));
		System.out.println();
		System.out.format("%1s%17s%17s%14s", "Containment:", recall.get("containment"), precision.get("containment"),
				f1.get("containment"));
		System.out.println();
		System.out.format("%1s%21s%17s%14s", "Disease:", recall.get("disease"), precision.get("disease"),
				f1.get("disease"));
		System.out.println();
		System.out.format("%1s%22s%17s%14s", "Victim:", recall.get("victim"), precision.get("victim"),
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
		//System.out.println((recall.get("status").split(" ")[0]));
		//(1/1)
		
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
		
		if(statusRecall + statusPrecision == 0){
			result.put("status", "0.00");

		} else {
			result.put("status", dec.format(2 * statusRecall * statusPrecision / (statusRecall + statusPrecision)));
		}
		
		if(eventRecall + eventPrecision == 0){
			result.put("event", "0.00");

		} else {
			result.put("event", dec.format(2 * eventRecall * eventPrecision / (eventRecall + eventPrecision)));
		}
		
		if(dateRecall + datePrecision == 0){
			result.put("date", "0.00");

		} else {
			result.put("date", dec.format(2 * dateRecall * datePrecision / (dateRecall + datePrecision)));
		}
		
		if(countryRecall + countryPrecision == 0){
			result.put("country", "0.00");

		} else {
			result.put("country", dec.format(2 * countryRecall * countryPrecision / (countryRecall + countryPrecision)));
		}
		
		if(containmentRecall + containmentPrecision == 0){
			result.put("containment", "0.00");

		} else {
			result.put("containment", dec.format(2 * containmentRecall * containmentPrecision / (containmentRecall + containmentPrecision)));
		}
		
		if(diseaseRecall + diseasePrecision == 0){
			result.put("disease", "0.00");

		} else {
			result.put("disease", dec.format(2 * diseaseRecall * diseasePrecision / (diseaseRecall + diseasePrecision)));
		}
		
		if(victimRecall + victimPrecision == 0){
			result.put("victim", "0.00");

		} else {
			result.put("victim", dec.format(2 * victimRecall * victimPrecision / (victimRecall + victimPrecision)));
		}
		

		return result;
	}
}
