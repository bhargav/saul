package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.mention;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.ListManager;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Mention;

public class MentionUtil {
	private static ListManager listManager = new ListManager();
	
	public static boolean compareConstituents(Constituent a, Constituent b) {
		boolean result = false;
		
		if(a.getStartSpan()==b.getStartSpan() && a.getEndSpan()==b.getEndSpan()) {
			result = true;
		}
		
		return result;
	}
	
	// given two mentions, return the mention (if any) that covers the other mention
	public static Mention getCoveringMention(Mention arg1, Mention arg2) {
		Mention largerMention = null;
		
		if(arg1.getStartTokenOffset()<=arg2.getStartTokenOffset() && arg1.getEndTokenOffset()>=arg2.getEndTokenOffset()) 
			largerMention = arg1;
		else if(arg2.getStartTokenOffset()<=arg1.getStartTokenOffset() && arg2.getEndTokenOffset()>=arg1.getEndTokenOffset()) 
			largerMention = arg2;
		
		return largerMention;
	}
	
	
	public static Pair<Mention, Mention> orderArgs(Mention m1, Mention m2) {
		int m1HeadStartTokenOffset = m1.getHeadStartTokenOffset();
		int m1HeadEndTokenOffset = m1.getHeadEndTokenOffset()-1;
		int m2HeadStartTokenOffset = m2.getHeadStartTokenOffset();
		int m2HeadEndTokenOffset = m2.getHeadEndTokenOffset()-1;
		Mention arg1 = null;
		Mention arg2 = null;
		if(m1HeadEndTokenOffset < m2HeadStartTokenOffset) {
			arg1 = m1;
			arg2 = m2;
		}
		else if(m2HeadEndTokenOffset < m1HeadStartTokenOffset) {
			arg1 = m2;
			arg2 = m1;
		}
		if(arg1!=null && arg2!=null) {
			return new Pair<Mention, Mention>(arg1, arg2);
		}
		else {
			return null;
		}
	}
	
	public static void sortMentionAsc(List<Mention> mentions) throws Exception {
		for(int i=0; i<mentions.size(); i++) {
			Mention m = mentions.get(i);
			if(m.getStartCharOffset()==-1 && m.getEndCharOffset()==-1 && m.getStartTokenOffset()==-1 && m.getEndTokenOffset()==-1) {
				throw new Exception("Char and Token offsets of a mention "+m.getId()+" are all -1");
			}
		}
		
		Collections.sort(mentions, new Comparator<Mention>() {
			public int compare(Mention arg0, Mention arg1) {				
				
				if(arg0.getStartCharOffset()!=-1 && arg0.getEndCharOffset()!=-1) {
					if (arg0.getStartCharOffset() > arg1.getStartCharOffset()) { return 1; }
					else {
						if (arg0.getStartCharOffset() == arg1.getStartCharOffset()) {
							if(arg0.getEndCharOffset() > arg1.getEndCharOffset()) { return 1; }
							else { return 0; }
						}
						else { return -1; }
					}
				}
				else {
					if (arg0.getStartTokenOffset() > arg1.getStartTokenOffset()) { return 1; }
					else {
						if (arg0.getStartTokenOffset() == arg1.getStartTokenOffset()) {
							if(arg0.getEndTokenOffset() > arg1.getEndTokenOffset()) { return 1; }
							else { return 0; }
						}
						else { return -1; }
					}
				}
				
			}
		});
	}
	
	public static int findHeadTokenOffset(Constituent c) {
		int offset = -1;
		String[] docTokens = c.getTextAnnotation().getTokens();			// get the tokens of this document
		SpanLabelView posView = (SpanLabelView) c.getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
		List<Constituent> posCons = posView.getConstituents();
		
		for(int i=(c.getStartSpan()+1); i<c.getEndSpan(); i++) {
			if(posCons.get(i).getLabel().compareTo("IN")==0 || posCons.get(i).getLabel().compareTo("TO")==0) {
				if(posCons.get(i-1).getLabel().startsWith("NN") || posCons.get(i-1).getLabel().startsWith("PRP")) {
					if(docTokens[i].compareTo("of")==0 && listManager.isCollectiveNoun(docTokens[i-1])) {}
					else {
						offset = i-1;
						break;
					}
				}
			}
		}
		
		// else use the last noun in the mention, as the mention head
		if(offset==-1) {
			for(int i=(c.getEndSpan()-1); i>=c.getStartSpan(); i--) {
				if(posCons.get(i).getLabel().startsWith("NN") || posCons.get(i).getLabel().startsWith("PRP")) {
					offset = i;
					break;
				}
			}
		}
		
		// if all else fails, just use last word in the constituent
		if(offset==-1) {
			offset = c.getEndSpan()-1;
		}
		
		return offset;
	}
	
	// check if two constituents overlap
	public static boolean constituentsOverlap(Constituent c1, Constituent c2) {
		int c1Start = c1.getStartSpan();
		int c1End = c1.getEndSpan()-1;
		int c2Start = c2.getStartSpan();
		int c2End = c2.getEndSpan()-1;
		
		if( ((c1Start<=c2Start) && (c2Start<=c1End)) || ((c1Start<=c2End) && (c2End<=c1End)) ) 
			return true;
		else
			return false;
	}
}