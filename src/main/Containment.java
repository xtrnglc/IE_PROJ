package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;
import net.sf.extjwnl.data.list.PointerTargetTreeNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

public class Containment {
	HashMap<String, ArrayList<String>> containment = new HashMap<String, ArrayList<String>>();

	public Containment() {
		try {
			createMapping(POS.NOUN, "vaccine");
			createMapping(POS.NOUN, "cull");
			createMapping(POS.VERB, "quarantine");
			createMapping(POS.NOUN, "pesticide");
			createMapping(POS.VERB, "disinfect");
			createMapping(POS.NOUN, "inspection");
			createMapping(POS.NOUN, "closure");
			createMapping(POS.NOUN, "surveillance");
			createMapping(POS.VERB, "notify");


		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}

	public void createMapping(POS pos, String label) throws JWNLException {
		Dictionary d = Dictionary.getDefaultResourceInstance();
		ArrayList<String> synonyms = new ArrayList<String>();

		IndexWord indexWord = d.getIndexWord(pos, label);
		PointerTargetTree hyponyms = PointerUtils.getHyponymTree(indexWord.getSenses().get(0));
		for (Word word : hyponyms.getRootNode().getSynset().getWords()) {
			synonyms.add(word.getLemma());
			// System.out.println(word.getLemma());
		}
		PointerTargetTreeNodeList treeList = hyponyms.getRootNode().getChildTreeList();
		for (PointerTargetTreeNode node : treeList) {
			for (Word word : node.getSynset().getWords()) {
				synonyms.add(word.getLemma());
				// System.out.println(word.getLemma());
			}
		}

		if (label.equals("cull")) {
			label = "culling";
		}
		if (label.equals("disinfect")) {
			label = "disinfecting";
		}
		if (label.equals("closure")) {
			label = "facility closure";
		}
		if (label.equals("surveillance") || label.equals("notify")) {
			label = "other";
		}
		containment.put(label, synonyms);
	}

	public HashSet<String> getContainment(String text) {
		HashSet<String> labels = new HashSet<String>();

		for (Entry<String, ArrayList<String>> entry : containment.entrySet()) {
			for (String synonym : entry.getValue()) {
				if (text.contains(synonym)) {
					labels.add(entry.getKey());
				}
			}
		}

		if (labels.isEmpty()) {
			labels.add("-----");
		}

		return labels;
	}
}
