package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;

import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;


import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Constants;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.ListManager;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.wordClusterManager;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Mention;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.SemanticRelation;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.mention.MentionUtil;


public class RelationFeatures {
	private static ListManager listManager = new ListManager();
	private static wordClusterManager bc = new wordClusterManager();
	
	public static String BinaryLabel(SemanticRelation eg) {
		return eg.getBinaryLabel();
	}
	public static String CoarseLabel(SemanticRelation eg) {
		return eg.getCoarseLabel();
	}
	public static String CoarseLabelUndirected(SemanticRelation eg) {
		return eg.getCoarseUnLabel();
	}
	public static String FineLabel(SemanticRelation eg) {
        return eg.getFineLabel();
	}
	
	public static boolean WordBetweenNull(SemanticRelation eg) {
		boolean wordBetweenNull = true;
	
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if(i1!=-1 && i2!=-1) {
			if((i1+1)<i2) {
				wordBetweenNull = false;
			}
		}
		return wordBetweenNull;
	}
	public static boolean M1IncludesM2(SemanticRelation eg) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m1.getStartTokenOffset()<=m2.getStartTokenOffset() && m2.getEndTokenOffset()<=m1.getEndTokenOffset()) {
			return true;
		}
		else {
			return false;
		}
	}
	public static boolean M2IncludesM1(SemanticRelation eg) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m2.getStartTokenOffset()<=m1.getStartTokenOffset() && m1.getEndTokenOffset()<=m2.getEndTokenOffset()) {
			return true;
		}
		else {
			return false;
		}
	}

	// is there a chunk phrase between the two mentions? any type of base chunk phrase
	public static boolean CpInBetweenNull(SemanticRelation eg) {
		SpanLabelView chunkView = (SpanLabelView) eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.SHALLOW_PARSE);
		Pair<Mention, Mention> args = MentionUtil.orderArgs(eg.getM1(), eg.getM2());
		if(args!=null) {
			Mention arg1 = args.getFirst();
			Mention arg2 = args.getSecond();
			//int arg1HeadStartTokenOffset = arg1.getHeadStartTokenOffset();
			int arg1HeadEndTokenOffset = arg1.getHeadEndTokenOffset()-1;
			int arg2HeadStartTokenOffset = arg2.getHeadStartTokenOffset();
			//int arg2HeadEndTokenOffset = arg2.getHeadEndTokenOffset()-1;
			List<Constituent> chunkConsInSpan = chunkView.getConstituentsCoveringSpan((arg1HeadEndTokenOffset+1), (arg2HeadStartTokenOffset));
			for(int i=0; i<chunkConsInSpan.size(); i++) {
				Constituent con = chunkConsInSpan.get(i);
				if( (arg1HeadEndTokenOffset<con.getStartSpan()) && ((con.getEndSpan()-1)<arg2HeadStartTokenOffset) ) {
					return true;
				}
			}
			return false;
		}
		else {		// m1, m2 are overlapping
			return false;
		}
	}
	
	// the types of base chunks in between the two mention head words
	public static String[] BagOfChunkTypesInBetween(SemanticRelation eg) {
		SpanLabelView chunkView = (SpanLabelView) eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.SHALLOW_PARSE);
		Pair<Mention, Mention> args = MentionUtil.orderArgs(eg.getM1(), eg.getM2());
		if(args!=null) {
			Mention arg1 = args.getFirst();
			Mention arg2 = args.getSecond();
			//int arg1HeadStartTokenOffset = arg1.getHeadStartTokenOffset();
			int arg1HeadEndTokenOffset = arg1.getHeadEndTokenOffset()-1;
			int arg2HeadStartTokenOffset = arg2.getHeadStartTokenOffset();
			//int arg2HeadEndTokenOffset = arg2.getHeadEndTokenOffset()-1;
			List<Constituent> chunkConsInSpan = chunkView.getConstituentsCoveringSpan((arg1HeadEndTokenOffset+1), (arg2HeadStartTokenOffset));
			HashSet<String> chunkTypesSet = new HashSet<String>();
			for(int i=0; i<chunkConsInSpan.size(); i++) {
				Constituent con = chunkConsInSpan.get(i);
				if( (arg1HeadEndTokenOffset<con.getStartSpan()) && ((con.getEndSpan()-1)<arg2HeadStartTokenOffset) ) {
					chunkTypesSet.add(con.getLabel());
				}
			}
			if(chunkTypesSet.size()>0) {
				String[] vList = new String[chunkTypesSet.size()];
				int i=0;
				for(Iterator<String> it=chunkTypesSet.iterator(); it.hasNext();) { 
					vList[i++] = it.next(); 
				}
				return vList;
			}
			else {
				return null;
			}
		}
		else {		// m1, m2 are overlapping
			return null;
		}
	}
	
/*	
	public static boolean NumberOfMentionsInBetweenZero(RelationExample e) {
		boolean v = false;
		if(e.getNumberOfMentionsInBetween()!=null) {
			if(e.getNumberOfMentionsInBetween().intValue()==0) {
				v = true;
			}
		}
		return v;
	}
	public static boolean NumberOfMentionsInBetweenOne(RelationExample e) {
		boolean v = false;
		if(e.getNumberOfMentionsInBetween()!=null) {
			if(e.getNumberOfMentionsInBetween().intValue()==1) {
				v = true;
			}
		}
		return v;
	}
	public static boolean NumberOfMentionsInBetweenTwo(RelationExample e) {
		boolean v = false;
		if(e.getNumberOfMentionsInBetween()!=null) {
			if(e.getNumberOfMentionsInBetween().intValue()==2) {
				v = true;
			}
		}
		return v;
	}
	public static boolean NumberOfMentionsInBetweenThree(RelationExample e) {
		boolean v = false;
		if(e.getNumberOfMentionsInBetween()!=null) {
			if(e.getNumberOfMentionsInBetween().intValue()==3) {
				v = true;
			}
		}
		return v;
	}
	public static boolean NumberOfMentionsInBetweenFourAndMore(RelationExample e) {
		boolean v = false;
		if(e.getNumberOfMentionsInBetween()!=null) {
			if(e.getNumberOfMentionsInBetween().intValue()>=4) {
				v = true;
			}
		}
		return v;
	}
	
	
	public static double NumberOfWordsInBetween(RelationExample e) {
		double v = -1;
		if(e.getNumberOfWordsInBetween()!=null) {
			//v = (double)e.getNumberOfWordsInBetween().intValue();
			// have to divide by maxNumberOfWordsInBetween
		}
		return v;
	}
*/

	
	public static String[] BowM1(SemanticRelation eg) {
		String[] vList = null;
		HashSet<String> mySet = new HashSet<String>();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		int i1 = eg.getM1().getConstituent().getStartSpan();
		int i2 = eg.getM1().getConstituent().getEndSpan()-1;
		for(int i=i1; i<i2; i++) {
			mySet.add(docTokens[i].toLowerCase());
		}
		
		vList = new String[mySet.size()];
		int i=0;
		for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
			vList[i++] = it.next(); 
		}

		return vList;
	}
	public static String[] BowM2(SemanticRelation eg) {
		String[] vList = null;
		HashSet<String> mySet = new HashSet<String>();
		String[] docTokens = eg.getM2().getConstituent().getTextAnnotation().getTokens();
		
		int i1 = eg.getM2().getConstituent().getStartSpan();
		int i2 = eg.getM2().getConstituent().getEndSpan()-1;
		for(int i=i1; i<i2; i++) {
			mySet.add(docTokens[i].toLowerCase());
		}
		
		vList = new String[mySet.size()];
		int i=0;
		for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
			vList[i++] = it.next(); 
		}

		return vList;
	}
	
	public static String HwM1(SemanticRelation eg) {
		return eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase();
	}	
	public static String HwM2(SemanticRelation eg) {
		return eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM2().getHeadTokenOffset()).toLowerCase();
	}
	
	public static String LhwM1(SemanticRelation eg) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		if(headIndex > 0) {
			TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
			return new String(ta.getToken(headIndex - 1) + " " + ta.getToken(headIndex)).toLowerCase();
		}
		else { return null; }
	}
	public static String HwM1R(SemanticRelation eg) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
		if((headIndex+1) < ta.getTokens().length) {
			return new String(ta.getToken(headIndex) + " " + ta.getToken(headIndex + 1)).toLowerCase();
		}
		else { return null; }
	}
	public static String LhwM2(SemanticRelation eg) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		if(headIndex > 0) {
			TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
			return new String(ta.getToken(headIndex - 1) + " " + ta.getToken(headIndex)).toLowerCase();
		}
		else { return null; }
	}
	public static String HwM2R(SemanticRelation eg) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
		if((headIndex+1) < ta.getTokens().length) {
			return new String(ta.getToken(headIndex) + " " + ta.getToken(headIndex + 1)).toLowerCase();
		}
		else { return null; }
	}

	public static String LLhwM1(SemanticRelation eg) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		if(headIndex > 1) {
			TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
			return new String(ta.getToken(headIndex - 2) + " " + ta.getToken(headIndex - 1) + " " + ta.getToken(headIndex)).toLowerCase();
		}
		else { return null; }
	}
	public static String LhwM1R(SemanticRelation eg) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
		if((headIndex > 0) && ((headIndex+1) < ta.getTokens().length)) {
			return new String(ta.getToken(headIndex - 1) + " " + ta.getToken(headIndex) + " " + ta.getToken(headIndex + 1)).toLowerCase();
		}
		else { return null; }
	}
	public static String HwM1RR(SemanticRelation eg) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
		if((headIndex+2) < ta.getTokens().length) {
			return new String(ta.getToken(headIndex) + " " + ta.getToken(headIndex + 1) + " " + ta.getToken(headIndex + 2)).toLowerCase();
		}
		else { return null; }
	}
	public static String LLhwM2(SemanticRelation eg) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		if(headIndex > 1) {
			TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
			return new String(ta.getToken(headIndex - 2) + " " + ta.getToken(headIndex - 1) + " " + ta.getToken(headIndex)).toLowerCase();
		}
		else { return null; }
	}
	public static String LhwM2R(SemanticRelation eg) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
		if((headIndex > 0) && ((headIndex+1) < ta.getTokens().length)) {
			return new String(ta.getToken(headIndex - 1) + " " + ta.getToken(headIndex) + " " + ta.getToken(headIndex + 1)).toLowerCase();
		}
		else { return null; }
	}
	public static String HwM2RR(SemanticRelation eg) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
		if((headIndex+2) < ta.getTokens().length) {
			return new String(ta.getToken(headIndex) + " " + ta.getToken(headIndex + 1) + " " + ta.getToken(headIndex + 2)).toLowerCase();
		}
		else { return null; }
	}

/*
	public static String WM1aWM2(RelationExample e) {
		return e.getWM1aWM2();
	}

	public static String M1WaM2W(RelationExample e) {
		return e.getM1WaM2W();
	}
	
	public static String WWM1aWWM2(RelationExample e) {
		return e.getWWM1aWWM2();
	}
	
	public static String WM1WaWM2W(RelationExample e) {
		return e.getWM1WaWM2W();
	}
	
	public static String M1WWaM2WW(RelationExample e) {
		return e.getM1WWaM2WW();
	}
*/
	
	// pos of (word before m1 headword) + m1 headword + pos of (word before m2 headword) + m2 headword
	public static String PM1aPM2(SemanticRelation eg) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset-1)>=0 && (m2HeadTokenOffset-1)>=0) {
			s.append(posCons.get(m1HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(docTokens[m1HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(docTokens[m2HeadTokenOffset].toLowerCase());
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String M1PaM2P(SemanticRelation eg) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset+1)<posCons.size() && (m2HeadTokenOffset+1)<posCons.size()) {
			s.append(docTokens[m1HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(docTokens[m2HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+1).getLabel());
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String PPM1aPPM2(SemanticRelation eg) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset-2)>=0 && (m2HeadTokenOffset-2)>=0) {
			s.append(posCons.get(m1HeadTokenOffset-2).getLabel());
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(docTokens[m1HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-2).getLabel());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(docTokens[m2HeadTokenOffset].toLowerCase());
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String PM1PaPM2P(SemanticRelation eg) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if(	(m1HeadTokenOffset-1)>=0 && (m2HeadTokenOffset-1)>=0 &&	
			(m1HeadTokenOffset+1)<posCons.size() && (m2HeadTokenOffset+1)<posCons.size()) {
			s.append(posCons.get(m1HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(docTokens[m1HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(docTokens[m2HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+1).getLabel());
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String M1PPaM2PP(SemanticRelation eg) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset+2)<posCons.size() && (m2HeadTokenOffset+2)<posCons.size()) {
			s.append(docTokens[m1HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+2).getLabel());
			s.append(" ");
			s.append(docTokens[m2HeadTokenOffset].toLowerCase());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+2).getLabel());
			return s.toString();
		}
		else {
			return null;
		}
	}

	// pos tag of m1 headword
	public static String PofM1Hw(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		return posCons.get(eg.getM1().getHeadStartTokenOffset()).getLabel();
	}
	public static String PofM2Hw(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM2().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		return posCons.get(eg.getM2().getHeadStartTokenOffset()).getLabel();
	}
	
	// pos tag of the single word between m1 headspan, m2 headspan
	public static String PosBetweenSingle(SemanticRelation eg) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i1+2)==i2) {
			SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
			List<Constituent> posCons = posView.getConstituents();
			return posCons.get(i1+1).getLabel();
		}
		else { return null; }
	}
	
	
	
	// pos tag before m1 headspan
	public static String PbeforeM1Head(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		
		int i = eg.getM1().getHeadStartTokenOffset();
		if((i-1)>=0) {
			return posCons.get(i-1).getLabel();
		}
		else {
			return null;
		}
	}
	
	public static String PafterM1Head(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		
		int i = eg.getM1().getHeadEndTokenOffset()-1;
		if((i+1)<posCons.size()) {
			return posCons.get(i+1).getLabel();
		}
		else {
			return null;
		}
	}
	
	public static String PbeforeM2Head(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM2().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		
		int i = eg.getM2().getHeadStartTokenOffset();
		if((i-1)>=0) {
			return posCons.get(i-1).getLabel();
		}
		else {
			return null;
		}
	}
	
	public static String PafterM2Head(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM2().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		
		int i = eg.getM2().getHeadEndTokenOffset()-1;
		if((i+1)<posCons.size()) {
			return posCons.get(i+1).getLabel();
		}
		else {
			return null;
		}
	}
	
/*	
	public static String WM1mainTypeaWM2mainType(RelationExample e) {
		return e.getWM1mainTypeaWM2mainType();
	}
	
	public static String M1mainTypeWaM2mainTypeW(RelationExample e) {
		return e.getM1mainTypeWaM2mainTypeW();
	}
	
	public static String WWM1mainTypeaWWM2mainType(RelationExample e) {
		return e.getWWM1mainTypeaWWM2mainType();
	}
	
	public static String WM1mainTypeWaWM2mainTypeW(RelationExample e) {
		return e.getWM1mainTypeWaWM2mainTypeW();
	}
	
	public static String M1mainTypeWWaM2mainTypeWW(RelationExample e) {
		return e.getM1mainTypeWWaM2mainTypeWW();
	}
	
	public static String PM1mainTypeaPM2mainType(RelationExample e) {
		return e.getPM1mainTypeaPM2mainType();
	}
	
	public static String M1mainTypePaM2mainTypeP(RelationExample e) {
		return e.getM1mainTypePaM2mainTypeP();
	}
	
	public static String PPM1mainTypeaPPM2mainType(RelationExample e) {
		return e.getPPM1mainTypeaPPM2mainType();
	}
	
	public static String PM1mainTypePaPM2mainTypeP(RelationExample e) {
		return e.getPM1mainTypePaPM2mainTypeP();
	}
	
	public static String M1mainTypePPaM2mainTypePP(RelationExample e) {
		return e.getM1mainTypePPaM2mainTypePP();
	}
	
	// ==========
	public static String PhwM1(RelationExample e) {
		return e.getPhwM1();
	}
	
	public static String HwM1P(RelationExample e) {
		return e.getHwM1P();
	}
	
	public static String PPhwM1(RelationExample e) {
		return e.getPPhwM1();
	}
	
	public static String PhwM1P(RelationExample e) {
		return e.getPhwM1P();
	}
	
	public static String HwM1PP(RelationExample e) {
		return e.getHwM1PP();
	}
	
	public static String PhwM2(RelationExample e) {
		return e.getPhwM2();
	}
	
	public static String HwM2P(RelationExample e) {
		return e.getHwM2P();
	}
	
	public static String PPhwM2(RelationExample e) {
		return e.getPPhwM2();
	}
	
	public static String PhwM2P(RelationExample e) {
		return e.getPhwM2P();
	}
	
	public static String HwM2PP(RelationExample e) {
		return e.getHwM2PP();
	}
	// ==============
*/
	
	public static String HwM1M2(SemanticRelation eg) {
		String m1Hw = eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset());
		String m2Hw = eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM2().getHeadTokenOffset());
		return new String(m1Hw + " " + m2Hw).toLowerCase();
	}
	
	public static String WordBetweenSingle(SemanticRelation eg) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i1+2)==i2) {
			return m1.getConstituent().getTextAnnotation().getToken(i1+1).toLowerCase();
		}
		else { return null; }
	}
	
	public static String WordBetweenFirst(SemanticRelation eg) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i2-i1)>2) {
			return m1.getConstituent().getTextAnnotation().getToken(i1+1).toLowerCase();
		}
		else { return null; }
	}

	public static String WordBetweenLast(SemanticRelation eg) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i2-i1)>2) {
			return m1.getConstituent().getTextAnnotation().getToken(i2-1).toLowerCase();
		}
		else { return null; }
	}
	
	public static String[] WordBetweenBow(SemanticRelation eg) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i2-i1)>2) {
			HashSet<String> mySet = new HashSet<String>();
			TextAnnotation ta = m1.getConstituent().getTextAnnotation();
			int i;
			for(i=(i1+1); i<i2; i++) {
				mySet.add(ta.getToken(i).toLowerCase());
			}
			String[] vList = new String[mySet.size()];
			i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
			return vList;
		}
		else { return null; }
	}
/*	
	public static String BeforeM1First(RelationExample e) {
		return e.getBeforeM1First();
	}
	
	public static String BeforeM1Second(RelationExample e) {
		return e.getBeforeM1Second();
	}
	
	public static String AfterM2First(RelationExample e) {
		return e.getAfterM2First();
	}
	
	public static String AfterM2Second(RelationExample e) {
		return e.getAfterM2Second();
	}
*/
	
	public static String[] BigramsInBetween(SemanticRelation eg) {
		String[] vList = null;
		HashSet<String> bigrams = new HashSet<String>();
		TextAnnotation ta=null;
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if(i1!=-1 && i2!=-1) {
			ta = eg.getSentence().getSentenceConstituent().getTextAnnotation();
			for(int i=(i1+1); (i+1)<i2; i++) {
				bigrams.add(new String(ta.getToken(i)+" "+ta.getToken(i+1)).toLowerCase());
			}
		}
		
		if(bigrams.size() > 0) {
			vList = new String[bigrams.size()];
			int i=0;
			for(Iterator<String> it=bigrams.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		/*
		if(vList!=null) {
			System.out.println("RelationFeatures.BigramsInBetween");
			System.out.println(ta.getTokenizedText());
			System.out.println("m1="+m1);
			System.out.println(" m2="+m2);
			for(int i=0; i<vList.length; i++) {
				System.out.print("["+vList[i]+"]");
			}
			System.out.println("");
		}
		*/
		return vList;
	}
	
	public static String M1MostConfidentMainType(SemanticRelation eg) {
		return eg.getM1().getSC();
	}
	
	public static String M2MostConfidentMainType(SemanticRelation eg) {
		return eg.getM2().getSC();
	}
	
	public static String M1M2MostConfidentMainType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getSC());
		s.append(" ");
		s.append(eg.getM2().getSC());
		return s.toString();
	}
	
	public static String M1MostConfidentSubType(SemanticRelation eg) {
		return eg.getM1().getFineSC();
	}
	
	public static String M2MostConfidentSubType(SemanticRelation eg) {
		return eg.getM2().getFineSC();
	}
	
	public static String M1M2MostConfidentSubType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getFineSC());
		s.append(" ");
		s.append(eg.getM2().getFineSC());
		return s.toString();
	}

	public static String M1M2MentionLevel(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getMentionLevel());
		s.append(" ");
		s.append(eg.getM2().getMentionLevel());
		return s.toString();
	}

	public static String M1LevelMainType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getMentionLevel());
		s.append(" ");
		s.append(eg.getM1().getSC());
		return s.toString();
	}
	
	public static String M2LevelMainType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM2().getMentionLevel());
		s.append(" ");
		s.append(eg.getM2().getSC());
		return s.toString();
	}
	
	public static String M1LevelMainTypeAndm2LevelMainType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getMentionLevel());
		s.append(" ");
		s.append(eg.getM1().getSC());
		s.append(" ");
		s.append(eg.getM2().getMentionLevel());
		s.append(" ");
		s.append(eg.getM2().getSC());
		return s.toString();
	}
	
	public static String M1LevelSubType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getMentionLevel());
		s.append(" ");
		s.append(eg.getM1().getFineSC());
		return s.toString();
	}
	
	public static String M2LevelSubType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM2().getMentionLevel());
		s.append(" ");
		s.append(eg.getM2().getFineSC());
		return s.toString();
	}
	
	public static String M1LevelSubTypeAndm2LevelSubType(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getMentionLevel());
		s.append(" ");
		s.append(eg.getM1().getFineSC());
		s.append(" ");
		s.append(eg.getM2().getMentionLevel());
		s.append(" ");
		s.append(eg.getM2().getFineSC());
		return s.toString();
	}
/*	
	public static String HwM1m2MainType(RelationExample e) {
		return e.getHwM1m2MainType();
	}
	public static String HwM1m2SubType(RelationExample e) {
		return e.getHwM1m2SubType();
	}
	public static String HwM1m2LevelMainType(RelationExample e) {
		return e.getHwM1m2LevelMainType();
	}
	public static String HwM1m2LevelSubType(RelationExample e) {
		return e.getHwM1m2LevelSubType();
	}
	public static String HwM2m1MainType(RelationExample e) {
		return e.getHwM2m1MainType();
	}
	public static String HwM2m1SubType(RelationExample e) {
		return e.getHwM2m1SubType();
	}
	public static String HwM2m1LevelMainType(RelationExample e) {
		return e.getHwM2m1LevelMainType();
	}
	public static String HwM2m1LevelSubType(RelationExample e) {
		return e.getHwM2m1LevelSubType();
	}
	
	public static String M1m2MainType_m1IncludesM2(RelationExample e) {
		return e.getM1m2MainType_m1IncludesM2();
	}
	public static String M1m2MainType_m2IncludesM1(RelationExample e) {
		return e.getM1m2MainType_m2IncludesM1();
	}
*/
	
	public static String M1MainType_m1IncludesM2(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m1.getStartTokenOffset()<=m2.getStartTokenOffset() && m2.getEndTokenOffset()<=m1.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	public static String M2MainType_m1IncludesM2(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM2().getSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m1.getStartTokenOffset()<=m2.getStartTokenOffset() && m2.getEndTokenOffset()<=m1.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M1m2MainType_m1IncludesM2(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getSC());
		s.append(" ");
		s.append(eg.getM2().getSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m1.getStartTokenOffset()<=m2.getStartTokenOffset() && m2.getEndTokenOffset()<=m1.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M1MainType_m2IncludesM1(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m2.getStartTokenOffset()<=m1.getStartTokenOffset() && m1.getEndTokenOffset()<=m2.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	public static String M2MainType_m2IncludesM1(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM2().getSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m2.getStartTokenOffset()<=m1.getStartTokenOffset() && m1.getEndTokenOffset()<=m2.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	public static String M1m2MainType_m2IncludesM1(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getSC());
		s.append(" ");
		s.append(eg.getM2().getSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m2.getStartTokenOffset()<=m1.getStartTokenOffset() && m1.getEndTokenOffset()<=m2.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M1SubType_m1IncludesM2(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getFineSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m1.getStartTokenOffset()<=m2.getStartTokenOffset() && m2.getEndTokenOffset()<=m1.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M2SubType_m1IncludesM2(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM2().getFineSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m1.getStartTokenOffset()<=m2.getStartTokenOffset() && m2.getEndTokenOffset()<=m1.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M1m2SubType_m1IncludesM2(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getFineSC());
		s.append(" ");
		s.append(eg.getM2().getFineSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m1.getStartTokenOffset()<=m2.getStartTokenOffset() && m2.getEndTokenOffset()<=m1.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M1SubType_m2IncludesM1(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getFineSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m2.getStartTokenOffset()<=m1.getStartTokenOffset() && m1.getEndTokenOffset()<=m2.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M2SubType_m2IncludesM1(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM2().getFineSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m2.getStartTokenOffset()<=m1.getStartTokenOffset() && m1.getEndTokenOffset()<=m2.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
	
	public static String M1m2SubType_m2IncludesM1(SemanticRelation eg) {
		StringBuilder s = new StringBuilder();
		s.append(eg.getM1().getFineSC());
		s.append(" ");
		s.append(eg.getM2().getFineSC());
		
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		if(m2.getStartTokenOffset()<=m1.getStartTokenOffset() && m1.getEndTokenOffset()<=m2.getEndTokenOffset()) {
			s.append(" TRUE");
		}
		else {
			s.append(" FALSE");
		}
		
		return s.toString();
	}
/*	
	public static String HwM1M2_m1IncludesM2(RelationExample e) {
		return e.getHwM1M2_m1IncludesM2();
	}
	public static String HwM1M2_m2IncludesM1(RelationExample e) {
		return e.getHwM1M2_m2IncludesM1();
	}
	
	public static String M1EntityTypeAndDepParentWord(RelationExample e) {
		return e.getM1EntityTypeAndDepParentWord();
	}
	public static String M2EntityTypeAndDepParentWord(RelationExample e) {
		return e.getM2EntityTypeAndDepParentWord();
	}
*/	
	public static String M1HeadWordAndDepParentWord(SemanticRelation eg) {
		String result = null;
		
		String m1Hw = eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset());
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());

		if(c1Cons.size()==1) {
			List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
			if(pathToRoot.size() > 1) {
				result = new String(m1Hw+" "+pathToRoot.get(1)).toLowerCase();
			}
		}
		
		return result;
	}	
	public static String M2HeadWordAndDepParentWord(SemanticRelation eg) {
		String result = null;
		
		String m2Hw = eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM2().getHeadTokenOffset());
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());

		if(c1Cons.size()==1) {
			List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
			if(pathToRoot.size() > 1) {
				result = new String(m2Hw+" "+pathToRoot.get(1)).toLowerCase();
			}
		}
		
		return result;
	}
	
	public static String M1DepLabel(SemanticRelation eg) {
		String m1DepLabel = null;
		
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> m1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());
		if(m1Cons!=null && m1Cons.size()==1) {
			Constituent con = m1Cons.get(0);
			List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(con, 40);
			if(pathToRoot!=null && pathToRoot.size()>0) {
				m1DepLabel = pathToRoot.get(0).getLabel();
			}
		}
		return m1DepLabel;
	}
	
	public static String M2DepLabel(SemanticRelation eg) {
		String m2DepLabel = null;
		
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> m2Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());
		if(m2Cons!=null && m2Cons.size()==1) {
			Constituent con = m2Cons.get(0);
			List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(con, 40);
			if(pathToRoot!=null && pathToRoot.size()>0) {
				m2DepLabel = pathToRoot.get(0).getLabel();
			}
		}
		return m2DepLabel;
	}
	
/*	
	public static String M1HeadWordAndDepParentPos(RelationExample e) {
		return e.getM1HeadWordAndDepParentPos();
	}
	public static String M2HeadWordAndDepParentPos(RelationExample e) {
		return e.getM2HeadWordAndDepParentPos();
	}
	
	public static String M1HeadWordAndEntityType(RelationExample e) {
		return e.getM1HeadWordAndEntityType();
	}
	public static String M2HeadWordAndEntityType(RelationExample e) {
		return e.getM2HeadWordAndEntityType();
	}
	
	public static String M1HeadPosAndDepParentWord(RelationExample e) {
		return e.getM1HeadPosAndDepParentWord();
	}
	public static String M2HeadPosAndDepParentWord(RelationExample e) {
		return e.getM2HeadPosAndDepParentWord();
	}
	public static String M1HeadPosAndDepParentPos(RelationExample e) {
		return e.getM1HeadPosAndDepParentPos();
	}
	public static String M2HeadPosAndDepParentPos(RelationExample e) {
		return e.getM2HeadPosAndDepParentPos();
	}
	
	public static String M1EntityTypeAndDepParentLabel(RelationExample e) {
		return e.getM1EntityTypeAndDepParentLabel();
	}
	public static String M2EntityTypeAndDepParentLabel(RelationExample e) {
		return e.getM2EntityTypeAndDepParentLabel();
	}		
	
	public static String M1HeadWordAndDepParentLabel(RelationExample e) {
		return e.getM1HeadWordAndDepParentLabel();
	}
	public static String M2HeadWordAndDepParentLabel(RelationExample e) {
		return e.getM2HeadWordAndDepParentLabel();
	}
	
	
*/	

	private static String pruneDirectionalSymbolsFromDepPath(String path) {
		String[] tokens = path.split(" ");
		StringBuffer s = new StringBuffer();
		for(int i=0; i<tokens.length; i++) {
			if(tokens[i].compareTo("*^*")!=0 && tokens[i].compareTo("*v*")!=0) {
				s.append(tokens[i]);
				s.append(" ");
			}
		}
		return s.toString().trim();
	}
	private static String pruneDirectionalAndRootSymbolsFromDepPath(String path) {
		String[] tokens = path.split(" ");
		StringBuffer s = new StringBuffer();
		for(int i=0; i<tokens.length; i++) {
			if(tokens[i].compareTo("*^*")!=0 && tokens[i].compareTo("*v*")!=0 && tokens[i].compareTo("ROOT")!=0) {
				s.append(tokens[i]);
				s.append(" ");
			}
		}
		return s.toString().trim();
	}
	
	public static String DepPathInBetween(SemanticRelation eg) {
		String depPath = null;
		
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		//System.out.println("RelationFeatures.DepPathInBetween");
		//System.out.println("------- dependency parse tree START ---------");
		//System.out.println(dependencyView);
		//System.out.println("------- dependency parse tree END ---------");
		//System.out.println(eg.getSentence().getSentenceConstituent().getTextAnnotation().getTokenizedText());
		//System.out.println("m1:"+eg.getM1().getSurfaceString()+"|"+eg.getM1().getHeadStartTokenOffset()+","+eg.getM1().getHeadEndTokenOffset()+","+eg.getM1().getHeadTokenOffset());
		//System.out.println("m2:"+eg.getM2().getSurfaceString()+"|"+eg.getM2().getHeadStartTokenOffset()+","+eg.getM2().getHeadEndTokenOffset()+","+eg.getM2().getHeadTokenOffset());
		//System.out.println("m1HeadTokenOffset="+eg.getM1().getHeadTokenOffset()+" m2HeadTokenOffset="+eg.getM2().getHeadTokenOffset());
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());
		List<Constituent> c2Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());
		
		//Constituent c1 = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset()).get(0);
		//System.out.println(eg.getM2());
		//System.out.println(eg.getSentence().getTokenizedText());
	//	System.out.println(dependencyView.getTree(eg.getM2().getSentId()));
		//Constituent c2 = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset()).get(0);
		if(c1Cons.size()==1 && c2Cons.size()==1) {
			//System.out.println("c1Cons:"+c1Cons.size());
			//System.out.println("c2Cons:"+c2Cons.size());
			try {
				depPath = PathFeatureHelper.getDependencyPathString(c1Cons.get(0), c2Cons.get(0), 40);
			} catch(IllegalArgumentException e) {
				System.out.println("RelationFeatures.DepPathInBetween "+e+" m1Id="+eg.getM1().getId()+" m2Id="+eg.getM2().getId());
			}
			//System.out.println(pruneDirectionalSymbolsFromDepPath(depPath));
			//System.out.println(depPath);
			if(depPath!=null) {
				depPath = pruneDirectionalAndRootSymbolsFromDepPath(depPath).toLowerCase();
			}
		}
		return depPath;
		
		/*
		Tree<String> parseTree = ParseHelper.getParseTree(ViewNames.DEPENDENCY_STANFORD, 
				 eg.getSentence().getSentenceConstituent().getTextAnnotation(), eg.getSentenceId());

		System.out.println("RelationFeatures.DepPathInBetween");
		System.out.println(eg.getSentence().getTokenizedText());
		System.out.println("sentence start, end spans "+eg.getSentence().getStartSpan()+","+eg.getSentence().getEndSpan());
		System.out.println("m1:"+eg.getM1().getSurfaceString()+"|"+eg.getM1().getHeadStartTokenOffset()+","+eg.getM1().getHeadEndTokenOffset()+","+eg.getM1().getHeadTokenOffset());
		System.out.println("m2:"+eg.getM2().getSurfaceString()+"|"+eg.getM2().getHeadStartTokenOffset()+","+eg.getM2().getHeadEndTokenOffset()+","+eg.getM2().getHeadTokenOffset());

		System.out.println(parseTree);

		System.out.println("m1HeadTokenOffset="+eg.getM1().getHeadTokenOffset()+" m2HeadTokenOffset="+eg.getM2().getHeadTokenOffset());

		try {
			Pair<List<Tree<String>>, List<Tree<String>>> p =
				ParseHelper.getPath(parseTree.getYield().get(eg.getM1().getHeadTokenOffset()), 
						parseTree.getYield().get(eg.getM2().getHeadTokenOffset()), 
						parseTree, Constants.TREE_MAX_DEPTH);

			System.out.print("start->Common ");
			List<Tree<String>> t1 = p.getFirst();
			for(int i=0; i<t1.size(); i++) {
				//System.out.print(t1.get(i).getLabel()+"|||");
				System.out.println("*"+i+"* "+t1.get(i));
			}
			System.out.print("end->Common ");
			List<Tree<String>> t2 = p.getSecond();
			for(int i=0; i<t2.size(); i++) {
				//System.out.print(t2.get(i).getLabel()+"|||");
				System.out.println("*"+i+"* "+t2.get(i));
			}

		} catch(Exception e) {
			System.out.println(e);
		}
		System.out.println("------------");
		*/

		//return null;
	}

/*
	public static String[] DepTripletsInBetween(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getDepTripletsInBetween();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}

	public static String[] DepTripletsWithPosInBetween(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getDepTripletsWithPosInBetween();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
*/
	
	public static String[] DepLabelsInBetween(SemanticRelation eg) {
		String depPath = null;
		String[] vList = null;
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());
		List<Constituent> c2Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());
		
		if(c1Cons.size()==1 && c2Cons.size()==1) {
			try {
				depPath = PathFeatureHelper.getDependencyPathString(c1Cons.get(0), c2Cons.get(0), 40);
			} catch(IllegalArgumentException e) {
				System.out.println("RelationFeatures.DepPathInBetween "+e+" m1Id="+eg.getM1().getId()+" m2Id="+eg.getM2().getId());
			}
			
			if(depPath!=null) {
				depPath = pruneDirectionalAndRootSymbolsFromDepPath(depPath);
				String[] tokens = depPath.split(" ");
				HashSet<String> mySet = new HashSet<String>();
				for(int i=0; i<tokens.length; i++) {
					mySet.add(tokens[i].toLowerCase());
				}
				vList = new String[mySet.size()];
				int i=0;
				for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
					vList[i++] = it.next(); 
				}
			}
		}
		return vList;
	}

	
	public static String FirstDepLabelInBetween(SemanticRelation eg) {
		String depPath = null;
		String depLabel = null;
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());
		List<Constituent> c2Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());
		
		if(c1Cons.size()==1 && c2Cons.size()==1) {
			try {
				depPath = PathFeatureHelper.getDependencyPathString(c1Cons.get(0), c2Cons.get(0), 40);
			} catch(IllegalArgumentException e) {
				System.out.println("RelationFeatures.DepPathInBetween "+e+" m1Id="+eg.getM1().getId()+" m2Id="+eg.getM2().getId());
			}
			
			if(depPath!=null) {
				depPath = pruneDirectionalAndRootSymbolsFromDepPath(depPath);
				String[] tokens = depPath.split(" ");
				if(tokens.length >= 2) {
					depLabel = tokens[0];
				}
			}
		}
		return depLabel;
	}
	
	public static String LastDepLabelInBetween(SemanticRelation eg) {
		String depPath = null;
		String depLabel = null;
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());
		List<Constituent> c2Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());
		
		if(c1Cons.size()==1 && c2Cons.size()==1) {
			try {
				depPath = PathFeatureHelper.getDependencyPathString(c1Cons.get(0), c2Cons.get(0), 40);
			} catch(IllegalArgumentException e) {
				System.out.println("RelationFeatures.DepPathInBetween "+e+" m1Id="+eg.getM1().getId()+" m2Id="+eg.getM2().getId());
			}
			
			if(depPath!=null) {
				depPath = pruneDirectionalAndRootSymbolsFromDepPath(depPath);
				String[] tokens = depPath.split(" ");
				if(tokens.length >= 2) {
					depLabel = tokens[tokens.length-1];
				}
			}
		}
		return depLabel;
	}
	
/*	
	public static String DepWordPathInBetween(RelationExample e) {
		return e.getDepWordPathInBetween();
	}
	
	public static String DepAndWordPathInBetween(RelationExample e) {
		return e.getDepAndWordPathInBetween();
	}

	public static String SyntacticPathInBetween(RelationExample e) {
		return e.getSyntacticPathInBetween();
	}

	public static String SyntacticRoot(RelationExample e) {
		return e.getSyntacticRoot();
	}

	public static String HwM1DepRootWordHwM2(RelationExample e) {
		return e.getHwM1DepRootWordHwM2();
	}

	public static String M1MainTypeDepRootWordM2MainType(RelationExample e) {
		return e.getM1MainTypeDepRootWordM2MainType();
	}

	public static String M1SubTypeDepRootWordM2SubType(RelationExample e) {
		return e.getM1SubTypeDepRootWordM2SubType();
	}

	public static String HwM1DepRootWordM2MainType(RelationExample e) {
		return e.getHwM1DepRootWordM2MainType();
	}
	public static String HwM1DepRootWordM2SubType(RelationExample e) {
		return e.getHwM1DepRootWordM2SubType();
	}
	
	public static String M1MainTypeDepRootWordHwM2(RelationExample e) {
		return e.getM1MainTypeDepRootWordHwM2();
	}
	public static String M1SubTypeDepRootWordHwM2(RelationExample e) {
		return e.getM1SubTypeDepRootWordHwM2();
	}
	
	public static String CpHeadInBetweenSingle(RelationExample e) {
		return e.getCpHeadInBetweenSingle();
	}

	public static String CpHeadInBetweenFirst(RelationExample e) {
		return e.getCpHeadInBetweenFirst();
	}
	public static String CpHeadInBetweenLast(RelationExample e) {
		return e.getCpHeadInBetweenLast();
	}
	
	public static String[] CpHeadInBetweenBow(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getCpHeadInBetweenBow();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	
	public static String CpSequenceInBetween(RelationExample e) {
		return e.getCpSequenceInBetween();
	}
	
	public static String CpBeforeM1First(RelationExample e) {
		return e.getCpBeforeM1First();
	}
	public static String CpBeforeM1Second(RelationExample e) {
		return e.getCpBeforeM1Second();
	}
	public static String CpAfterM2First(RelationExample e) {
		return e.getCpAfterM2First();
	}
	public static String CpAfterM2Second(RelationExample e) {
		return e.getCpAfterM2Second();
	}
	
	// ==== brown cluster ====
	
	public static String[] BowM1bc4(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBowM1bc4();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] BowM1bc6(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBowM1bc6();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] BowM1bc10(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBowM1bc10();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] BowM2bc4(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBowM2bc4();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] BowM2bc6(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBowM2bc6();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] BowM2bc10(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBowM2bc10();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	
	public static String HwM1bc4(RelationExample e) {
		return e.getHwM1bc4();
	}
	public static String HwM1bc6(RelationExample e) {
		return e.getHwM1bc6();
	}		
	public static String HwM1bc10(RelationExample e) {
		return e.getHwM1bc10();
	}
	public static String HwM2bc4(RelationExample e) {
		return e.getHwM2bc4();
	}
	public static String HwM2bc6(RelationExample e) {
		return e.getHwM2bc6();
	}
	public static String HwM2bc10(RelationExample e) {
		return e.getHwM2bc10();
	}
	
	public static String LhwM1bc4(RelationExample e) {
		return e.getLhwM1bc4();
	}
	public static String LhwM1bc6(RelationExample e) {
		return e.getLhwM1bc6();
	}		
	public static String LhwM1bc10(RelationExample e) {
		return e.getLhwM1bc10();
	}
	public static String HwM1Rbc4(RelationExample e) {
		return e.getHwM1Rbc4();
	}
	public static String HwM1Rbc6(RelationExample e) {
		return e.getHwM1Rbc6();
	}
	public static String HwM1Rbc10(RelationExample e) {
		return e.getHwM1Rbc10();
	}
	
	public static String LhwM2bc4(RelationExample e) {
		return e.getLhwM2bc4();
	}
	public static String LhwM2bc6(RelationExample e) {
		return e.getLhwM2bc6();
	}	
	public static String LhwM2bc10(RelationExample e) {
		return e.getLhwM2bc10();
	}
	public static String HwM2Rbc4(RelationExample e) {
		return e.getHwM2Rbc4();
	}
	public static String HwM2Rbc6(RelationExample e) {
		return e.getHwM2Rbc6();
	}
	public static String HwM2Rbc10(RelationExample e) {
		return e.getHwM2Rbc10();
	}
	
	public static String LLhwM1bc4(RelationExample e) {
		return e.getLLhwM1bc4();
	}
	public static String LLhwM1bc6(RelationExample e) {
		return e.getLLhwM1bc6();
	}		
	public static String LLhwM1bc10(RelationExample e) {
		return e.getLLhwM1bc10();
	}
	
	public static String LhwM1Rbc4(RelationExample e) {
		return e.getLhwM1Rbc4();
	}
	public static String LhwM1Rbc6(RelationExample e) {
		return e.getLhwM1Rbc6();
	}
	public static String LhwM1Rbc10(RelationExample e) {
		return e.getLhwM1Rbc10();
	}
	public static String HwM1RRbc4(RelationExample e) {
		return e.getHwM1RRbc4();
	}
	public static String HwM1RRbc6(RelationExample e) {
		return e.getHwM1RRbc6();
	}
	public static String HwM1RRbc10(RelationExample e) {
		return e.getHwM1RRbc10();
	}
	
	public static String LLhwM2bc4(RelationExample e) {
		return e.getLLhwM2bc4();
	}
	public static String LLhwM2bc6(RelationExample e) {
		return e.getLLhwM2bc6();
	}		
	public static String LLhwM2bc10(RelationExample e) {
		return e.getLLhwM2bc10();
	}
	public static String LhwM2Rbc4(RelationExample e) {
		return e.getLhwM2Rbc4();
	}
	public static String LhwM2Rbc6(RelationExample e) {
		return e.getLhwM2Rbc6();
	}
	public static String LhwM2Rbc10(RelationExample e) {
		return e.getLhwM2Rbc10();
	}
	public static String HwM2RRbc4(RelationExample e) {
		return e.getHwM2RRbc4();
	}
	public static String HwM2RRbc6(RelationExample e) {
		return e.getHwM2RRbc6();
	}
	public static String HwM2RRbc10(RelationExample e) {
		return e.getHwM2RRbc10();
	}
	
	public static String HwM1M2bc4(RelationExample e) {
		return e.getHwM1M2bc4();
	}
	public static String HwM1M2bc6(RelationExample e) {
		return e.getHwM1M2bc6();
	}
	public static String HwM1M2bc10(RelationExample e) {
		return e.getHwM1M2bc10();
	}
	
	public static String WordBetweenSinglebc4(RelationExample e) {
		return e.getWordBetweenSinglebc4();
	}
	public static String WordBetweenSinglebc6(RelationExample e) {
		return e.getWordBetweenSinglebc6();
	}		
	public static String WordBetweenSinglebc10(RelationExample e) {
		return e.getWordBetweenSinglebc10();
	}
	
	public static String WordBetweenFirstbc4(RelationExample e) {
		return e.getWordBetweenFirstbc4();
	}
	public static String WordBetweenFirstbc6(RelationExample e) {
		return e.getWordBetweenFirstbc6();
	}		
	public static String WordBetweenFirstbc10(RelationExample e) {
		return e.getWordBetweenFirstbc10();
	}
	
	public static String WordBetweenLastbc4(RelationExample e) {
		return e.getWordBetweenLastbc4();
	}
	public static String WordBetweenLastbc6(RelationExample e) {
		return e.getWordBetweenLastbc6();
	}		
	public static String WordBetweenLastbc10(RelationExample e) {
		return e.getWordBetweenLastbc10();
	}
	
	public static String[] WordBetweenBowbc4(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getWordBetweenBowbc4();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] WordBetweenBowbc6(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getWordBetweenBowbc6();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}


	public static String[] WordBetweenBowbc10(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getWordBetweenBowbc10();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}

	public static String BeforeM1Firstbc4(RelationExample e) {
		return e.getBeforeM1Firstbc4();
	}
	public static String BeforeM1Firstbc6(RelationExample e) {
		return e.getBeforeM1Firstbc6();
	}		
	public static String BeforeM1Firstbc10(RelationExample e) {
		return e.getBeforeM1Firstbc10();
	}
	
	public static String BeforeM1Secondbc4(RelationExample e) {
		return e.getBeforeM1Secondbc4();
	}
	public static String BeforeM1Secondbc6(RelationExample e) {
		return e.getBeforeM1Secondbc6();
	}
	public static String BeforeM1Secondbc10(RelationExample e) {
		return e.getBeforeM1Secondbc10();
	}
	
	public static String AfterM2Firstbc4(RelationExample e) {
		return e.getAfterM2Firstbc4();
	}
	public static String AfterM2Firstbc6(RelationExample e) {
		return e.getAfterM2Firstbc6();
	}		
	public static String AfterM2Firstbc10(RelationExample e) {
		return e.getAfterM2Firstbc10();
	}
	
	public static String AfterM2Secondbc4(RelationExample e) {
		return e.getAfterM2Secondbc4();
	}
	public static String AfterM2Secondbc6(RelationExample e) {
		return e.getAfterM2Secondbc6();
	}		
	public static String AfterM2Secondbc10(RelationExample e) {
		return e.getAfterM2Secondbc10();
	}
	
	public static String HwM1m2MainTypebc4(RelationExample e) {
		return e.getHwM1m2MainTypebc4();
	}
	public static String HwM1m2MainTypebc6(RelationExample e) {
		return e.getHwM1m2MainTypebc6();
	}		
	public static String HwM1m2MainTypebc10(RelationExample e) {
		return e.getHwM1m2MainTypebc10();
	}
	
	public static String HwM1m2LevelMainTypebc4(RelationExample e) {
		return e.getHwM1m2LevelMainTypebc4();
	}
	public static String HwM1m2LevelMainTypebc6(RelationExample e) {
		return e.getHwM1m2LevelMainTypebc6();
	}	
	public static String HwM1m2LevelMainTypebc10(RelationExample e) {
		return e.getHwM1m2LevelMainTypebc10();
	}
	
	public static String HwM2m1MainTypebc4(RelationExample e) {
		return e.getHwM2m1MainTypebc4();
	}
	public static String HwM2m1MainTypebc6(RelationExample e) {
		return e.getHwM2m1MainTypebc6();
	}	
	public static String HwM2m1MainTypebc10(RelationExample e) {
		return e.getHwM2m1MainTypebc10();
	}
	
	public static String HwM2m1LevelMainTypebc4(RelationExample e) {
		return e.getHwM2m1LevelMainTypebc4();
	}
	public static String HwM2m1LevelMainTypebc6(RelationExample e) {
		return e.getHwM2m1LevelMainTypebc6();
	}	
	public static String HwM2m1LevelMainTypebc10(RelationExample e) {
		return e.getHwM2m1LevelMainTypebc10();
	}
	
	public static String M1HeadWordAndDepParentWordbc4(RelationExample e) {
		return e.getM1HeadWordAndDepParentWordbc4();
	}
	public static String M1HeadWordAndDepParentWordbc6(RelationExample e) {
		return e.getM1HeadWordAndDepParentWordbc6();
	}		
	public static String M1HeadWordAndDepParentWordbc10(RelationExample e) {
		return e.getM1HeadWordAndDepParentWordbc10();
	}
	
	public static String M2HeadWordAndDepParentWordbc4(RelationExample e) {
		return e.getM2HeadWordAndDepParentWordbc4();
	}
	public static String M2HeadWordAndDepParentWordbc6(RelationExample e) {
		return e.getM2HeadWordAndDepParentWordbc6();
	}		
	public static String M2HeadWordAndDepParentWordbc10(RelationExample e) {
		return e.getM2HeadWordAndDepParentWordbc10();
	}
	
	public static String M1MainTypeDepRootWordM2MainTypebc4(RelationExample e) {
		return e.getM1MainTypeDepRootWordM2MainTypebc4();
	}
	public static String M1MainTypeDepRootWordM2MainTypebc6(RelationExample e) {
		return e.getM1MainTypeDepRootWordM2MainTypebc6();
	}	
	public static String M1MainTypeDepRootWordM2MainTypebc10(RelationExample e) {
		return e.getM1MainTypeDepRootWordM2MainTypebc10();
	}
	
	public static String M1SubTypeDepRootWordM2SubTypebc4(RelationExample e) {
		return e.getM1SubTypeDepRootWordM2SubTypebc4();
	}
	public static String M1SubTypeDepRootWordM2SubTypebc6(RelationExample e) {
		return e.getM1SubTypeDepRootWordM2SubTypebc6();
	}
	public static String M1SubTypeDepRootWordM2SubTypebc10(RelationExample e) {
		return e.getM1SubTypeDepRootWordM2SubTypebc10();
	}
	
	public static String[] BigramsInBetweenbc4(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBigramsInBetweenbc4();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] BigramsInBetweenbc6(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBigramsInBetweenbc6();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	public static String[] BigramsInBetweenbc10(RelationExample e) {
		String[] vList = null;
		HashSet<String> mySet = e.getBigramsInBetweenbc10();
		if(mySet!=null) {
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
*/	
	
	public static String[] FeaturesOfFirstPrep(SemanticRelation eg) {
		String[] vList = null;
		List<Set<String>> prepFeatures = eg.getAllPrepFeatures();
		
		if(prepFeatures!=null && prepFeatures.size()>=1) {
			Set<String> mySet = prepFeatures.get(0);
	
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	
	public static String[] FeaturesOfSecondPrep(SemanticRelation eg) {
		String[] vList = null;
		List<Set<String>> prepFeatures = eg.getAllPrepFeatures();
		
		if(prepFeatures!=null && prepFeatures.size()>=2) {
			Set<String> mySet = prepFeatures.get(1);
	
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	
	public static String[] FeaturesOfLastPrep(SemanticRelation eg) {
		String[] vList = null;
		List<Set<String>> prepFeatures = eg.getAllPrepFeatures();
		
		if(prepFeatures!=null && prepFeatures.size()>=3) {
			Set<String> mySet = prepFeatures.get(prepFeatures.size()-1);
	
			vList = new String[mySet.size()];
			int i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
		}
		return vList;
	}
	
	private static List<String> getPrepsInBetween(SemanticRelation eg) {
		List<String> preps = new ArrayList<String>();
		
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
		List<Constituent> posCons = posView.getConstituents();

		Mention arg1=null, arg2=null;
		if((eg.getM1().getHeadEndTokenOffset()-1) < eg.getM2().getHeadStartTokenOffset()) {
			arg1 = eg.getM1();
			arg2 = eg.getM2();

		}
		else if((eg.getM2().getHeadEndTokenOffset()-1) < eg.getM1().getHeadStartTokenOffset()) {
			arg1 = eg.getM2();
			arg2 = eg.getM1();
		}
		
		if(arg1!=null && arg2!=null) {
			int i1 = arg1.getHeadEndTokenOffset();
			int i2 = arg2.getHeadStartTokenOffset()-1;
					
			for(int i=i1; i<=i2; i++) {
				if(posCons.get(i).getLabel().compareTo("IN")==0 || posCons.get(i).getLabel().compareTo("TO")==0) {
					preps.add(docTokens[i].toLowerCase());
				}
			}
		}
		
		return preps;
	}
	
	public static boolean OnePrepInBetween(SemanticRelation eg) {
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()==1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean TwoPrepInBetween(SemanticRelation eg) {
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()==2) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean MoreThanTwoPrepInBetween(SemanticRelation eg) {
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()>2) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static String SinglePrepStringInBetween(SemanticRelation eg) {	
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()==1) {
			return preps.get(0);
		}
		else {
			return null;
		}
	}
	
	public static String FirstPrepStringInBetween(SemanticRelation eg) {	
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()>1) {
			return preps.get(0);
		}
		else {
			return null;
		}
	}
	
	public static String LastPrepStringInBetween(SemanticRelation eg) {	
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()>1) {
			return preps.get(preps.size()-1);
		}
		else {
			return null;
		}
	}
	
	public static boolean M1IsNationality(SemanticRelation eg) {
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		int i1 = eg.getM1().getConstituent().getStartSpan();
		int i2 = eg.getM1().getConstituent().getEndSpan()-1;
		StringBuffer s = new StringBuffer("");
		for(int i=i1; i<i2; i++) {
			s.append(docTokens[i]);
			s.append(" ");
		}
		return listManager.isNationality(s.toString().trim());
	}
	
	public static boolean M2IsNationality(SemanticRelation eg) {
		String[] docTokens = eg.getM2().getConstituent().getTextAnnotation().getTokens();
		int i1 = eg.getM2().getConstituent().getStartSpan();
		int i2 = eg.getM2().getConstituent().getEndSpan()-1;
		StringBuffer s = new StringBuffer("");
		for(int i=i1; i<i2; i++) {
			s.append(docTokens[i]);
			s.append(" ");
		}
		return listManager.isNationality(s.toString().trim());
	}
	
	public static boolean PreModIsPartOfWikiTitle(SemanticRelation eg) {
		return eg.premod_isPartOfWikiTitle();
	}
	
	public static boolean PremodIsWordNetNounCollocation(SemanticRelation eg) {
		return eg.premod_isWordNetNounCollocation();
	}
	
	// check whether the two mentions of the relation are involved in SRL relations (connected via the same SRL predicate)
	// if yes, return the list of pairs of SRL relations
	private static List<Pair<String, Pair<String, String>>> SRLRelations(SemanticRelation eg) {
		List<Pair<String, Pair<String, String>>> srlRelations = new ArrayList<Pair<String, Pair<String, String>>>();
		
		//Constituent m1Con = eg.getM1().getConstituent();
		//Constituent m2Con = eg.getM2().getConstituent();
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		
		PredicateArgumentView srl = (PredicateArgumentView) m1.getConstituent().getTextAnnotation().getView(ViewNames.SRL_VERB);
		List<Constituent> srlPredicates = srl.getPredicates();
		for(Constituent p : srlPredicates) {
			String m1r=null, m2r=null;
			
			List<Relation> pRelations = srl.getArguments(p);
			for(Relation pRel : pRelations) {
				Constituent pArgument = pRel.getTarget();		// SRL argument for this predicate
				if( (pArgument.getStartSpan()<=m1.getHeadTokenOffset()) && (m1.getHeadTokenOffset()<pArgument.getEndSpan()) ) {
					m1r = pRel.getRelationName();
				}
				if( (pArgument.getStartSpan()<=m2.getHeadTokenOffset()) && (m2.getHeadTokenOffset()<pArgument.getEndSpan()) ) {
					m2r = pRel.getRelationName();
				}
			}
			
			if(m1r!=null && m2r!=null && m1r.compareTo(m2r)!=0) {
				srlRelations.add(new Pair<String, Pair<String, String>>(p.getSurfaceForm(), new Pair<String, String>(m1r, m2r)));
			}
		}
		
		return srlRelations;
	}
	
	private static boolean isSameSRLArg(String arg1, String arg2) {
		String a1=null, a2=null;
		
		if(arg1.startsWith("C-") || arg1.startsWith("R-")) 
			a1 = arg1.substring(2);
		else 
			a1 = arg1;
		
		if(arg2.startsWith("C-") || arg2.startsWith("R-")) 
			a2 = arg2.substring(2);
		else 
			a2 = arg2;
		
		if(a1.compareTo(a2)==0) 
			return true;
		else
			return false;
	}
	
	private static boolean isCoreSRLArgument(String arg) {
		if(arg.contains("A0") || arg.contains("A1") || arg.contains("A2") || arg.contains("A3") || arg.contains("A4") || arg.contains("A5"))
			return true;
		else
			return false;
	}
	
	private static boolean isFreqSRLArgument(String arg) {
		return Constants.freqSRLArguments.contains(arg);
	}
	
	public static boolean HasCommonVerbSRLPredicate(SemanticRelation eg) {
		//List<Pair<String, String>> srlRelations = SRLRelations(eg);
		List<Pair<String, Pair<String, String>>> srlRelations = SRLRelations(eg);
		List<Pair<String, String>> filteredSRLRelations = new ArrayList<Pair<String, String>>();
		
		if(srlRelations!=null && srlRelations.size()>0) {
			
			// ignore same-arg
			for(Pair<String, Pair<String, String>> pRel : srlRelations) {
				Pair<String, String> p = pRel.getSecond();
				if(!isSameSRLArg(p.getFirst(), p.getSecond())) {
					//if(isCoreSRLArgument(p.getFirst()) || isCoreSRLArgument(p.getSecond())) {			// b
						//if(isFreqSRLArgument(p.getFirst()) || isFreqSRLArgument(p.getSecond())) {		// a
					
//							if( (p.getFirst().contains("A0") || p.getFirst().contains("A1") || p.getFirst().contains("A2")) &&
//								(p.getSecond().contains("A0") || p.getSecond().contains("A1") || p.getSecond().contains("A2")) &&
//								eg.getLexicalCondition()!=null && eg.getLexicalCondition().compareTo("Verbal")==0 ) {
//								System.out.println("SRL-PREDICATE "+eg.getFineUnLabel()+" "+pRel.getFirst());
//								
//							}
							filteredSRLRelations.add(new Pair<String, String>(p.getFirst(), p.getSecond()));
						//}
					//}
				}
				
			}
		}
		
		if(filteredSRLRelations.size() > 0)
			return true;
		else
			return false;
	}
	
	// =========== BROWN CLUSTER FEATURES BELOW
	
	public static String[] BowM1bc(SemanticRelation eg, int bitNum) {
		String[] vList = null;
		HashSet<String> mySet = new HashSet<String>();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		int i1 = eg.getM1().getConstituent().getStartSpan();
		int i2 = eg.getM1().getConstituent().getEndSpan()-1;
		for(int i=i1; i<i2; i++) {
			mySet.add( bc.getCluster(docTokens[i].toLowerCase(), bitNum) );
		}
		
		vList = new String[mySet.size()];
		int i=0;
		for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
			vList[i++] = it.next(); 
		}

		return vList;
	}
	public static String[] BowM2bc(SemanticRelation eg, int bitNum) {
		String[] vList = null;
		HashSet<String> mySet = new HashSet<String>();
		String[] docTokens = eg.getM2().getConstituent().getTextAnnotation().getTokens();
		
		int i1 = eg.getM2().getConstituent().getStartSpan();
		int i2 = eg.getM2().getConstituent().getEndSpan()-1;
		for(int i=i1; i<i2; i++) {
			mySet.add( bc.getCluster(docTokens[i].toLowerCase(), bitNum) );
		}
		
		vList = new String[mySet.size()];
		int i=0;
		for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
			vList[i++] = it.next(); 
		}

		return vList;
	}
	
	public static String HwM1bc(SemanticRelation eg, int bitNum) {
		return bc.getCluster(eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase(), bitNum);
	}	
	public static String HwM2bc(SemanticRelation eg, int bitNum) {
		return bc.getCluster(eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM2().getHeadTokenOffset()).toLowerCase(), bitNum);
	}
	
	public static String LhwM1bc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		if(headIndex > 0) {
			TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
			return new String(bc.getCluster(ta.getToken(headIndex - 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String HwM1Rbc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
		if((headIndex+1) < ta.getTokens().length) {
			return new String(bc.getCluster(ta.getToken(headIndex), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 1).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String LhwM2bc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		if(headIndex > 0) {
			TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
			return new String(bc.getCluster(ta.getToken(headIndex - 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String HwM2Rbc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
		if((headIndex+1) < ta.getTokens().length) {
			return new String(bc.getCluster(ta.getToken(headIndex), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 1).toLowerCase(), bitNum));
		}
		else { return null; }
	}

	public static String LLhwM1bc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		if(headIndex > 1) {
			TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
			return new String(bc.getCluster(ta.getToken(headIndex - 2), bitNum) + " " + bc.getCluster(ta.getToken(headIndex - 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String LhwM1Rbc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
		if((headIndex > 0) && ((headIndex+1) < ta.getTokens().length)) {
			return new String(bc.getCluster(ta.getToken(headIndex - 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 1).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String HwM1RRbc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM1().getHeadTokenOffset();
		TextAnnotation ta = eg.getM1().getConstituent().getTextAnnotation();
		if((headIndex+2) < ta.getTokens().length) {
			return new String(bc.getCluster(ta.getToken(headIndex), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 2).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String LLhwM2bc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		if(headIndex > 1) {
			TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
			return new String(bc.getCluster(ta.getToken(headIndex - 2), bitNum) + " " + bc.getCluster(ta.getToken(headIndex - 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String LhwM2Rbc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
		if((headIndex > 0) && ((headIndex+1) < ta.getTokens().length)) {
			return new String(bc.getCluster(ta.getToken(headIndex - 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 1).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	public static String HwM2RRbc(SemanticRelation eg, int bitNum) {
		int headIndex = eg.getM2().getHeadTokenOffset();
		TextAnnotation ta = eg.getM2().getConstituent().getTextAnnotation();
		if((headIndex+2) < ta.getTokens().length) {
			return new String(bc.getCluster(ta.getToken(headIndex), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 1), bitNum) + " " + bc.getCluster(ta.getToken(headIndex + 2).toLowerCase(), bitNum));
		}
		else { return null; }
	}
	
	public static String PM1aPM2bc(SemanticRelation eg, int bitNum) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset-1)>=0 && (m2HeadTokenOffset-1)>=0) {
			s.append(posCons.get(m1HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m1HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m2HeadTokenOffset].toLowerCase(), bitNum));
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String M1PaM2Pbc(SemanticRelation eg, int bitNum) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset+1)<posCons.size() && (m2HeadTokenOffset+1)<posCons.size()) {
			s.append(bc.getCluster(docTokens[m1HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m2HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+1).getLabel());
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String PPM1aPPM2bc(SemanticRelation eg, int bitNum) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset-2)>=0 && (m2HeadTokenOffset-2)>=0) {
			s.append(posCons.get(m1HeadTokenOffset-2).getLabel());
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m1HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-2).getLabel());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m2HeadTokenOffset].toLowerCase(), bitNum));
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String PM1PaPM2Pbc(SemanticRelation eg, int bitNum) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if(	(m1HeadTokenOffset-1)>=0 && (m2HeadTokenOffset-1)>=0 &&	
			(m1HeadTokenOffset+1)<posCons.size() && (m2HeadTokenOffset+1)<posCons.size()) {
			s.append(posCons.get(m1HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m1HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset-1).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m2HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+1).getLabel());
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String M1PPaM2PPbc(SemanticRelation eg, int bitNum) {
		int m1HeadTokenOffset = eg.getM1().getHeadTokenOffset();
		int m2HeadTokenOffset = eg.getM2().getHeadTokenOffset();
		
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	
		List<Constituent> posCons = posView.getConstituents();
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();
		
		StringBuffer s = new StringBuffer();
		if((m1HeadTokenOffset+2)<posCons.size() && (m2HeadTokenOffset+2)<posCons.size()) {
			s.append(bc.getCluster(docTokens[m1HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(posCons.get(m1HeadTokenOffset+2).getLabel());
			s.append(" ");
			s.append(bc.getCluster(docTokens[m2HeadTokenOffset].toLowerCase(), bitNum));
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+1).getLabel());
			s.append(" ");
			s.append(posCons.get(m2HeadTokenOffset+2).getLabel());
			return s.toString();
		}
		else {
			return null;
		}
	}
	
	public static String HwM1M2bc(SemanticRelation eg, int bitNum) {
		String m1Hw = eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset());
		String m2Hw = eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM2().getHeadTokenOffset());
		return new String(bc.getCluster(m1Hw.toLowerCase(), bitNum) + " " + bc.getCluster(m2Hw.toLowerCase(), bitNum));
	}
	
	public static String WordBetweenSinglebc(SemanticRelation eg, int bitNum) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i1+2)==i2) {
			return bc.getCluster(m1.getConstituent().getTextAnnotation().getToken(i1+1).toLowerCase(), bitNum);
		}
		else { return null; }
	}
	
	public static String WordBetweenFirstbc(SemanticRelation eg, int bitNum) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i2-i1)>2) {
			return bc.getCluster(m1.getConstituent().getTextAnnotation().getToken(i1+1).toLowerCase(), bitNum);
		}
		else { return null; }
	}

	public static String WordBetweenLastbc(SemanticRelation eg, int bitNum) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i2-i1)>2) {
			return bc.getCluster(m1.getConstituent().getTextAnnotation().getToken(i2-1).toLowerCase(), bitNum);
		}
		else { return null; }
	}
	
	public static String[] WordBetweenBowbc(SemanticRelation eg, int bitNum) {
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		int i1=-1, i2=-1;
		if((m1.getEndTokenOffset()-1) < m2.getStartTokenOffset()) {
			i1 = m1.getEndTokenOffset()-1;
			i2 = m2.getStartTokenOffset();
		}
		else if((m2.getEndTokenOffset()-1) < m1.getStartTokenOffset()) {
			i1 = m2.getEndTokenOffset()-1;
			i2 = m1.getStartTokenOffset();
		}
		if((i2-i1)>2) {
			HashSet<String> mySet = new HashSet<String>();
			TextAnnotation ta = m1.getConstituent().getTextAnnotation();
			int i;
			for(i=(i1+1); i<i2; i++) {
				mySet.add(bc.getCluster(ta.getToken(i).toLowerCase(), bitNum));
			}
			String[] vList = new String[mySet.size()];
			i=0;
			for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
				vList[i++] = it.next(); 
			}
			return vList;
		}
		else { return null; }
	}
	
	public static String M1HeadWordAndDepParentWordbc(SemanticRelation eg, int bitNum) {
		String result = null;
		
		String m1Hw = eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset());
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());

		if(c1Cons.size()==1) {
			List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
			if(pathToRoot.size() > 1) {
				result = new String(bc.getCluster(m1Hw.toLowerCase(), bitNum) + " " + bc.getCluster(pathToRoot.get(1).getSurfaceForm().toLowerCase(), bitNum));
			}
		}
		
		return result;
	}	
	public static String M2HeadWordAndDepParentWordbc(SemanticRelation eg, int bitNum) {
		String result = null;
		
		String m2Hw = eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM2().getHeadTokenOffset());
		TreeView dependencyView = (TreeView)eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
		List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());

		if(c1Cons.size()==1) {
			List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
			if(pathToRoot.size() > 1) {
				result = new String(bc.getCluster(m2Hw.toLowerCase(), bitNum) + " " + bc.getCluster(pathToRoot.get(1).getSurfaceForm().toLowerCase(), bitNum));
			}
		}
		
		return result;
	}
	
	public static String SinglePrepStringInBetweenbc(SemanticRelation eg, int bitNum) {	
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()==1) {
			return bc.getCluster(preps.get(0), bitNum);
		}
		else {
			return null;
		}
	}
	
	public static String FirstPrepStringInBetweenbc(SemanticRelation eg, int bitNum) {	
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()>1) {
			return bc.getCluster(preps.get(0), bitNum);
		}
		else {
			return null;
		}
	}
	
	public static String LastPrepStringInBetweenbc(SemanticRelation eg, int bitNum) {	
		List<String> preps = getPrepsInBetween(eg);
		if(preps.size()>1) {
			return bc.getCluster(preps.get(preps.size()-1), bitNum);
		}
		else {
			return null;
		}
	}
	
	// ========================
	
	public static List<String> M1WikiAttributes(SemanticRelation eg) {
		Set<String> res = new HashSet<String>();
		Constituent c = eg.getM1().getConstituent();
		TextAnnotation ta = c.getTextAnnotation();
		
		SpanLabelView wikiView = (SpanLabelView) ta.getView(ViewNames.WIKIFIER);
		List<Constituent> wiki = (List<Constituent>) wikiView.where(Queries.sameEndSpanAs(c));	
		if(wiki!=null && wiki.size()>0) {
			for(Constituent w : wiki) {
				if(w.getLabel().startsWith("http://en.wikipedia.org/wiki/")) {
					String s = w.getLabel().substring(29);
					Set<String> attributes = listManager.getAttributeForWikiTitle(s);
					if(attributes!=null) {
						for(Iterator<String> attIt=attributes.iterator(); attIt.hasNext();) {
							String attribute = attIt.next();
							res.add("WIKI:" + attribute);
						}
					}
				}
			}
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> M2WikiAttributes(SemanticRelation eg) {
		Set<String> res = new HashSet<String>();
		Constituent c = eg.getM2().getConstituent();
		TextAnnotation ta = c.getTextAnnotation();
		
		SpanLabelView wikiView = (SpanLabelView) ta.getView(ViewNames.WIKIFIER);
		List<Constituent> wiki = (List<Constituent>) wikiView.where(Queries.sameEndSpanAs(c));	
		if(wiki!=null && wiki.size()>0) {
			for(Constituent w : wiki) {
				if(w.getLabel().startsWith("http://en.wikipedia.org/wiki/")) {
					String s = w.getLabel().substring(29);
					Set<String> attributes = listManager.getAttributeForWikiTitle(s);
					if(attributes!=null) {
						for(Iterator<String> attIt=attributes.iterator(); attIt.hasNext();) {
							String attribute = attIt.next();
							res.add("WIKI:" + attribute);
						}
					}
				}
			}
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	////////////
	
	public static boolean HasCoveringMention(SemanticRelation eg) {
		Mention largerMention = MentionUtil.getCoveringMention(eg.getM1(), eg.getM2());
		if(largerMention!=null)
			return true;
		else
			return false;
	}
	
	public static String FrontPosSequence(SemanticRelation eg) {
		String posSequence = null;
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		Mention largerMention=null, smallerMention=null;
		largerMention = MentionUtil.getCoveringMention(m1, m2);
		if(largerMention!=null) {
			if(largerMention==m1) { smallerMention = m2; }
			else if(largerMention==m2) { smallerMention = m1; }
		}		
		
		if(largerMention!=null) {
			String[] docTokens = m1.getConstituent().getTextAnnotation().getTokens();			// get the tokens of this document
			SpanLabelView posView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
			List<Constituent> posCons = posView.getConstituents();
			
			// make use of the POS sequences in frontPOSSequences
			if(largerMention.getStartTokenOffset() < smallerMention.getStartTokenOffset()) {
				List<String> frontPos = new ArrayList<String>();
				List<String> frontTokens = new ArrayList<String>();
				for(int i=largerMention.getStartTokenOffset(); i<smallerMention.getStartTokenOffset(); i++) {
					frontPos.add(posCons.get(i).getLabel());
					frontTokens.add(docTokens[i]);
				}
				StringBuffer frontPosBuffer = new StringBuffer("");
				for(String pos : frontPos) {
					frontPosBuffer.append(pos);
					frontPosBuffer.append(" ");
				}
				posSequence = frontPosBuffer.toString().trim();
			}
			else {
				posSequence = "FRONTPOS:EMPTY";
			}
		}
		else {
			posSequence = "FRONTPOS:NOT_COVERING";
		}
		
		return posSequence;
	}
	
	public static String BackPosSequence(SemanticRelation eg) {
		String posSequence = null;
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		Mention largerMention=null, smallerMention=null;
		largerMention = MentionUtil.getCoveringMention(m1, m2);
		if(largerMention!=null) {
			if(largerMention==m1) { smallerMention = m2; }
			else if(largerMention==m2) { smallerMention = m1; }
		}		
		
		if(largerMention!=null) {
			String[] docTokens = m1.getConstituent().getTextAnnotation().getTokens();			// get the tokens of this document
			SpanLabelView posView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
			List<Constituent> posCons = posView.getConstituents();
		
			// either (1) a mixture of noun and adj ; (2) and/CC followed by a mixture of noun and adj
			if(smallerMention.getEndTokenOffset() < largerMention.getEndTokenOffset()) {
				List<String> backPos = new ArrayList<String>();
				List<String> backTokens = new ArrayList<String>();
				for(int i=smallerMention.getEndTokenOffset(); i<largerMention.getEndTokenOffset(); i++) {
					backPos.add(posCons.get(i).getLabel());
					backTokens.add(docTokens[i]);
				}
				int startIndex=0;
				if(backTokens.get(0).compareTo("and")==0) 
					startIndex = 1;
				boolean foundNounAdj=false, onlyNounAdj=true;
				for(int i=startIndex; i<backPos.size(); i++) {
					if(backPos.get(i).startsWith("NN") || backPos.get(i).startsWith("JJ")) 
						foundNounAdj = true;
					else 
						onlyNounAdj = false;
				}
				if(foundNounAdj==true && onlyNounAdj==true) {
					posSequence = "BACKPOS:NOUN_ADJ";
				}
				else {
					posSequence = "BACKPOS:NOUN_ADJ_OTHERS";
				}
			}
			else {
				posSequence = "BACKPOS:EMPTY";
			}
			
		}
		else {
			posSequence = "BACKPOS:NOT_COVERING";
		}
		
		return posSequence;
	}
	
	public static String SmallerMentionIsPerTitle(SemanticRelation eg) {
		String mentionString = null;
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		Mention largerMention=null, smallerMention=null;
		largerMention = MentionUtil.getCoveringMention(m1, m2);
		if(largerMention!=null) {
			if(largerMention==m1) { smallerMention = m2; }
			else if(largerMention==m2) { smallerMention = m1; }
		}		
		
		if(largerMention!=null) {
			String[] tokens = smallerMention.getSurfaceString().split(" ");
			String smallerMentionString = " " + smallerMention.getSurfaceString().toLowerCase() + " ";
			if(tokens.length > 1) {
				mentionString = "SMALLERMENTION:MULTIPLEWORDS";
			}
			else {
				if(smallerMentionString.contains(" mr. ") || smallerMentionString.contains(" ms. ") || smallerMentionString.contains(" mrs. "))
					mentionString = "SMALLERMENTION:PERTITLE";
				else
					mentionString = "SMALLERMENTION:SINGLEWORD";
			}
		}
		else {
			mentionString = "SMALLERMENTION:NOT_COVERED";
		}
		
		return mentionString;
	}
	
	public static String PosAfterM1(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
		List<Constituent> posCons = posView.getConstituents();
		
		int index = eg.getM1().getEndTokenOffset();
		if(index < posCons.size()) {
			return "POSAFTERM1:"+posCons.get(index).getLabel();
		}
		else {
			return "POSAFTERM1:END";
		}
	}
	
	public static String PosBeforeM1(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
		List<Constituent> posCons = posView.getConstituents();
		
		int index = eg.getM1().getStartTokenOffset()-1;
		if(index >= 0) {
			return "POSBEFOREM1:"+posCons.get(index).getLabel();
		}
		else {
			return "POSBEFOREM1:HEAD";
		}
	}
	
	public static String WordAfterM1(SemanticRelation eg) {
		String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();			// get the tokens of this document
		
		int index = eg.getM1().getEndTokenOffset();
		if(index < docTokens.length) {
			return "WORDAFTERM1:"+docTokens[index].toLowerCase();
		}
		else {
			return "WORDAFTERM1:END";
		}
	}
	
	public static String PosOfLastWordInM1(SemanticRelation eg) {
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
		List<Constituent> posCons = posView.getConstituents();
		
		int index = eg.getM1().getEndTokenOffset()-1;
		return posCons.get(index).getLabel();
	}
	
	public static boolean OnlyPrepInDepPath(SemanticRelation eg) {
		String[] depLabelsInBetween = RelationFeatures.DepLabelsInBetween(eg);
		boolean prepLabelInDepPath = false;
		boolean onlyPrepLabelInDepPath = true;
		if(depLabelsInBetween!=null) {
			for(int i=0; i<depLabelsInBetween.length; i++) {
				if(depLabelsInBetween[i].startsWith(":prep_")) {
					prepLabelInDepPath = true;
				}
				if(!depLabelsInBetween[i].startsWith(":prep_")) {
					onlyPrepLabelInDepPath = false;
				}
			}
		}
		
		if(prepLabelInDepPath==true && onlyPrepLabelInDepPath==true) 
			return true;
		else
			return false;
	}
	
	public static boolean ApposInDepPath(SemanticRelation eg) {
		String depPath = RelationFeatures.DepPathInBetween(eg);
		if(depPath!=null && depPath.contains("appos")) 
			return true;
		else
			return false;
	}
	
	public static String PosOfSingleWordBetweenMentions(SemanticRelation eg) {
		String posTag = null;
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
		List<Constituent> posCons = posView.getConstituents();
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		
		if(MentionUtil.getCoveringMention(m1, m2)==null) {
			if( (m1.getEndTokenOffset()+1)==m2.getStartTokenOffset() ) {
				posTag = "POSOFSINGLEWORD:"+posCons.get(m1.getEndTokenOffset()).getLabel();
			}
			else {
				posTag = "POSOFSINGLEWORD:MULTIWORDS";
			}
		}
		else {
			posTag = "POSOFSINGLEWORD:COVERING";
		}
		
		return posTag;
	}
	
	public static String SingleWordBetweenMentions(SemanticRelation eg) {
		String word = null;
		SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);	// get POS tags of this document
		List<Constituent> posCons = posView.getConstituents();
		Mention m1 = eg.getM1();
		Mention m2 = eg.getM2();
		
		if(MentionUtil.getCoveringMention(m1, m2)==null) {
			if( (m1.getEndTokenOffset()+1)==m2.getStartTokenOffset() ) {
				word = "SINGLEWORD:" + WordHelpers.getWord(m1.getConstituent().getTextAnnotation(), m1.getEndTokenOffset()).toLowerCase();
			}
			else {
				word = "SINGLEWORD:MULTIWORDS";
			}
		}
		else {
			word = "SINGLEWORD:COVERING";
		}
		
		return word;
	}
}