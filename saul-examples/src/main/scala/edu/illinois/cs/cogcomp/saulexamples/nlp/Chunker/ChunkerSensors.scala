/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, Sentence }

import scala.collection.JavaConversions._

object ChunkerSensors {

  /** Sensor to populate tokens node from a Sentence instance */
  def getTokensInSentence(sentence: Sentence): Seq[Constituent] = {
    sentence.getView(ChunkerConstants.SHALLOW_PARSE_GOLD_BIO_VIEW).getConstituents
  }
}
