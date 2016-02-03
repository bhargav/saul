package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Constants {
	
	//public static final String PATH_BROWN_CLUSTER_NYT = ResourceManager.getProjectResourceModelsPath() + "/brown_clusters";  

	public static final String GOLD_ACE_MENTION_VIEW = "GOLD_ACE_MENTION_VIEW";
	public static final String GOLD_ACE_RELATION_VIEW = "GOLD_ACE_RELATION_VIEW";

	public static final String GOLD_IC_MENTION_VIEW = "GOLD_IC_MENTION_VIEW";
	public static final String GOLD_IC_RELATION_VIEW = "GOLD_IC_RELATION_VIEW";

	public static final String GOLD_MENTION_VIEW = "GOLD_MENTION_VIEW";
	public static final String GOLD_RELATION_VIEW = "GOLD_RELATION_VIEW";
	
	public static final String CANDIDATE_MENTION_VIEW = "CANDIDATE_MENTION_VIEW";
	public static final String TYPED_CANDIDATE_MENTION_VIEW = "TYPED_CANDIDATE_MENTION_VIEW";

	public static final String PRED_ACE_MENTION_VIEW = "PRED_ACE_MENTION_VIEW";
	public static final String PRED_ACE_RELATION_VIEW = "PRED_ACE_RELATION_VIEW";

	public static final String PRED_IC_MENTION_VIEW = "PRED_IC_MENTION_VIEW";
	public static final String PRED_IC_RELATION_VIEW = "PRED_IC_RELATION_VIEW";
	
	public static final String PRED_MENTION_VIEW = "PRED_MENTION_VIEW";
	public static final String PRED_RELATION_VIEW = "PRED_RELATION_VIEW";

	public static final String POS_MENTION = "POS_MENTION";
	public static final String PARSE_PHRASE = "PARSE_PHRASE";
	public static final String CHUNK_PHRASE = "CHUNK_PHRASE";
	
	public static final String SPAN_EXTENT = "SPAN_EXTENT";
	public static final String SPAN_CANDIDATE = "SPAN_CANDIDATE";
	public static final String SPAN_HEAD = "SPAN_HEAD";

	public static final String NO_RELATION = "NO_RELATION";
	public static final String HAS_RELATION = "HAS_RELATION";

	public static final int TREE_MAX_DEPTH = 100;
	
	public static final String PREMOD = "PREMOD";
	public static final String POSS = "POSS";
	public static final String PREP = "PREP";
	public static final String FORMULA = "FORMULA";
	public static final String VERBAL = "VERBAL";
	
	public static final String IC_PREFIX = "IC_";
	public static final String IC_MENTION_VIEW = "IC_MENTION_VIEW";

	public static final String NONE_MENTION = "NULL";

	public static Set<String> validStartPOS = new HashSet<String>();
	static {
		validStartPOS.add("D");
		validStartPOS.add("J");
		validStartPOS.add("V");
		validStartPOS.add("N");
		validStartPOS.add("P");
		validStartPOS.add("W");
		// ---------------------
		validStartPOS.add("C");
		validStartPOS.add("D");
		validStartPOS.add("E");
		validStartPOS.add("F");
		validStartPOS.add("I");
		validStartPOS.add("J");
		validStartPOS.add("L");
		validStartPOS.add("M");
		validStartPOS.add("N");
		validStartPOS.add("P");
		validStartPOS.add("R");
		validStartPOS.add("S");
		validStartPOS.add("T");
		validStartPOS.add("U");
		validStartPOS.add("V");
		validStartPOS.add("W");
	}
	public static Set<String> validEndPOS = new HashSet<String>();
	static {
		validEndPOS.add("N");
		validEndPOS.add("R");
		validEndPOS.add("P");
		validEndPOS.add("W");
		// ---------------------
		validEndPOS.add("C");
		validEndPOS.add("D");
		validEndPOS.add("E");
		validEndPOS.add("F");
		validEndPOS.add("I");
		validEndPOS.add("J");
		validEndPOS.add("L");
		validEndPOS.add("M");
		validEndPOS.add("N");
		validEndPOS.add("P");
		validEndPOS.add("R");
		validEndPOS.add("S");
		validEndPOS.add("T");
		validEndPOS.add("U");
		validEndPOS.add("V");
		validEndPOS.add("W");
	}

	public static Set<String> invalidStartPOS = new HashSet<String>();
	static {
		invalidStartPOS.add(".");
		invalidStartPOS.add(",");
		invalidStartPOS.add(":");
		invalidStartPOS.add(";");
		invalidStartPOS.add("!");
		invalidStartPOS.add("$");
		invalidStartPOS.add("*");
		// -----------------------
		// invalidStartPOS.add("POS");
	}

	public static Set<String> invalidEndPOS = new HashSet<String>();
	static {
		invalidEndPOS.add(".");
		invalidEndPOS.add(",");
		invalidEndPOS.add(":");
		invalidEndPOS.add(";");
		invalidEndPOS.add("!");
		invalidEndPOS.add("$");
		invalidEndPOS.add("*");
		// -----------------------
		// invalidEndPOS.add("D");
		// invalidEndPOS.add("J");
		// invalidEndPOS.add("V");
		// invalidEndPOS.add("POS");
	}

	// the following by YS
	public static Set<String> mentionEndPos = new HashSet<String>();
	static {
		mentionEndPos.add("NN");
		mentionEndPos.add("NNP");
		mentionEndPos.add("NNS");
		mentionEndPos.add("NNPS");
		mentionEndPos.add("PRP");
		mentionEndPos.add("PRP$");
		mentionEndPos.add("WP");
		mentionEndPos.add("WDT");
		mentionEndPos.add("WRB");
		mentionEndPos.add("WP$");
		mentionEndPos.add("JJ");
		mentionEndPos.add("JJR");
		mentionEndPos.add("JJS");
		mentionEndPos.add("VB");
		mentionEndPos.add("VBZ");
		mentionEndPos.add("VBG");
		mentionEndPos.add("VBP");
		mentionEndPos.add("VBD");
		mentionEndPos.add("VBN");
		mentionEndPos.add("FW");
		mentionEndPos.add("RB");
		mentionEndPos.add("CD");
		mentionEndPos.add("DT");
	}
	public static Set<String> mentionStartPos = new HashSet<String>();
	static {
		mentionStartPos.add("NNP");
		mentionStartPos.add("NN");
		mentionStartPos.add("NNS");
		mentionStartPos.add("NNPS");
		mentionStartPos.add("PRP");
		mentionStartPos.add("PRP$");
		mentionStartPos.add("WP");
		mentionStartPos.add("WDT");
		mentionStartPos.add("WRB");
		mentionStartPos.add("WP$");
		mentionStartPos.add("JJ");
		mentionStartPos.add("JJR");
		mentionStartPos.add("JJS");
		mentionStartPos.add("DT");
		mentionStartPos.add("PDT");
		mentionStartPos.add("CD");
		mentionStartPos.add("FW");
		
		mentionStartPos.add("RB");
		mentionStartPos.add("RBR");
		mentionStartPos.add("RBS");
		mentionStartPos.add("IN");
		mentionStartPos.add("VB");
		mentionStartPos.add("VBG");
		mentionStartPos.add("VBN");
		mentionStartPos.add("VBZ");
		mentionStartPos.add("VBP");
		mentionStartPos.add("VBD");
	}
	public static Set<String> mentionSinglePos = new HashSet<String>();
	static {
		mentionSinglePos.add("FW");
		mentionSinglePos.add("JJ");
		mentionSinglePos.add("NN");
		mentionSinglePos.add("NNP");
		mentionSinglePos.add("NNPS");
		mentionSinglePos.add("NNS");
		mentionSinglePos.add("PRP$");
		mentionSinglePos.add("PRP");
		mentionSinglePos.add("RB");
		mentionSinglePos.add("VB");
		mentionSinglePos.add("WP$");
		mentionSinglePos.add("WP");
		mentionSinglePos.add("CD");
		mentionSinglePos.add("DT");
		mentionSinglePos.add("WDT");
		mentionSinglePos.add("WRB");
	}
	
	public static Set<String> setXMLTags = new HashSet<String>();
	static {
		setXMLTags.add("ANNOTATION");
		setXMLTags.add("BODY");
		setXMLTags.add("DATE");
		setXMLTags.add("DATETIME");
		setXMLTags.add("DATE_TIME");
		setXMLTags.add("DOC");
		setXMLTags.add("DOCID");
		setXMLTags.add("DOCNO");
		setXMLTags.add("DOCTYPE");
		setXMLTags.add("ENDTIME");
		setXMLTags.add("END_TIME");
		setXMLTags.add("FOOTER");	// not found in either ace04, ace05, nor IC
		setXMLTags.add("HEADER");
		setXMLTags.add("HEADLINE");
		setXMLTags.add("KEYWORD");	// not found in either ace04, ace05, nor IC
		setXMLTags.add("P");
		setXMLTags.add("POST");
		setXMLTags.add("POSTDATE");
		setXMLTags.add("POSTER");
		setXMLTags.add("QUOTE");
		setXMLTags.add("SLUG");
		setXMLTags.add("SPEAKER");
		setXMLTags.add("SUBJECT");
		setXMLTags.add("TEXT");
		setXMLTags.add("TRAILER");
		setXMLTags.add("TURN");
	}
	
	public static Set<String> relationsToIgnore = new HashSet<String>();
	static {
		// I might want to ignore: DISC PHYS:Located PHYS:Near EMP-ORG:Partner
		//relationsToIgnore.add("PHYS:Located");
		relationsToIgnore.add("DISC");
		//relationsToIgnore.add("PHYS:Near");
	}

	// these relations are allowed to be NOT picked up by the premod, poss, prep, and formula patterns
	public static Set<String> verbalRelations = new HashSet<String>();
	static {
		verbalRelations.add("m1-PHYS:Located-m2");
		verbalRelations.add("m1-PHYS:Near-m2");
		verbalRelations.add("m2-PHYS:Located-m1");
		verbalRelations.add("m2-PHYS:Near-m1");
		verbalRelations.add(NO_RELATION);
		verbalRelations.add("m1-ART:User-or-Owner-m2");
		verbalRelations.add("m2-ART:User-or-Owner-m1");
		verbalRelations.add("m1-EMP-ORG:Employ-Executive-m2");
		verbalRelations.add("m2-EMP-ORG:Employ-Executive-m1");
		verbalRelations.add("m1-EMP-ORG:Employ-Staff-m2");
		verbalRelations.add("m2-EMP-ORG:Employ-Staff-m1");
		verbalRelations.add("m1-EMP-ORG:Employ-Undetermined-m2");
		verbalRelations.add("m2-EMP-ORG:Employ-Undetermined-m1");
		verbalRelations.add("m1-GPE-AFF:Citizen-or-Resident-m2");
		verbalRelations.add("m2-GPE-AFF:Citizen-or-Resident-m1");
		verbalRelations.add("m1-PER-SOC:Family-m2");
		verbalRelations.add("m2-PER-SOC:Family-m1");
		verbalRelations.add("m1-PHYS:Part-Whole-m2");
		verbalRelations.add("m2-PHYS:Part-Whole-m1");
	}
	
	public static Set<String> binaryRelationLabels = new HashSet<String>();
	static {
		binaryRelationLabels.add(new String("HAS_RELATION"));
		binaryRelationLabels.add(Constants.NO_RELATION);
	}
	
	public static Set<String> coarseRelationLabels = new HashSet<String>();
	static {
		coarseRelationLabels.add("m1-ART-m2");
		coarseRelationLabels.add("m1-EMP-ORG-m2");
		coarseRelationLabels.add("m1-GPE-AFF-m2");
		coarseRelationLabels.add("m1-OTHER-AFF-m2");
		coarseRelationLabels.add("m1-PER-SOC-m2");
		coarseRelationLabels.add("m1-PHYS-m2");
		coarseRelationLabels.add("m2-ART-m1");
		coarseRelationLabels.add("m2-EMP-ORG-m1");
		coarseRelationLabels.add("m2-GPE-AFF-m1");
		coarseRelationLabels.add("m2-OTHER-AFF-m1");
		coarseRelationLabels.add("m2-PER-SOC-m1");
		coarseRelationLabels.add("m2-PHYS-m1");
		coarseRelationLabels.add(Constants.NO_RELATION);
	}
	
	public static Set<String> fineRelationLabels = new HashSet<String>();
	static {
		fineRelationLabels.add("m1-ART:Inventor-or-Manufacturer-m2");
		fineRelationLabels.add("m1-ART:Other-m2");
		fineRelationLabels.add("m1-ART:User-or-Owner-m2");
		fineRelationLabels.add("m1-EMP-ORG:Employ-Executive-m2");
		fineRelationLabels.add("m1-EMP-ORG:Employ-Staff-m2");
		fineRelationLabels.add("m1-EMP-ORG:Employ-Undetermined-m2");
		fineRelationLabels.add("m1-EMP-ORG:Member-of-Group-m2");
		fineRelationLabels.add("m1-EMP-ORG:Other-m2");
		fineRelationLabels.add("m1-EMP-ORG:Partner-m2");
		fineRelationLabels.add("m1-EMP-ORG:Subsidiary-m2");
		fineRelationLabels.add("m1-GPE-AFF:Based-In-m2");
		fineRelationLabels.add("m1-GPE-AFF:Citizen-or-Resident-m2");
		fineRelationLabels.add("m1-GPE-AFF:Other-m2");
		fineRelationLabels.add("m1-OTHER-AFF:Ethnic-m2");
		fineRelationLabels.add("m1-OTHER-AFF:Ideology-m2");
		fineRelationLabels.add("m1-OTHER-AFF:Other-m2");
		fineRelationLabels.add("m1-PER-SOC:Business-m2");
		fineRelationLabels.add("m1-PER-SOC:Family-m2");
		fineRelationLabels.add("m1-PER-SOC:Other-m2");
		fineRelationLabels.add("m1-PHYS:Located-m2");
		fineRelationLabels.add("m1-PHYS:Near-m2");
		fineRelationLabels.add("m1-PHYS:Part-Whole-m2");
		fineRelationLabels.add("m2-ART:Inventor-or-Manufacturer-m1");
		fineRelationLabels.add("m2-ART:Other-m1");
		fineRelationLabels.add("m2-ART:User-or-Owner-m1");
		fineRelationLabels.add("m2-EMP-ORG:Employ-Executive-m1");
		fineRelationLabels.add("m2-EMP-ORG:Employ-Staff-m1");
		fineRelationLabels.add("m2-EMP-ORG:Employ-Undetermined-m1");
		fineRelationLabels.add("m2-EMP-ORG:Member-of-Group-m1");
		fineRelationLabels.add("m2-EMP-ORG:Other-m1");
		fineRelationLabels.add("m2-EMP-ORG:Partner-m1");
		fineRelationLabels.add("m2-EMP-ORG:Subsidiary-m1");
		fineRelationLabels.add("m2-GPE-AFF:Based-In-m1");
		fineRelationLabels.add("m2-GPE-AFF:Citizen-or-Resident-m1");
		fineRelationLabels.add("m2-GPE-AFF:Other-m1");
		fineRelationLabels.add("m2-OTHER-AFF:Ethnic-m1");
		fineRelationLabels.add("m2-OTHER-AFF:Ideology-m1");
		fineRelationLabels.add("m2-OTHER-AFF:Other-m1");
		fineRelationLabels.add("m2-PER-SOC:Business-m1");
		fineRelationLabels.add("m2-PER-SOC:Family-m1");
		fineRelationLabels.add("m2-PER-SOC:Other-m1");
		fineRelationLabels.add("m2-PHYS:Located-m1");
		fineRelationLabels.add("m2-PHYS:Near-m1");
		fineRelationLabels.add("m2-PHYS:Part-Whole-m1");
		fineRelationLabels.add(Constants.NO_RELATION);
	}
	
	public static Set<String> freqSRLArguments = new HashSet<String>();
	static {
		freqSRLArguments.add("A0");
		freqSRLArguments.add("A1");
		freqSRLArguments.add("A2");
		freqSRLArguments.add("AM-ADV");
		freqSRLArguments.add("AM-DIR");
		freqSRLArguments.add("AM-DIS");
		freqSRLArguments.add("AM-EXT");
		freqSRLArguments.add("AM-LOC");
		freqSRLArguments.add("AM-MNR");
		freqSRLArguments.add("AM-MOD");
		freqSRLArguments.add("AM-NEG");
		freqSRLArguments.add("AM-PRD");
		freqSRLArguments.add("AM-PRP");
		freqSRLArguments.add("AM-REC");
		freqSRLArguments.add("AM-TMP");
	}
	
	public static HashMap<String, String> aceToIcMentionsRemap = new HashMap<String, String>();
	static {
		//FAC:Building			One or more buildings that can be referred to as a unit, e.g.: the [national archives]
		//IC: Building
		aceToIcMentionsRemap.put("FAC:Building", "Building");//
		
		//GPE:County-or-District	Any county, district, or prefecture, e.g.: [Palm Beach] and [Volusia] [counties]
		// IC: Municipality
		aceToIcMentionsRemap.put("GPE:County-or-District", "Municipality");
		
		//GPE:Nation		Any nation, e.g.: Japan, Spain
		//IC: NationState
		aceToIcMentionsRemap.put("GPE:Nation", "NationState");//
		
		//GPE:Population-Center	E.g.: Washington, New Delhi, Salzburg
		//IC: Municipality
		aceToIcMentionsRemap.put("GPE:Population-Center", "Municipality");
		
		aceToIcMentionsRemap.put("GPE:Municipality", "Municipality");//
		
		//GPE:State-or-Province	State or province, e.g.: Texas
		//IC: StateOrProvince
		aceToIcMentionsRemap.put("GPE:State-or-Province", "StateOrProvince");//
		
		//ORG:Commercial	Organizations focusing primarily on profits. Also includes industries and industrial sectors. E.g. [Major League Baseball], [Techsource Marine Industries], the [Press Agency]
		//IC: CommericalOrganization
		aceToIcMentionsRemap.put("ORG:Commercial", "CommercialOrganization");//
		
		//ORG:Educational	E.g.: the [Applied Research Laboratory], [Pennsylvania State University]
		//IC: EducationalInstitution
		aceToIcMentionsRemap.put("ORG:Educational", "EducationalInstitution");//
		
		//ORG:Government	Organizations dealing with the affairs of government or the state. Political parties are not included here. E.g.: a former [KGB] agent, the [court], the security [services], [Congress]
		//IC: GovernmentOrganization
		aceToIcMentionsRemap.put("ORG:Government", "GovernmentOrganization");//
		
		aceToIcMentionsRemap.put("ORG:Terrorist", "TerroristOrganization");//
		aceToIcMentionsRemap.put("ORG:Political", "PoliticalParty");//
		
		//PER		Includes a single person, groups of people (e.g. family, painters, linguists), as well as ethnic and religious groups (the Arabs, the Christians)
		//IC: Person
		aceToIcMentionsRemap.put("PER", "Person");//
		
		//WEA:Exploding		Weapons that are designed to accomplish damage through explosion, e.g.: explosives
		//IC: Bomb
		aceToIcMentionsRemap.put("WEA:Exploding", "Bomb");//
		
		/*
		WEA:Blunt			Weapons used as a bludgeoning instrument, e.g.: a [baseball bat]
		IC: Weapon

		WEA:Chemical		E.g.: Sarin gas
		IC: Weapon

		WEA:Nuclear			E.g.: nuclear [missiles]
		IC: Weapon

		WEA:Other
		IC: Weapon

		WEA:Projectile		E.g.: bullets
		IC: Weapon

		WEA:Sharp			E.g.: knife
		IC: Weapon

		WEA:Shooting		Weapons used to send projectile objects at great speed, e.g.: pistol
		IC: Weapon
		*/
		//aceToIcMentionsRemap.put("WEA:Blunt", "Weapon");
		//aceToIcMentionsRemap.put("WEA:Chemical", "Weapon");
		//aceToIcMentionsRemap.put("WEA:Nuclear", "Weapon");
		//aceToIcMentionsRemap.put("WEA:Other", "Weapon");
		//aceToIcMentionsRemap.put("WEA:Projectile", "Weapon");
		//aceToIcMentionsRemap.put("WEA:Sharp", "Weapon");
		//aceToIcMentionsRemap.put("WEA:Shooting", "Weapon");
	}
	
	public static HashMap<String, String> aceToIcRelationsRemap = new HashMap<String, String>();
	static {
		//GEN-AFF:Citizen						arg1 is a citizen of arg2
		//Person hasCitizenship NationState		arg1 is a citizen of arg2
		aceToIcRelationsRemap.put("m1-GEN-AFF:Citizen-m2", "m1-hasCitizenship-m2");
		aceToIcRelationsRemap.put("m2-GEN-AFF:Citizen-m1", "m2-hasCitizenship-m1");
		
		//ORG-AFF:Employ-Executive				arg1 is an executive of arg2 *change direction
		//HumanAgent employs Person				// HumanAgent can be ORG
		//HumanOrganization isLedBy Person		arg1 has leader arg2
		aceToIcRelationsRemap.put("m1-ORG-AFF:Employ-Executive-m2", "m2-isLedBy-m1");
		aceToIcRelationsRemap.put("m2-ORG-AFF:Employ-Executive-m1", "m1-isLedBy-m2");
		
		//ORG-AFF:Employ-Staff					arg1 is a staff of arg2	*change direction
		//HumanAgent employs Person				arg1 employs arg2
		aceToIcRelationsRemap.put("m1-ORG-AFF:Employ-Staff-m2", "m2-employs-m1");
		aceToIcRelationsRemap.put("m2-ORG-AFF:Employ-Staff-m1", "m1-employs-m2");
		
		//ORG-AFF:Member						arg1 is a member of arg2 *change direction
		//Group hasMember Thing					arg1 has member arg2
		aceToIcRelationsRemap.put("m1-ORG-AFF:Member-m2", "m2-hasMember-m1");
		aceToIcRelationsRemap.put("m2-ORG-AFF:Member-m1", "m1-hasMember-m2");
		
		//ORG-AFF:Student-Alum							arg1 is an alum of arg2
		//Person attendedSchool EducationalInstitution	arg1 attended arg2
		aceToIcRelationsRemap.put("m1-ORG-AFF:Student-Alum-m2", "m1-attendedSchool-m2");
		aceToIcRelationsRemap.put("m2-ORG-AFF:Student-Alum-m1", "m2-attendedSchool-m1");
		
		//ORG-AFF:Subsidiary										arg1 is a subsidiary of arg2 *change direction
		//HumanOrganization hasSubOrganization HumanOrganization	arg1 has arg2 as sub
		aceToIcRelationsRemap.put("m1-ORG-AFF:Subsidiary-m2", "m2-hasSubOrganization-m1");
		aceToIcRelationsRemap.put("m2-ORG-AFF:Subsidiary-m1", "m1-hasSubOrganization-m2");
		
		//PER-SOC:hasChild						arg1 hasChild arg2
		//Person hasChild Person				arg1 has arg2 as child
		aceToIcRelationsRemap.put("m1-PER-SOC:hasChild-m2", "m1-hasChild-m2");
		aceToIcRelationsRemap.put("m2-PER-SOC:hasChild-m1", "m2-hasChild-m1");
		
		//PER-SOC:hasSibling
		//Person hasSibling Person		should be undirected
		aceToIcRelationsRemap.put("PER-SOC:hasSibling", "hasSibling");
		
		//PER-SOC:hasSpouse
		//Person hasSpouse Person		should be undirected
		aceToIcRelationsRemap.put("PER-SOC:hasSpouse", "hasSpouse");
		
		//PHYS:Gpe-Part-Whole									arg1 is a part of arg2 *change direction
		//GeopoliticalEntity hasSubGPE GeopoliticalEntity		arg1 is larger than arg2
		aceToIcRelationsRemap.put("m1-PHYS:Gpe-Part-Whole-m2", "m2-hasSubGPE-m1");
		aceToIcRelationsRemap.put("m2-PHYS:Gpe-Part-Whole-m1", "m1-hasSubGPE-m2");
	}
	
}
