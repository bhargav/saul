package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.mention;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.ListManager;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.wordClusterManager;

public final class MentionTypeFeatures {
	public static wordClusterManager wcm = new wordClusterManager();
	private static ListManager listManager = new ListManager();
	
	public static List<String> generateFeatures(Constituent c){
		
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		
		String end_word = WordHelpers.getWord(ta, c.getEndSpan()-1);
		String c_end_word = wcm.getCluster(end_word);
		
		//res.add("ENDWORD" + ":" + end_word);
		
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			String word = WordHelpers.getWord(ta, i);
			res.add("POS:" + pos + ":" + (i - c.getEndSpan()));
			res.add("WORD" + (i - c.getEndSpan())+ ":" + word.toLowerCase());
			res.add("POS-WORD"+ pos + ":" + (i - c.getEndSpan())+ ":" + word.toLowerCase());
			res.add("POS-ENDWORD"+ pos + ":" + (i - c.getEndSpan())+ ":" + end_word.toLowerCase());
						
			String c_word = wcm.getCluster(word);
			res.add("WORD" + (i - c.getEndSpan())+ ":" + c_word);
			res.add("POS-WORD"+ pos + ":" + (i - c.getEndSpan())+ ":" + c_word);
			res.add("POS-ENDWORD"+ pos + ":" + (i - c.getEndSpan())+ ":" + c_end_word);
			
//			//System.out.println(c_word);
//			if (c_word.length() >= 4){
//				String c_short = c_word.substring(0,4);
//				res.add("WORD" + (i - c.getEndSpan())+ ":" + c_short);
//				res.add("POS-WORD"+ pos + ":" + (i - c.getEndSpan())+ ":" + c_short);
//			}
//			
//			if (c_word.length() >= 6){
//				String c_short = c_word.substring(0,6);
//				res.add("WORD" + (i - c.getEndSpan())+ ":" + c_short);
//				res.add("POS-WORD"+ pos + ":" + (i - c.getEndSpan())+ ":" + c_short);
//			}
//			if (c_word.length() >= 10){
//				String c_short = c_word.substring(0,10);
//				res.add("WORD" + (i - c.getEndSpan())+ ":" + c_short);
//				res.add("POS-WORD"+ pos + ":" + (i - c.getEndSpan())+ ":" + c_short);
//			}						

		}
		
						
		//sentence
		Sentence sen = ta.getSentence(ta.getSentenceId(c));
			
		// parse tree
		TreeView tv = (TreeView) ta.getView(ViewNames.PARSE_STANFORD);
		IQueryable<Constituent> tree_c_list = tv.where(Queries.sameSpanAsConstituent(c));
		
		for (Constituent n:tree_c_list){
			res.add("PARSE_EXACT:" + n.getLabel());
		}
		
		tree_c_list = tv.where(Queries.containsConstituent(c));
		
		for (Constituent n:tree_c_list){
			res.add("PARSE_COVER:" + n.getLabel());
		}
				
		// start span
		if (c.getStartSpan() == sen.getStartSpan()){
			res.add("Context-POS-1:HEAD");
			res.add("Context-WORD-1:HEAD");
		}
		else{
			res.add("Context-POS-1:" + WordHelpers.getPOS(ta, c.getStartSpan()-1));
			res.add("Context-WORD-1:" + WordHelpers.getWord(ta, c.getStartSpan()-1).toLowerCase());
		}
		
		// end span
		if (c.getEndSpan() == sen.getEndSpan()){
			res.add("Context-POS+1:HEAD");
			res.add("Context-WORD+1:HEAD");
		}
		else{
			res.add("Context-POS+1:" + WordHelpers.getPOS(ta, c.getEndSpan()));
			res.add("Context-WORD+1:" + WordHelpers.getWord(ta, c.getEndSpan()).toLowerCase());
		}
				
		SpanLabelView nerView = (SpanLabelView) ta.getView(ViewNames.NER);		
		List<Constituent> ner = (List<Constituent>) nerView.where(Queries.sameEndSpanAs(c));	
		for (Constituent n:ner){
			res.add("NER:" + n.getLabel());
		}
		
		SpanLabelView wikiView = (SpanLabelView) ta.getView(ViewNames.WIKIFIER);
		List<Constituent> wiki = (List<Constituent>) wikiView.where(Queries.sameEndSpanAs(c));	
		if(wiki!=null && wiki.size()>0) {
//			res.add("WIKI:1");
			for(Constituent w : wiki) {
				if(w.getLabel().startsWith("http://en.wikipedia.org/wiki/")) {
					String s = w.getLabel().substring(29);
//					res.add("WIKI:"+ s);
					Set<String> attributes = listManager.getAttributeForWikiTitle(s);
					if(attributes!=null) {
						for(Iterator<String> attIt=attributes.iterator(); attIt.hasNext();) {
							String attribute = attIt.next();
							res.add("WIKI:" + attribute);
						}
					}
				}
//				else {
//					res.add("WIKI:"+ w.getLabel());
//				}
			}
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static boolean NoPrep(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		
		int prepCount = 0;
		for(int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			if(pos.compareTo("IN")==0 || pos.compareTo("TO")==0) {
				prepCount += 1;
			}
		}
		
		if(prepCount==0) 
			return true;
		else
			return false;
	}
	
	public static boolean OnePrep(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		
		int prepCount = 0;
		for(int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			if(pos.compareTo("IN")==0 || pos.compareTo("TO")==0) {
				prepCount += 1;
			}
		}
		
		if(prepCount==1) 
			return true;
		else
			return false;
	}
	
	public static boolean TwoPrep(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		
		int prepCount = 0;
		for(int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			if(pos.compareTo("IN")==0 || pos.compareTo("TO")==0) {
				prepCount += 1;
			}
		}
		
		if(prepCount==2) 
			return true;
		else
			return false;
	}
	
	public static boolean MoreThanTwoPrep(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		
		int prepCount = 0;
		for(int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			if(pos.compareTo("IN")==0 || pos.compareTo("TO")==0) {
				prepCount += 1;
			}
		}
		
		if(prepCount>2) 
			return true;
		else
			return false;
	}
	
	public static boolean NoVerb(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		
		int verbCount = 0;
		for(int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			if(pos.startsWith("VB")) 
				verbCount += 1;
		}
		
		if(verbCount==0) 
			return true;
		else
			return false;
	}
	
	public static boolean NoComma(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		
		int commaCount = 0;
		for(int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String w = WordHelpers.getWord(ta, i);
			if(w.compareTo(",")==0)
				commaCount += 1;
		}
		
		if(commaCount==0) 
			return true;
		else
			return false;
	}
	
	public static List<String> PosIndexBag(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			res.add("POS:" + pos + ":" + (i - c.getEndSpan()));
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> WordIndexBag(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
	
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {		
			String word = WordHelpers.getWord(ta, i);	
			res.add("WORD:" + (i - c.getEndSpan())+ ":" + word.toLowerCase());
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> PosWordIndexBag(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
	
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			String word = WordHelpers.getWord(ta, i);	
			res.add("POS-WORD:"+ pos + ":" + (i - c.getEndSpan())+ ":" + word.toLowerCase());
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> PosEndWordIndexBag(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		String end_word = WordHelpers.getWord(ta, c.getEndSpan()-1);
		
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			res.add("POS-ENDWORD:"+ pos + ":" + (i - c.getEndSpan())+ ":" + end_word.toLowerCase());
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> WordBCIndexBag(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String word = WordHelpers.getWord(ta, i);	
			String c_word = wcm.getCluster(word);
			res.add("WORDBC:" + (i - c.getEndSpan())+ ":" + c_word);
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> PosWordBCIndexBag(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
	
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			String word = WordHelpers.getWord(ta, i);					
			String c_word = wcm.getCluster(word);
			res.add("POS-WORDBC:"+ pos + ":" + (i - c.getEndSpan())+ ":" + c_word);
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> PosEndWordBCIndexBag(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		String end_word = WordHelpers.getWord(ta, c.getEndSpan()-1);
		String c_end_word = wcm.getCluster(end_word);
		
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			res.add("POS-ENDWORDBC:"+ pos + ":" + (i - c.getEndSpan())+ ":" + c_end_word);
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> ParseExact(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		TreeView tv = (TreeView) ta.getView(ViewNames.PARSE_STANFORD);
		IQueryable<Constituent> tree_c_list = tv.where(Queries.sameSpanAsConstituent(c));
		
		for (Constituent n:tree_c_list){
			res.add("PARSE_EXACT:" + n.getLabel());
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> ParseCover(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		TreeView tv = (TreeView) ta.getView(ViewNames.PARSE_STANFORD);
		IQueryable<Constituent> tree_c_list = tv.where(Queries.containsConstituent(c));
				
		for (Constituent n:tree_c_list){
			res.add("PARSE_COVER:" + n.getLabel());
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static String ContextLeftWord(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		Sentence sen = ta.getSentence(ta.getSentenceId(c));
		String res = null;
		
		if (c.getStartSpan() == sen.getStartSpan()){
			res = new String("Context-WORD-1:HEAD");
		}
		else{
			res = new String("Context-WORD-1:" + WordHelpers.getWord(ta, c.getStartSpan()-1).toLowerCase());
		}
		return res;
	}
	public static String ContextLeftPos(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		Sentence sen = ta.getSentence(ta.getSentenceId(c));
		String res = null;
		
		if (c.getStartSpan() == sen.getStartSpan()){
			res = new String("Context-POS-1:HEAD");
		}
		else{
			res = new String("Context-POS-1:" + WordHelpers.getPOS(ta, c.getStartSpan()-1));
		}
		return res;
	}
	public static String ContextRightWord(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		Sentence sen = ta.getSentence(ta.getSentenceId(c));
		String res = null;
		
		if (c.getEndSpan() == sen.getEndSpan()){
			res = new String("Context-WORD+1:HEAD");
		}
		else{
			res = new String("Context-WORD+1:" + WordHelpers.getWord(ta, c.getEndSpan()).toLowerCase());
		}
		return res;
	}
	public static String ContextRightPos(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		Sentence sen = ta.getSentence(ta.getSentenceId(c));
		String res = null;
		
		if (c.getEndSpan() == sen.getEndSpan()){
			res = new String("Context-POS+1:HEAD");
		}
		else{
			res = new String("Context-POS+1:" + WordHelpers.getPOS(ta, c.getEndSpan()));
		}
		return res;
	}
	
	public static List<String> NerLabels(Constituent c) {
		Set<String> res = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		SpanLabelView nerView = (SpanLabelView) ta.getView(ViewNames.NER_CONLL);
		List<Constituent> ner = (List<Constituent>) nerView.where(Queries.sameEndSpanAs(c));	
		for (Constituent n:ner){
			res.add("NER:" + n.getLabel());
		}
		
		List<String> r_list = new ArrayList<String>();
		for (String s:res){
			r_list.add(s);
		}
		return r_list;
	}
	
	public static List<String> WikiAttributes(Constituent c) {
		Set<String> res = new HashSet<String>();
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
	
	// surrounding lemmas in -1,0,+1 sentence
	public static String[] SurroundingWords(Constituent c) {
		String[] vList = null;
		Set<String> mySet = new HashSet<String>();
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		int sentId = c.getSentenceId();
		TextAnnotation ta = c.getTextAnnotation();
		
		int startIndex = ta.getSentence(sentId).getStartSpan();
		int endIndex = ta.getSentence(sentId).getEndSpan();
		
		String[] docTokens = ta.getTokens();			// get the tokens of this document
		List<Constituent> posCons = ((SpanLabelView) ta.getView(ViewNames.POS)).getConstituents();	// get POS tags of this document
		
		if(sentId > 0) 
			startIndex = ta.getSentence(sentId-1).getStartSpan();
		if((sentId+1) < ta.getNumberOfSentences()) 
			endIndex = ta.getSentence(sentId+1).getEndSpan();
		
		for(int i=startIndex; i<endIndex; i++) {
			String posTag = posCons.get(i).getLabel();
			if(posTag.startsWith("JJ") || posTag.startsWith("NN") || posTag.startsWith("PRP") || 
			   posTag.startsWith("RB") || posTag.startsWith("VB") || posTag.startsWith("W")) {
				String w = docTokens[i].toLowerCase();
//				if(posTag.startsWith("NN") || posTag.startsWith("VB")) {
//					try {
//						String lemma = WordNetHelper.getLemma(w, posTag);
//						w = lemma;
//					} catch (FileNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (JWNLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				mySet.add(w);
			}
		}
		
		vList = new String[mySet.size()];
		int i=0;
		for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
			vList[i++] = it.next(); 
		}
		
		return vList;
	}
	
	public static String[] Bow(Constituent c) {
		String[] vList = null;
		Set<String> mySet = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			String word = WordHelpers.getWord(ta, i);
			mySet.add(word.toLowerCase());
		}
		
		vList = new String[mySet.size()];
		int i=0;
		for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
			vList[i++] = it.next(); 
		}
		
		return vList;
	}
	
	public static String[] SynOfAllNoun(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		String[] vList = null;
		Set<String> mySet = new HashSet<String>();
		TextAnnotation ta = c.getTextAnnotation();
		
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			String word = WordHelpers.getWord(ta, i);
			if(pos.startsWith("NN")) {
				String[] syns = null;
//				try {
//					syns = WordNetHelper.getSynset(word.toLowerCase(), pos);
//					for(int j=0; j<syns.length; j++) {
//						mySet.add(syns[j]);
//					}
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (JWNLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
		
		vList = new String[mySet.size()];
		int i=0;
		for(Iterator<String> it=mySet.iterator(); it.hasNext();) { 
			vList[i++] = it.next(); 
		}
		
		return vList;
	}
	
	public static String Hw(Constituent c) {
		TextAnnotation ta = c.getTextAnnotation();
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		return WordHelpers.getWord(ta, headTokenOffset);
	}
	
	public static String WordSequence(Constituent c) {
		StringBuffer s = new StringBuffer("");
		TextAnnotation ta = c.getTextAnnotation();
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			//String pos = WordHelpers.getPOS(ta, i);
			String word = WordHelpers.getWord(ta, i);
			s.append(word.toLowerCase());
			s.append(" ");
		}
		return s.toString().trim();
	}
	
	public static String PosSequence(Constituent c) {
		StringBuffer s = new StringBuffer("");
		TextAnnotation ta = c.getTextAnnotation();
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			//String word = WordHelpers.getWord(ta, i);
			s.append(pos);
			s.append(" ");
		}
		return s.toString().trim();
	}
	
	public static String WordAndPosSequence(Constituent c) {
		StringBuffer s = new StringBuffer("");
		TextAnnotation ta = c.getTextAnnotation();
		for (int i=c.getStartSpan(); i<c.getEndSpan(); i++) {
			String pos = WordHelpers.getPOS(ta, i);
			String word = WordHelpers.getWord(ta, i);
			s.append(word.toLowerCase());
			s.append("/");
			s.append(pos);
			s.append(" ");
		}
		return s.toString().trim();
	}
	
	
	public static boolean InPersonList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isPerson(c.getSurfaceForm().trim().toLowerCase()) || listManager.isPerson(hw) ||
			(lwHw!=null && listManager.isPerson(lwHw)) || (lwlwHw!=null && listManager.isPerson(lwlwHw)) ) 
			//listManager.isEthnicGroup(originalHw.toLowerCase()) || listManager.isEthnicGroup(hw) || (lwHw!=null && listManager.isEthnicGroup(lwHw)) || (lwlwHw!=null && listManager.isEthnicGroup(lwlwHw)) ||
			//listManager.isNationality(originalHw.toLowerCase()) || listManager.isNationality(hw) || (lwHw!=null && listManager.isNationality(lwHw)) || (lwlwHw!=null && listManager.isNationality(lwlwHw)) ) 
			return true;
		else
			return false;
	}
	
	public static boolean InPersonTitleList(Constituent c) {
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String lwHw = null;		// word left of HW + HW
		String lwlwHw = null;
		
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isPersonTitle(hw) || (lwHw!=null && listManager.isPersonTitle(lwHw)) || (lwlwHw!=null && listManager.isPersonTitle(lwlwHw)) ) 
			return true;
		else
			return false;
	}

	public static boolean InPersonNameList(Constituent c) {
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String lwHw = null;		// word left of HW + HW
		String lwlwHw = null;
		
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isPersonName(hw) || (lwHw!=null && listManager.isPersonName(lwHw)) || (lwlwHw!=null && listManager.isPersonName(lwlwHw)) ) 
			return true;
		else
			return false;
	}
	
	public static boolean InPersonPronounList(Constituent c) {
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String lwHw = null;		// word left of HW + HW
		String lwlwHw = null;
		
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isPersonPronoun(hw) || (lwHw!=null && listManager.isPersonPronoun(lwHw)) || (lwlwHw!=null && listManager.isPersonPronoun(lwlwHw)) ) 
			return true;
		else
			return false;
	}
	
	public static boolean InPersonDBpediaList(Constituent c) {
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String lwHw = null;		// word left of HW + HW
		String lwlwHw = null;
		
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isPersonDBpedia(hw) || (lwHw!=null && listManager.isPersonDBpedia(lwHw)) || (lwlwHw!=null && listManager.isPersonDBpedia(lwlwHw)) ) 
			return true;
		else
			return false;
	}
	
	public static boolean InGPEList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
			
		if( listManager.isGPECity(originalHw) || (lwHw!=null && listManager.isGPECity(lwHw)) || (lwlwHw!=null && listManager.isGPECity(lwlwHw)) ||
			(hwPos.startsWith("NN") && listManager.isGPECountry(originalHw)) || (lwHw!=null && listManager.isGPECountry(lwHw)) || (lwlwHw!=null && listManager.isGPECountry(lwlwHw)) ||
			listManager.isGPECounty(originalHw) || (lwHw!=null && listManager.isGPECounty(lwHw)) || (lwlwHw!=null && listManager.isGPECounty(lwlwHw)) ||
			listManager.isGPEState(originalHw) || (lwHw!=null && listManager.isGPEState(lwHw)) || (lwlwHw!=null && listManager.isGPEState(lwlwHw)) ||
			listManager.isGPECommonNoun(hw) || (lwHw!=null && listManager.isGPECommonNoun(lwHw)) || (lwlwHw!=null && listManager.isGPECommonNoun(lwlwHw)) ||
			listManager.isGPEMajorArea(originalHw) || (lwHw!=null && listManager.isGPEMajorArea(lwHw)) ) {
			//listManager.isEthnicGroup(originalHw.toLowerCase()) || listManager.isEthnicGroup(hw) || (lwHw!=null && listManager.isEthnicGroup(lwHw)) || (lwlwHw!=null && listManager.isEthnicGroup(lwlwHw)) ||
			//listManager.isNationality(originalHw.toLowerCase()) || listManager.isNationality(hw) || (lwHw!=null && listManager.isNationality(lwHw)) || (lwlwHw!=null && listManager.isNationality(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean InGPECityList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
			
		if( listManager.isGPECity(originalHw) || (lwHw!=null && listManager.isGPECity(lwHw)) || (lwlwHw!=null && listManager.isGPECity(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	public static boolean InGPECountryList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
			
		if( (hwPos.startsWith("NN") && listManager.isGPECountry(originalHw)) || (lwHw!=null && listManager.isGPECountry(lwHw)) || (lwlwHw!=null && listManager.isGPECountry(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	public static boolean InGPECountyList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
			
		if( listManager.isGPECounty(originalHw) || (lwHw!=null && listManager.isGPECounty(lwHw)) || (lwlwHw!=null && listManager.isGPECounty(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	public static boolean InGPEStateList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
			
		if( listManager.isGPEState(originalHw) || (lwHw!=null && listManager.isGPEState(lwHw)) || (lwlwHw!=null && listManager.isGPEState(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	public static boolean InGPECommonNounList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
			
		if( listManager.isGPECommonNoun(hw) || (lwHw!=null && listManager.isGPECommonNoun(lwHw)) || (lwlwHw!=null && listManager.isGPECommonNoun(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	public static boolean InGPEMajorAreaList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
			
		if( listManager.isGPEMajorArea(originalHw) || (lwHw!=null && listManager.isGPEMajorArea(lwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean InEthnicGroupList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isEthnicGroup(originalHw.toLowerCase()) || listManager.isEthnicGroup(hw) || (lwHw!=null && listManager.isEthnicGroup(lwHw)) || (lwlwHw!=null && listManager.isEthnicGroup(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean InNationalityList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isNationality(originalHw.toLowerCase()) || listManager.isNationality(hw) || (lwHw!=null && listManager.isNationality(lwHw)) || (lwlwHw!=null && listManager.isNationality(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// should this be combined with isGPE ; isPerson ?
	public static boolean InEthnicGroupOrNationalityList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isNationality(originalHw.toLowerCase()) || listManager.isNationality(hw) || (lwHw!=null && listManager.isNationality(lwHw)) || (lwlwHw!=null && listManager.isNationality(lwlwHw)) ||
			listManager.isNationality(originalHw.toLowerCase()) || listManager.isNationality(hw) || (lwHw!=null && listManager.isNationality(lwHw)) || (lwlwHw!=null && listManager.isNationality(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean InOrgGovtList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isOrgGovtMultiWords(c.getSurfaceForm().trim()) ||
			listManager.isOrgGovtAbbrev(originalHw.toLowerCase()) ||
			listManager.isOrgGovtSingleWord(originalHw.toLowerCase()) ) {
			return true;
		}
		else {
			return false;
		}	
		
	}
	
	public static boolean InOrgCommercialList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isOrgCommercial(c.getSurfaceForm().trim()) ||
			hw.toLowerCase().compareTo("company")==0 || hw.toLowerCase().compareTo("business")==0 || hw.toLowerCase().compareTo("retailer")==0 || hw.toLowerCase().compareTo("coporation")==0 ||
			hw.toLowerCase().compareTo("co.")==0 || hw.toLowerCase().compareTo("ltd.")==0 || hw.toLowerCase().compareTo("inc.")==0 || hw.toLowerCase().compareTo("corp.")==0 ) {
			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InOrgEducationalList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + originalHw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( hw.toLowerCase().compareTo("university")==0 || hw.toLowerCase().compareTo("school")==0 || hw.toLowerCase().compareTo("college")==0 || 
			hw.toLowerCase().compareTo("academy")==0 || hw.toLowerCase().compareTo("institute")==0 || hw.toLowerCase().compareTo("institution")==0 ) {
					
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InOrgPoliticalList(Constituent c) {
		if(listManager.isOrgPolitical(c.getSurfaceForm()))
			return true;
		else 
			return false;
	}
	
	public static boolean InOrgTerroristList(Constituent c) {
		if(listManager.isOrgTerrorist(c.getSurfaceForm()))
			return true;
		else 
			return false;
	}
	
	public static boolean InFacBarrierList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isFacBarrier(hw) || (lwHw!=null && listManager.isFacBarrier(lwHw)) || (lwlwHw!=null && listManager.isFacBarrier(lwlwHw)) ) {			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InFacBuildingList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isFacBuilding(hw) || (lwHw!=null && listManager.isFacBuilding(lwHw)) || (lwlwHw!=null && listManager.isFacBuilding(lwlwHw)) ) {			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InFacConduitList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isFacConduit(hw) || (lwHw!=null && listManager.isFacConduit(lwHw)) || (lwlwHw!=null && listManager.isFacConduit(lwlwHw)) ) {			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InFacPathList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isFacPath(hw) || (lwHw!=null && listManager.isFacPath(lwHw)) || (lwlwHw!=null && listManager.isFacPath(lwlwHw)) ) {			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InFacPlantList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isFacPlant(hw) || (lwHw!=null && listManager.isFacPlant(lwHw)) || (lwlwHw!=null && listManager.isFacPlant(lwlwHw)) ) {			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InFacBuildingSubAreaList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isFacBuildingSubArea(hw) || (lwHw!=null && listManager.isFacBuildingSubArea(lwHw)) || (lwlwHw!=null && listManager.isFacBuildingSubArea(lwlwHw)) ) {			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InFacGenericList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					postag = "NN";
//				String hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isFacGeneric(hw) || (lwHw!=null && listManager.isFacGeneric(lwHw)) || (lwlwHw!=null && listManager.isFacGeneric(lwlwHw)) ) {			
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean InWeaList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		String hwLemma = null;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.compareTo("NNS")==0) {
//			try {
//				String postag = hwPos;
//				//if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					//postag = "NN";
//				hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isWea(hw) || listManager.isWea(originalHw) || (lwHw!=null && listManager.isWea(lwHw)) || (lwlwHw!=null && listManager.isWea(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}	
	}
	
	public static boolean InVehList(Constituent c) {
		//WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();
		int headTokenOffset = MentionUtil.findHeadTokenOffset(c);
		String hw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset);
		String originalHw = hw;
		String hwLemma = null;
		
		String hwPos = WordHelpers.getPOS(c.getTextAnnotation(), headTokenOffset);
		//if(hwPos.startsWith("NN") || hwPos.startsWith("JJ") || hwPos.startsWith("VB")) {
		if(hwPos.compareTo("NNS")==0) {
		//if(hwPos.startsWith("NN")) {
//			try {
//				String postag = hwPos;
//				//if(hwPos.startsWith("JJ") || hwPos.startsWith("VB")) 
//					//postag = "NN";
//				hwLemma = WordNetHelper.getLemma(hw.toLowerCase(), postag);
//				hw = hwLemma;
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JWNLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		String lwHw = null;		// word left of HW + HW
		if( (c.getEndSpan()-c.getStartSpan())>1 && c.getStartSpan()<headTokenOffset ) {
			lwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-1) + "_" + hw;
		}
		String lwlwHw = null;
		if( (c.getEndSpan()-c.getStartSpan())>2 && c.getStartSpan()<(headTokenOffset-1) ) {
			lwlwHw = WordHelpers.getWord(c.getTextAnnotation(), headTokenOffset-2) + "_" + lwHw;
		}
		
		if( listManager.isVeh(hw) || listManager.isVeh(hw.toLowerCase()) || (lwHw!=null && listManager.isVeh(lwHw)) || (lwlwHw!=null && listManager.isVeh(lwlwHw)) ) {
			return true;
		}
		else {
			return false;
		}	
	}
	
	public static void showFeatures(Constituent gc) {
		List<String> features = MentionTypeFeatures.PosIndexBag(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.WordIndexBag(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.PosWordIndexBag(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.PosEndWordIndexBag(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.WordBCIndexBag(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.PosWordBCIndexBag(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.PosEndWordBCIndexBag(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.ParseExact(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.ParseCover(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		System.out.print(" "+MentionTypeFeatures.ContextLeftWord(gc));
		System.out.print(" "+MentionTypeFeatures.ContextLeftPos(gc));
		System.out.print(" "+MentionTypeFeatures.ContextRightWord(gc));
		System.out.print(" "+MentionTypeFeatures.ContextRightPos(gc));
		features = MentionTypeFeatures.NerLabels(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		features = MentionTypeFeatures.WikiAttributes(gc);
		for(String s : features) {
			System.out.print(" "+s);
		}
		
		if(MentionTypeFeatures.InOrgPoliticalList(gc))
			System.out.print(" InOrgPoliticalList");
		if(MentionTypeFeatures.InOrgTerroristList(gc))
			System.out.print(" InOrgTerroristList");
		if(MentionTypeFeatures.InVehList(gc))
			System.out.print(" InVehList");
		if(MentionTypeFeatures.InWeaList(gc))
			System.out.print(" InWeaList");
		if(MentionTypeFeatures.InFacBarrierList(gc))
			System.out.print(" InFacBarrierList");
		if(MentionTypeFeatures.InFacBuildingList(gc))
			System.out.print(" InFacBuildingList");
		if(MentionTypeFeatures.InFacConduitList(gc))
			System.out.print(" InFacConduitList");
		if(MentionTypeFeatures.InFacPathList(gc))
			System.out.print(" InFacPathList");
		if(MentionTypeFeatures.InFacPlantList(gc))
			System.out.print(" InFacPlantList");
		if(MentionTypeFeatures.InFacBuildingSubAreaList(gc))
			System.out.print(" InFacBuildingSubAreaList");
		if(MentionTypeFeatures.InFacGenericList(gc))
			System.out.print(" InFacGenericList");
		if(MentionTypeFeatures.InPersonList(gc))
			System.out.print(" InPersonList");
		
		if(MentionTypeFeatures.InEthnicGroupOrNationalityList(gc))
			System.out.print(" InEthnicGroupOrNationalityList");
		if(MentionTypeFeatures.InGPECityList(gc))
			System.out.print(" InGPECityList");
		if(MentionTypeFeatures.InGPECountryList(gc))
			System.out.print(" InGPECountryList");
		if(MentionTypeFeatures.InGPECountyList(gc))
			System.out.print(" InGPECountyList");
		if(MentionTypeFeatures.InGPEStateList(gc))
			System.out.print(" InGPEStateList");
		
		if(MentionTypeFeatures.InGPECommonNounList(gc))
			System.out.print(" InGPECommonNounList");
		if(MentionTypeFeatures.InGPEMajorAreaList(gc))
			System.out.print(" InGPEMajorAreaList");
		
		if(MentionTypeFeatures.InOrgGovtList(gc))
			System.out.print(" InOrgGovtList");
		if(MentionTypeFeatures.InOrgCommercialList(gc))
			System.out.print(" InOrgCommercialList");
		if(MentionTypeFeatures.InOrgEducationalList(gc))
			System.out.print(" InOrgEducationalList");
	}
}
