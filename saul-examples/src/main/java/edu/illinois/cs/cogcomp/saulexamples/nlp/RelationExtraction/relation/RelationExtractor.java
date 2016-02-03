package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Constants;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.Document;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.BinaryRelationView;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Mention;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.SemanticRelation;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.mention.MentionUtil;

public class RelationExtractor {
	
	private static String reverseRelationLabelDirection(String label) {
		if(label==null) {
			return null;
		}
		if(label.startsWith("m1-") && label.endsWith("-m2")) {
			String unLabel = label.substring(3, label.length()-3);
			return new String("m2-" + unLabel + "-m1");
		}
		else if(label.startsWith("m2-") && label.endsWith("-m1")) {
			String unLabel = label.substring(3, label.length()-3);
			return new String("m1-" + unLabel + "-m2");
		}
		else {
			return label;
		}
	}
	
	private static void establishImplicitRelations(List<SemanticRelation> relationExamples) {
		//List<SemanticRelation> newExamples = new ArrayList<SemanticRelation>();
		
		// first, let's group the relations by sentence
		HashMap<Integer, List<SemanticRelation>> relationsBySentence = new HashMap<Integer, List<SemanticRelation>>();
		for(SemanticRelation eg : relationExamples) {
			Integer sentId = new Integer(eg.getM1().getConstituent().getSentenceId());
			List<SemanticRelation> egs = null;
			if(!relationsBySentence.containsKey(sentId)) 
				egs = new ArrayList<SemanticRelation>();
			else 
				egs = relationsBySentence.get(sentId);
			egs.add(eg);
			relationsBySentence.put(sentId, egs);
		}
		
		for(Iterator<Integer> sentIt=relationsBySentence.keySet().iterator(); sentIt.hasNext();) {		
			// for each sentence, let's first get the entity to entity fine-grained relation labels
			HashMap<String, HashSet<String>> entToEntRelations = new HashMap<String, HashSet<String>>();
			
			List<SemanticRelation> egs = relationsBySentence.get(sentIt.next());
			for(SemanticRelation eg : egs) {
				if(eg.getFineLabel().compareTo(Constants.NO_RELATION)!=0) {
					String entEntId = eg.getM1().getEntityId()+" "+eg.getM2().getEntityId();
					HashSet<String> relationLabels = null;
					if(!entToEntRelations.containsKey(entEntId)) 
						relationLabels = new HashSet<String>();
					else 
						relationLabels = entToEntRelations.get(entEntId);
					relationLabels.add(eg.getFineLabel());
					entToEntRelations.put(entEntId, relationLabels);
				}
			}
			//System.out.println("entToEntRelations:"+entToEntRelations+"\n");
			
			// now, let's add implicit fine-grained relation labels
			for(SemanticRelation eg : egs) {
				String entEntId = eg.getM1().getEntityId()+" "+eg.getM2().getEntityId();
				Set<String> relationLabels = new HashSet<String>();
				if(entToEntRelations.containsKey(entEntId)) 
					relationLabels.addAll(entToEntRelations.get(entEntId));
				else {
					String[] idTokens = entEntId.split(" ");
					String reverseEntEntId = new String(idTokens[1] + " " + idTokens[0]);
					if(entToEntRelations.containsKey(reverseEntEntId)) {
						Set<String> myrelations = entToEntRelations.get(reverseEntEntId);
						for(Iterator<String> relIt=myrelations.iterator(); relIt.hasNext();) {
							String label = relIt.next();
							relationLabels.add(reverseRelationLabelDirection(label));
						}
					}
				}
				eg.addImplicitFineLabels(relationLabels);
				//if(eg.hasImplicitLabels()) {
				//	System.out.print(RelationPatternExtractor.showExampleDetails(eg, "extent")+"|||implicitFineLabels:"+eg.getImplicitFineLabels()+"\n");
				//}
			}
		}
		
		//return newExamples;
	}
	
	/*
	public static Map<Integer, List<Constituent>> indexMentionsBySentence(List<Constituent> docMentions) {
		Map<Integer, List<Constituent>> sentenceMentions = new TreeMap<Integer, List<Constituent>>();
		
		for(int i=0; i<docMentions.size(); i++) {
			Constituent m = docMentions.get(i);
			TextAnnotation ta = m.getTextAnnotation();
			//System.out.println("mention semantic class:" + m.getLabel());
			if(m.getLabel().compareTo(MentionTyper.NONE_MENTION)!=0) {	// if this is a valid mention
				Integer sentId = new Integer(ta.getSentenceId(m));		// which sentence did this mention appear in
				if(sentenceMentions.containsKey(sentId)) {
					sentenceMentions.get(sentId).add(m);
				}
				else {
					ArrayList<Constituent> a = new ArrayList<Constituent>();
					a.add(m);
					sentenceMentions.put(sentId, a);
				}
				//System.out.println("["+m.getSurfaceString()+"] "+m.getLabel()+" "+m.getStartSpan()+","+m.getEndSpan()+" sentId="+ta.getSentenceId(m));
			}
		}
		
		return sentenceMentions;
	}
	*/
	
	public static Map<Integer, List<Mention>> indexMentionsBySentence(List<Mention> docMentions) {
		Map<Integer, List<Mention>> sentenceMentions = new TreeMap<Integer, List<Mention>>();
		
		for(int i=0; i<docMentions.size(); i++) {
			Mention m = docMentions.get(i);
			Constituent c = m.getConstituent();
			TextAnnotation ta = c.getTextAnnotation();

			//if(m.getLabel().compareTo(MentionTyper.NONE_MENTION)!=0) {	// if this is a valid mention
				Integer sentId = new Integer(ta.getSentenceId(c));		// which sentence did this mention appear in
				if(sentenceMentions.containsKey(sentId)) {
					sentenceMentions.get(sentId).add(m);
				}
				else {
					ArrayList<Mention> a = new ArrayList<Mention>();
					a.add(m);
					sentenceMentions.put(sentId, a);
				}
				//System.out.println("["+m.getSurfaceString()+"] "+m.getLabel()+" "+m.getStartSpan()+","+m.getEndSpan()+" sentId="+ta.getSentenceId(m));
			//}
		}
		
		return sentenceMentions;
	}
	
	/*
	public static ArrayList<Pair<Pair<Constituent, Constituent>, String>> getGoldRelations(Document doc) {
		PredicateArgumentView pav = (PredicateArgumentView) doc.ta.getView(Constants.GOLD_RELATION_VIEW);
		
		// let's first gather all the gold relations
		List<Constituent> predicates = pav.getPredicates();
		ArrayList<Pair<Pair<Constituent, Constituent>, String>> goldRelations = new ArrayList<Pair<Pair<Constituent, Constituent>, String>>();
		for(int i=0; i<predicates.size(); i++) {
			Constituent p = predicates.get(i);
			String relLabel = p.getLabel();
			List<Relation> relations = p.getOutgoingRelations();
			
			Constituent m1=null, m2=null;
			for(int j=0; j<relations.size(); j++) {
				Relation r = relations.get(j);
				if(r.getRelationName().compareTo("m1")==0) { m1 = r.getTarget(); }
				else if(r.getRelationName().compareTo("m2")==0) { m2 = r.getTarget(); }
			}
			if(m1!=null && m2!=null) {
				Pair<Pair<Constituent, Constituent>, String> goldRelation = new Pair<Pair<Constituent, Constituent>, String>(new Pair<Constituent, Constituent>(m1, m2), relLabel);
				goldRelations.add(goldRelation);
			}
		}
		
		return goldRelations;
	}
	*/

    public static List<Pair<Pair<Constituent, Constituent>, Map<String, String>>> getGoldRelations(Document doc) {
        return getGoldRelations(doc.getTextAnnotation());
    }

	// Get relations from GOLD_RELATION_VIEW of documents, provided relation label is not in Constants.relationsToIgnore
	public static List<Pair<Pair<Constituent, Constituent>, Map<String, String>>> getGoldRelations(TextAnnotation ta) {
		List<Pair<Pair<Constituent, Constituent>, Map<String, String>>> goldRelations = new ArrayList<Pair<Pair<Constituent, Constituent>, Map<String, String>>>();
		
		BinaryRelationView brv = (BinaryRelationView) ta.getView(Constants.GOLD_RELATION_VIEW);
		List<Relation> relations = brv.getRelations();
		for(int i=0; i<relations.size(); i++) {
			Relation r = relations.get(i);
			Constituent m1 = r.getSource();
			Constituent m2 = r.getTarget();
			String relLabel = r.getRelationName();
			//System.out.println("relLabel="+relLabel);
			String lexicalCondition = relLabel.substring(relLabel.indexOf("|")+1);
			relLabel = relLabel.substring(0, relLabel.indexOf("|"));
			//HashMap<String, String> relationAttributes = brv.getRelationAttributes(r);
			
			if(!Constants.relationsToIgnore.contains(relLabel)) {
				Map<String, String> rAttributes = new HashMap<String, String>();
				rAttributes.put("label", relLabel);
				rAttributes.put("lexicalCondition", lexicalCondition);
				Pair<Pair<Constituent, Constituent>, Map<String, String>> goldRelation = new Pair<Pair<Constituent, Constituent>, Map<String, String>>(new Pair<Constituent, Constituent>(m1, m2), rAttributes);
				goldRelations.add(goldRelation);
			}
		}
			
		return goldRelations;
	}
	
	public static List<Mention> convertConstituentsIntoMentions(List<Constituent> cons, boolean discardNullMentions) {
		List<Mention> mentions = new ArrayList<Mention>();
		for(int i=0; i<cons.size(); i++) {
			Constituent c = cons.get(i);
			if(discardNullMentions==true && c.getLabel().compareTo(Constants.NONE_MENTION)==0)
				continue;
			
			Mention m=null;
			if(c.getAttributeKeys().contains("id")) 
				m = new Mention(c.getAttribute("id"), c);
			else 
				m = new Mention(new String(c.getStartSpan()+"-"+c.getEndSpan()), c);
			if(c.getAttributeKeys().contains("fineSc")) {
				m.setFineSC(c.getAttribute("fineSc"));
				if(m.getFineSC().contains(":"))				 
					m.setSC(m.getFineSC().substring(0, m.getFineSC().indexOf(":")));
				else 
					m.setSC(m.getFineSC());
			}
			mentions.add(m);
		}
		return mentions;
	}
	
	/*
	public static Map<Integer, List<Mention>> convertConstituentsIntoMentions(Map<Integer, List<Constituent>> docMentions) {
		Map<Integer, List<Mention>> candMentions = new TreeMap<Integer, List<Mention>>();
		for(Iterator<Integer> sentIt=docMentions.keySet().iterator(); sentIt.hasNext();) {
			Integer sentId = sentIt.next();		// sentence index
			List<Constituent> mentions = docMentions.get(sentId);
			List<Mention> newMentions = convertConstituentsIntoMentions( docMentions.get(sentId) );
			candMentions.put(sentId, newMentions);
		}
		return candMentions;
	}
	*/
	
	// Input arguments:
	// TreeMap<Integer, ArrayList<Constituent>> docMentions : {sentence id, list of (mention) constituents in that sentence}
	// ArrayList<Pair<Pair<Constituent, Constituent>, HashMap<String, String>>> goldRelations : 
	//		list of { (m1, m2) constituents ; HashMap giving attributes of the (gold) relation
	// 		relevant attribute keys are : label, lexicalCondition
	// Also establish implicit relations
	public static List<SemanticRelation> formRelationTrainingExamples(Map<Integer, List<Mention>> candMentions, List<Pair<Pair<Constituent, Constituent>, Map<String, String>>> goldRelations) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		
		for(Iterator<Integer> sentIt=candMentions.keySet().iterator(); sentIt.hasNext();) {
			Integer sentId = sentIt.next();		// sentence index
			List<Mention> mentions = candMentions.get(sentId);
			try {
				MentionUtil.sortMentionAsc(mentions);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mentions.size() > 1) {			// there must be at least 2 mentions in a sentence, before I form relations
				for(int i=0; i<(mentions.size()-1); i++) {
					for(int j=(i+1); j<mentions.size(); j++) {
						SemanticRelation r = new SemanticRelation(mentions.get(i), mentions.get(j));
						Mention cM1 = mentions.get(i);		// candidate m1
						Mention cM2 = mentions.get(j);
						// now let's check whether there is any valid relation between these two mentions
						for(int k=0; k<goldRelations.size(); k++) {
							Constituent gM1 = goldRelations.get(k).getFirst().getFirst();
							Constituent gM2 = goldRelations.get(k).getFirst().getSecond();
							Map<String, String> rAttributes = goldRelations.get(k).getSecond();
							String fineLabel = rAttributes.get("label");
							String lexicalCondition = rAttributes.get("lexicalCondition");
							String coarseLabel = fineLabel;
							if(coarseLabel.indexOf(":")!=-1) {
								coarseLabel = coarseLabel.substring(0, coarseLabel.indexOf(":"));
							}
							//String coarseLabel = fineLabel.substring(0, fineLabel.indexOf(":"));
							if(MentionUtil.compareConstituents(cM1.getConstituent(), gM1) && MentionUtil.compareConstituents(cM2.getConstituent(), gM2)) {
								r.setFineLabel("m1-"+fineLabel+"-m2");
								r.setLexicalCondition(lexicalCondition);
								r.setCoarseLabel("m1-"+coarseLabel+"-m2");
								break;
							}
							else if(MentionUtil.compareConstituents(cM2.getConstituent(), gM1) && MentionUtil.compareConstituents(cM1.getConstituent(), gM2)) {
								r.setFineLabel("m2-"+fineLabel+"-m1");
								r.setLexicalCondition(lexicalCondition);
								r.setCoarseLabel("m2-"+coarseLabel+"-m1");
								break;
							}
						}
						relationExamples.add(r);
					}
				}
			}
		}
		
		establishImplicitRelations(relationExamples);
		return relationExamples;
	}
	
	// use this to form relation examples based on PREDICTED mentions ; whether they are predicted as non-null or null
	public static List<SemanticRelation> formRelationTrainingExamples(Map<Integer, List<Mention>> candMentions) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		
		for(Iterator<Integer> sentIt=candMentions.keySet().iterator(); sentIt.hasNext();) {
			Integer sentId = sentIt.next();		// sentence index
			List<Mention> mentions = candMentions.get(sentId);
			try {
				MentionUtil.sortMentionAsc(mentions);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mentions.size() > 1) {			// there must be at least 2 mentions in a sentence, before I form relations
				for(int i=0; i<(mentions.size()-1); i++) {
					for(int j=(i+1); j<mentions.size(); j++) {
						if(mentions.get(i).getFineSCProbs()!=null && mentions.get(j).getFineSCProbs()!=null && 
						   mentions.get(i).getFineSCProbs().get(0).getFirst().compareTo(Constants.NONE_MENTION)==0 && mentions.get(j).getFineSCProbs().get(0).getFirst().compareTo(Constants.NONE_MENTION)==0) {}
						else {	// I will form a relation if at least one of the mention in the mention-pair is predicted as non NULL
							SemanticRelation r = new SemanticRelation(mentions.get(i), mentions.get(j));
							relationExamples.add(r);
						}
					}
				}
			}
		}
		
		return relationExamples;
	}
	
	////////////////////
	/*
	public static Learner train(List<Document> docs, String mentionViewname) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();

		if(mentionViewname.compareTo(Constants.GOLD_MENTION_VIEW)==0) {
			for(Document doc:docs){
				relationExamples.addAll(getAllExamplesFromDocument(doc));
			}
		}
		else {
			for(Document doc:docs) {
				//MentionDetector.labelDocMentionCandidates(doc);		// generate all the possible mentions
				//MentionTyper.createTypedCandidateMentions(doc.ta, (SpanLabelView) doc.ta.getView(gold_view_name));	// assign the mention types
			}
		}

		return train(relationExamples);
	}

	public static Learner train(List<SemanticRelation> relationExamples) {
		Learner relClassifier = new relationTypeClassifier();
		relClassifier.forget();
		for(int i=0; i<relationExamples.size(); i++) {
			relClassifier.learn(relationExamples.get(i));
		}
        relClassifier.doneLearning();
		return relClassifier;
	}
	public static Learner trainBinary(List<SemanticRelation> relationExamples) {
		Learner relClassifier = new relationTypeBinaryClassifier();
		relClassifier.forget();
		for(int i=0; i<relationExamples.size(); i++) {
			relClassifier.learn(relationExamples.get(i));
		}
        relClassifier.doneLearning();
		return relClassifier;
	}
	public static Learner trainCoarse(List<SemanticRelation> relationExamples) {
		Learner relClassifier = new relationTypeCoarseClassifier();
		relClassifier.forget();
		for(int i=0; i<relationExamples.size(); i++) {
			relClassifier.learn(relationExamples.get(i));
		}
        relClassifier.doneLearning();
		return relClassifier;
	}
	public static Learner trainFine(List<SemanticRelation> relationExamples) {
		Learner relClassifier = new relationTypeFineClassifier();
		relClassifier.forget();
		for(int i=0; i<relationExamples.size(); i++) {
			relClassifier.learn(relationExamples.get(i));
		}
        relClassifier.doneLearning();
		return relClassifier;
	}*/

	/*
	public static List<SemanticRelation> getAllRelationCandidates(List<Document> docs, String viewName, Map<Document, String> docToIdMap) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		
		for (Document doc: docs){
			List<Constituent> docMentions = doc.ta.getView(viewName).getConstituents();
			//String docId = docToIdMap.get(doc);
			
			// let's first slot the non-NULL mentions into sentenceMentions; indexed by their respective sentence ids (start from index 0)
			Map<Integer, List<Constituent>> sentenceMentions = indexMentionsBySentence(docMentions);
			
			// get the gold relations in this document
			List<Pair<Pair<Constituent, Constituent>, Map<String, String>>> goldRelations = getGoldRelations(doc);
			
			// let's now form relation examples from all possible binary mention-pairs
			List<SemanticRelation> egs = formRelationTrainingExamples(convertConstituentsIntoMentions(sentenceMentions), goldRelations);	
			for(int i=0; i<egs.size(); i++) {
				SemanticRelation eg = egs.get(i);
				//if(doc.getId()!=null) {
				if(docToIdMap!=null) {
					String docId = docToIdMap.get(doc);
					String rid = new String(docId+"_"+eg.getM1().getStartTokenOffset()+"-"+eg.getM1().getEndTokenOffset()+"_"+eg.getM2().getStartTokenOffset()+"-"+eg.getM2().getEndTokenOffset());
					eg.setId(rid);
				}
				relationExamples.add(eg);
			}
			//relationExamples.addAll(formRelationTrainingExamples(sentenceMentions, goldRelations));	
		}	
		
		return relationExamples;
	}
	*/
	
	
	/*
	public static relationTypeClassifier trainRelationModel(List<Document> docs, String viewName) {
		relationTypeClassifier relClassifier = new relationTypeClassifier();
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		
		for (Document doc: docs){
			
			
			
			List<Constituent> docCons = doc.ta.getView(viewName).getConstituents();
			List<Mention> docMentions = convertConstituentsIntoMentions(docCons, true);
			// let's first slot the non-NULL mentions into sentenceMentions; indexed by their respective sentence ids (start from index 0)
			Map<Integer, List<Mention>> sentenceMentions = indexMentionsBySentence(docMentions);
			
			// get the gold relations in this document
			List<Pair<Pair<Constituent, Constituent>, Map<String, String>>> goldRelations = getGoldRelations(doc);
			
			// let's now form relation examples from all possible binary mention-pairs
			relationExamples.addAll(formRelationTrainingExamples(sentenceMentions, goldRelations));	
		}	
		
		
		relClassifier.forget();
		//relClassifier.readModel("/home/roth/chanys/workspace/illinoisRelationExtraction/dist/save_objs/re.model");
		//relClassifier.readLexicon("/home/roth/chanys/workspace/illinoisRelationExtraction/dist/save_objs/re.lex");
		
		for(int i=0; i<relationExamples.size(); i++) {
			//System.out.println("***** LEARNING RELATION EXAMPLE *****");
			relClassifier.learn(relationExamples.get(i));
			//System.out.println("Prediction:"+relClassifier.discreteValue(relationExamples.get(i)));
		}

        relClassifier.doneLearning();
        File model_f = new File(ResourceManager.getProjectResourceModelsPath()+"/re.model");
		File lex_f = new File(ResourceManager.getProjectResourceModelsPath()+"/re.lex");
		if (model_f.exists())
			model_f.delete();
		if(lex_f.exists())
			lex_f.delete();
		relClassifier.write(ResourceManager.getProjectResourceModelsPath()+"/re.model", ResourceManager.getProjectResourceModelsPath()+"/re.lex");
        
		
//        relationTypeClassifier tester = new relationTypeClassifier();
//        for(int i=0; i<relationExamples.size(); i++) {
//			String guess = tester.discreteValue(relationExamples.get(i));
//			String key = relationExamples.get(i).getFineLabel();
//			System.out.println("===> " + key + " " + guess);
//		}
		
		return relClassifier;
	}
	*/

    /*
	
	public static relationTypeClassifier applyRelationModel(Document doc) {
		relationTypeClassifier relClassifier = new relationTypeClassifier();
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		
		List<Constituent> docCons = doc.ta.getView(Constants.PRED_MENTION_VIEW).getConstituents();
		List<Mention> docMentions = convertConstituentsIntoMentions(docCons, true);
		// let's first slot the non-NULL mentions into sentenceMentions; indexed by their respective sentence ids (start from index 0)
		Map<Integer, List<Mention>> sentenceMentions = indexMentionsBySentence(docMentions);
			
		// let's now form relation examples from all possible binary mention-pairs
		relationExamples.addAll(formRelationTrainingExamples(sentenceMentions));	
			
		relClassifier.unclone();
		relClassifier.readModel(ResourceManager.getREModelResource());
		relClassifier.readLexicon(ResourceManager.getRELexResource());
		
		for(int i=0; i<relationExamples.size(); i++) {
			SemanticRelation e = relationExamples.get(i);
			e.setFineLabel(relClassifier.discreteValue(e));
			//System.out.println("1-best prediction:"+e.getFineLabel()+"*****");
			
			ScoreSet scoreSet = relClassifier.scores(e);
			//System.out.println(scoreSet);
			Softmax mSoftmax = new Softmax();
            scoreSet = mSoftmax.normalize(scoreSet);
            Score[] scores = scoreSet.toArray();
            for(int j=0; j<scores.length; j++) {
            	double score = scores[j].score;
            	String label = scores[j].value;
            	e.addScore(label, new Double(score));
            	//System.out.print("["+label+"|"+score+"]");
            }
            //System.out.println("");
            
		}

		// add the binary relation view
		BinaryRelationView relationView = new BinaryRelationView(Constants.PRED_RELATION_VIEW, "prediction", doc.getTextAnnotation(), 1.0);
		for (SemanticRelation rel : relationExamples) {
			//TreeSet<Integer> constituentTokens = new TreeSet<Integer>();
			//constituentTokens.add(rel.getM1().getStartTokenOffset());
			//constituentTokens.add(rel.getM1().getEndTokenOffset());
			
			//System.out.println("m1:["+rel.getM1().getConstituent().getSurfaceString()+"]"+rel.getM1().getFineSC()+","+rel.getM1().getConstituent().getLabel()+","+rel.getM1().getConstituent().getConstituentScore()+","+rel.getM1().getConstituent().getStartCharOffset()+","+rel.getM1().getConstituent().getEndCharOffset());
			//System.out.println("m2:["+rel.getM2().getConstituent().getSurfaceString()+"]"+rel.getM2().getFineSC()+","+rel.getM2().getConstituent().getLabel()+","+rel.getM2().getConstituent().getConstituentScore()+","+rel.getM2().getConstituent().getStartCharOffset()+","+rel.getM2().getConstituent().getEndCharOffset());
			
			Constituent m1 = new Constituent(rel.getM1().getConstituent().getLabel(), rel.getM1().getConstituent().getConstituentScore(),
					Constants.PRED_RELATION_VIEW, doc.getTextAnnotation(), rel.getM1().getConstituent().getStartSpan(), rel.getM1().getConstituent().getEndSpan());
			
			//constituentTokens = new TreeSet<Integer>();
			//constituentTokens.add(rel.getM2().getStartTokenOffset());
			//constituentTokens.add(rel.getM2().getEndTokenOffset());
			
			Constituent m2 = new Constituent(rel.getM2().getConstituent().getLabel(), rel.getM2().getConstituent().getConstituentScore(),
					Constants.PRED_RELATION_VIEW, doc.getTextAnnotation(), rel.getM2().getConstituent().getStartSpan(), rel.getM2().getConstituent().getEndSpan());
			
			relationView.addConstituent(m1);
			relationView.addConstituent(m2);
			
			//relationView.addRelation(rel.getFineLabel(), rel.getM1().getConstituent(), rel.getM2().getConstituent(), 1.0, rel.getScores());
			relationView.addRelation(rel.getFineLabel(), m1, m2, RelationEvaluator.getOneBest(rel.getScores()).getSecond(), rel.getScores());
		}
		doc.getTextAnnotation().addView(Constants.PRED_RELATION_VIEW, relationView);
		
		
		return relClassifier;
	}
	
	public static Learner applyRelationModel(Document doc, Learner relClassifier) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		
		List<Constituent> docCons = doc.ta.getView(Constants.PRED_MENTION_VIEW).getConstituents();
		List<Mention> docMentions = convertConstituentsIntoMentions(docCons, true);
		// let's first slot the non-NULL mentions into sentenceMentions; indexed by their respective sentence ids (start from index 0)
		Map<Integer, List<Mention>> sentenceMentions = indexMentionsBySentence(docMentions);
			
		// let's now form relation examples from all possible binary mention-pairs
		relationExamples.addAll(formRelationTrainingExamples(sentenceMentions));	
			
		for(int i=0; i<relationExamples.size(); i++) {
			SemanticRelation e = relationExamples.get(i);
			e.setFineLabel(relClassifier.discreteValue(e));
			//System.out.println("1-best prediction:"+e.getFineLabel()+"*****");
			
			ScoreSet scoreSet = relClassifier.scores(e);
			//System.out.println(scoreSet);
			Softmax mSoftmax = new Softmax();
            scoreSet = mSoftmax.normalize(scoreSet);
            Score[] scores = scoreSet.toArray();
            for(int j=0; j<scores.length; j++) {
            	double score = scores[j].score;
            	String label = scores[j].value;
            	e.addScore(label, new Double(score));
            	//System.out.print("["+label+"|"+score+"]");
            }
            //System.out.println("");
            
		}

		// add the binary relation view
		BinaryRelationView relationView = new BinaryRelationView(Constants.PRED_RELATION_VIEW, "prediction", doc.getTextAnnotation(), 1.0);
		for (SemanticRelation rel : relationExamples) {
			TreeSet<Integer> constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM1().getStartTokenOffset());
			constituentTokens.add(rel.getM1().getEndTokenOffset());
			//Constituent m1 = new Constituent(rel.getM1().getSC(), 1.0, Constants.PRED_RELATION_VIEW, doc.getTextAnnotation(), constituentTokens, true);
			Constituent m1 = new Constituent(rel.getM1().getFineSC(), 1.0, Constants.PRED_RELATION_VIEW, doc.getTextAnnotation(), rel.getM1().getStartTokenOffset(), rel.getM1().getEndTokenOffset());
			
			constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM2().getStartTokenOffset());
			constituentTokens.add(rel.getM2().getEndTokenOffset());
			//Constituent m2 = new Constituent(rel.getM2().getSC(), 1.0, Constants.PRED_RELATION_VIEW, doc.getTextAnnotation(), constituentTokens, true);
			Constituent m2 = new Constituent(rel.getM2().getFineSC(), 1.0, Constants.PRED_RELATION_VIEW, doc.getTextAnnotation(), rel.getM2().getStartTokenOffset(), rel.getM2().getEndTokenOffset());
			
			relationView.addRelation(rel.getFineLabel(), m1, m2, 1.0, rel.getScores());
		}
		doc.getTextAnnotation().addView(Constants.PRED_RELATION_VIEW, relationView);
		
		return relClassifier;
	}
	
	/*
	public static Map<String, Pair<String, Double>> applyRelationModel(List<SemanticRelation> relationExamples, URL reModel, URL reLex) {
		relationTypeClassifier relClassifier = new relationTypeClassifier();
		relClassifier.unclone();
		relClassifier.readModel(reModel);
		relClassifier.readLexicon(reLex);
		
		Map<String, Pair<String, Double>> relationsWithScores = new HashMap<String, Pair<String, Double>>();
		for(int i=0; i<relationExamples.size(); i++) {
			SemanticRelation e = relationExamples.get(i);
			String oneBestLabel = relClassifier.discreteValue(e);
			
			ScoreSet scoreSet = relClassifier.scores(e);
			Softmax mSoftmax = new Softmax();
            scoreSet = mSoftmax.normalize(scoreSet);
            double oneBestScore = scoreSet.get(oneBestLabel);
            
            relationsWithScores.put(e.getId(), new Pair<String, Double>(oneBestLabel, new Double(oneBestScore)));
		}
		
		return relationsWithScores;
	}
	*/

    /*
	public static Map<String, Pair<String, Double>> applyRelationModel(List<SemanticRelation> relationExamples, relationTypeClassifier relClassifier) {
		Map<String, Pair<String, Double>> relationsWithScores = new HashMap<String, Pair<String, Double>>();
		for(int i=0; i<relationExamples.size(); i++) {
			SemanticRelation e = relationExamples.get(i);
			String oneBestLabel = relClassifier.discreteValue(e);
			ScoreSet scoreSet = relClassifier.scores(e);
			Softmax mSoftmax = new Softmax();
            scoreSet = mSoftmax.normalize(scoreSet);
            double oneBestScore = scoreSet.get(oneBestLabel);
            relationsWithScores.put(e.getId(), new Pair<String, Double>(oneBestLabel, new Double(oneBestScore)));
		}
		return relationsWithScores;
	}
	public static Map<String, List<Pair<String, Double>>> applyRelationModel(List<SemanticRelation> relationExamples, Learner relClassifier) {
		Map<String, List<Pair<String, Double>>> relationsWithScores = new HashMap<String, List<Pair<String, Double>>>();
		for(int i=0; i<relationExamples.size(); i++) {
			SemanticRelation e = relationExamples.get(i);
			ScoreSet scoreSet = relClassifier.scores(e);
			Softmax mSoftmax = new Softmax();
            scoreSet = mSoftmax.normalize(scoreSet);
            Score[] scores = scoreSet.toArray();
            List<Pair<String, Double>> scoreDistribution = new ArrayList<Pair<String, Double>>();
            for(int j=0; j<scores.length; j++) {
            	scoreDistribution.add(new Pair<String, Double>(scores[j].value, new Double(scores[j].score)));
            }
            relationsWithScores.put(e.getId(), scoreDistribution);
		}
		return relationsWithScores;
	}
	
	public static Map<String, Integer> testAndEvaluate(Learner relClassifier, List<Document> docs, String mentionViewname, Map<String, Map<String, Integer>> lexCondMap) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		
		if(mentionViewname.compareTo(Constants.GOLD_MENTION_VIEW)==0) {
			for(Document doc:docs){
				relationExamples.addAll(getAllExamplesFromDocument(doc));	
			}
		}
		
		int correct=0, R=0, P=0;
		for(int i=0; i<relationExamples.size(); i++) {
			SemanticRelation e = relationExamples.get(i);
			String prediction = relClassifier.discreteValue(e);
			String goldLabel = e.getFineLabel();
			//String goldLabel = e.getBinaryLabel();
			//String goldLabel = e.getCoarseUnLabel();
			String lexicalCondition = e.getLexicalCondition();
			if(lexicalCondition == null) { lexicalCondition = new String("NULL"); }
			
			if(goldLabel.compareTo(Constants.NO_RELATION)!=0) {
				R += 1;
			}
			if(prediction.compareTo(Constants.NO_RELATION)!=0) {
				P += 1;
				if(goldLabel.compareTo(prediction)==0) {
					correct += 1;
					
					// ---- this part is for keeping track of lexical condition statistics ---
					if(lexCondMap!=null) {
						if(!lexCondMap.containsKey(goldLabel)) {
							Map<String, Integer> mymap = new HashMap<String, Integer>();
							mymap.put(lexicalCondition, new Integer(1));
							lexCondMap.put(goldLabel, mymap);
						}
						else {
							Map<String, Integer> mymap = lexCondMap.get(goldLabel);
							if(!mymap.containsKey(lexicalCondition)) {
								mymap.put(lexicalCondition, new Integer(1));
							}
							else {
								int count = mymap.get(lexicalCondition);
								mymap.put(lexicalCondition, new Integer(count+1));
							}
							lexCondMap.put(goldLabel, mymap);
						}
					}
					// ----- done -----
				}
			}
		}
		
		double f1 = Util.calculateF1(correct, R, P);
		System.out.println("#candidates="+relationExamples.size()+" recall="+correct+"/"+R+"="+(new Double(correct)/new Double(R))+" precision="+correct+"/"+P+"="+(new Double(correct)/new Double(P))+" F1="+f1);
		
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		results.put("c", new Integer(correct));
		results.put("r", new Integer(R));
		results.put("p", new Integer(P));
		return results;
	}
	
	public static double testAndEvaluate(Learner relClassifier, List<SemanticRelation> relationExamples) {
		int correct=0, R=0, P=0;
		
		for(int i=0; i<relationExamples.size(); i++) {
			SemanticRelation e = relationExamples.get(i);
			String prediction = relClassifier.discreteValue(e);
			//String goldLabel = e.getFineLabel();
			String goldLabel = e.getBinaryLabel();
			
			if(goldLabel.compareTo(Constants.NO_RELATION)!=0) {
				R += 1;
			}
			if(prediction.compareTo(Constants.NO_RELATION)!=0) {
				P += 1;
				if(goldLabel.compareTo(prediction)==0) {
					correct += 1;
				}
			}		
		}
				
		double recall = correct;
		recall = recall/R;
		double precision = correct;
		precision = precision/P;
		double f1 = (2*precision*recall)/(precision+recall);
		//System.out.println("#candidates="+relationExamples.size()+" recall="+correct+"/"+R+"="+recall+" precision="+correct+"/"+P+"="+precision+" F1="+f1);
		return f1;
	}*/

	public static List<SemanticRelation> getAllPositiveExamples(List<SemanticRelation> relationExamples) {
		List<SemanticRelation> positiveExamples = new ArrayList<SemanticRelation>();
		for(SemanticRelation r:relationExamples) {
			if(r.getBinaryLabel().compareTo(Constants.NO_RELATION)!=0) 
				positiveExamples.add(r);
		}
		return positiveExamples;
	}
	
	public static List<SemanticRelation> getAllExamplesFromDocument(TextAnnotation ta) {
		List<Constituent> docCons = ta.getView(Constants.GOLD_MENTION_VIEW).getConstituents();
		List<Mention> docMentions = convertConstituentsIntoMentions(docCons, true);
		// let's first slot the non-NULL mentions into sentenceMentions; indexed by their respective sentence ids (start from index 0)
		Map<Integer, List<Mention>> sentenceMentions = indexMentionsBySentence(docMentions);
		// get the gold relations in this document
		List<Pair<Pair<Constituent, Constituent>, Map<String, String>>> goldRelations = getGoldRelations(ta);
		// let's now form relation examples from all possible binary mention-pairs
		return formRelationTrainingExamples(sentenceMentions, goldRelations);	
	}
	
	public static List<SemanticRelation> getAllExamplesFromDocuments(List<Document> docs) {
		ArrayList<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();
		for(Document doc:docs)
			relationExamples.addAll(getAllExamplesFromDocument(doc.getTextAnnotation()));
		return relationExamples;
	}


	public static List<SemanticRelation> getAllExamplesFromDocuments(List<Document> docs, List<String> docIds) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();

		for(int docIndex=0; docIndex<docs.size(); docIndex++) {
			Document doc = docs.get(docIndex);
			List<SemanticRelation> egs = getAllExamplesFromDocument(doc.getTextAnnotation());
			for(int i=0; i<egs.size(); i++) {
				SemanticRelation eg = egs.get(i);
				String docId = docIds.get(docIndex);
				String rid = new String(docId+"_"+eg.getM1().getStartTokenOffset()+"-"+eg.getM1().getEndTokenOffset()+"_"+eg.getM2().getStartTokenOffset()+"-"+eg.getM2().getEndTokenOffset());
				eg.setId(rid);
				relationExamples.add(eg);
			}
		}

		return relationExamples;
	}
	
	public static List<SemanticRelation> getValidExamplesFromDocuments(List<Document> docs, List<String> docIds) {
		List<SemanticRelation> relationExamples = new ArrayList<SemanticRelation>();

		for(int docIndex=0; docIndex<docs.size(); docIndex++) {
			Document doc = docs.get(docIndex);
			List<SemanticRelation> egs = getAllExamplesFromDocument(doc.getTextAnnotation());
			for(int i=0; i<egs.size(); i++) {
				SemanticRelation eg = egs.get(i);
				if(eg.getBinaryLabel().compareTo("HAS_RELATION")==0 || eg.hasImplicitLabels()) {
					String docId = docIds.get(docIndex);
					String rid = new String(docId+"_"+eg.getM1().getStartTokenOffset()+"-"+eg.getM1().getEndTokenOffset()+"_"+eg.getM2().getStartTokenOffset()+"-"+eg.getM2().getEndTokenOffset());
					eg.setId(rid);
					relationExamples.add(eg);
				}
			}
		}

		return relationExamples;
	}
	
	public static List<SemanticRelation> findMatchingRelations(SemanticRelation targetRelation, List<SemanticRelation> goldRelations) {
		List<SemanticRelation> matchedRelations = new ArrayList<SemanticRelation>();
		boolean relaxedMatching = true;

		Mention m1 = targetRelation.getM1();
		Mention m2 = targetRelation.getM2();
		
		for(SemanticRelation gr : goldRelations) {
			Mention gM1 = gr.getM1();
			Mention gM2 = gr.getM2();
		
			if(relaxedMatching==false) {
				if( ( m1.getConstituent().getStartSpan()==gM1.getConstituent().getStartSpan() && 
						m1.getConstituent().getEndSpan()==gM1.getConstituent().getEndSpan() &&
						m2.getConstituent().getStartSpan()==gM2.getConstituent().getStartSpan() && 
						m2.getConstituent().getEndSpan()==gM2.getConstituent().getEndSpan() ) ) {
					matchedRelations.add(gr);
				}
			}
			else {
				if( (MentionUtil.constituentsOverlap(gM1.getConstituent(), m1.getConstituent()) && 
						MentionUtil.constituentsOverlap(gM2.getConstituent(), m2.getConstituent())) ) {
					if(MentionUtil.findHeadTokenOffset(gM1.getConstituent())==MentionUtil.findHeadTokenOffset(m1.getConstituent()) &&
							MentionUtil.findHeadTokenOffset(gM2.getConstituent())==MentionUtil.findHeadTokenOffset(m2.getConstituent())) {
						matchedRelations.add(gr);
					}
				}
			}
		}
		
		return matchedRelations;
	}
}

