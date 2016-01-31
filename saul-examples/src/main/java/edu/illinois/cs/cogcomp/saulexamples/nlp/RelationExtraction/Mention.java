package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;


public final class Mention implements Serializable {
	private boolean gold;
	private String id;								// mention id
	private String entityId;
	private int startCharOffset, endCharOffset;		// needed
	private int startTokenOffset, endTokenOffset;
	private int headStartCharOffset, headEndCharOffset;
	private int headStartTokenOffset, headEndTokenOffset;
	private String corpusId;
	private int sentId;
	private String surfaceString;
	private String headSurfaceString;
	private List<String> surfaceStringTokens;
	private String sc;								// needed
	private String fineSc;							// fine-grained semantic class
	private List<String> pos;						// part of speech tags for the tokens of the mention surfaceString
	private String headPos;							// part of speech of the head word
	private String mentionLevel;
	private Constituent c;				
	private int headTokenOffset;						// with respect to the entire input text; mainly for use for the constituent
	private List<Pair<String, Double>> fineSCProbs;
	
	public Mention(String id) {
		gold = false;
		this.id = id;
		if(id.indexOf("-")!=-1) {
			entityId = id.substring(0, id.indexOf("-"));
		}
		startCharOffset = endCharOffset = -1;
		startTokenOffset = endTokenOffset = -1;
		headStartCharOffset = headEndCharOffset = -1;
		headStartTokenOffset = headEndTokenOffset = -1;
		corpusId = null;
		sentId = -1;
		surfaceString = null;
		headSurfaceString = null;
		surfaceStringTokens = new ArrayList<String>();
		sc = null;
		fineSc = null;
		pos = new ArrayList<String>();
		headPos = null;
		mentionLevel = null;
		c = null;
		headTokenOffset = -1;
	}
	
	public Mention(Mention m) {
		this.gold = m.gold;
		this.id = m.id;
		this.entityId = m.entityId;
		this.startCharOffset = m.startCharOffset;
		this.endCharOffset = m.endCharOffset;
		this.startTokenOffset = m.startTokenOffset;
		this.endTokenOffset = m.endTokenOffset;
		this.headStartCharOffset = m.headStartCharOffset;
		this.headEndCharOffset = m.headEndCharOffset;
		this.headStartTokenOffset = m.headStartTokenOffset;
		this.headEndTokenOffset = m.headEndTokenOffset;
		this.corpusId = m.corpusId;
		this.sentId = m.sentId;		
		this.surfaceString = m.surfaceString;
		this.headSurfaceString = m.headSurfaceString;
		this.surfaceStringTokens = m.surfaceStringTokens;
		this.sc = m.sc;
		this.fineSc = m.fineSc;
		this.pos = m.pos;
		this.headPos = m.headPos;
		this.mentionLevel = m.mentionLevel;
		this.c = m.c;
		this.headTokenOffset = m.headTokenOffset;
	}

	// invoked by RelationExtractor.formRelationTrainingExamples
	public Mention(String id, Constituent c) {
		gold = false;
		this.c = c;
		this.id = id;
		if(id.indexOf("-")!=-1) {
			entityId = id.substring(0, id.indexOf("-"));
		}
		
		startTokenOffset = c.getStartSpan();	// I will assume this is token span
		endTokenOffset = c.getEndSpan();
		
		surfaceString = c.getSurfaceForm();
		//String[] tokens = c.getTextAnnotation().getTokensInSpan(c.getStartSpan(), c.getEndSpan());
		surfaceStringTokens = new ArrayList<String>();
		surfaceStringTokens.addAll(Arrays.asList(c.getTextAnnotation().getTokensInSpan(c.getStartSpan(), c.getEndSpan())));
		//surfaceStringTokens.addAll(Arrays.asList(surfaceString.split(" ")));
		
		sc = c.getLabel();	// semantic class
		fineSc = c.getLabel();
		if(c.hasAttribute("sc")) {
			sc = c.getAttribute("sc");
		}
		if(c.hasAttribute("fineSc")) {
			fineSc = c.getAttribute("fineSc");
		}
		if(fineSc.contains(":")) {
			sc = fineSc.substring(0, fineSc.indexOf(":"));
		}
		
		TokenLabelView posView = (TokenLabelView)c.getTextAnnotation().getView(ViewNames.POS);
		pos = new ArrayList<String>();
		for(int i=startTokenOffset; i<endTokenOffset; i++) {
			pos.add(posView.getLabel(i));
		}
		
		if(c.hasAttribute("headStartTokenOffset")) {
			headStartTokenOffset = new Integer(c.getAttribute("headStartTokenOffset")).intValue();
		}
		//if(headStartTokenOffset==-1) {
		else {
			headStartTokenOffset = startTokenOffset;
			//System.out.println("Mention.class constructor: headStartTokenOffset not defined for mention "+id);	// TODO can remove once I rerun the cache building
		}
		if(c.hasAttribute("headEndTokenOffset")) {
			headEndTokenOffset = new Integer(c.getAttribute("headEndTokenOffset")).intValue();
		}
		else {
			headEndTokenOffset = endTokenOffset;
		}
		
		findHead();
		determineMentionLevel(headPos);
		
		startCharOffset = endCharOffset = -1;
		headStartCharOffset = headEndCharOffset = -1;
		this.corpusId = null;
		this.sentId = c.getTextAnnotation().getSentenceId(c);		
		
		
		//headStartTokenOffset = headEndTokenOffset = -1;
		
		headSurfaceString = null;
	}
	
	public void setGold(boolean gold) {
		this.gold = gold;
	}
	public boolean isGold() {
		return gold;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getEntityId() {
		return entityId;
	}
	
	public void setStartCharOffset(int offset) {
		startCharOffset = offset;
	}
	public int getStartCharOffset() {
		return startCharOffset;
	}
	public void setEndCharOffset(int offset) {
		endCharOffset = offset;
	}
	public int getEndCharOffset() {
		return endCharOffset;
	}
	
	public void setStartTokenOffset(int offset) {
		startTokenOffset = offset;
	}
	public int getStartTokenOffset() {
		return startTokenOffset;
	}
	public void setEndTokenOffset(int offset) {
		endTokenOffset = offset;
	}
	public int getEndTokenOffset() {
		return endTokenOffset;
	}

	public void setHeadStartCharOffset(int offset) {
		headStartCharOffset = offset;
	}
	public int getHeadStartCharOffset() {
		return headStartCharOffset;
	}
	public void setHeadEndCharOffset(int offset) {
		headEndCharOffset = offset;
	}
	public int getHeadEndCharOffset() {
		return headEndCharOffset;
	}
	
	public void setHeadStartTokenOffset(int offset) {
		headStartTokenOffset = offset;
	}
	public int getHeadStartTokenOffset() {
		return headStartTokenOffset;
	}
	public void setHeadEndTokenOffset(int offset) {
		headEndTokenOffset = offset;
	}
	public int getHeadEndTokenOffset() {
		return headEndTokenOffset;
	}
	
	public void setCorpusId(String id) {
		corpusId = id;
	}
	public String getCorpusId() {
		return corpusId;
	}

	public void setSentId(int id) {
		sentId = id;
	}
	public int getSentId() {
		return sentId;
	}
	
	
	public void setSurfaceString(String surfaceString) {
		this.surfaceString = surfaceString;
		surfaceStringTokens.clear();
		surfaceStringTokens.addAll(Arrays.asList(this.surfaceString.split(" ")));
	}
	public String getSurfaceString() {
		return surfaceString;
	}
	public void reviseSurfaceString() {		// revise surface string using current startCharOffset, endCharOffset
		int i2 = endCharOffset - startCharOffset + 1;
		surfaceString = surfaceString.substring(0, i2);
		surfaceStringTokens.clear();
		surfaceStringTokens.addAll(Arrays.asList(this.surfaceString.split(" ")));
	}
	
	
	public void setHeadSurfaceString(String headSurfaceString) {
		this.headSurfaceString = headSurfaceString;
	}
	public String getHeadSurfaceString() {
		return headSurfaceString;
	}
	
	public void setSurfaceStringTokens(List<String> tokens) {
		surfaceStringTokens.clear();
		surfaceStringTokens.addAll(tokens);
	}
	public List<String> getSurfaceStringTokens() {
		return surfaceStringTokens;
	}
	
	public void setSC(String sc) {
		// do some remapping
		if (sc.compareTo("gpe") == 0) {
			this.sc = new String("GPE:");
		} else if (sc.compareTo("org") == 0) {
			this.sc = new String("ORG:");
		} else if (sc.compareTo("person") == 0) {
			this.sc = new String("PER:");
		} else if (sc.compareTo("human-agent") == 0) {
			this.sc = new String("HUMANAGENT:");
		} else if (sc.compareTo("unknown/other") == 0) {
			this.sc = new String("OTHER:");
		} else {
			this.sc = sc;
		}
	}
	public String getSC() {
		return sc;
	}
	
	public void setFineSC(String sc) {
		fineSc = sc;
	}
	public String getFineSC() {
		return fineSc;
	}

	public void setPos(List<String> pos) {
		this.pos.clear();
		this.pos.addAll(pos);
	}
	public List<String> getPos() {
		return pos;
	}
	
	public void setHeadPos(String pos) {
		headPos = pos;
	}
	public String getHeadPos() {
		return headPos;
	}

	public void setMentionLevel(String mentionLevel) {
		this.mentionLevel = mentionLevel;
	}
	public String getMentionLevel() {
		return mentionLevel;
	}

	public Constituent getConstituent() {
		return c;
	}
	
	public void setFineSCProbs(List<Pair<String, Double>> probs) {
		fineSCProbs = probs;
	}
	public List<Pair<String, Double>> getFineSCProbs() {
		return fineSCProbs;
	}
	
	public void determineMentionLevel(String pos) {
		if (pos.compareTo("NNP") == 0 || pos.compareTo("NNPS") == 0) {
			mentionLevel = "NAM";
		} else if (pos.compareTo("PRP") == 0 || pos.compareTo("PRP$") == 0
				|| pos.compareTo("WP") == 0 || pos.compareTo("WP$") == 0) {
			mentionLevel = "PRO";
		} else if (pos.compareTo("NN") == 0 || pos.compareTo("NNS") == 0) {
			mentionLevel = "NOM";
		} else {
			mentionLevel = "DUMMY";
		}
	}

	private void findHead() {
		//headTokenOffset = endTokenOffset;				// default
		headTokenOffset = -1;
		
		//System.out.println(startTokenOffset+","+endTokenOffset+" "+headStartTokenOffset+","+headEndTokenOffset+"|||"+pos);
		
		
		// find the first (noun, preposition); and use the noun as the mention head
		for(int i=(headStartTokenOffset+1); i<headEndTokenOffset; i++) {
			if(pos.get(i-startTokenOffset).compareTo("IN")==0 || pos.get(i-startTokenOffset).compareTo("TO")==0) {
				if(pos.get(i-startTokenOffset-1).startsWith("NN") || pos.get(i-startTokenOffset-1).startsWith("PRP")) {
					headTokenOffset = i-1;
					break;
				}
			}
		}
		// else use the last noun in the mention, as the mention head
		if(headTokenOffset==-1) {
			for(int i=(headEndTokenOffset-1); i>=headStartTokenOffset; i--) {
				if(pos.get(i-startTokenOffset).startsWith("NN") || pos.get(i-startTokenOffset).startsWith("PRP")) {
					headTokenOffset = i;
					break;
				}
			}
		}
		if(headTokenOffset==-1) {
			headTokenOffset = (headEndTokenOffset-1);
		}
		headPos = pos.get(headTokenOffset-startTokenOffset);
		
		/*
		// use the first noun
		headPos = pos.get(pos.size()-1);			// default
		for(int i=0; i<pos.size(); i++) {
			if(pos.get(i).startsWith("N")) {
				headTokenOffset = startTokenOffset + i;
				headPos = pos.get(i);
				break;
			}
		}
		*/
	}
	
	public int getHeadTokenOffset() {
		return headTokenOffset;
	}
	

	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("id="+id);
		s.append(" sentId="+sentId);
		s.append(" startCharOffset="+startCharOffset);
		s.append(" endCharOffset="+endCharOffset);
		s.append(" startTokenOffset="+startTokenOffset);
		s.append(" endTokenOffset="+endTokenOffset);
		s.append(" headStartCharOffset="+headStartCharOffset);
		s.append(" headEndCharOffset="+headEndCharOffset);
		s.append(" headStartTokenOffset="+headStartTokenOffset);
		s.append(" headEndTokenOffset="+headEndTokenOffset);
		s.append(" surfaceString:"+surfaceString);
		s.append(" surfaceStringTokens:"+surfaceStringTokens);
		s.append(" fineSc="+fineSc);
		s.append(" pos:"+pos);
		s.append(" headPos="+headPos);
		s.append(" mentionLevel="+mentionLevel);
		s.append(" headTokenOffset="+headTokenOffset);
		
		return s.toString();
	}
}
