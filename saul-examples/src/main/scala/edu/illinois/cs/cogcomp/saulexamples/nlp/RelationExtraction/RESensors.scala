package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Sentence, Constituent }
import edu.illinois.cs.cogcomp.illinoisRE.common.{ Document, Constants }
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.illinoisRE.relation.RelationExtractor

import scala.collection.JavaConversions._

/** Created by Bhargav Mangipudi on 2/2/16.
  */
object RESensors {

  /** Sensor to extract tokens from a given [[Sentence]] instance. */
  def sentenceToTokens(sentence: Sentence): List[Constituent] = {
    val tokens = sentence.getView(Constants.TYPED_CANDIDATE_MENTION_VIEW).getConstituents.toList
    assert(tokens.forall(_.getSentenceId == sentence.getSentenceId))

    tokens
  }

  /** Sensor to extract relations from a given [[Sentence]] instance. */
  def sentenceToRelations(sentence: Sentence): List[SemanticRelation] = {
    //    Temporarily using the RelationExtractor -- Move away from that.
    val tempDoc: Document = new Document(sentence.getSentenceConstituent.getTextAnnotation)
    val relations = RelationExtractor.getAllExamplesFromDocument(tempDoc)
      .filter(_.getSentenceId == sentence.getSentenceId).toList

    relations
  }
}
