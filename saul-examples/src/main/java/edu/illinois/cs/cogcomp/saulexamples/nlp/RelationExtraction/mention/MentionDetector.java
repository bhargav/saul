package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.mention;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Constants;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.Document;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.MyCuratorClient;

import java.util.HashSet;

public class MentionDetector {

	protected static MyCuratorClient curator = MyCuratorClient.getInstance();

	public static void labelDocMentionCandidates(Document doc) {
		curator.addPhraseChunkView(doc.ta, Constants.CHUNK_PHRASE);
		curator.addPhraseParseView(doc.ta, Constants.PARSE_PHRASE);
		curator.addMentionView(doc.ta);
	}

	public static void labelMentionCandidates(Document doc) {
		labelDocMentionCandidates(doc);
	}

	public static Document labelMentionCandidates(String plainText) throws Exception {
		TextAnnotation ta = null;

		try {
            HashSet<String> requiredViews = new HashSet<>();
            requiredViews.add(ViewNames.PARSE_STANFORD);
            requiredViews.add(ViewNames.DEPENDENCY_STANFORD);
            requiredViews.add(ViewNames.POS);
            requiredViews.add(ViewNames.SHALLOW_PARSE);
            requiredViews.add(ViewNames.NER_CONLL);

			ta = curator.getTextAnnotation("", "", plainText, requiredViews);

			// Now, the ta has SENTENCE view, the tokens and MsTokens in AbstractTextAnnotation are also set
			System.out.println("MentionDetector.labelMentionCandidates: Number of sentences = " + ta.getNumberOfSentences());
			for(int i=0; i<ta.getNumberOfSentences(); i++) {
				System.out.println(ta.getSentence(i));
			}
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to annotate the following text with TextAnnotation:");
			System.out.println(plainText);
			System.exit(1);
		}
		Document doc = new Document(ta);
		labelMentionCandidates(doc);
		return doc;
	}
}
