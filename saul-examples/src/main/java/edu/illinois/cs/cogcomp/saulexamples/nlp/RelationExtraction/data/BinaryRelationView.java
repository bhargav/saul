package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Constants;

public class BinaryRelationView extends View {
	
	private static final long serialVersionUID = 1;
	List<Relation> relations;
	HashMap<Relation, List<Pair<String, Double>>> scores;		// so now each relation can have its associated posterior distribution over labels
	HashMap<Relation, HashMap<String, String>> rAttributes;
	
	
	public BinaryRelationView(String viewName, String viewGenerator, TextAnnotation text, Double score) {
		super(viewName, viewGenerator, text, score);
		relations = new ArrayList<Relation>();
		scores = new HashMap<Relation, List<Pair<String, Double>>>();
		rAttributes = new HashMap<Relation, HashMap<String, String>>();
	}
	
	public Relation addRelation(String label, Constituent m1, Constituent m2, Double score) {
		Relation r = new Relation(label, m1, m2, score);
		//System.out.println("BinaryRelationView.addRelation: label="+label);
		// sanity check
		if(m1.getStartSpan()!=-1 && m1.getEndSpan()!=-1 && m2.getStartSpan()!=-1 && m2.getEndSpan()!=-1) {
			relations.add(r);
		}
		return r;
	}
	public Relation addRelation(String label, Constituent m1, Constituent m2, Double score, List<Pair<String, Double>> scores) {
		Relation r = new Relation(label, m1, m2, score);
		if(m1.getStartSpan()!=-1 && m1.getEndSpan()!=-1 && m2.getStartSpan()!=-1 && m2.getEndSpan()!=-1) {
			relations.add(r);
			this.scores.put(r, scores);
		}
		return r;
	}
	
	public List<Relation> getRelations() {
		return relations;
	}
	
	public HashMap<Relation, List<Pair<String, Double>>> getScores() {
		return scores;
	}
	
	public void addScoresForRelation(Relation r, List<Pair<String, Double>> scores) {
		this.scores.put(r, scores);
	}
	
	public List<Pair<String, Double>> getAllScoresForRelation(Relation r) {
		return scores.get(r);
	}
	
	public Pair<String, Double> getMaxScoreForRelation(Relation r) {
		if(!scores.containsKey(r)) {
			return null;
		}
		else {
			return getMaxScore(scores.get(r));
		}
	}
	
	public Pair<String, Double> getMaxScore(List<Pair<String, Double>> scores) {
		Pair<String, Double> maxScore = null;
		double s = 0;
		
		for(int i=0; i<scores.size(); i++) {
			if(scores.get(i).getSecond().doubleValue() > s) {
				maxScore = scores.get(i);
				s = scores.get(i).getSecond().doubleValue();
			}
		}
		
		return maxScore;
	}
	
	public void setRelationAttributes(Relation r, HashMap<String, String> attributes) {
		rAttributes.put(r, attributes);
	}
	public HashMap<String, String> getRelationAttributes(Relation r) {
		return rAttributes.get(r);
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		
		for(int i=0; i<relations.size(); i++) {
			Relation r = relations.get(i);
			s.append("[");
			s.append(r.getSource().getSurfaceForm());		// surface string of mention 1
			s.append("]");
			s.append(r.getSource().getStartSpan()+","+r.getSource().getEndSpan());	// start, end token index of mention 1
			s.append(" ");
			s.append(r.getRelationName());		// the 1-best relation label prediction; either a valid relation, or noRelation
			s.append(" [");
			s.append(r.getTarget().getSurfaceForm());		// surface string of mention 2
			s.append("]");
			s.append(r.getTarget().getStartSpan()+","+r.getTarget().getEndSpan());	// start, end token index of mention 2
			
			// print out the entire probability distribution over all the possible relation labels/classes
			if(scores.containsKey(r)) {
				List<Pair<String, Double>> relScores = scores.get(r);
				if(relScores!=null) {
					for(int j=0; j<relScores.size(); j++) {
						s.append(" "+relScores.get(j).getFirst()+"|"+relScores.get(j).getSecond());		// relation label, posterior probability prediction
					}
				}
			}
			
			s.append("\n");
		}
		
		return s.toString();
	}
	
	public String toICString() {
		StringBuffer s = new StringBuffer();
		
		for(int i=0; i<relations.size(); i++) {
			Relation r = relations.get(i);
			s.append("[");
			s.append(r.getSource().getSurfaceForm());		// surface string of mention 1
			s.append("]");
			s.append(r.getSource().getStartSpan()+","+r.getSource().getEndSpan());	// start, end token index of mention 1
			s.append(" ");
			
			if(!Constants.aceToIcRelationsRemap.containsKey(r.getRelationName())) {
				s.append(r.getRelationName());		// the 1-best relation label prediction; either a valid relation, or noRelation
			}
			else {
				s.append(Constants.aceToIcRelationsRemap.get(r.getRelationName()));
			}
			
			s.append(" [");
			s.append(r.getTarget().getSurfaceForm());		// surface string of mention 2
			s.append("]");
			s.append(r.getTarget().getStartSpan()+","+r.getTarget().getEndSpan());	// start, end token index of mention 2
			
			// print out the entire probability distribution over all the possible relation labels/classes
			if(scores.containsKey(r)) {
				List<Pair<String, Double>> relScores = scores.get(r);
				if(relScores!=null) {
					for(int j=0; j<relScores.size(); j++) {
						if(!Constants.aceToIcRelationsRemap.containsKey(relScores.get(j).getFirst())) {
							s.append(" "+relScores.get(j).getFirst()+"|"+relScores.get(j).getSecond());		// relation label, posterior probability prediction
						}
						else {
							s.append(" "+Constants.aceToIcRelationsRemap.get(relScores.get(j).getFirst())+"|"+relScores.get(j).getSecond());
						}
					}
				}
			}
			
			s.append("\n");
		}
		
		return s.toString();
	}
	
	public List<String> getICRelationPredictions() {
		List<String> resultStrings = new ArrayList<String>();
		
		for(int i=0; i<relations.size(); i++) {
			Relation r = relations.get(i);
			
			String oneBestLabel = r.getRelationName();
			if(oneBestLabel.startsWith(Constants.IC_PREFIX)) {
				StringBuffer s = new StringBuffer();
				
				s.append("[");
				s.append(r.getSource().getSurfaceForm());		// surface string of mention 1
				s.append("]");
				s.append(r.getSource().getStartSpan()+","+r.getSource().getEndSpan());	// start, end token index of mention 1
				s.append(" ");

				oneBestLabel = oneBestLabel.substring(Constants.IC_PREFIX.length());
				s.append(oneBestLabel);

				s.append(" [");
				s.append(r.getTarget().getSurfaceForm());		// surface string of mention 2
				s.append("]");
				s.append(r.getTarget().getStartSpan()+","+r.getTarget().getEndSpan());	// start, end token index of mention 2

				// print out the entire probability distribution over all the possible (IC only) relation labels/classes
				if(scores.containsKey(r)) {
					List<Pair<String, Double>> relScores = scores.get(r);
					if(relScores!=null) {
						for(int j=0; j<relScores.size(); j++) {
							String label = relScores.get(j).getFirst();
							double score = relScores.get(j).getSecond().doubleValue();
							if(label.startsWith(Constants.IC_PREFIX)) {
								label = label.substring(Constants.IC_PREFIX.length());
								s.append(" "+label+"|"+score);
							}
						}
					}
				}

				resultStrings.add(s.toString());
			}
		}
		
		return resultStrings;
	}
	
}
