package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
// import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import edu.illinois.cs.cogcomp.nlp.utilities.SentenceUtils;

public class Util {
	
	public static List<Pair<String, Double>> scoreSetAsSortedList(ScoreSet scoreSet) {
		//Softmax mSoftmax = new Softmax();
        //scoreSet = mSoftmax.normalize(scoreSet);
        Score[] scores = scoreSet.toArray();
        Map<String, Double> probs = new HashMap<String, Double>();
        for(int j=0; j<scores.length; j++) {
        	double score = scores[j].score;
        	String label = scores[j].value;
        	probs.put(label, new Double(score));
        }
        probs = sortByValue(probs);
        return mapToListPair(probs);
	}
	
	public static List<Pair<String, Double>> mapToListPair(Map<String, Double> mymap) {
		List<Pair<String, Double>> mylist = new ArrayList<Pair<String, Double>>();
		for(Iterator<String> it=mymap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Double value = mymap.get(key);
			mylist.add(new Pair<String, Double>(key, value));
		}
		return mylist;
	}
	
	public static Map sortByValue(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});
		// logger.info(list);
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry)it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public static void sortConstituents(List<Constituent> constituents) {

		Collections.sort(constituents, new Comparator<Constituent>() {

			public int compare(Constituent o1, Constituent o2) {
				if (o1.getStartSpan() > o2.getStartSpan()) {
					return 1;			// o1 is greater
				} else {
					if (o1.getStartSpan() == o2.getStartSpan()) {
						if (o1.getEndSpan() > o2.getEndSpan())
							return 1;	// o1 is greater
						else
							return 0;	// 
					} else
						return -1;
				}
			}
		});
	}
	
	
	
	public static String cleanLine(String line) {
		String s = line;
	
		s = s.replaceAll("\\t+", " ");
		s = s.replaceAll("__", "_");
		s = s.replaceAll(" _ ", " ");
		s = s.replaceAll("_ ", " ");
		s = s.replaceAll(" _", " ");
		s = s.replaceAll("//", " ");
		s = s.replaceAll("\\s\\s+", " ");
	
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&apos;", "'");
		s = s.replaceAll("&quot;", "\"");
		s = s.replaceAll("&amp;", "&");
		
		return s;
	}
	
	public static double calculateF1(int c, int r, int p) {
		double recall = c;
		recall = recall/r;
		double precision = c;
		precision = precision/p;
		double f1 = (2*precision*recall)/(precision+recall);
		return f1;
	}
	
	public static double calculateF1(double c, double r, double p) {
		double recall = c/r;
		double precision = c/p;
		double f1 = (2*precision*recall)/(precision+recall);
		return f1;
	}
	
	public static double logNatural(double s) {
        if(s==0) {
        	return Math.log(Float.MIN_VALUE);
        }
        else {
        	return Math.log(s);
        }
	}

    // Copied over from an older implementation at edu.illinois.cs.cogcomp.edison.data.curator.TokenAligner
    public static class TokenAligner {
        /**
         * For every token, map it to the index of its starting character in the raw
         * text
         *
         * @param ta
         * @return
         */
        private static int[] getTokensToCharMapping(TextAnnotation ta) {
            int[] mapping = new int[ta.size()];

            int charId = 0;

            while (Character.isWhitespace(ta.getText().charAt(charId))) {
                mapping[charId++] = 0;
            }

            for (int tokenId = 0; tokenId < ta.size(); tokenId++) {
                int start = charId;
                String token = ta.getToken(tokenId);

                token = SentenceUtils.makeSentencePresentable(token);

                // move to the start and record the starting position
                while (ta.getText().charAt(charId) != token.charAt(0)) {
                    charId++;
                }
                start = charId;

                for (int tokenCharId = 0; tokenCharId < token.length(); tokenCharId++) {
                    charId++;
                    if (charId >= ta.getText().length())
                        break;
                }
                mapping[tokenId] = start;
            }
            return mapping;
        }

        /**
         * Get a list of character offsets for constituents. This function can be
         * used as a building block for converting Edison objects to the Curator
         * format.
         *
         * @param ta
         *            The {@code TextAnnotation}, which contains the reference raw
         *            text
         * @param constituents
         *            The constituents whose character offsets are required
         * @return A list of spans (pairs of integers). Each element of the list is
         *         the character span of the corresponding constituent of the input
         *         list of constituents
         */
        public static List<Pair<Integer, Integer>> getCharacterOffsets(
                TextAnnotation ta, List<Constituent> constituents) {
            int[] tokenToChars = getTokensToCharMapping(ta);

            List<Pair<Integer, Integer>> offsets = new ArrayList<Pair<Integer, Integer>>();

            for (Constituent constituent : constituents) {
                int startToken = constituent.getStartSpan();
                int endToken = constituent.getEndSpan() - 1;

                int endTokenLength = SentenceUtils.makeSentencePresentable(
                        ta.getToken(endToken)).length();

                offsets.add(new Pair<Integer, Integer>(tokenToChars[startToken],
                        tokenToChars[endToken] + endTokenLength));
            }

            return offsets;
        }
    }

}