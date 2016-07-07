/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.util.{ HashMap => JHashMap, List => JList, Map => JMap }
import edu.illinois.cs.cogcomp.core.datastructures.textannotation._
import edu.illinois.cs.cogcomp.illinoisRE.data.{ Mention, SemanticRelation }
import edu.illinois.cs.cogcomp.illinoisRE.mention.MentionUtil
import edu.illinois.cs.cogcomp.illinoisRE.relation.RelationExtractor
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader
import edu.illinois.cs.cogcomp.saul.util.Logging
import scala.collection.JavaConversions._

object RESensors extends Logging {

  /** Sensor to extract tokens from a given [[Sentence]] instance. */
  def sentenceToTokens(sentence: Sentence): List[Constituent] = {
    val tokens = sentence.getView(REConstants.TYPED_CANDIDATE_MENTION_VIEW).getConstituents.toList
    assert(tokens.forall(_.getSentenceId == sentence.getSentenceId))

    tokens
  }

  /** Sensor to extract first Mention from a [[SemanticRelation]] instance */
  def relationToFirstMention(relation: SemanticRelation): Constituent = relation.getM1.getConstituent

  /** Sensor to extract second Mention from a [[SemanticRelation]] instance */
  def relationToSecondMention(relation: SemanticRelation): Constituent = relation.getM2.getConstituent

  def populateRelations(document: TextAnnotation, mentionViewName: String, fineRelationView: String, coarseRelationView: String): Seq[SemanticRelation] = {
    val constituents = document.getView(mentionViewName).getConstituents
    val mentions = convertConstituentsIntoMentions(constituents, discardNullMentions = true)
    val sentenceMentions = RelationExtractor.indexMentionsBySentence(mentions)
    val goldRelations = getGoldRelations(document, mentionViewName, fineRelationView, coarseRelationView)

    formRelationTraningExamples(sentenceMentions, goldRelations)
  }

  /** Converts constituents into [[Mention]] instances
    *
    * @param constituentList List of constituents (Entity Mentions)
    * @param discardNullMentions Decide if we need to discard NULL labelled entities
    * @return List of [[Mention]] instances.
    */
  private def convertConstituentsIntoMentions(constituentList: Seq[Constituent], discardNullMentions: Boolean): Seq[Mention] = {
    val consList = if (discardNullMentions) constituentList.filterNot(_.getLabel.equalsIgnoreCase("NULL")) else constituentList

    consList.map({ cons: Constituent =>
      val id = s"${cons.getStartSpan}-${cons.getEndSpan}"
      new Mention(id, cons)
    })
  }

  /** Gets Gold Relations from the given [[TextAnnotation]] instance.
    *
    * @param document [[TextAnnotation]] document instance
    * @param mentionViewName View Name for Mentions
    * @param relationFineViewName View Name for Fine Relations - Gold
    * @param relationCoarseViewName View Name for Coarse Relations - Gold
    * @return Gold relation data
    */
  private def getGoldRelations(document: TextAnnotation, mentionViewName: String, relationFineViewName: String, relationCoarseViewName: String): Seq[((Constituent, Constituent), JMap[String, String])] = {
    val mentionView = document.getView(mentionViewName)
    val relationView: PredicateArgumentView = document.getView(relationFineViewName).asInstanceOf[PredicateArgumentView]
    val coarseRelationView: PredicateArgumentView = document.getView(relationCoarseViewName).asInstanceOf[PredicateArgumentView]

    relationView.getRelations.flatMap({ relation: Relation =>
      val predicateForSource = Queries.sameSpanAsConstituent(relation.getSource)
      val predicateForTarget = Queries.sameSpanAsConstituent(relation.getTarget)

      val m1 = mentionView.getConstituentsCovering(relation.getSource).filter(predicateForSource.transform)
      val m2 = mentionView.getConstituentsCovering(relation.getTarget).filter(predicateForTarget.transform)

      val m1Coarse = coarseRelationView.getConstituentsCovering(relation.getSource).filter(predicateForSource.transform)
      val coarseRelation = Option(m1Coarse).map(_.head)
        .map(_.getOutgoingRelations)
        .map(_.filter(rel => predicateForTarget.transform(rel.getTarget)))
        .flatMap(_.headOption)

      if (m1.isEmpty || m2.isEmpty || coarseRelation.isEmpty) {
        logger.warn("Cannot find constituents")

        None
      } else {
        val relName = relation.getRelationName
        val separatorIndex = relName.indexOf("|")
        val (relLabel, lexicalCondition) = {
          if (separatorIndex == -1)
            (relName, relName)
          else
            (relName.substring(0, separatorIndex), relName.substring(separatorIndex + 1))
        }

        val relationAttributes: JMap[String, String] = new JHashMap[String, String]()
        relationAttributes.put("fineLabel", relLabel)
        relationAttributes.put("coarseLabel", coarseRelation.get.getRelationName)
        relationAttributes.put("lexicalCondition", lexicalCondition)

        Some(((m1.head, m2.head), relationAttributes))
      }
    })
  }

  private def formRelationTraningExamples(candMentions: JMap[Integer, JList[Mention]], goldRelations: Seq[((Constituent, Constituent), JMap[String, String])]): Seq[SemanticRelation] = {
    candMentions.keySet().flatMap({
      case sentId: Integer =>
        val mentions = candMentions.get(sentId)

        try {
          MentionUtil.sortMentionAsc(mentions)
        } catch {
          case e: Exception => logger.error("Error while sorting mentions!", e); Seq.empty
        }

        if (mentions.size() <= 1) {
          Seq.empty
        } else {

          val validRelations = for (
            m1 <- mentions;
            m2 <- mentions if m1 != m2
          ) yield {
            val relation = new SemanticRelation(m1, m2)

            val directedRel = goldRelations.find({
              case ((gold1, gold2), _) =>
                MentionUtil.compareConstituents(m1.getConstituent, gold1) &&
                  MentionUtil.compareConstituents(m2.getConstituent, gold2)
            })

            val reverseRel = goldRelations.find({
              case ((gold1, gold2), _) =>
                MentionUtil.compareConstituents(m2.getConstituent, gold1) &&
                  MentionUtil.compareConstituents(m1.getConstituent, gold2)
            })

            if (directedRel.isDefined || reverseRel.isDefined) {
              val labelMap = if (directedRel.isDefined) directedRel.get._2 else reverseRel.get._2
              val coarseLabel = labelMap.get("coarseLabel")
              val fineLabel = coarseLabel + ":" + labelMap.get("fineLabel")
              val lexicalCondition = labelMap.get("lexicalCondition")

              val prefix = if (directedRel.isDefined) "m1-" else "m2-"
              val suffix = if (directedRel.isDefined) "-m2" else "-m1"

              relation.setFineLabel(prefix + fineLabel + suffix)
              relation.setLexicalCondition(lexicalCondition)
              relation.setCoarseLabel(prefix + coarseLabel + suffix)
            }

            relation
          }

          validRelations
        }
    }).toSeq
  }
}
