/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.SpatialRoleLabeling

import java.lang.Boolean

import edu.illinois.cs.cogcomp.core.datastructures.textannotation._
import edu.illinois.cs.cogcomp.core.datastructures.{ IntPair, ViewNames }
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder
import edu.illinois.cs.cogcomp.saul.util.Logging
import edu.illinois.cs.cogcomp.saulexamples.nlp.CommonSensors
import edu.illinois.cs.cogcomp.saulexamples.nlp.SpatialRoleLabeling.SpRL2013.{ RELATION, SpRL2013Document }
import edu.illinois.cs.cogcomp.saulexamples.nlp.SpatialRoleLabeling.SpRLSensors._

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/** Created by taher on 7/28/16.
  */
object PopulateSpRLDataModel extends Logging {
  def apply(path: String, isTraining: Boolean, dataVersion: String, modelName: String, savedLexicon: HashSet[String]) = {

    modelName match {
      case "Roberts" =>
        val getLex: (List[SpRL2013Document]) => HashSet[String] = if (isTraining) getLexicon else (x) => savedLexicon

        val (sentences, relations, lex) =
          SpRLDataModelReader.read(path, isTraining, dataVersion, getRobertsRelations, getLex)

        RobertsDataModel.spLexicon = lex
        RobertsDataModel.sentences.populate(sentences, train = isTraining)
        RobertsDataModel.relations.populate(relations, train = isTraining)
      case _ =>
        val (sentences, pairs, lex) = SpRLDataModelReader.read(path, isTraining, dataVersion, getRelations, null)
        SpRLDataModel.sentences.populate(sentences, train = isTraining)
        SpRLDataModel.pairs.populate(pairs, train = isTraining)
    }
  }

  def getLexicon(docs: List[SpRL2013Document]): HashSet[String] = {
    val dic : Seq[String] = Dictionaries.prepositions.toSeq
    val indicators : Seq[String] = docs.map(d => d.getTAGS.getSPATIALINDICATOR.
      asScala.map(s => s.getText.toLowerCase.trim)).flatten

    HashSet[String]( dic ++ indicators : _*)
  }

  def getRobertsRelations(sentence: Sentence, doc: SpRL2013Document, lexicon: HashSet[String], offset: IntPair): List[RobertsRelation] = {

    def getIndicatorCandidates(sentence: Sentence, constituents: List[Constituent], lexicon: HashSet[String]): ListBuffer[Constituent] = {

      val beforeSp = "([^a-zA-Z\\d-]|^)"
      val afterSp = "[^a-zA-Z\\d-]"
      def contains(sentence: String, phrase: String): Boolean = {
        val pattern = new Regex(beforeSp + phrase + afterSp)
        pattern.findFirstIn(sentence.toLowerCase).isDefined
      }

      val indicators = ListBuffer[Constituent]()
      val matched = lexicon.filter(x => contains(sentence.getText, x)).toList
      for (m <- matched) {
        val pattern = new Regex(beforeSp + m + afterSp)
        val occurrences = pattern.findAllMatchIn(sentence.getText.toLowerCase).toList
        for (i <- occurrences) {
          if (!indicators.exists(x => x.getStartCharOffset == i.start && i.end == x.getEndCharOffset)) {
            val covering = sentence.getView(ViewNames.TOKENS).asScala
              .filter(x => i.start <= x.getStartCharOffset && x.getEndCharOffset <= i.end)
            val c = new Constituent("", "", sentence.getSentenceConstituent.getTextAnnotation,
              covering.head.getStartSpan, covering.last.getEndSpan)
            indicators += c
          }
        }
      }

      indicators
    }

    def isGoldTrajector(r: RELATION, tr: Constituent): Boolean = {
      tr == getHeadword(doc.getTrajectorHashMap.get(r.getTrajectorId), tr.getTextAnnotation, offset)
    }

    def isGoldLandmark(r: RELATION, lm: Constituent): Boolean = {
      lm == getHeadword(doc.getLandmarkHashMap.get(r.getLandmarkId), lm.getTextAnnotation, offset)
    }

    def getGoldRelations(goldRelations: List[RELATION], sp: Constituent): List[RELATION] = {
      goldRelations.filter(x =>
        doc.getSpatialIndicatorMap.get(x.getSpatialIndicatorId).getStart.intValue() == sp.getStartCharOffset + offset.getFirst &&
          doc.getSpatialIndicatorMap.get(x.getSpatialIndicatorId).getEnd.intValue() == sp.getEndCharOffset + offset.getFirst).toList
    }

    val relations = ListBuffer[RobertsRelation]()
    val constituents = sentence.getView(ViewNames.TOKENS).asScala.toList
    val args = constituents.filter(x => CommonSensors.getPosTag(x).startsWith("NN") ||
      CommonSensors.getPosTag(x).startsWith("PRP"))
    val indicators: ListBuffer[Constituent] = getIndicatorCandidates(sentence, constituents, lexicon)

    val goldRelations = doc.getTAGS.getRELATION.asScala
      .filter(x => !tagIsNullOrOutOfSentence(doc.getSpatialIndicatorMap.get(x.getSpatialIndicatorId), offset)).toList

    for (i <- indicators) {

      val rels = getGoldRelations(goldRelations, i)

      for (tr <- args) {

        var label = RobertsRelation.RobertsRelationLabels.CANDIDATE

        if (rels.exists(r => isGoldTrajector(r, tr) && tagIsNullOrOutOfSentence(doc.getLandmarkHashMap.get(r.getLandmarkId), offset)))
          label = RobertsRelation.RobertsRelationLabels.GOLD

        relations += new RobertsRelation(sentence, tr.getSpan, i.getSpan, null, label)

        for (lm <- args) {
          if (lm.getSpan != tr.getSpan) {

            if (rels.exists(r => isGoldTrajector(r, tr) && isGoldLandmark(r, lm))) {
              label = RobertsRelation.RobertsRelationLabels.GOLD
            } else
              label = RobertsRelation.RobertsRelationLabels.CANDIDATE

            relations += new RobertsRelation(sentence, tr.getSpan, i.getSpan, lm.getSpan, label)

          }
        }

      }
    }

    relations.toList
  }

  def tagIsNullOrOutOfSentence(t: HasSpan, offset: IntPair): Boolean = {
    t == null || t.getStart.intValue() < 0 || t.getEnd.intValue() < 0 ||
      !(offset.getFirst <= t.getStart.intValue() && t.getEnd.intValue() <= offset.getSecond)
  }

  def getHeadword(t: HasSpan, ta: TextAnnotation, offset: IntPair): Constituent = {
    ta.getView(ViewNames.TOKENS).asInstanceOf[TokenLabelView].getConstituentAtToken(getHeadwordId(t, ta, offset))
  }

  def getHeadwordId(t: HasSpan, ta: TextAnnotation, offset: IntPair): Int = {

    if (tagIsNullOrOutOfSentence(t, offset))
      return -1

    val start = t.getStart().intValue() - offset.getFirst
    val startTokenId = ta.getTokenIdFromCharacterOffset(start)

    val phrases = ta.getView(ViewNames.SHALLOW_PARSE).getConstituentsCoveringToken(startTokenId)

    if (phrases.size > 0) {
      val phrase = phrases.get(0)
      val tree: TreeView = ta.getView(SpRLDataModel.parseView).asInstanceOf[TreeView]
      val parsePhrase = tree.getParsePhrase(phrase)
      return CollinsHeadFinder.getInstance.getHeadWordPosition(parsePhrase)
    } else {
      logger.warn("cannot find phrase for '" + ta.getToken(startTokenId) + "'")
    }
    startTokenId
  }

  def getRelations(sentence: Sentence, doc: SpRL2013Document, lexicon: HashSet[String], offset: IntPair): List[Relation] = {

    def canAddRelation(relations: Iterable[Relation], a: Constituent, b: Constituent): Boolean = {
      getUniqueSentenceId(a) == getUniqueSentenceId(b) &&
        !relations.exists(x => x.getSource.getSpan == a.getSpan && x.getTarget.getSpan == b.getSpan)
    }

    def addRelation(relations: ListBuffer[Relation], ta: TextAnnotation, pivot: HasSpan, other: HasSpan, relationType: String, offset: IntPair) = {

      if (!tagIsNullOrOutOfSentence(pivot, offset) && !tagIsNullOrOutOfSentence(other, offset)) {
        val r = new Relation(relationType, getHeadword(other, ta, offset), getHeadword(pivot, ta, offset), 1)
        relations += r
      }
    }

    val relations = ListBuffer[Relation]()

    // GOLD pivots
    doc.getTAGS.getRELATION.asScala.foreach(r => {

      val pivot = doc.getSpatialIndicatorMap.get(r.getSpatialIndicatorId)
      val tr = doc.getTrajectorHashMap.get(r.getTrajectorId)
      val lm = doc.getLandmarkHashMap.get(r.getLandmarkId)

      addRelation(relations, sentence.getSentenceConstituent.getTextAnnotation, pivot, tr, "tr", offset)
      addRelation(relations, sentence.getSentenceConstituent.getTextAnnotation, pivot, lm, "lm", offset)
    })

    // CANDIDATE pivots
    val constituents = sentence.getView(ViewNames.TOKENS).asScala
    val pivots = constituents.filter(x => SpRLSensors.isCandidate(x))
    val args = constituents.filter(x => CommonSensors.getPosTag(x).startsWith("NN") ||
      CommonSensors.getPosTag(x).startsWith("PRP"))
    for (a <- args; p <- pivots) {
      if (canAddRelation(relations, a, p))
        relations += new Relation("none", a, p, 0.1)
    }
    relations.toList
  }

}
