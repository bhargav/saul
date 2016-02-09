package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{Relation, Sentence, Constituent}
import edu.illinois.cs.cogcomp.illinoisRE.common.{Document, Constants}
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.illinoisRE.relation.RelationExtractor

import scala.collection.JavaConversions._

/**
  * Created by Bhargav Mangipudi on 2/2/16.
  */
object RESensors {

  def sentenceToTokens(sentence : Sentence) : List[Constituent] = {
    val tokens = sentence.getView(Constants.TYPED_CANDIDATE_MENTION_VIEW).getConstituents.toList
    assert(tokens.forall(_.getSentenceId == sentence.getSentenceId))

    tokens
  }

  def sentenceToRelations(sentence: Sentence) : List[SemanticRelation] = {
//    Temporarily using the RelationExtractor -- Move away from that.
    val tempDoc: Document = new Document(sentence.getSentenceConstituent.getTextAnnotation)
    val relations = RelationExtractor.getAllExamplesFromDocument(tempDoc)
      .filter(_.getSentenceId == sentence.getSentenceId)
      .filterNot(_.getBinaryLabel.equalsIgnoreCase(Constants.NO_RELATION)).toList

    /* TODO@bhargav Revisit this once more
    val sentenceConstituents = sentenceToTokens(sentence)

    val validMentions = sentenceConstituents.filterNot(_.getLabel.equalsIgnoreCase(Constants.NONE_MENTION)).map({
      c: Constituent => {
        val mentionId = if (c.getAttributeKeys.contains("id")) c.getAttribute("id") else c.getStartSpan + "-" + c.getEndSpan
        val mention = new Mention(mentionId, c)

        if (c.getAttributeKeys.contains("fineSc")) {
          mention.setFineSC(c.getAttribute("fineSc"))

          if (mention.getFineSC.contains(":"))
            mention.setSC(mention.getFineSC.substring(0, mention.getFineSC.indexOf(":")))
          else
            mention.setSC(mention.getFineSC)
        }

        mention
      }
    })

    val binaryRelations = sentence.getSentenceConstituent.getTextAnnotation.getView(Constants.GOLD_RELATION_VIEW).getRelations.filter({
      rel: Relation => {
        val relLabel = rel.getRelationName
        rel.getSource.getSentenceId == sentence.getSentenceId && !Constants.relationsToIgnore.contains(relLabel.substring(0, relLabel.indexOf("|")))
      }
    })

    val mutableMentions = validMentions.asJava
    MentionUtil.sortMentionAsc(mutableMentions)

    val immutableMentions = mutableMentions.asScala
    val mentionPairs = immutableMentions.dropRight(1).zip(immutableMentions.drop(1))
*/
    assert(relations.forall(_.getSentenceId == sentence.getSentenceId))
    relations
  }
}
