package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data;

import java.io.Serializable;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.SemanticRelation;

public class Document implements Serializable{
	//private String id = null;
	public TextAnnotation ta = null;
	private List<SemanticRelation> relations;


	/**
	 * 
	 */
	public Document() {
		//id = null;
		ta = null;
	}

	/*
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	*/
	
	/**
	 * 
	 */
	public Document(TextAnnotation ta) {
		this.ta = ta;
	}

	

	/**
	 * @return the ta
	 */
	public TextAnnotation getTextAnnotation() {
		return ta;
	}
	
	public void setRelations(List<SemanticRelation> relations) {
		this.relations = relations;
	}
	public List<SemanticRelation> getRelations() {
		return relations;
	}
	
	
}
