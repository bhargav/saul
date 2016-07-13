/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;

import edu.illinois.cs.cogcomp.edison.utilities.WordNetHelper;

import edu.illinois.cs.cogcomp.illinoisRE.common.ListManager;
import edu.illinois.cs.cogcomp.illinoisRE.common.ResourceManager;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
import edu.illinois.cs.cogcomp.illinoisRE.mention.MentionUtil;
import edu.illinois.cs.cogcomp.illinoisRE.relation.RelationFeatures;
import edu.illinois.cs.cogcomp.illinoisRE.relation.RelationUtil;

import net.didion.jwnl.JWNLException;

import java.util.*;

public final class RelationStructuralFeatures {
    private static Set<String> frontPOSSequences;
    private static ListManager listManager = new ListManager();

    static {
        WordNetHelper.wordNetPropertiesFile = ResourceManager.getJNWLFilePath();

        // Initialize POS Sequences
        frontPOSSequences = new HashSet<>();
        frontPOSSequences.add("JJ");
        frontPOSSequences.add("JJ JJ");
        frontPOSSequences.add("JJ JJ JJ");
        frontPOSSequences.add("JJ and");
        frontPOSSequences.add("JJ and JJ");
        frontPOSSequences.add("DT");
        frontPOSSequences.add("DT RB");
        frontPOSSequences.add("DT JJ");
        frontPOSSequences.add("DT VBG");
        frontPOSSequences.add("DT VBD");
        frontPOSSequences.add("DT VBN");
        frontPOSSequences.add("DT CD");
        frontPOSSequences.add("CD");
        frontPOSSequences.add("CD JJ JJ");
        frontPOSSequences.add("RB DT");
        frontPOSSequences.add("RB DT JJ");
        frontPOSSequences.add("RB CD JJ");
    }

    public static String matchNestedPattern(SemanticRelation r) {
        String lexicalCondition = r.getLexicalCondition();
        if (lexicalCondition == null) {
            if (r.hasImplicitLabels())
                lexicalCondition = new String("IMPLICIT");
            else
                lexicalCondition = new String("NO_LEXCOND");
        }
        Mention m1 = r.getM1();
        Mention m2 = r.getM2();

        String pattern = null;

        if (m1.getStartTokenOffset() <= m2.getStartTokenOffset()
                && m2.getEndTokenOffset() <= m1.getEndTokenOffset()) {
            pattern = new String("M1[M2]");
        } else if (m2.getStartTokenOffset() <= m1.getStartTokenOffset()
                && m1.getEndTokenOffset() <= m2.getEndTokenOffset()) {
            pattern = new String("M2[M1]");
        }
        if (pattern == null) {
            pattern = new String("NO_PATTERN");
        }
        return pattern;
    }

    public static String matchPreModPattern(SemanticRelation r) {
        HashMap<String, Integer> patternExampleTypes = new HashMap<String, Integer>();
        String targetLexicalCondition = new String("PreMod");
        List<SemanticRelation> selectedExamples = new ArrayList<SemanticRelation>();
        StringBuffer depS = null;

        String fineLabel = r.getFineLabel();
        String lexicalCondition = r.getLexicalCondition();
        if (lexicalCondition == null) {
            if (r.hasImplicitLabels())
                lexicalCondition = new String("IMPLICIT");
            else
                lexicalCondition = new String("NO_LEXCOND");
        }
        Mention m1 = r.getM1();
        Mention m2 = r.getM2();

        // ---- find the larger, smaller mention
        Mention largerMention = null, smallerMention = null;
        largerMention = MentionUtil.getCoveringMention(m1, m2);
        if (largerMention != null) {
            if (largerMention == m1) {
                smallerMention = m2;
            } else if (largerMention == m2) {
                smallerMention = m1;
            }
        }

        String[] docTokens = m1.getConstituent().getTextAnnotation().getTokens(); // get the tokens of this document
        SpanLabelView posView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.POS); // get POS tags of

        // this document
        List<Constituent> posCons = posView.getConstituents();
        String pattern = null;

        SpanLabelView chunkView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.SHALLOW_PARSE);

        // ----- these are my new patterns ----
        if (largerMention != null) {
            pattern = new String("[M [M] ]");
            // r.setPremodReject(true);

            // make use of the POS sequences in frontPOSSequences
            if (largerMention.getStartTokenOffset() < smallerMention.getStartTokenOffset()) {
                List<String> frontPos = new ArrayList<String>();
                List<String> frontTokens = new ArrayList<String>();
                for (int i = largerMention.getStartTokenOffset(); i < smallerMention.getStartTokenOffset(); i++) {
                    frontPos.add(posCons.get(i).getLabel());
                    frontTokens.add(docTokens[i]);
                }
                StringBuffer frontPosBuffer = new StringBuffer("");
                for (String pos : frontPos) {
                    frontPosBuffer.append(pos);
                    frontPosBuffer.append(" ");
                }
                String frontPosSequence = frontPosBuffer.toString().trim();
                if (!frontPOSSequences.contains(frontPosSequence)) {
                    pattern = null;
                    r.setPremodReject(true);
                }
                if (frontPos.size() == 2
                        && frontPos.get(0).compareTo("JJ") == 0
                        && frontTokens.get(1).compareTo("and") == 0) {
                    pattern = new String("[JJ and [m] ...]");
                }
                if (frontPos.size() == 3
                        && frontPos.get(0).compareTo("JJ") == 0
                        && frontTokens.get(1).compareTo("and") == 0
                        && frontPos.get(2).compareTo("JJ") == 0) {
                    pattern = new String("[JJ and JJ [m] ...]");
                }
            }

            // either (1) a mixture of noun and adj ; (2) and/CC followed by a
            // mixture of noun and adj
            if (smallerMention.getEndTokenOffset() < largerMention
                    .getEndTokenOffset()) {
                List<String> backPos = new ArrayList<String>();
                List<String> backTokens = new ArrayList<String>();
                for (int i = smallerMention.getEndTokenOffset(); i < largerMention
                        .getEndTokenOffset(); i++) {
                    backPos.add(posCons.get(i).getLabel());
                    backTokens.add(docTokens[i]);
                }
                int startIndex = 0;
                if (backTokens.get(0).compareTo("and") == 0)
                    startIndex = 1;
                boolean foundNounAdj = false, onlyNounAdj = true;
                for (int i = startIndex; i < backPos.size(); i++) {
                    if (backPos.get(i).startsWith("NN")
                            || backPos.get(i).startsWith("JJ"))
                        foundNounAdj = true;
                    else
                        onlyNounAdj = false;
                }
                if (foundNounAdj == true && onlyNounAdj == true) {
                } else {
                    pattern = null;
                    r.setPremodReject(true);
                }
            }
            if (smallerMention.getEndTokenOffset() == largerMention
                    .getEndTokenOffset()) {
                pattern = null;
                r.setPremodReject(true);
            }

            if (smallerMention.getEndTokenOffset() < largerMention
                    .getEndTokenOffset()) {
                // word following smaller mention should not be 's
                if (docTokens[smallerMention.getEndTokenOffset()]
                        .compareTo("'s") == 0) {
                    pattern = null;
                    r.setPremodReject(true);
                }

                // last word of smaller mention should not be a possessive
                // POS-tag
                String smallerMentionLastPOS = posCons.get(
                        smallerMention.getEndTokenOffset() - 1).getLabel();
                if (smallerMentionLastPOS.compareTo("PRP$") == 0
                        || smallerMentionLastPOS.compareTo("WP$") == 0) {
                    pattern = null;
                    r.setPremodReject(true);
                }

                // rule: [ … [M] … ]
                // take all the words in [M] and as long as the rest of the
                // words after M are nouns,
                // add them to form a sequence of words, and check if it is in
                // wordnet or wikipedia title
                // If the sequence of words contains commas or "and", just take
                // the words in [M] and the last word in the bigger mention
                boolean lemmaError = false;
                StringBuffer wordStringInSmallerMention = new StringBuffer("");
                for (int i = smallerMention.getStartTokenOffset(); i < smallerMention
                        .getEndTokenOffset(); i++) { // first grab the sequence
                    // of words in the
                    // smaller mention
                    try {
                        String lemma = docTokens[i].toLowerCase();
                        if (posCons.get(i).getLabel().startsWith("NN")) {
                            lemma = WordNetHelper.getLemma(docTokens[i]
                                    .toLowerCase(), posCons.get(i).getLabel());
                        }
                        wordStringInSmallerMention.append(lemma);
                        wordStringInSmallerMention.append(" ");
                    } catch (JWNLException e) {
                        lemmaError = true;
                        e.printStackTrace();
                    }
                }
                // check: is there a comma or "and" in the sequence of words
                // after the smaller mention, till end of the larger mention?
                // check: are the sequence of words all nouns?
                List<String> wordsAfterSmallerMention = new ArrayList<String>();
                List<String> posAfterSmallerMention = new ArrayList<String>();
                boolean hasComma = false, hasAnd = false, allNouns = true;
                for (int i = smallerMention.getEndTokenOffset(); i < largerMention
                        .getEndTokenOffset(); i++) {
                    if (docTokens[i].compareTo(",") == 0)
                        hasComma = true;
                    if (docTokens[i].toLowerCase().compareTo("and") == 0)
                        hasAnd = true;
                    if (!posCons.get(i).getLabel().startsWith("NN"))
                        allNouns = false;

                    try {
                        String lemma = docTokens[i].toLowerCase();
                        if (posCons.get(i).getLabel().startsWith("NN")) {
                            lemma = WordNetHelper.getLemma(docTokens[i]
                                    .toLowerCase(), posCons.get(i).getLabel());
                        }
                        wordsAfterSmallerMention.add(lemma);
                    } catch (JWNLException e) {
                        lemmaError = true;
                        e.printStackTrace();
                    }

                    posAfterSmallerMention.add(posCons.get(i).getLabel());
                }
                boolean useNounCollocation = false;
                StringBuffer wordStringAfterSmallerMention = new StringBuffer(
                        "");
                if ((hasComma == true || hasAnd == true || allNouns == false)
                        && posAfterSmallerMention.size() > 0
                        && posAfterSmallerMention.get(
                        posAfterSmallerMention.size() - 1).startsWith(
                        "NN")) {
                    // just use the last word
                    wordStringAfterSmallerMention
                            .append(wordsAfterSmallerMention.get(
                                    wordsAfterSmallerMention.size() - 1)
                                    .toLowerCase());
                    wordStringAfterSmallerMention.append(" ");
                    useNounCollocation = true;
                }
                if (allNouns == true && wordsAfterSmallerMention.size() > 0) {
                    useNounCollocation = true;
                    for (int i = 0; i < wordsAfterSmallerMention.size(); i++) {
                        wordStringAfterSmallerMention
                                .append(wordsAfterSmallerMention.get(i)
                                        .toLowerCase());
                        wordStringAfterSmallerMention.append(" ");
                    }
                }
                if (lemmaError == true) {
                    System.exit(1);
                }

                // if(listManager.isWordnetNounCollocation(wordStringInSmallerMention.toString().trim()+" "+wordStringAfterSmallerMention.toString().trim()))
                // {
                // if(listManager.isTitleCollocation(wordStringInSmallerMention.toString().trim()+" "+wordStringAfterSmallerMention.toString().trim()))
                // {
                if (listManager.isPersonAndJobTitles(wordStringInSmallerMention
                        .toString().trim())
                        && smallerMention.getSC().compareTo("PER") == 0
                        && largerMention.getSC().compareTo("PER") == 0) {

                    // if(posAfterSmallerMention.get(posAfterSmallerMention.size()-1).compareTo("NNP")==0)
                    // { // then most probably name of a person
                    // pattern = null;
                    // }
                    // else
                    // if(posAfterSmallerMention.get(posAfterSmallerMention.size()-1).startsWith(("NN")))
                    // {
                    if (!listManager
                            .isPersonAndJobTitles(wordStringAfterSmallerMention
                                    .toString().trim())) {
                        pattern = null;
                        r.setPremodReject(true);
                    }
                    // }
                }
                if (wordStringAfterSmallerMention.toString().trim()
                        .compareTo("") != 0) {
                    if (listManager
                            .isPersonAndJobTitlesEntireMatch(wordStringInSmallerMention
                                    .toString().trim()
                                    + " "
                                    + wordStringAfterSmallerMention.toString()
                                    .trim())
                            && smallerMention.getSC().compareTo("PER") == 0
                            && largerMention.getSC().compareTo("PER") == 0) {
                        pattern = null;
                        r.setPremodReject(true);
                    }
                }

                if (listManager.isCounty(wordStringInSmallerMention.toString()
                        .trim())) {
                    String[] tokens = wordStringAfterSmallerMention.toString()
                            .trim().split(" ");
                    if (tokens.length > 0
                            && (tokens[tokens.length - 1].toLowerCase()
                            .compareTo("county") == 0 || tokens[tokens.length - 1]
                            .toLowerCase().compareTo("counties") == 0)) {
                        pattern = null;
                        r.setPremodReject(true);
                    }
                }

                if (listManager.isState(wordStringInSmallerMention.toString()
                        .trim())) {
                    String[] tokens = wordStringAfterSmallerMention.toString()
                            .trim().split(" ");
                    if (tokens.length > 0
                            && (tokens[tokens.length - 1].toLowerCase()
                            .compareTo("state") == 0 || tokens[tokens.length - 1]
                            .toLowerCase().compareTo("states") == 0)) {
                        pattern = null;
                        r.setPremodReject(true);
                    }
                }

                String depPath = RelationFeatures.DepPathInBetween(r);
                if (depPath != null && depPath.contains("appos")) {
                    pattern = null;
                    r.setPremodReject(true);
                }

                if (wordStringInSmallerMention.toString().trim().toLowerCase()
                        .compareTo("mr.") == 0
                        || wordStringInSmallerMention.toString().trim()
                        .toLowerCase().compareTo("mrs.") == 0
                        || wordStringInSmallerMention.toString().trim()
                        .toLowerCase().compareTo("ms.") == 0) {
                    pattern = null;
                    r.setPremodReject(true);
                }

                // if(wordStringAfterSmallerMention.toString().trim().compareTo("")!=0)
                // {
                // if(listManager.isPartOfWikiTitle(wordStringInSmallerMention.toString().trim()+" "+wordStringAfterSmallerMention.toString().trim()))
                // {
                // r.setPremod_isPartOfWikiTitle(true);
                // }
                // }
                // if(wordStringAfterSmallerMention.toString().trim().compareTo("")!=0)
                // {
                // if(listManager.isWordnetNounCollocation(wordStringInSmallerMention.toString().trim()+" "+wordStringAfterSmallerMention.toString().trim()))
                // {
                // r.setPremod_isWordNetNounCollocation(true);
                // }
                // }
            }
        }

        if (pattern == null) {
            pattern = new String("NO_PATTERN");
        }
        return pattern;
    }

    public static String matchPossesivePattern(SemanticRelation r) {
        HashMap<String, Integer> patternExampleTypes = new HashMap<String, Integer>();
        String targetLexicalCondition = new String("Possessive");
        List<SemanticRelation> selectedExamples = new ArrayList<SemanticRelation>();
        //StringBuffer depS=null;
        String lexicalCondition = r.getLexicalCondition();
        if (lexicalCondition == null) {
            if (r.hasImplicitLabels())
                lexicalCondition = new String("IMPLICIT");
            else
                lexicalCondition = new String("NO_LEXCOND");
        }

        Mention m1 = r.getM1();
        Mention m2 = r.getM2();

        // ---- find the larger, smaller mention
        Mention largerMention = null, smallerMention = null;
        largerMention = MentionUtil.getCoveringMention(m1, m2);
        if (largerMention != null) {
            if (largerMention == m1) {
                smallerMention = m2;
            } else if (largerMention == m2) {
                smallerMention = m1;
            }
        }

        String[] docTokens = m1.getConstituent().getTextAnnotation().getTokens();            // get the tokens of this document
        SpanLabelView posView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.POS);    // get POS tags of this document
        List<Constituent> posCons = posView.getConstituents();
        String pattern = null;

        SpanLabelView chunkView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.SHALLOW_PARSE);

        // ----- these are my new patterns ----
        if (largerMention != null) {

            if (smallerMention.getEndTokenOffset() < largerMention.getEndTokenOffset()) {
                r.setPossReject(true);

                // word following smaller mention
                if (posCons.get(smallerMention.getEndTokenOffset()).getLabel().compareTo("POS") == 0 || docTokens[smallerMention.getEndTokenOffset()].compareTo("'s") == 0) {
//					int startIndex = smallerMention.getEndTokenOffset()+1;
//					if(posCons.get(startIndex).getLabel().compareTo("CD")==0)
//						startIndex += 1;
//					// now, make sure that what follows until end of larger mention, is a mixture of noun and adj
//					boolean foundNounAdj=false, onlyNounAdj=true;
//					for(int i=startIndex; i<largerMention.getEndTokenOffset(); i++) {
//						if(posCons.get(i).getLabel().startsWith("NN") || posCons.get(i).getLabel().startsWith("JJ"))
//							foundNounAdj = true;
//						else
//							onlyNounAdj = false;
//					}
//					if(foundNounAdj==true && onlyNounAdj==true) {
//						pattern = new String("[ .. [M] 's CD-optional only noun,adj]");
//					}
//					else {
//						pattern = null;
//						r.setPossReject(true);
//					}
                    //if(posCons.get(smallerMention.getEndTokenOffset()-1).getLabel().startsWith("NN"))
                    //pattern = new String("[ .. [M] 's CD-optional only noun,adj]");
                    pattern = new String("[..[m] POS/'s ...]");
                }

                // last word of smaller mention should be a possessive POS-tag
                String smallerMentionLastPOS = posCons.get(smallerMention.getEndTokenOffset() - 1).getLabel();
                if (smallerMentionLastPOS.compareTo("PRP$") == 0 || smallerMentionLastPOS.compareTo("WP$") == 0) {
                    pattern = new String("[ .. [M/POS] ..]");
//					int startIndex = smallerMention.getEndTokenOffset();
//					if(posCons.get(startIndex).getLabel().compareTo("CD")==0)
//						startIndex += 1;
//					// now, make sure that what follows until end of larger mention, is a mixture of noun and adj
//					boolean foundNounAdj=false, onlyNounAdj=true;
//					for(int i=startIndex; i<largerMention.getEndTokenOffset(); i++) {
//						if(posCons.get(i).getLabel().startsWith("NN") || posCons.get(i).getLabel().startsWith("JJ"))
//							foundNounAdj = true;
//						else
//							onlyNounAdj = false;
//					}
//					if(foundNounAdj==true && onlyNounAdj==true) {
//						pattern = new String("[ .. [PRP$ or WP$] CD-optional only noun,adj]");
//					}
                }
            } else {
                r.setPossReject(true);
            }

            // the front portion is either empty, DT, or PDT
            if (largerMention.getStartTokenOffset() < smallerMention.getStartTokenOffset()) {
                List<String> frontPos = new ArrayList<String>();
                for (int i = largerMention.getStartTokenOffset(); i < smallerMention.getStartTokenOffset(); i++) {
                    frontPos.add(posCons.get(i).getLabel());
                }
                if (frontPos.size() == 1 && (frontPos.get(0).compareTo("DT") == 0 || frontPos.get(0).compareTo("PDT") == 0)) {
                } else {
                    pattern = null;
                    r.setPossReject(true);
                }
            }
        }

        if (pattern == null) {
            pattern = new String("NO_PATTERN");
        }
        return pattern;
    }

    public static String matchPrepositionPatterns(SemanticRelation r) {
        HashMap<String, Integer> patternExampleTypes = new HashMap<String, Integer>();
        String targetLexicalCondition = new String("Preposition");
        List<SemanticRelation> selectedExamples = new ArrayList<SemanticRelation>();
        String fineLabel = r.getFineLabel();
        String lexicalCondition = r.getLexicalCondition();
        if (lexicalCondition == null) {
            if (r.hasImplicitLabels())
                lexicalCondition = new String("IMPLICIT");
            else
                lexicalCondition = new String("NO_LEXCOND");
        }

        Mention m1 = r.getM1();
        Mention m2 = r.getM2();

        String[] docTokens = m1.getConstituent().getTextAnnotation().getTokens();            // get the tokens of this document
        SpanLabelView posView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.POS);    // get POS tags of this document
        List<Constituent> posCons = posView.getConstituents();
        String pattern = null;

        SpanLabelView chunkView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.SHALLOW_PARSE);

        String depPathInBetween = RelationFeatures.DepPathInBetween(r);
        String[] depLabelsInBetween = RelationFeatures.DepLabelsInBetween(r);
        boolean prepLabelInDepPath = false;
        boolean onlyPrepLabelInDepPath = true;
        if (depLabelsInBetween != null) {
            for (int i = 0; i < depLabelsInBetween.length; i++) {
                if (depLabelsInBetween[i].startsWith(":prep_")) {
                    prepLabelInDepPath = true;
                }
                if (!depLabelsInBetween[i].startsWith(":prep_")) {
                    onlyPrepLabelInDepPath = false;
                }
            }
        }

        if (prepLabelInDepPath == true && onlyPrepLabelInDepPath == true) {
            pattern = new String("[M] dep/IN [M]");
        }

        // ---- find the larger, smaller mention
        Mention largerMention = null, smallerMention = null;
        largerMention = MentionUtil.getCoveringMention(m1, m2);
        if (largerMention != null) {
            if (largerMention == m1) {
                smallerMention = m2;
            } else if (largerMention == m2) {
                smallerMention = m1;
            }
        }

        if (largerMention == null) {                // m1 and m2 are not overlapping
            int numberOfPrepsInBetween = 0;
            for (int i = m1.getEndTokenOffset(); i < m2.getStartTokenOffset(); i++) {
                if (posCons.get(i).getLabel().compareTo("IN") == 0 || posCons.get(i).getLabel().compareTo("TO") == 0)
                    numberOfPrepsInBetween += 1;
            }
            if (numberOfPrepsInBetween <= 2) {

                if (m1.getEndTokenOffset() == m2.getStartTokenOffset()) {
                    if (m1.getStartTokenOffset() > 0 && posCons.get(m1.getStartTokenOffset() - 1).getLabel().compareTo("IN") == 0) {
                        pattern = new String("/IN [m][m]");
                    }
                }

                if ((m1.getEndTokenOffset() + 1) == m2.getStartTokenOffset()) {
                    if (posCons.get(m1.getEndTokenOffset()).getLabel().compareTo("IN") == 0 || posCons.get(m1.getEndTokenOffset()).getLabel().compareTo("TO") == 0) {
                        pattern = new String("[M] prep [M]");
                    }
                }

                if ((m1.getEndTokenOffset() + 1) < m2.getStartTokenOffset()) {    // there is at least 2 tokens between m1, m2
                    // only NP and PP chunks in between
                    boolean onlyNPandPPchunks = true;
                    boolean noVP = true;
                    //List<Constituent> chunkConsInSpan = chunkView.getConstituentsCoveringSpan((arg1HeadEndTokenOffset+1), (arg2HeadStartTokenOffset));
                    List<Constituent> chunkConsInSpan = chunkView.getConstituentsCoveringSpan(m1.getEndTokenOffset(), m2.getStartTokenOffset());
                    for (int i = 0; i < chunkConsInSpan.size(); i++) {
                        if (chunkConsInSpan.get(i).getLabel().compareTo("NP") != 0 &&
                                chunkConsInSpan.get(i).getLabel().compareTo("ADVP") != 0 &&
                                chunkConsInSpan.get(i).getLabel().compareTo("PP") != 0) {
                            onlyNPandPPchunks = false;
                        }
                        if (chunkConsInSpan.get(i).getLabel().compareTo("VP") == 0) {
                            noVP = false;
                        }
                    }

                    if (onlyNPandPPchunks == true) {
                        if (posCons.get(m1.getEndTokenOffset()).getLabel().compareTo("IN") == 0 ||
                                posCons.get(m1.getEndTokenOffset()).getLabel().compareTo("TO") == 0) {
                            pattern = new String("[M] /INor/TO any [M]");    // *
                        } else if (posCons.get(m2.getStartTokenOffset() - 1).getLabel().compareTo("IN") == 0) {
                            pattern = new String("[M] any /IN [M]");        // *
                        } else if (posCons.get(m2.getStartTokenOffset() - 2).getLabel().compareTo("IN") == 0 &&
                                posCons.get(m2.getStartTokenOffset() - 1).getLabel().compareTo("DT") == 0) {
                            pattern = new String("[M] any /IN /DT [M]");    // *
                        } else {
                            //if(r.getLexicalCondition()!=null && r.getLexicalCondition().compareTo("Verbal")==0) {
                            //	System.out.println(showExampleDetails(r, "extent"));
                            //}
                            //r.setPrepReject(true);
                        }
                    }
                    //else {
                    //pattern = null;
                    //r.setPrepReject(true);
                    //}
                }


                // collective nouns patterns
                if (pattern != null && depLabelsInBetween != null && depLabelsInBetween.length == 1 && depLabelsInBetween[0].compareTo(":prep_of") == 0) {
                    try {
                        String lemma = docTokens[m1.getHeadTokenOffset()].toLowerCase();
                        if (posCons.get(m1.getHeadTokenOffset()).getLabel().startsWith("NN")) {
                            lemma = WordNetHelper.getLemma(lemma, posCons.get(m1.getHeadTokenOffset()).getLabel());
                        }
                        if (listManager.isCollectiveNoun(lemma)) {
                            if ((posCons.get(m2.getHeadTokenOffset()).getLabel().compareTo("NN") == 0 || posCons.get(m2.getHeadTokenOffset()).getLabel().compareTo("NNS") == 0)) {
                                if (m1.getSC().compareTo(m2.getSC()) == 0) {
                                    pattern = null;
                                    r.setPrepReject(true);
                                }
                            }
                            //else {
                            //	r.setPrepReject(true);
                            //}
                        }
                    } catch (JWNLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                //Coreference patterns
                //GPE:Nation				nation country
                //GPE:State-or-Province	state province county
                //GPE:Population-Center	capital city town village
                //if(pattern!=null && m1.getSC().compareTo("GPE")==0 && m2.getSC().compareTo("GPE")==0) {
                //					StringBuffer arg1HeadString = new StringBuffer("");
                //					for(int i=m1.getHeadStartTokenOffset(); i<m1.getHeadEndTokenOffset(); i++) {
                //						arg1HeadString.append(docTokens[i]);
                //						arg1HeadString.append(" ");
                //					}
                //if(listManager.isGPECorefNoun(arg1HeadString.toString().trim())) {
                if (listManager.isGPECorefNoun(docTokens[m1.getHeadTokenOffset()])) {
                    //if(	(arg1HeadEndTokenOffset+2)==arg2HeadStartTokenOffset && docTokens[arg1HeadEndTokenOffset+1].compareTo("of")==0) {
                    if ((m1.getEndTokenOffset() + 1) == m2.getStartTokenOffset() && docTokens[m1.getEndTokenOffset()].compareTo("of") == 0) {
                        pattern = null;
                        r.setPrepReject(true);
                    }
                }

                //Coreference patterns
                //GPE:arg1=here in [arg2]
                //GPE:arg1=here in the [arg2]
                //GPE:arg1=here at [arg2]
                //GPE:arg1=here at the [arg2]
                if (pattern != null && m2.getSC().compareTo("GPE") == 0) {
                    if (docTokens[m1.getHeadTokenOffset()].compareTo("here") == 0) {
                        String wordAfterM1 = docTokens[m1.getEndTokenOffset()];
                        if ((wordAfterM1.compareTo("in") == 0 || wordAfterM1.compareTo("at") == 0) && (m1.getEndTokenOffset() + 1) == m2.getStartTokenOffset()) {
                            pattern = null;
                            r.setPrepReject(true);
                        }
                    }
                }

                if (pattern != null) {
                    List<Pair<String, String>> textBetweenMentions = RelationUtil.getTextBetweenMentions(r, "extent");
                    List<Integer> prepPositions = new ArrayList<Integer>();
                    for (int i = 0; i < textBetweenMentions.size(); i++) {
                        if (textBetweenMentions.get(i).getSecond().compareTo("IN") == 0 || textBetweenMentions.get(i).getSecond().compareTo("TO") == 0) {
                            prepPositions.add(new Integer(m1.getEndTokenOffset() + i));
                        }
                    }
                    if (prepPositions.size() >= 2) {        // if there are 2 or more prepositions in between the two mentions
                        int lastPrepPosition = prepPositions.get(prepPositions.size() - 1);
                        if ((m2.getStartTokenOffset() - lastPrepPosition) > 1) {    // the last prep is not immedidately before m2
                            List<Constituent> prepChunkCons = chunkView.getConstituentsCoveringSpan(lastPrepPosition, lastPrepPosition + 1);
                            List<Constituent> arg2ChunkCons = chunkView.getConstituentsCoveringSpan(m2.getStartTokenOffset(), m2.getStartTokenOffset() + 1);
                            if (prepChunkCons.size() == 1 && arg2ChunkCons.size() == 1) {
                                List<Constituent> otherChunkCons = chunkView.getConstituentsCoveringSpan(prepChunkCons.get(0).getEndSpan(), arg2ChunkCons.get(0).getStartSpan());
                                // find the base chunk between the last prep and before m2
                                if (otherChunkCons.size() == 1 && otherChunkCons.get(0).getLabel().compareTo("NP") == 0
                                        && (posCons.get(otherChunkCons.get(0).getEndSpan()).getLabel().compareTo("POS") == 0 ||
                                        posCons.get(otherChunkCons.get(0).getEndSpan()).getLabel().compareTo("PRP$") == 0)) {
                                } else if (otherChunkCons.size() == 0) {
                                } else {
                                    pattern = null;
                                    r.setPrepReject(true);
                                }
                            } else {
                                //System.out.println(showExampleDetails(r, "head"));
                                //System.exit(1);
                            }
                        }
                    }
                }

            }
        } else {
            r.setPrepReject(true);
        }

        if (pattern == null) {
            pattern = new String("NO_PATTERN");
        }
        return pattern;
    }

    public static String matchFormulaicPattern(SemanticRelation r) {
        HashMap<String, Integer> patternExampleTypes = new HashMap<String, Integer>();
        String targetLexicalCondition = new String("Formulaic");
        List<SemanticRelation> selectedExamples = new ArrayList<SemanticRelation>();
        List<Constituent> arg1Chunks = null, arg2Chunks = null;
        boolean makePattern;
        String fineLabel = r.getFineLabel();
        String lexicalCondition = r.getLexicalCondition();
        if (lexicalCondition == null) {
            if (r.hasImplicitLabels())
                lexicalCondition = new String("IMPLICIT");
            else
                lexicalCondition = new String("NO_LEXCOND");
        }
        Mention m1 = r.getM1();
        Mention m2 = r.getM2();

        String[] docTokens = m1.getConstituent().getTextAnnotation().getTokens();            // get the tokens of this document
        SpanLabelView posView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.POS);    // get POS tags of this document
        List<Constituent> posCons = posView.getConstituents();
        String pattern = null;

        SpanLabelView chunkView = (SpanLabelView) m1.getConstituent().getTextAnnotation().getView(ViewNames.SHALLOW_PARSE);

        boolean prepLabelInDepPath = false;
        boolean onlyPrepLabelInDepPath = true;

        // ---- find the larger, smaller mention
        Mention largerMention = null, smallerMention = null;
        largerMention = MentionUtil.getCoveringMention(m1, m2);
        if (largerMention != null) {
            if (largerMention == m1) {
                smallerMention = m2;
            } else if (largerMention == m2) {
                smallerMention = m1;
            }
        }

        if (largerMention == null) {    // not overlapping

            if ((m1.getEndTokenOffset() + 1) == m2.getStartTokenOffset()) {
                if (docTokens[m1.getEndTokenOffset()].compareTo("/") == 0) {
                    if (m1.getSC().compareTo("PER") == 0 && m2.getSC().compareTo("ORG") == 0) {
                        pattern = new String("[m] / [m]");
                    } else {
                        r.setFormulaReject(true);
                    }
                }
            }

            if (m1.getEndTokenOffset() == m2.getStartTokenOffset()) {
                if ((m2.getEndTokenOffset() - m2.getStartTokenOffset()) == 1) {
                    if (docTokens[m2.getStartTokenOffset()].toLowerCase().compareTo("here") == 0 || docTokens[m2.getStartTokenOffset()].toLowerCase().compareTo("there") == 0) {
                        pattern = new String("[m][here|there]");
                    } else {
                        //r.setFormulaReject(true);
                    }
                }
            }

            if (m1.getEndTokenOffset() == m2.getStartTokenOffset()) {

                if ((m1.getSC().compareTo("PER") == 0 && m2.getSC().compareTo("ORG") == 0) ||
                        (m1.getSC().compareTo("PER") == 0 && m2.getSC().compareTo("GPE") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:Population-Center")==0 && m2.getFineSC().compareTo("GPE:Population-Center")==0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:County-or-District") == 0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:State-or-Province") == 0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:Nation") == 0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:County-or-District")==0 && m2.getFineSC().compareTo("GPE:County-or-District")==0) ||
                        (m1.getFineSC().compareTo("GPE:County-or-District") == 0 && m2.getFineSC().compareTo("GPE:State-or-Province") == 0) ||
                        (m1.getFineSC().compareTo("GPE:County-or-District") == 0 && m2.getFineSC().compareTo("GPE:Nation") == 0) ||
                        (m1.getFineSC().compareTo("GPE:County-or-District") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:State-or-Province")==0 && m2.getFineSC().compareTo("GPE:State-or-Province")==0) ||
                        (m1.getFineSC().compareTo("GPE:State-or-Province") == 0 && m2.getFineSC().compareTo("GPE:Nation") == 0) ||
                        (m1.getFineSC().compareTo("GPE:State-or-Province") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:Nation")==0 && m2.getFineSC().compareTo("GPE:Nation")==0) ||
                        (m1.getFineSC().compareTo("GPE:Nation") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0)) {

                    pattern = new String("[m][m]");
                } else {
                    r.setFormulaReject(true);
                }

            }

            List<String> posTagsInBetween = new ArrayList<String>();
            for (int i = m1.getEndTokenOffset(); i < m2.getStartTokenOffset(); i++) {
                if (posCons.get(i).getLabel().compareTo("``") == 0 || posCons.get(i).getLabel().compareTo("''") == 0) {
                } else {
                    posTagsInBetween.add(posCons.get(i).getLabel());
                }
            }
            if ((posTagsInBetween.size() == 1 && posTagsInBetween.get(0).compareTo(",") == 0) ||
                    (posTagsInBetween.size() == 2 && posTagsInBetween.get(0).compareTo(",") == 0 &&
                            (posTagsInBetween.get(1).compareTo("IN") == 0 || posTagsInBetween.get(1).compareTo("TO") == 0))) {
                //if((m1.getEndTokenOffset()+1)==m2.getStartTokenOffset()) {
                //if(docTokens[m1.getEndTokenOffset()].compareTo(",")==0) {
                if ((m1.getSC().compareTo("PER") == 0 && m2.getSC().compareTo("ORG") == 0) ||
                        (m1.getSC().compareTo("PER") == 0 && m2.getSC().compareTo("GPE") == 0) ||
                        (m1.getSC().compareTo("PER") == 0 && m2.getSC().compareTo("FAC") == 0) ||
                        (m1.getSC().compareTo("PER") == 0 && m2.getSC().compareTo("LOC") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:Population-Center")==0 && m2.getFineSC().compareTo("GPE:Population-Center")==0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:County-or-District") == 0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:State-or-Province") == 0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:Nation") == 0) ||
                        (m1.getFineSC().compareTo("GPE:Population-Center") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:County-or-District")==0 && m2.getFineSC().compareTo("GPE:County-or-District")==0) ||
                        (m1.getFineSC().compareTo("GPE:County-or-District") == 0 && m2.getFineSC().compareTo("GPE:State-or-Province") == 0) ||
                        (m1.getFineSC().compareTo("GPE:County-or-District") == 0 && m2.getFineSC().compareTo("GPE:Nation") == 0) ||
                        (m1.getFineSC().compareTo("GPE:County-or-District") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:State-or-Province")==0 && m2.getFineSC().compareTo("GPE:State-or-Province")==0) ||
                        (m1.getFineSC().compareTo("GPE:State-or-Province") == 0 && m2.getFineSC().compareTo("GPE:Nation") == 0) ||
                        (m1.getFineSC().compareTo("GPE:State-or-Province") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0) ||
                        //(m1.getFineSC().compareTo("GPE:Nation")==0 && m2.getFineSC().compareTo("GPE:Nation")==0) ||
                        (m1.getFineSC().compareTo("GPE:Nation") == 0 && m2.getFineSC().compareTo("GPE:Continent") == 0)) {

                    pattern = new String("[m] , [m]");
                } else {
                    r.setFormulaReject(true);
                }
                //}
            }

            List<Integer> commaOffsets = new ArrayList<Integer>();
            for (int i = m1.getEndTokenOffset(); i < m2.getStartTokenOffset(); i++) {
                if (docTokens[i].compareTo(",") == 0) {
                    commaOffsets.add(new Integer(i));
                }
            }
            if (commaOffsets.size() == 2) {
                //if(commaOffsets.get(0).intValue()==m1.getEndTokenOffset() && commaOffsets.get(1).intValue()==(m2.getStartTokenOffset()-1)) {
                List<Constituent> chunksInBetween = chunkView.getConstituentsCoveringSpan(commaOffsets.get(0).intValue() + 1, commaOffsets.get(1).intValue());

                if (chunksInBetween.size() > 0) {
                    int minStartIndex = 99999, maxEndIndex = 0;
                    List<String> chunkLabelsInBetween = new ArrayList<String>();
                    for (Constituent con : chunksInBetween) {
                        chunkLabelsInBetween.add(con.getLabel());
                        if (con.getStartSpan() < minStartIndex) {
                            minStartIndex = con.getStartSpan();
                        }
                        if (con.getEndSpan() > maxEndIndex) {
                            maxEndIndex = con.getEndSpan();
                        }
                    }
                    List<String> posList1 = new ArrayList<String>();
                    for (int i = m1.getEndTokenOffset(); i < minStartIndex; i++) {
                        if (posCons.get(i).getLabel().compareTo("``") == 0 || posCons.get(i).getLabel().compareTo("''") == 0) {
                        } else {
                            posList1.add(posCons.get(i).getLabel());
                        }
                    }
                    List<String> posList2 = new ArrayList<String>();
                    for (int i = maxEndIndex; i < m2.getStartTokenOffset(); i++) {
                        if (posCons.get(i).getLabel().compareTo("``") == 0 || posCons.get(i).getLabel().compareTo("''") == 0) {
                        } else {
                            posList2.add(posCons.get(i).getLabel());
                        }
                    }
                    if (posList1.size() == 1 && posList2.size() == 1 && posList1.get(0).compareTo(",") == 0 && posList2.get(0).compareTo(",") == 0) {

                        if ((chunkLabelsInBetween.size() == 1 && chunkLabelsInBetween.get(0).compareTo("NP") == 0) ||
                                (chunkLabelsInBetween.size() == 2 && chunkLabelsInBetween.get(0).compareTo("PP") == 0 && chunkLabelsInBetween.get(1).compareTo("NP") == 0)) {
                            //if(r.getLexicalCondition()!=null && r.getLexicalCondition().compareTo("Formulaic")==0) {
                            //	System.out.println(">Formulaic "+showExampleDetails(r, "extent"));
                            //}
                            //if(m1.getSC().compareTo("PER")==0 && m2.getSC().compareTo("GPE")==0) {
                            pattern = new String("[m] , {np} , [m]");
                            //}
                            //else {
                            //pattern = null;
                            //r.setFormulaReject(true);
                            //}
                        }

                        //if(chunksInBetween!=null && chunksInBetween.size()==1 && chunksInBetween.get(0).getLabel().compareTo("NP")==0) {
                        //	pattern = new String("[m] , {np} , [m]");
                        //}
                        else {
                            //if(r.getLexicalCondition()!=null) {
                            //	System.out.println("*formula: "+showExampleDetails(r, "extent"));
                            //}
                            r.setFormulaReject(true);
                        }

                    }
                }
                //}
            }

            if (posCons.get(r.getM1().getHeadTokenOffset()).getLabel().startsWith("NN") && posCons.get(r.getM2().getHeadTokenOffset()).getLabel().startsWith("NN")) {
            } else {
                pattern = null;
                r.setFormulaReject(true);
            }
        }

        if (pattern == null) {
            pattern = new String("NO_PATTERN");
        }
        return pattern;
    }
}

