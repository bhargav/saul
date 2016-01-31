package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Constants;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.ResourceManager;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Util;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.CleanDoc;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.Mention;
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.SemanticRelation;

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.mention.MentionDetector;

import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;

public class DataLoader {

	protected static MyCuratorClient curator = MyCuratorClient.getInstance();

	/*
	public static List<Document> getACEDocuments(String masterfilename) throws FileNotFoundException {
		List<Document> docs = new ArrayList<Document>();
		ArrayList<String> files = IOManager.readLines(masterfilename);
		for (String file : files) {
			Document myDoc = annotateACEText(ResourceManager.getProjectRoot() + file);
			if (myDoc != null) { docs.add(myDoc); }
		}
		return docs;
	}
	*/

	public static List<CleanDoc> readACEDocumentsAnnotations(String masterfilename) throws FileNotFoundException {
		List<CleanDoc> docs = new ArrayList<CleanDoc>();
		ArrayList<String> files = IOManager.readLines(masterfilename);
		for (String file : files) {
			CleanDoc doc = readACEData(ResourceManager.getDataPath()+file);
			if (doc != null) { docs.add(doc); }
		}
		return docs;
	}
	
	
	//////////////////
//	public static List<Document> getICDocuments(String filename, String xmlDir, String srcDir) {
//		List<Document> docs = new ArrayList<Document>();
//		ArrayList<String> files = IOManager.readLines(filename);
//		for (String file : files) {
//			CleanDoc doc = readICData(file, xmlDir, srcDir);
//			Document myDoc = annotateSourceText(doc);
//			if (myDoc != null) { docs.add(myDoc); }
//		}
//		return docs;
//	}

	public static List<Document> getACEDocuments(String masterfilename) throws FileNotFoundException {
		List<Document> docs = new ArrayList<Document>();
		ArrayList<String> files = IOManager.readLines(masterfilename);
		for (String file : files) {
			CleanDoc doc = readACEData(ResourceManager.getDataPath()+file);
			Document myDoc = annotateSourceText(doc);
			/*
			System.out.println("== DataLoader.getACEDocuments: " + file + " ==");
			System.out.println(myDoc.getTextAnnotation().getTokenizedText());
			System.out.println("*GOLD_MENTION_VIEW:\n");
			for(Constituent c : myDoc.getTextAnnotation().getView(Constants.GOLD_MENTION_VIEW).getConstituents()) {
				System.out.print(c.getAttribute("id")+" ["+c.getSurfaceString()+"]"+c.getStartSpan()+","+c.getEndSpan()+" "+c.getLabel()+"|||");
				Set<String> attributeKeys = c.getAttributeKeys();
				for(Iterator<String> it=attributeKeys.iterator(); it.hasNext(); ) {
					String mykey = it.next();
					System.out.print(" "+mykey+"|"+c.getAttribute(mykey));
				}
				System.out.println("");
			}
			System.out.println("*GOLD_RELATION_VIEW\n"+myDoc.getTextAnnotation().getView(Constants.GOLD_RELATION_VIEW)+"\n");
			*/
			//int numSent = myDoc.getTextAnnotation().getNumberOfSentences();
			//for(int i=0; i<numSent; i++) {
			//	System.out.println(myDoc.getTextAnnotation().getSentence(i));
			//}
			
			if (myDoc != null) { docs.add(myDoc); }
		}
		return docs;
	}
	public static Document getACEDocument(String filename) throws FileNotFoundException {
		CleanDoc doc = readACEData(ResourceManager.getDataPath()+filename);
		Document myDoc = annotateSourceText(doc);
		return myDoc;
	}
	//////////////////
	
	//////////////////
//	private static CleanDoc readICData(String file, String xmlDir, String srcDir) {
//		String xmlFile = xmlDir + "/" + file + ".gui.xml";
//		String srcFile = srcDir + "/" + file + ".src.xml";
//		CleanDoc doc = new CleanDoc();
//		doc.setId(file);
//		doc.setCorpusId("IC");
//
//		ICDataHandler.readMentionRelationAnnotationInfo(xmlFile, doc);	// read relations and mentions from IC xml file
//		ICDataHandler.addAnnotationsToFile(srcFile, doc);				// add the mention annotations (start, end tags) to the src text
//
//		return doc;
//	}

	private static CleanDoc readACEData(String file) {
		String xmlFile = file + ".apf.xml";
		String sgmFile = file + ".sgm";
		
		System.out.println("xmlFile:"+xmlFile);
		System.out.println("sgmFile:"+sgmFile);
		
		CleanDoc doc = new CleanDoc();
		doc.setId(file);
		if(file.indexOf("ace2005/nw/")!=-1) {
			doc.setCorpusId("ace2005nw");
		}
		else if(file.indexOf("ace2005/bc/")!=-1) {
			doc.setCorpusId("ace2005bc");
		}
		else if(file.indexOf("ace2005/bn/")!=-1) {
			doc.setCorpusId("ace2005bn");
		}
		else if(file.indexOf("ace2005/cts/")!=-1) {
			doc.setCorpusId("ace2005cts");
		}
		else if(file.indexOf("ace2005/un/")!=-1) {
			doc.setCorpusId("ace2005un");
		}
		else if(file.indexOf("ace2005/wl/")!=-1) {
			doc.setCorpusId("ace2005wl");
		}
		else if(file.indexOf("ace2004/nwire/")!=-1) {
			doc.setCorpusId("ace2004nwire");
		}
		else if(file.indexOf("ace2004/bnews/")!=-1) {
			doc.setCorpusId("ace2004bnews");
		}
		else if(file.indexOf("ace2004_normalized/nwire/")!=-1) {
			doc.setCorpusId("ace2004nwire");
		}
		else if(file.indexOf("ace2004_normalized/bnews/")!=-1) {
			doc.setCorpusId("ace2004bnews");
		}
		else if(file.indexOf("ace2004_normalized/arabic_treebank/")!=-1) {
			doc.setCorpusId("ace2004atb");
		}
		else if(file.indexOf("ace2004_normalized/chinese_treebank/")!=-1) {
			doc.setCorpusId("ace2004ctb");
		}
		else if(file.indexOf("ace2005_normalized/bc/")!=-1) {
			doc.setCorpusId("ace2005bc");
		}
		else if(file.indexOf("ace2005_normalized/nw/")!=-1) {
			doc.setCorpusId("ace2005nw");
		}
		else if(file.indexOf("ace2005_normalized/wl/")!=-1) {
			doc.setCorpusId("ace2005wl");
		}
		
		ACEDataHandler.readMentionRelationAnnotationInfo(xmlFile, doc);	// read relations and mentions from ACE xml file
		ACEDataHandler.addAnnotationsToFile(sgmFile, doc);				// add the mention annotations (start, end tags) to the sgm text
		
		return doc;
	}
	//////////////////
	
	/*
	public static Document readACETextAnnotation(String filename) {
		String xmlFile = filename + ".apf.xml";

		// Reading mentions from the XML file
		ArrayList<String> xmlLines = IOManager.readLines(xmlFile);
		List<Mention> mentions = ACEDataHandler.readMentionsAnnotationInfo(xmlLines);
		List<SemanticRelation> relations = ACEDataHandler.readRelationsAnnotationInfo(xmlLines, mentions);
		
		Document doc = new Document();
		doc.setRelations(relations);
		return doc;
	}
	*/
	/*
	public static Document annotateACEText(String filename) {
		TextAnnotation ta=null;
		String sgmFile = filename + ".sgm";
		String xmlFile = filename + ".apf.xml";
		
		// Reading SGM file
		StringBuffer sgm = ACEDataHandler.readingSgmFile(sgmFile);
		String cleanSgm = ACEDataHandler.removeXMLTags(sgm);
		String cpCleanSgm = new String(cleanSgm);

		// Reading mentions from the XML file
		ArrayList<String> xmlLines = IOManager.readLines(xmlFile);
		List<Mention> mentions = ACEDataHandler.readMentionsAnnotationInfo(xmlLines);
		List<SemanticRelation> relations = ACEDataHandler.readRelationsAnnotationInfo(xmlLines, mentions);
		
		//Document doc = new Document();
		//doc.setRelations(relations);
		//return doc;
	
		// ================ START ===========

		ACEDataHandler.sortAce05MentionDes(mentions);

		int n = mentions.size();
		int i = 0;
		while (i < n) {
			Ace05Mention m = mentions.get(i);

			// Extents
			int endExtent = m.endExtent;
			int startExtent = m.startExtent;
			cleanSgm = cleanSgm.substring(0, endExtent + 1) + "</m_" + m.type
					+ "_" + m.id + "_m>" + cleanSgm.substring(endExtent + 1);
			cleanSgm = cleanSgm.substring(0, startExtent) + "<m_" + m.type
					+ "_" + m.id + "_m>" + cleanSgm.substring(startExtent);

			// Heads
			
			//  int endHead = m.endHead; cpCleanSgm = cpCleanSgm.substring(0,
			//  endHead + 1) + "</m_" + m.type + "_" + m.id + "_m>" +
			//  cpCleanSgm.substring(endHead + 1); cpCleanSgm =
			//  cpCleanSgm.substring(0, startExtent) + "<m_" + m.type + "_" +
			//  m.id + "_m>" + cpCleanSgm.substring(startExtent);
			 

			ACEDataHandler.modifyOffset(mentions, i);
			i++;
		}

		cleanSgm = cleanSgm.trim();
		cleanSgm = cleanSgm.replaceAll("\\s\\s+", " ");

		cpCleanSgm = cpCleanSgm.trim();
		cpCleanSgm = cpCleanSgm.replaceAll("\\s\\s+", " ");

		// System.out.println();
		// System.out.println(cleanSgm);
		// System.out.println();

		String text = ACEDataHandler.cleanMentionTags(cleanSgm);

		List<Ace05Mention> newMentions = ACEDataHandler.getMentionsFromMentionTags(cleanSgm);
		List<Span> spanExtents = new ArrayList<Span>();
		for (Ace05Mention m : newMentions) {
			Span span = new Span(m.startExtent, m.endExtent);
			span.label = m.type;
			spanExtents.add(span);
		}

		Labeling extentLabels = new Labeling();
		extentLabels.setRawText(text);
		extentLabels.setLabels(spanExtents);

		TextAnnotation ta = null;
		// -----------------
		try {
			boolean forceUpdate = ResourceManager.getCuratorForceUpdate();
			ta = curator.getTextAnnotation("ACE05", filename, text, forceUpdate);
			curator.addStanfordParse(ta, forceUpdate);
			curator.addPOSView(ta, forceUpdate);
			curator.addChunkView(ta, forceUpdate);
			curator.addNamedEntityView(ta, forceUpdate);
			// curator.addNumericalQuantitiesView(ta, forceUpdate);

		} catch (Exception e) {
			e.printStackTrace();
		}

		SpanLabelView goldTypedView = alignLabelingToSpans(Constants.GOLD_MENTION_VIEW, ta, extentLabels);

		ta.addView(Constants.GOLD_MENTION_VIEW, goldTypedView);

		return new Document(ta);
	}
	*/
	
	protected static SpanLabelView alignLabelingToSpans(String viewName, TextAnnotation ta, Labeling spanLabeling) {
		List<Span> labels = spanLabeling.getLabels();
		double score = spanLabeling.getScore();
		String generator = spanLabeling.getSource();

		SpanLabelView view = new SpanLabelView(viewName, generator, ta, score, true);

		for (Span span : labels) {

			int tokenId = ta.getTokenIdFromCharacterOffset(span.getStart());
			//System.out.println("char>>" + spanLabeling.rawText.substring(span.getStart(), span.getEnding()));
			
			int endTokenId = ta.getTokenIdFromCharacterOffset(span.getEnding() - 1);

			view.addSpanLabel(tokenId, endTokenId + 1, span.getLabel(), span.getScore());

			if (span.isSetAttributes() && span.getAttributes().size() > 0) {

				Constituent newConstituent = view.getConstituentsCoveringSpan(tokenId, endTokenId + 1).get(0);

				if (span.isSetAttributes()) {
					for (String attribKey : span.getAttributes().keySet()) {
						newConstituent.addAttribute(attribKey, span.getAttributes().get(attribKey));
					}
				}
				//System.out.println("token>>" +newConstituent);
			}
		}
		return view;
	}

	
	private static Document annotateSourceText(CleanDoc doc) {
		//ArrayList<String> rawTexts = doc.getRawSource();
		//StringBuffer buf = new StringBuffer();
		//for (String t : rawTexts) {
		//	buf.append(t);
		//	buf.append(" ");
		//}
		//String rawText = buf.toString();

		/*
		System.out.println("******** printing out mentions *********");
		HashMap<Pair<Integer, Integer>, Mention> mentions = doc.getMentions();
		for (Pair<Integer, Integer> m : mentions.keySet()) {
			Mention mention = mentions.get(m);
			System.out.println("(" + m.getFirst() + "," + m.getSecond() + ") "
					+ rawText.substring(mention.getStartCharOffset(), mention.getEndCharOffset() + 1) + " - "
					+ rawText.substring(m.getFirst(), m.getSecond() + 1));
		}
		System.out.println("******** END *********");
		*/
		
		String cleanText;
		try {
			cleanText = doc.cleanRawText();	// this is where I will generate cleanMentions
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		
		//System.out.println("Clean text: " + cleanText);
		/*
		System.out.println("*** relations ***");
		{
			ArrayList<SemanticRelation> relations = doc.getRelations();
			for(int i=0; i<relations.size(); i++) {
				System.out.print(relations.get(i));
				System.out.print("|||"+relations.get(i).getM1());
				System.out.println("|||"+relations.get(i).getM2());
			}
		}
		System.out.println("*** end of relations ***");
		*/
		
		try {
			System.out.println("---------- cleanText ---------");
			System.out.println(cleanText);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	        Date date = new Date();
	        System.out.println( dateFormat.format(date) );
			//System.out.println("DataLoader.annotateSourceText A");

            HashSet<String> requiredViews = new HashSet();
            requiredViews.add(ViewNames.PARSE_STANFORD);
            requiredViews.add(ViewNames.DEPENDENCY_STANFORD);
            requiredViews.add(ViewNames.POS);
            requiredViews.add(ViewNames.SHALLOW_PARSE);
            requiredViews.add(ViewNames.NER_CONLL);
            requiredViews.add(ViewNames.WIKIFIER);

			TextAnnotation ta = curator.getTextAnnotation(doc.getCorpusId(), doc.getId(), cleanText, requiredViews);
			//System.out.println("---------- cleanText ---------");
			//System.out.println(cleanText);
			//System.out.println("----------------DetokenizedText----------");
			//System.out.println(ta.getDetokenizedText());
			System.out.println("DataLoader.annotateSourceText: Number of sentences = " + ta.getNumberOfSentences());
			for(int i=0; i<ta.getNumberOfSentences(); i++) {
				System.out.println(ta.getSentence(i));
			}
			System.out.println("");
			
//			try {
//				curator.addCorefView(ta, forceUpdate);
//			} catch(ServiceUnavailableException e1) {
//				e1.printStackTrace();
//				System.out.println(e1);
//			}

			// Get POS view
			SpanLabelView posView = (SpanLabelView) ta.getView(ViewNames.POS);
			List<Constituent> cons = posView.getConstituents();
			//for(int i=0; i<cons.size(); i++) {
			//	System.out.println(cons.get(i).getSurfaceString()+" "+cons.get(i).getSpan());
			//}
			//System.out.println(ta.getDetokenizedText().length()+"|||"+ta.getTokenizedText().length());
			//System.out.println("----------------DetokenizedText----------");
			//System.out.println(ta.getDetokenizedText());
			//System.out.println("---------------Text-----------");
			//System.out.println(ta.getText());
			//System.out.println(ta.getText().length());
			//System.out.println("------------------------------");
			List<Pair<Integer, Integer>> charOffsetsForTokens = Util.TokenAligner.getCharacterOffsets(ta, cons);
			if (cons.size() != charOffsetsForTokens.size()) {
				throw new Exception();
			}
			//System.out.println("cons="+cons);
			//System.out.println("charOffsetsForTokens="+charOffsetsForTokens);
			
			// ==========================
			/*
			 * Modifying mention offsets : adding token offsets for the mentions in relations; but note that mentionCons will store all IC gold mentions too
			 */

			List<Constituent> mentionCons = new ArrayList<Constituent>();	// these are mentions annotated by the IC annotators
																			// I will now add them to this mentionCons, where their start/end spans are tokens

			HashMap<Pair<Integer, Integer>, Mention> cleanMentions = doc.getCleanMentions();
			List<Mention> cleanMentionsList = new ArrayList<Mention>(cleanMentions.values());
			int n = cons.size();		// n = number of POS constituent
			boolean flag = false;
			
			for(int mIndex=0; mIndex<cleanMentionsList.size(); mIndex++) {	// go through each mention, to change their offsets (from char to token based)
				Mention m = cleanMentionsList.get(mIndex);
				int mStartCharOffset = m.getStartCharOffset();
				int mEndCharOffset = m.getEndCharOffset();
				int mHeadStartCharOffset = m.getHeadStartCharOffset();
				int mHeadEndCharOffset = m.getHeadEndCharOffset();
				int startTokenOffset=-1, endTokenOffset=-1, headStartTokenOffset=-1, headEndTokenOffset=-1;
				//if(m.getId().compareTo("66-119")==0) {
				//	System.out.println("**** "+mStartCharOffset+","+mEndCharOffset+" "+mHeadStartCharOffset+","+mHeadEndCharOffset); 
				//}
				flag = false;
				for (int i = 0; i < n; i++) {					// for each POS constituent
					if(mStartCharOffset == charOffsetsForTokens.get(i).getFirst().intValue()) {
						startTokenOffset = i;
						//if(m.getId().compareTo("66-119")==0) {
						//	System.out.println("**** startTokenOffset="+startTokenOffset);
						//}
						for (int j = i; j < n; j++) {
							Pair<Integer, Integer> charOffsetForToken = charOffsetsForTokens.get(j);
							int startCharOffsetForToken = charOffsetForToken.getFirst();
							int endCharOffsetForToken = charOffsetForToken.getSecond() - 1;
							
							if(mHeadStartCharOffset == startCharOffsetForToken) {
								headStartTokenOffset = j;
							}
							if(mHeadEndCharOffset == endCharOffsetForToken) {
								headEndTokenOffset = j;
							}
							if((mHeadEndCharOffset+1) == endCharOffsetForToken) {
								if(ta.getToken(j).charAt( ta.getToken(j).length()-1 )=='.') {
									headEndTokenOffset = j;
								}
							}
							
							if (mEndCharOffset == endCharOffsetForToken) {			// I have found a pos constituent whose end char offset is the same as end char offset of the clean mention
								flag = true;
								endTokenOffset = j;
								//System.out.println("**** endTokenOffset="+endTokenOffset);
								break;
							}
							if((mEndCharOffset+1) == endCharOffsetForToken) {
								if(ta.getToken(j).charAt( ta.getToken(j).length()-1 )=='.') {
									flag = true;
									endTokenOffset = j;
									break;
								}
							}
							if (flag == true) { break; }
						}
						if (flag == false) {
							System.out.println("ERROR: Unable to find the corresponding end constituent.");
							System.out.println(m.getId()+" ["+m.getSurfaceString()+"]"+m.getStartCharOffset()+","+m.getEndCharOffset());
						} else {
							// headStartTokenOffset might still be -1 (i.e. not yet set). E.g. mention id 7-94 "lower-court" where "court" is the head
							// and so the head does not match the beginning span as lower-court is still kept as one single token ; so set to startTokenOffset
							if(headStartTokenOffset==-1) { headStartTokenOffset = startTokenOffset; }
							
							Constituent newCon = createNewConstituent(startTokenOffset, endTokenOffset + 1, Constants.GOLD_MENTION_VIEW, m.getFineSC(), ta);
							newCon.addAttribute("id", m.getId());
							newCon.addAttribute("headStartTokenOffset", new Integer(headStartTokenOffset).toString());
							newCon.addAttribute("headEndTokenOffset", new Integer(headEndTokenOffset+1).toString());
							newCon.addAttribute("sc", m.getSC());
							newCon.addAttribute("fineSc", m.getFineSC());
							//System.out.println("DataLoader.AnnotateSourceText: "+m);
							mentionCons.add(newCon);

							// Quang: Adding token offset for the mentions in the relations
							String mId = m.getId();
							doc.addRelationMentionTokenOffset(mId, startTokenOffset, endTokenOffset + 1);
							doc.addRelationMentionHeadTokenOffset(mId, headStartTokenOffset, headEndTokenOffset + 1);
						}
					}
					if (flag == true)
						break;		// so that I can go on to the next clean mention
				}
			}
			
			/*
			Set<Pair<Integer, Integer>> mKeys = cleanMentions.keySet();
			List<Pair<Integer, Integer>> keys = new ArrayList<Pair<Integer, Integer>>(mKeys);
			int n = cons.size();
			boolean flag = false;
			for (int p = 0; p < keys.size(); p++) {		// for each clean mention
				Pair<Integer, Integer> key = keys.get(p);
				flag = false;
				for (int i = 0; i < n; i++) {			// for each POS constituent
					Pair<Integer, Integer> offset = offsets.get(i);
					int a = key.getFirst().intValue();			// this is start char offset of clean mention
					int b = offset.getFirst().intValue();		// this is start char offset of a particular pos constituent
					if (a == b) {
						int jIdx = -1;
						for (int j = i; j < n; j++) {
							Pair<Integer, Integer> offsetE = offsets.get(j);
							int c = key.getSecond().intValue();
							int d = offsetE.getSecond() - 1;
							if (c == d) {			// I have found a pos constituent whose end char offset is the same as end char offset of the clean mention
								flag = true;
								jIdx = j;
								break;
							}
							if((c+1) == d) {
								//System.out.println("****"+ta.getToken(j)+"****");
								if(ta.getToken(j).charAt( ta.getToken(j).length()-1 )=='.') {
									//System.out.println("MATCH!");
									flag = true;
									jIdx = j;
									break;
								}
							}
							if (flag == true)
								break;
						}
						if (flag == false) {
							System.out.println("ERROR: Unable to find the corresponding end constituent.");
							Mention mention = cleanMentions.get(key);
							System.out.println(mention.getId()+" ["+mention.getSurfaceString()+"]"+mention.getStartCharOffset()+","+mention.getEndCharOffset());
						} else {
							Mention mention = cleanMentions.get(key);
							// i and jIdx+1 are starting/ending token offsets for the mention
							Constituent newCon = createNewConstituent(i, jIdx + 1, Constants.GOLD_MENTION_VIEW, mention.getSC(), ta);
							newCon.addAttribute("id", mention.getId());
							System.out.println("DataLoader.AnnotateSourceText: "+mention);
							mentionCons.add(newCon);

							// Quang: Adding token offset for the mentions in the relations
							String mId = mention.getId();
							doc.addRelationMentionTokenOffset(mId, i, jIdx + 1);
						}
					}
					if (flag == true)
						break;		// so that I can go on to the next clean mention
				}
			}
			// =================
			*/
			
			// =================
			/*
			 * Add mention view
			 */
			SpanLabelView mentionView = new SpanLabelView(Constants.GOLD_MENTION_VIEW, "Default", ta, 1.0, true);
			Util.sortConstituents(mentionCons);
			for (Constituent mCon : mentionCons) {
				mentionView.addConstituent(mCon);
				//System.out.println("mCon="+mCon + " "+mCon.getStartSpan()+","+mCon.getEndSpan());
				// startspan, endspan are the start & end token offsets
				//System.out.print("Constituents covering this:");
				//List<Constituent> coveringCons = mentionView.getConstituentsCoveringSpan(mCon.getStartSpan(), mCon.getEndSpan());
				//for(int i=0; i<coveringCons.size(); i++) {
				//	System.out.print("|||"+coveringCons.get(i));
				//}
				//System.out.println("");
				// TODO: CYS: What is the purpose of the following? get(0) just returns the constituent itself, right?
				mentionView.getConstituentsCoveringSpan(mCon.getStartSpan(), mCon.getEndSpan()).get(0).addAttribute("SPAN_ATTRIBUTE", "NULL");
			}
			ta.addView(Constants.GOLD_MENTION_VIEW, mentionView);
			// =================

			// =================
			/*
			 * Add relation view
			 */
			BinaryRelationView pav = getBinaryRelationView(ta, doc);
			ta.addView(Constants.GOLD_RELATION_VIEW, pav);
			// =================

			// =================
			/*
			 * Create a Document object
			 */

			Document myDoc = new Document(ta);
			
			return myDoc;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to annotate the following text with TextAnnotation:");
			System.out.println(cleanText);
			return null;
		}
	}
	
	
	
	/**
	 * @param ta
	 * @return
	 */
	/*
	private static PredicateArgumentView getPredicateArgumentView(TextAnnotation ta, CleanDoc doc) {
		PredicateArgumentView relationView = new PredicateArgumentView(Constants.GOLD_RELATION_VIEW, "GoldStandard", ta, 1.0);
		for (SemanticRelation rel : doc.getRelations()) {

			// Predicate
			// A dummy predicate token (from 0 to 1), which is useless, but we
			// need it to construct a predicate
			TreeSet<Integer> predicateTokens = new TreeSet<Integer>();
			predicateTokens.add(0);
			predicateTokens.add(1);

			Constituent predicate = new Constituent(rel.getCoarseLabel()+":"+rel.getFineLabel(), 1.0, Constants.GOLD_RELATION_VIEW, ta, predicateTokens, true);

			// Arguments
			List<Constituent> args = new ArrayList<Constituent>();
			List<String> relations = new ArrayList<String>();

			TreeSet<Integer> constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM1().getStartTokenOffset());
			constituentTokens.add(rel.getM1().getEndTokenOffset());
			Constituent arg = new Constituent(rel.getM1().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, constituentTokens, true);
			args.add(arg);
			relations.add("m1");

			constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM2().getStartTokenOffset());
			constituentTokens.add(rel.getM2().getEndTokenOffset());
			arg = new Constituent(rel.getM2().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, constituentTokens, true);
			args.add(arg);
			relations.add("m2");

			double[] scoresDoubleArray = new double[relations.size()];
			for (int relationId = 0; relationId < relations.size(); relationId++) {
				scoresDoubleArray[relationId] = 1.0;
			}

			relationView.addPredicateArguments(predicate, args, relations.toArray(new String[relations.size()]), scoresDoubleArray);
		}
		return relationView;
	}
	*/
	
	private static BinaryRelationView getBinaryRelationView(TextAnnotation ta, CleanDoc doc) {
		BinaryRelationView relationView = new BinaryRelationView(Constants.GOLD_RELATION_VIEW, "GoldStandard", ta, 1.0);
		for (SemanticRelation rel : doc.getRelations()) {
			TreeSet<Integer> constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM1().getStartTokenOffset());
			constituentTokens.add(rel.getM1().getEndTokenOffset());
			//Constituent m1 = new Constituent(rel.getM1().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, constituentTokens, true);
			Constituent m1 = new Constituent(rel.getM1().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, rel.getM1().getStartTokenOffset(), rel.getM1().getEndTokenOffset());
			
			constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM2().getStartTokenOffset());
			constituentTokens.add(rel.getM2().getEndTokenOffset());
			//Constituent m2 = new Constituent(rel.getM2().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, constituentTokens, true);
			Constituent m2 = new Constituent(rel.getM2().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, rel.getM2().getStartTokenOffset(), rel.getM2().getEndTokenOffset());
			
			Relation r = relationView.addRelation(rel.getFineLabel()+"|"+rel.getLexicalCondition(), m1, m2, 1.0);		// TODO  ask Vivek
			
			HashMap<String, String> rAttributes = new HashMap<String, String>();
			//if(rel.getCoarseLabel()!=null) { relationAttributes.put("coarseLabel", rel.getCoarseLabel()); }
			//if(rel.getFineLabel()!=null) { relationAttributes.put("fineLabel", rel.getFineLabel()); }
			//if(rel.getLexicalCondition()!=null) { relationAttributes.put("lexicalCondition", rel.getLexicalCondition()); }
			//if(rel.getId()!=null) { relationAttributes.put("id", rel.getId()); }
			//relationView.setRelationAttributes(r, rAttributes);
		}
		return relationView;
	}

	
	private static Constituent createNewConstituent(int start, int end, String viewName, String label, TextAnnotation ta) {
		Constituent con = new Constituent(label, viewName, ta, start, end);
		return con;
	}

	
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		testACEDataReader();
		//testICDataReader();
	}

	public static void testACEDataReader() throws FileNotFoundException {
		String filelist = ResourceManager.getProjectRoot() + "/data/ACE2004/allfiles";
		DataLoader loader = new DataLoader();
		List<Document> docs = loader.getACEDocuments(filelist);
		System.out.println("=========");

		MentionDetector detector = new MentionDetector();
		for (Document doc : docs) {
			TextAnnotation ta = doc.ta;
			if (ta == null)
				continue;

			// Labeling mention candidates
			detector.labelMentionCandidates(doc);

			SpanLabelView mentionView = (SpanLabelView) ta
					.getView(Constants.GOLD_MENTION_VIEW);
			List<Constituent> cons = mentionView.getConstituents();
//			System.out.println("Text: " + ta.getText());
			for (Constituent con : cons) {
				String[] tokens = ta.getTokensInSpan(con.getStartSpan(),
						con.getEndSpan());
				String t = new String();
				for (String s : tokens) {
					t += s + " ";
				}
//				System.out.println("(" + con.getStartSpan() + ","
//						+ con.getEndSpan() + ") >>>" + t + "<<<");
			}

			SpanLabelView candidateView = (SpanLabelView) ta
					.getView(Constants.CANDIDATE_MENTION_VIEW);
			List<Constituent> cands = candidateView.getConstituents();
//			System.out.println("--Candidates:");
			for (Constituent con : cands) {
				String[] tokens = ta.getTokensInSpan(con.getStartSpan(),
						con.getEndSpan());
				String t = new String();
				for (String s : tokens) {
					t += s + " ";
				}
//				System.out.println("(" + con.getStartSpan() + ","
//						+ con.getEndSpan() + ") >>>" + t + "<<<");
			}
		}

		try {
			Document myDoc = detector.labelMentionCandidates("Bill Gates is my figure. I wish that I can be as rich as him.");
			SpanLabelView candidateView = (SpanLabelView) myDoc.ta.getView(Constants.CANDIDATE_MENTION_VIEW);
			List<Constituent> cands = candidateView.getConstituents();
//			System.out.println("--Candidates:");
			for (Constituent con : cands) {
				String[] tokens = myDoc.ta.getTokensInSpan(con.getStartSpan(),
						con.getEndSpan());
				String t = new String();
				for (String s : tokens) {
					t += s + " ";
				}
//				System.out.println("(" + con.getStartSpan() + ","
//						+ con.getEndSpan() + ") >>>" + t + "<<<");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

    /*
	public static void testICDataReader() {
		String filelist = ResourceManager.getProjectRoot()
				+ "/data/IC/smallfilelist";
		String xmlDir = ResourceManager.getProjectRoot() + "/data/IC/gui_xml/";
		String srcDir = ResourceManager.getProjectRoot() + "/data/IC/src_xml/";
		//String outDir = ResourceManager.getProjectRoot() + "/data/IC/out_dir/";
		DataLoader loader = new DataLoader();
		List<Document> docs = loader.getICDocuments(filelist, xmlDir, srcDir);
		System.out.println("=========");

		MentionDetector detector = new MentionDetector();
		for (Document doc : docs) {
			TextAnnotation ta = doc.ta;
			if (ta == null)
				continue;

			// Labeling mention candidates
			detector.labelMentionCandidates(doc);

			SpanLabelView mentionView = (SpanLabelView) ta
					.getView(Constants.GOLD_MENTION_VIEW);
			List<Constituent> cons = mentionView.getConstituents();
			System.out.println("Text: " + ta.getText());
			for (Constituent con : cons) {
				String[] tokens = ta.getTokensInSpan(con.getStartSpan(),
						con.getEndSpan());
				String t = new String();
				for (String s : tokens) {
					t += s + " ";
				}
				System.out.println("(" + con.getStartSpan() + ","
						+ con.getEndSpan() + ") >>>" + t + "<<<");
			}

			PredicateArgumentView relationView = (PredicateArgumentView) ta
					.getView(Constants.GOLD_RELATION_VIEW);
			List<Constituent> preds = relationView.getPredicates();
			System.out.println("--Relations:");
			for (Constituent pred : preds) {
				System.out.println(pred.getLabel());
				List<Relation> relations = relationView.getArguments(pred);
				for (Relation rel : relations) {
					System.out.println("\t" + rel.getRelationName());
					Constituent tgt = rel.getTarget();
					String[] tokens = ta.getTokensInSpan(tgt.getStartSpan(),
							tgt.getEndSpan());
					String t = new String();
					for (String s : tokens) {
						t += s + " ";
					}
					System.out.println("\t\t" + "(" + tgt.getStartSpan() + ","
							+ tgt.getEndSpan() + ") >>>" + t + "<<< "
							+ tgt.getLabel());
				}
			}

			SpanLabelView candidateView = (SpanLabelView) ta
					.getView(Constants.CANDIDATE_MENTION_VIEW);
			List<Constituent> cands = candidateView.getConstituents();
			System.out.println("--Candidates:");
			for (Constituent con : cands) {
				String[] tokens = ta.getTokensInSpan(con.getStartSpan(),
						con.getEndSpan());
				String t = new String();
				for (String s : tokens) {
					t += s + " ";
				}
				System.out.println("(" + con.getStartSpan() + ","
						+ con.getEndSpan() + ") >>>" + t + "<<<");
			}
		}

		try {
			Document myDoc = detector.labelMentionCandidates("Bill Gates is my figure. I wish that I can be as rich as him.");
			SpanLabelView candidateView = (SpanLabelView) myDoc.ta.getView(Constants.CANDIDATE_MENTION_VIEW);
			List<Constituent> cands = candidateView.getConstituents();
			System.out.println("--Candidates:");
			for (Constituent con : cands) {
				String[] tokens = myDoc.ta.getTokensInSpan(con.getStartSpan(),
						con.getEndSpan());
				String t = new String();
				for (String s : tokens) {
					t += s + " ";
				}
				System.out.println("(" + con.getStartSpan() + ","
						+ con.getEndSpan() + ") >>>" + t + "<<<");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}*/
}
