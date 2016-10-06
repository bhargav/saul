/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, Sentence, TextAnnotation }

import scala.collection.JavaConversions._

object ChunkerSensors {

  def getSentencesInDocument(document: TextAnnotation): Seq[Sentence] = {
    val numberOfSentences = document.getNumberOfSentences
    (0 until numberOfSentences).map(document.getSentence)
  }

  def getTokensInSentence(sentence: Sentence): Seq[Constituent] = {
    sentence.getView(ViewNames.SHALLOW_PARSE).getConstituents
  }
}
