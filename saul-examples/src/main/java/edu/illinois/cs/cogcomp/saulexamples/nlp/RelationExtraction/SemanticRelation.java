package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;


public class SemanticRelation implements Serializable {
	private String id;
	private String docid;					// id of the document in which this relation occurs
	private Sentence sentence;
	private int sentenceId;
	private String coarseLabel, fineLabel; 	// relation type/label
	private Set<String> implicitFineLabels;
	private Mention m1, m2;					
	private List<Pair<String, Double>> scores;
	private String surfaceString;
	private int startCharOffset;			// of the relation extent surface string
	private int endCharOffset;
	private String lexicalCondition;
	private List< Set<String> > prepFeatures;
	private boolean premod_isPartOfWikiTitle;
	private boolean premod_isWordNetNounCollocation;
	private boolean premodAccept, premodReject, possAccept, possReject, prepAccept, prepReject;
	private boolean formulaAccept, formulaReject, verbalAccept, verbalReject;
	
	public SemanticRelation(String id) {
		this.id = id;
		docid = null;
		m1 = null;
		m2 = null;
		sentence = null;
		sentenceId = -1;
		coarseLabel = new String(Constants.NO_RELATION);
		fineLabel = new String(Constants.NO_RELATION);
		this.scores = new ArrayList<Pair<String, Double>>();
		surfaceString = null;
		startCharOffset = endCharOffset = -1;
		lexicalCondition = null;
		prepFeatures = null;
		implicitFineLabels = null;
		premod_isPartOfWikiTitle = false;
		premod_isWordNetNounCollocation = false;
		premodAccept = false;	premodReject = false;	possAccept = false;	possReject = false;	prepAccept = false;	prepReject = false;
		formulaAccept = false;	formulaReject = false;	verbalAccept = false;	verbalReject = false;
	}
	
	public SemanticRelation(Mention m1, Mention m2) {
		id = new String("dummy");
		docid = null;
		this.m1 = m1;
		this.m2 = m2;
		sentence = m1.getConstituent().getTextAnnotation().getSentence( m1.getConstituent().getTextAnnotation().getSentenceId(m1.getConstituent()) );
		sentenceId = m1.getConstituent().getTextAnnotation().getSentenceId(m1.getConstituent());
		coarseLabel = new String(Constants.NO_RELATION);
		fineLabel = new String(Constants.NO_RELATION);
		this.scores = new ArrayList<Pair<String, Double>>();
		surfaceString = null;
		startCharOffset = endCharOffset = -1;
		lexicalCondition = null;
		prepFeatures = null;
		implicitFineLabels = null;
		premod_isPartOfWikiTitle = false;
		premod_isWordNetNounCollocation = false;
		premodAccept = false;	premodReject = false;	possAccept = false;	possReject = false;	prepAccept = false;	prepReject = false;
		formulaAccept = false;	formulaReject = false;	verbalAccept = false;	verbalReject = false;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}

	public void setDocId(String id) {
		this.docid = id;
	}
	public String getDocId() {
		if(docid!=null) {
			return docid;
		}
		else {
			if(id!=null && id.contains("_")) {
				return id.substring(0, id.indexOf("_"));
			}
			else 
				return null;
		}
	}
	
	public String getBinaryLabel() {
		if(fineLabel.compareTo(Constants.NO_RELATION)==0) {
			return Constants.NO_RELATION;
		}
		else {
			return Constants.HAS_RELATION;
		}
	}
	
	public void setCoarseLabel(String label) {
		// if(label.toLowerCase().compareTo("affiliation")==0) {
		// this.coarseLabel = "emp-org";
		// }
		// else if(label.toLowerCase().compareTo("fam")==0) {
		// this.coarseLabel = "per-soc";
		// }
		// else {
		this.coarseLabel = label;
		// }
	}
	public String getCoarseLabel() {
		return coarseLabel;
	}

	public String getCoarseUnLabel() {
		if(coarseLabel.compareTo(Constants.NO_RELATION)==0) {
			return Constants.NO_RELATION;
		}
		else {
			String l = null;
			if(coarseLabel.indexOf("m1")!=-1) {
				l = coarseLabel.substring(3, coarseLabel.length()-3);
			}
			else {
				l = coarseLabel;
			}
			return l;
		}
	}
	
	public void setFineLabel(String label) {
		/*
		 * if(label.toLowerCase().compareTo("hassuborg")==0) { this.fineLabel =
		 * "subsidiary"; } else
		 * if(label.toLowerCase().compareTo("hasemployee")==0) { this.fineLabel
		 * = "employ-staff"; } else
		 * if(label.toLowerCase().compareTo("hasleader")==0) { this.fineLabel =
		 * "employ-executive"; } else
		 * if(label.toLowerCase().compareTo("hasmember")==0) { this.fineLabel =
		 * "member-of-group"; } else
		 * if(label.toLowerCase().compareTo("hassibling")==0) { this.fineLabel =
		 * "family"; } else if(label.toLowerCase().compareTo("hasspouse")==0) {
		 * this.fineLabel = "family"; } else
		 * if(label.toLowerCase().compareTo("haschild")==0) { this.fineLabel =
		 * "family"; } else { this.fineLabel = label; }
		 */
		this.fineLabel = label;
	}
	public String getFineLabel() {
		return fineLabel;
	}

	public String getFineUnLabel() {
		if(fineLabel.compareTo(Constants.NO_RELATION)==0) {
			return Constants.NO_RELATION;
		}
		else {
			String l = null;
			if(fineLabel.indexOf("m1")!=-1) {
				l = fineLabel.substring(3, fineLabel.length()-3);
			}
			else {
				l = fineLabel;
			}
			return l;
		}
	}
	
	public void addImplicitFineLabels(Set<String> labels) {
		if(implicitFineLabels == null) {
			implicitFineLabels = new HashSet<String>();
		}
		for(Iterator<String> it=labels.iterator(); it.hasNext();) {
			String label = it.next();
			if(fineLabel!=null && fineLabel.compareTo(label)!=0) {
				implicitFineLabels.add(label);
			}
		}
	}
	public boolean hasImplicitLabels() {
		if(implicitFineLabels!=null && implicitFineLabels.size()>0) {
			return true;
		}
		else {
			return false;
		}
	}
	public Set<String> getImplicitFineLabels() {
		return implicitFineLabels;
	}
	
	public void setM1(Mention m) {
		m1 = m;
	}
	public Mention getM1() {
		return m1;
	}

	public void setM2(Mention m) {
		m2 = m;
	}
	public Mention getM2() {
		return m2;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}
	public Sentence getSentence() {
		return sentence;
	}

	public void setSentenceId(int sentenceId) {
		this.sentenceId = sentenceId;
	}
	public int getSentenceId() {
		return sentenceId;
	}
	
	public void addScore(String label, double d) {
		scores.add(new Pair<String, Double>(label, new Double(d)));
	}
	public List<Pair<String, Double>> getScores() {
		return scores;
	}
	
	public void setSurfaceString(String surfaceString) {
		this.surfaceString = surfaceString;
	}
	public String getSurfaceString() {
		return surfaceString;
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
	
	public void setLexicalCondition(String lexicalCondition) {
		this.lexicalCondition = lexicalCondition;
	}
	public String getLexicalCondition() {
		return lexicalCondition;
	}
	
	
	
	
	public void clearPrepFeatures() {
		this.prepFeatures = null;
	}
	public void clearPrepFeatures(int index) {
		prepFeatures.get(index).clear();
	}
	public void addPrepFeatures(Set<String> prepFeatures) {
		if(this.prepFeatures==null) {
			this.prepFeatures = new ArrayList< Set<String> >();
		}
		this.prepFeatures.add(prepFeatures);
	}
	public List<Set<String>> getAllPrepFeatures() {
		return prepFeatures;
	}
	
	public void setPremod_isPartOfWikiTitle(boolean v) {
		premod_isPartOfWikiTitle = v;
	}
	public boolean premod_isPartOfWikiTitle() {
		return premod_isPartOfWikiTitle;
	}
	
	public void setPremod_isWordNetNounCollocation(boolean v) {
		premod_isWordNetNounCollocation = v;
	}
	public boolean premod_isWordNetNounCollocation() {
		return premod_isWordNetNounCollocation;
	}
	
	public void setPremodAccept(boolean v) {
		premodAccept = v;
	}
	public void setPremodReject(boolean v) {
		premodReject = v;
	}
	public void setPossAccept(boolean v) {
		possAccept = v;
	}
	public void setPossReject(boolean v) {
		possReject = v;
	}
	public void setPrepAccept(boolean v) {
		prepAccept = v;
	}
	public void setPrepReject(boolean v) {
		prepReject = v;
	}
	public void setFormulaAccept(boolean v) {
		formulaAccept = v;
	}
	public void setFormulaReject(boolean v) {
		formulaReject = v;
	}
	public void setVerbalAccept(boolean v) {
		verbalAccept = v;
	}
	public void setVerbalReject(boolean v) {
		verbalReject = v;
	}
	public boolean isPremodAccept() {
		return premodAccept;
	}
	public boolean isPremodReject() {
		return premodReject;
	}
	public boolean isPossAccept() {
		return possAccept;
	}
	public boolean isPossReject() {
		return possReject;
	}
	public boolean isPrepAccept() {
		return prepAccept;
	}
	public boolean isPrepReject() {
		return prepReject;
	}
	public boolean isFormulaAccept() {
		return formulaAccept;
	}
	public boolean isFormulaReject() {
		return formulaReject;
	}
	public boolean isVerbalAccept() {
		return verbalAccept;
	}
	public boolean isVerbalReject() {
		return verbalReject;
	}
	
	public String showMarkedUpSurfaceString() {
		int m1Start = m1.getHeadStartCharOffset() - startCharOffset;
		int m1End = m1.getHeadEndCharOffset() - startCharOffset;
		int m2Start = m2.getHeadStartCharOffset() - startCharOffset;
		int m2End = m2.getHeadEndCharOffset() - startCharOffset;
		
		int firstIndex = -1;
		int finalIndex = -1;
		if(m1Start < m2Start) { firstIndex = m1Start; }
		else { firstIndex = m2Start; }
		if(m2End > m1End) { finalIndex = m2End; }
		else { finalIndex = m1End; }
		
		//System.out.println(startCharOffset+","+endCharOffset+" "+m1.getStartCharOffset()+","+m1.getEndCharOffset()+" "+m2.getStartCharOffset()+","+m2.getEndCharOffset());
		
		StringBuffer s = new StringBuffer();
		
		char[] chars = surfaceString.toCharArray();
		if(lexicalCondition.compareTo("Verbal")==0 || lexicalCondition.compareTo("Other")==0) {
			firstIndex = 0;
			finalIndex = chars.length;
		}
		
		for(int i=firstIndex; i<chars.length && i<=finalIndex; i++) {
			if(i==m1Start) {
				s.append("[");
				s.append(chars[i]);
			}
			else if(i==m1End) {
				s.append(chars[i]);
				s.append("|");
				s.append(m1.getFineSC());
				s.append("]");
			}
			else if(i==m2Start) {
				s.append("[");
				s.append(chars[i]);
			}
			else if(i==m2End) {
				s.append(chars[i]);
				s.append("|");
				s.append(m2.getFineSC());
				s.append("]");
			}
			else {
				s.append(chars[i]);
			}
		}
		
		
		//s.append(surfaceString);
		//s.append(" m1=["+m1.getHeadSurfaceString()+"]");
		//s.append(" m2=["+m2.getHeadSurfaceString()+"]");
		
		return s.toString();
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("<r_id=\"");
		s.append(id);
		s.append("\"_arg1=\"");
		if (m1 == null) {
			s.append("null");
		} else {
			s.append(m1.getId());
		}
		s.append("\"_r=\"");
		s.append(coarseLabel);
		s.append(":");
		s.append(fineLabel);
		s.append("\"_arg2=\"");
		if (m2 == null) {
			s.append("null");
		} else {
			s.append(m2.getId());
		}
		s.append("\">");
		return s.toString();
	}
}



