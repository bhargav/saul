package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.util.{List => JList, Map => JMap, HashMap => JHashMap}

import edu.illinois.cs.cogcomp.core.datastructures.textannotation._
import edu.illinois.cs.cogcomp.illinoisRE.data.{Mention, SemanticRelation}
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

  def populateRelations(document: TextAnnotation, mentionViewName: String, goldRelationViewName: String): Seq[SemanticRelation] = {
    val constituents = document.getView(mentionViewName).getConstituents
    val mentions = convertConstituentsIntoMentions(constituents, discardNullMentions = true)
    val sentenceMentions = RelationExtractor.indexMentionsBySentence(mentions)
    val goldRelations = getGoldRelations(document, mentionViewName, goldRelationViewName)

    formRelationTraningExamples(sentenceMentions, goldRelations)
  }

  /**
    * Converts constituents into [[Mention]] instances
    *
    * @param constituentList List of constituents (Entity Mentions)
    * @param discardNullMentions Decide if we need to discard NULL labelled entities
    * @return List of [[Mention]] instances.
    */
  private def convertConstituentsIntoMentions(constituentList: Seq[Constituent], discardNullMentions: Boolean): Seq[Mention] = {
    val consList = if (discardNullMentions) constituentList.filterNot(_.getLabel.equalsIgnoreCase("NULL")) else constituentList

    consList.map({ cons: Constituent =>
      val id = s"${ cons.getStartSpan }-${ cons.getEndSpan }"
      val mention = new Mention(id, cons)

      // For ACEReader, SubType is an optional parameter.
      // Fallback to Coarse type in such an instance.
      if (cons.hasAttribute(ACEReader.EntitySubtypeAttribute)) {
        mention.setFineSC(cons.getAttribute(ACEReader.EntitySubtypeAttribute))
      } else if (cons.hasAttribute(ACEReader.EntityTypeAttribute)) {
        mention.setFineSC(cons.getAttribute(ACEReader.EntityTypeAttribute))
        mention.setSC(cons.getAttribute(ACEReader.EntityTypeAttribute))
      }

      mention
    })
  }

  /**
    * Gets Gold Relations from the given [[TextAnnotation]] instance.
    *
    * @param document [[TextAnnotation]] document instance
    * @param mentionViewName View Name for Mentions
    * @param goldViewName View Name for Gold Relations
    * @return Gold relation data
    */
  private def getGoldRelations(document: TextAnnotation, mentionViewName: String, goldViewName: String): Seq[((Constituent, Constituent), JMap[String, String])] = {
    val mentionView = document.getView(mentionViewName)
    val relationView: PredicateArgumentView = document.getView(goldViewName).asInstanceOf[PredicateArgumentView]

    relationView.getRelations.flatMap({ relation: Relation =>
      val m1 = Option(mentionView.getConstituentsCovering(relation.getSource))
      val m2 = Option(mentionView.getConstituentsCovering(relation.getTarget))

      if (!(m1.exists(_.nonEmpty) && m2.exists(_.nonEmpty))) {
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
        relationAttributes.put("label", relLabel)
        relationAttributes.put("lexicalCondition", lexicalCondition)

        Some(((m1.get.head, m2.get.head), relationAttributes))
      }
    })
  }

  private def formRelationTraningExamples(candMentions: JMap[Integer, JList[Mention]], goldRelations: Seq[((Constituent, Constituent), JMap[String, String])]): Seq[SemanticRelation] = {
    candMentions.keySet().flatMap({ case sentId: Integer =>
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
          first <- 1 until mentions.size();
          second <- 1 until mentions.size()
          if first != second
        ) yield {
          val m1 = mentions.get(first)
          val m2 = mentions.get(second)
          val relation = new SemanticRelation(m1, m2)

          val directedRel = goldRelations.find({ case ((gold1, gold2), _) =>
            MentionUtil.compareConstituents(m1.getConstituent, gold1) &&
              MentionUtil.compareConstituents(m2.getConstituent, gold2)
          })

          val reverseRel = goldRelations.find({ case ((gold1, gold2), _) =>
            MentionUtil.compareConstituents(m2.getConstituent, gold1) &&
              MentionUtil.compareConstituents(m1.getConstituent, gold2)
          })

          if (directedRel.isDefined || reverseRel.isDefined) {
            val labelMap = if (directedRel.isDefined) directedRel.get._2 else reverseRel.get._2
            val fineLabel = labelMap.get("label")
            val lexicalCondition = labelMap.get("lexicalCondition")
            val coarseLabel = if (fineLabel.indexOf(":") == -1) fineLabel else fineLabel.substring(0, fineLabel.indexOf(":"))

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
