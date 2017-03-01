/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.annotation.Annotator
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{Constituent, TextAnnotation, TokenLabelView}
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager
import edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger.{POSAnnotator, POSTaggerApp}

import scala.collection.JavaConversions._

/** Chunker Annotator implementation
  *
  * @param useHeuristics To use heuristics to fix BIO annotation.
  */
class ChunkerAnnotator(val useHeuristics: Boolean = true)
  extends Annotator(ChunkerConstants.SHALLOW_PARSE_ANNOTATED_SPAN_VIEW, Array(ViewNames.TOKENS)) {

  override def initialize(rm: ResourceManager): Unit = {}

  /** Adds the POS view to a TextAnnotation
    * Note: Assumes that the classifiers are populated with required models
    * @param ta TextAnnotation instance
    */
  override def addView(ta: TextAnnotation): Unit = {
    if (!ta.hasView(ViewNames.POS)) {
      ChunkerAnnotator.localPOSAnnotator.addView(ta)
    }

    val tokens = ta.getView(ChunkerConstants.SHALLOW_PARSE_GOLD_BIO_VIEW).getConstituents

    ChunkerDataModel.sentence.clear()
    val sentences = (0 until ta.getNumberOfSentences).map(ta.getSentence)
    ChunkerDataModel.sentence.populate(sentences, train = false)

    val chunkerBIOView = new TokenLabelView(ChunkerConstants.SHALLOW_PARSE_ANNOTATED_BIO_VIEW, ta)

    tokens.foreach({ cons: Constituent =>
      val label = ChunkerClassifiers.ChunkerClassifier(cons)
      val posCons = cons.cloneForNewViewWithDestinationLabel(chunkerBIOView.getViewName, label)
      chunkerBIOView.addConstituent(posCons)
    })

    ta.addView(chunkerBIOView.getViewName, chunkerBIOView)

    if (useHeuristics) {
      ChunkerUtilities.addSpanLabelViewUsingHeuristics(
        ta,
        chunkerBIOView.getViewName,
        ChunkerConstants.SHALLOW_PARSE_ANNOTATED_SPAN_VIEW)
    } else {
      ChunkerUtilities.addGoldSpanLabelView(
        ta,
        chunkerBIOView.getViewName,
        ChunkerConstants.SHALLOW_PARSE_ANNOTATED_SPAN_VIEW)
    }
  }
}

object ChunkerAnnotator {
  /** Instance of a local POS Annotator if required */
  private lazy val localPOSAnnotator: POSAnnotator = POSTaggerApp.getPretrainedAnnotator()
}