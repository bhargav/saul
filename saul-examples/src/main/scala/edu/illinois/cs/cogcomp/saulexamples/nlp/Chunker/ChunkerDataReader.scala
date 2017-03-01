/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{Constituent, SpanLabelView, TextAnnotation, TokenLabelView}
import edu.illinois.cs.cogcomp.saul.util.Logging

import scala.collection.mutable
import scala.collection.JavaConversions._
import scala.io.Source

/** Data Reader for the CONLL format for training the Chunker */
object ChunkerDataReader extends Logging {
  import ChunkerConstants._

  /** Parse the input data and create the POS View and GOLD Shallow Parse BIO View */
  def parseData(fileName: String): Seq[TextAnnotation] = {
    logger.info(s"Parsing file - $fileName")

    val arrayBuffer = mutable.Buffer[TextAnnotation]()

    val tokenConstituents = mutable.ArrayBuffer[String]()
    val posLabels = mutable.ArrayBuffer[String]()
    val chunkLabels = mutable.ArrayBuffer[String]()
    var numSentences = 0

    Source.fromFile(fileName)
      .getLines()
      .foreach({ line: String =>
        if (line.isEmpty) {
          val sentenceList = List(tokenConstituents.toArray[String])
          val textAnnotation = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(sentenceList)

          val posView = new TokenLabelView(ViewNames.POS, textAnnotation)
          val chunkLabelView = new SpanLabelView(SHALLOW_PARSE_GOLD_BIO_VIEW, textAnnotation)

          textAnnotation.getView(ViewNames.TOKENS)
            .getConstituents
            .zipWithIndex
            .foreach({
              case (constituent: Constituent, idx: Int) =>
                val posCons = constituent.cloneForNewViewWithDestinationLabel(ViewNames.POS, posLabels(idx))
                posView.addConstituent(posCons)

                val chunkCons = constituent.cloneForNewViewWithDestinationLabel(SHALLOW_PARSE_GOLD_BIO_VIEW, chunkLabels(idx))
                chunkLabelView.addConstituent(chunkCons)
            })

          textAnnotation.addView(ViewNames.POS, posView)
          textAnnotation.addView(SHALLOW_PARSE_GOLD_BIO_VIEW, chunkLabelView)

          ChunkerUtilities.addGoldSpanLabelView(textAnnotation, SHALLOW_PARSE_GOLD_BIO_VIEW, SHALLOW_PARSE_GOLD_SPAN_VIEW)

          arrayBuffer.append(textAnnotation)
          tokenConstituents.clear()
          posLabels.clear()
          chunkLabels.clear()

          numSentences += 1
        } else {
          val reader = line.split(" ")
          tokenConstituents.append(reader(0))
          posLabels.append(reader(1))
          chunkLabels.append(reader(2))
        }
      })

    logger.info(s"Number of sentences : $numSentences")

    arrayBuffer
  }
}
