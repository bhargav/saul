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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;

import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
import edu.illinois.cs.cogcomp.illinoisRE.mention.MentionUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Word2Vec based features for [[SemanticRelation]] instances.
 * NOTE: Vectors are trained on ACE2005 dataset and Wikipedia data.
 * Adopted from Zefu Lu's codebase at: https://gitlab-beta.engr.illinois.edu/cogcomp/illinois_RE_SL
 */
public final class RelationWord2VecFeatures {
    public static Word2VecManager w2v = new Word2VecManager(REConfigurator.getResourceManager().getString(REConfigurator.WORD2VEC_VECTORS_PATH));
    public static Word2VecClusterManager w2vc = new Word2VecClusterManager(REConfigurator.getResourceManager().getString(REConfigurator.WORD2VEC_CLUSTERS_PATH));

    public static Pair<List<Constituent>, List<Constituent>> getPathsToCommonAncestor(Constituent start, Constituent end, int maxDepth) {

        assert start.getView() == end
                .getView() : "Cannot find paths across different views. " + "The start and end constituents should be from the same view.";

        List<Constituent> p1 = PathFeatureHelper.getPathToRoot(start, maxDepth);
        List<Constituent> p2 = PathFeatureHelper.getPathToRoot(end, maxDepth);

        Set<Constituent> s1 = new LinkedHashSet<>(p1);
        Set<Constituent> s2 = new LinkedHashSet<>(p2);

        boolean foundAncestor = false;
        List<Constituent> pathUp = new ArrayList<>();

        for (Constituent aP1 : p1) {
            if (!foundAncestor) {
                pathUp.add(aP1);
            }
            if (s2.contains(aP1)) {
                foundAncestor = true;
                break;
            }
        }
        if (!foundAncestor) throw new IllegalArgumentException("Common ancestor not found in path down.");

        List<Constituent> pathDown = new ArrayList<Constituent>();
        foundAncestor = false;

        for (Constituent aP2 : p2) {
            if (!foundAncestor) {
                pathDown.add(aP2);
            }
            if (s1.contains(aP2)) {
                foundAncestor = true;
                break;
            }
        }

        if (!foundAncestor) throw new IllegalArgumentException("Common ancestor not found in path up.");

        return new Pair<>(pathUp, pathDown);
    }

    public static double[] M1M2SubtypeAndCommonAncestor(SemanticRelation eg) {
        String commonAncestor = new String("none");
        TreeView dependencyView = (TreeView) eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());
        List<Constituent> c2Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());

        if (c1Cons.size() == 1 && c2Cons.size() == 1) {

            try {
                Pair<List<Constituent>, List<Constituent>> paths = getPathsToCommonAncestor(c1Cons.get(0), c2Cons.get(0), 40);
                commonAncestor = paths.getFirst().get(paths.getFirst().size() - 1).toString().toLowerCase();

            } catch (IllegalArgumentException e) {
                System.out.println("RelationFeatures.CommonAncestor " + e + " m1Id=" + eg.getM1().getId() + " m2Id=" + eg.getM2().getId());
            }

        }
        return w2vc.getWordVector(commonAncestor);
    }

    public static double[] M1HWw2v(SemanticRelation eg) {
        String hw1 = eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase();
        return w2v.getWordVector(hw1);
    }

    public static double[] M2HWw2v(SemanticRelation eg) {
        String hw2 = eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase();
        return w2v.getWordVector(hw2);
    }

    public static double[] M1HWw2vc(SemanticRelation eg) {
        String hw1 = eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase();
        return w2vc.getWordVector(hw1);
    }

    public static double[] M2HWw2vc(SemanticRelation eg) {
        String hw2 = eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase();
        return w2vc.getWordVector(hw2);
    }

    public static double[] HWw2vSubtraction(SemanticRelation eg) {
        String hw1 = eg.getM1().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase();
        String hw2 = eg.getM2().getConstituent().getTextAnnotation().getToken(eg.getM1().getHeadTokenOffset()).toLowerCase();
        double[] hw1w2v = w2v.getWordVector(hw1);
        double[] hw2w2v = w2v.getWordVector(hw2);
        for (int i = 0; i < hw1w2v.length; i++)
            hw1w2v[i] -= hw2w2v[i];
        return hw1w2v;
    }

    public static double[] WordBetweenSingle(SemanticRelation eg) {
        Mention m1 = eg.getM1();
        Mention m2 = eg.getM2();
        int i1 = -1, i2 = -1;
        if ((m1.getEndTokenOffset() - 1) < m2.getStartTokenOffset()) {
            i1 = m1.getEndTokenOffset() - 1;
            i2 = m2.getStartTokenOffset();
        } else if ((m2.getEndTokenOffset() - 1) < m1.getStartTokenOffset()) {
            i1 = m2.getEndTokenOffset() - 1;
            i2 = m1.getStartTokenOffset();
        }
        if ((i1 + 2) == i2) {
            return w2v.getWordVector(m1.getConstituent().getTextAnnotation().getToken(i1 + 1).toLowerCase());
        } else {
            return new double[50];
        }
    }

    public static double[] WordBetweenSingleW2vc(SemanticRelation eg) {
        Mention m1 = eg.getM1();
        Mention m2 = eg.getM2();
        int i1 = -1, i2 = -1;
        if ((m1.getEndTokenOffset() - 1) < m2.getStartTokenOffset()) {
            i1 = m1.getEndTokenOffset() - 1;
            i2 = m2.getStartTokenOffset();
        } else if ((m2.getEndTokenOffset() - 1) < m1.getStartTokenOffset()) {
            i1 = m2.getEndTokenOffset() - 1;
            i2 = m1.getStartTokenOffset();
        }
        if ((i1 + 2) == i2) {
            return w2vc.getWordVector(m1.getConstituent().getTextAnnotation().getToken(i1 + 1).toLowerCase());
        } else {
            return new double[w2vc.getSize()];
        }
    }

    public static double[] WordBetweenFirst(SemanticRelation eg) {
        Mention m1 = eg.getM1();
        Mention m2 = eg.getM2();
        int i1 = -1, i2 = -1;
        if ((m1.getEndTokenOffset() - 1) < m2.getStartTokenOffset()) {
            i1 = m1.getEndTokenOffset() - 1;
            i2 = m2.getStartTokenOffset();
        } else if ((m2.getEndTokenOffset() - 1) < m1.getStartTokenOffset()) {
            i1 = m2.getEndTokenOffset() - 1;
            i2 = m1.getStartTokenOffset();
        }
        if ((i2 - i1) > 2) {
            return w2v.getWordVector(m1.getConstituent().getTextAnnotation().getToken(i1 + 1).toLowerCase());
        } else {
            return new double[50];
        }
    }

    public static double[] WordBetweenFirstW2vc(SemanticRelation eg) {
        Mention m1 = eg.getM1();
        Mention m2 = eg.getM2();
        int i1 = -1, i2 = -1;
        if ((m1.getEndTokenOffset() - 1) < m2.getStartTokenOffset()) {
            i1 = m1.getEndTokenOffset() - 1;
            i2 = m2.getStartTokenOffset();
        } else if ((m2.getEndTokenOffset() - 1) < m1.getStartTokenOffset()) {
            i1 = m2.getEndTokenOffset() - 1;
            i2 = m1.getStartTokenOffset();
        }
        if ((i2 - i1) > 2) {
            return w2vc.getWordVector(m1.getConstituent().getTextAnnotation().getToken(i1 + 1).toLowerCase());
        } else {
            return new double[w2vc.getSize()];
        }
    }

    public static double[] WordBetweenLast(SemanticRelation eg) {
        Mention m1 = eg.getM1();
        Mention m2 = eg.getM2();
        int i1 = -1, i2 = -1;
        if ((m1.getEndTokenOffset() - 1) < m2.getStartTokenOffset()) {
            i1 = m1.getEndTokenOffset() - 1;
            i2 = m2.getStartTokenOffset();
        } else if ((m2.getEndTokenOffset() - 1) < m1.getStartTokenOffset()) {
            i1 = m2.getEndTokenOffset() - 1;
            i2 = m1.getStartTokenOffset();
        }
        if ((i2 - i1) > 2) {
            return w2v.getWordVector(m1.getConstituent().getTextAnnotation().getToken(i2 - 1).toLowerCase());
        } else {
            return new double[50];
        }
    }

    public static double[] WordBetweenLastW2vc(SemanticRelation eg) {
        Mention m1 = eg.getM1();
        Mention m2 = eg.getM2();
        int i1 = -1, i2 = -1;
        if ((m1.getEndTokenOffset() - 1) < m2.getStartTokenOffset()) {
            i1 = m1.getEndTokenOffset() - 1;
            i2 = m2.getStartTokenOffset();
        } else if ((m2.getEndTokenOffset() - 1) < m1.getStartTokenOffset()) {
            i1 = m2.getEndTokenOffset() - 1;
            i2 = m1.getStartTokenOffset();
        }
        if ((i2 - i1) > 2) {
            return w2vc.getWordVector(m1.getConstituent().getTextAnnotation().getToken(i2 - 1).toLowerCase());
        } else {
            return new double[w2vc.getSize()];
        }
    }

    public static double[] M1DepParentWordW2v(SemanticRelation eg) {
        String result = null;

        TreeView dependencyView = (TreeView) eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());

        if (c1Cons.size() == 1) {
            List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
            if (pathToRoot.size() > 1) {
                result = new String(pathToRoot.get(1).toString());
            }
        }
        return w2v.getWordVector(result);
    }

    public static double[] M1DepParentWordW2vc(SemanticRelation eg) {
        String result = null;

        TreeView dependencyView = (TreeView) eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM1().getHeadTokenOffset());

        if (c1Cons.size() == 1) {
            List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
            if (pathToRoot.size() > 1) {
                result = new String(pathToRoot.get(1).toString());
            }
        }
        return w2vc.getWordVector(result);
    }

    public static double[] M2DepParentWordW2v(SemanticRelation eg) {
        String result = null;

        TreeView dependencyView = (TreeView) eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());

        if (c1Cons.size() == 1) {
            List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
            if (pathToRoot.size() > 1) {
                result = new String(pathToRoot.get(1).toString());
            }
        }
        return w2v.getWordVector(result);
    }

    public static double[] M2DepParentWordW2vc(SemanticRelation eg) {
        String result = null;

        TreeView dependencyView = (TreeView) eg.getSentence().getSentenceConstituent().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> c1Cons = dependencyView.getConstituentsCoveringToken(eg.getM2().getHeadTokenOffset());

        if (c1Cons.size() == 1) {
            List<Constituent> pathToRoot = PathFeatureHelper.getPathToRoot(c1Cons.get(0), 40);
            if (pathToRoot.size() > 1) {
                result = new String(pathToRoot.get(1).toString());
            }
        }
        return w2vc.getWordVector(result);
    }

    public static double[] WordAfterM1(SemanticRelation eg) {
        String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();            // get the tokens of this document

        int index = eg.getM1().getEndTokenOffset();
        if (index < docTokens.length) {
            return w2v.getWordVector(docTokens[index].toLowerCase());
        } else {
            return w2v.getWordVector(null);
        }
    }

    public static double[] WordAfterM1W2vc(SemanticRelation eg) {
        String[] docTokens = eg.getM1().getConstituent().getTextAnnotation().getTokens();            // get the tokens of this document

        int index = eg.getM1().getEndTokenOffset();
        if (index < docTokens.length) {
            return w2vc.getWordVector(docTokens[index].toLowerCase());
        } else {
            return w2vc.getWordVector(null);
        }
    }

    public static double[] WordBeforeM2(SemanticRelation eg) {
        String[] docTokens = eg.getM2().getConstituent().getTextAnnotation().getTokens();            // get the tokens of this document

        int index = eg.getM2().getStartTokenOffset() - 1;
        if (index < docTokens.length && index >= 0) {
            return w2v.getWordVector(docTokens[index].toLowerCase());
        } else {
            return w2v.getWordVector(null);
        }
    }

    public static double[] WordBeforeM2W2vc(SemanticRelation eg) {
        String[] docTokens = eg.getM2().getConstituent().getTextAnnotation().getTokens();            // get the tokens of this document

        int index = eg.getM2().getStartTokenOffset() - 1;
        if (index < docTokens.length && index >= 0) {
            return w2vc.getWordVector(docTokens[index].toLowerCase());
        } else {
            return w2vc.getWordVector(null);
        }
    }

    public static double[] SingleWordBetweenMentionsW2v(SemanticRelation eg) {
        String word;
        SpanLabelView posView = (SpanLabelView) eg.getM1().getConstituent().getTextAnnotation().getView(ViewNames.POS);    // get POS tags of this document
        Mention m1 = eg.getM1();
        Mention m2 = eg.getM2();

        if (MentionUtil.getCoveringMention(m1, m2) == null) {
            if ((m1.getEndTokenOffset() + 1) == m2.getStartTokenOffset()) {
                word = WordHelpers.getWord(m1.getConstituent().getTextAnnotation(), m1.getEndTokenOffset()).toLowerCase();
            } else {
                word = null;
            }
        } else {
            word = null;
        }

        return w2v.getWordVector(word);
    }
}