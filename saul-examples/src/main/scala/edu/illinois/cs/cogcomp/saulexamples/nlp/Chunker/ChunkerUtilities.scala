/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, SpanLabelView, TextAnnotation }
import edu.illinois.cs.cogcomp.saul.util.Logging

import scala.collection.JavaConversions._

object ChunkerUtilities extends Logging {

  /** Convert the gold BIO labelling to Span Label View
    * Note: Use this method only for the GOLD view as this does not perform error handling. */
  def addGoldSpanLabelView(ta: TextAnnotation, sourceBIOView: String, destView: String): Unit = {
    assert(ta.hasView(sourceBIOView))
    assert(!ta.hasView(destView))

    val destinationView = new SpanLabelView(destView, ta)

    var currentChunkStart = -1
    var currentChunkEnd = -1
    var cLabel = ""

    ta.getView(sourceBIOView).getConstituents.foreach({ constituent =>
      val inASpan = currentChunkStart != -1

      if (inASpan) {
        if (constituent.getLabel.startsWith("O") || constituent.getLabel.startsWith("B-")) {
          destinationView.addSpanLabel(currentChunkStart, currentChunkEnd, cLabel, 1.0d)
          currentChunkStart = -1
          currentChunkEnd = -1
          cLabel = ""
        } else {
          // Label Starts with I-
          if (constituent.getLabel.endsWith(cLabel)) {
            currentChunkEnd = constituent.getEndSpan
          } else {
            destinationView.addSpanLabel(currentChunkStart, currentChunkEnd, cLabel, 1.0d)
            logger.info("Dangling I-label")

            currentChunkStart = -1
            currentChunkEnd = -1
            cLabel = ""
          }
        }
      }

      if (constituent.getLabel.startsWith("B-")) {
        currentChunkStart = constituent.getStartSpan
        currentChunkEnd = constituent.getEndSpan
        cLabel = constituent.getLabel.substring(2)
      }
      else if (!inASpan && constituent.getLabel.startsWith("I-")) {
        logger.info(s"Dangling I- label for constituent - $constituent")
      }
    })

    if (currentChunkStart != -1 && currentChunkEnd != -1 && cLabel.nonEmpty) {
      destinationView.addSpanLabel(currentChunkStart, currentChunkEnd, cLabel, 1.0d)
    }

    ta.addView(destView, destinationView)
  }

  /** Convert BIO labelled annotation to SpanLabelView using some heuristics to handle error scenarios */
  def addSpanLabelViewUsingHeuristics(ta: TextAnnotation, sourceBIOView: String, destView: String): Unit = {
    assert(ta.hasView(sourceBIOView))
    assert(!ta.hasView(destView))

    val destinationView = new SpanLabelView(destView, ta)

    var currentChunkStart = -1
    var currentChunkEnd = -1
    var cLabel = ""
    var previousConstituent: Option[Constituent] = None

    ta.getView(sourceBIOView).getConstituents.foreach({ constituent =>
      // Running version of current constituent's predicted label.
      var currentLabel = constituent.getLabel

      if (currentLabel.startsWith("I-")) {
        if (cLabel.isEmpty) {
          currentLabel = "B" + currentLabel.substring(1)
        } else if (!currentLabel.endsWith(cLabel)) {
          currentLabel = "B" + currentLabel.substring(1)
        }
      }

      if ((currentLabel.startsWith("B-") || currentLabel.startsWith("O")) && cLabel.nonEmpty) {
        if (previousConstituent.nonEmpty) {
          currentChunkEnd = previousConstituent.get.getEndSpan
          destinationView.addSpanLabel(currentChunkStart, currentChunkEnd, cLabel, 1.0d)
          cLabel = ""
        }
      }

      if (currentLabel.startsWith("B-")) {
        currentChunkStart = constituent.getStartSpan
        cLabel = currentLabel.substring(2)
      }

      previousConstituent = Some(constituent)
    })

    if (cLabel.nonEmpty && previousConstituent.nonEmpty) {
      currentChunkEnd = previousConstituent.get.getEndSpan
      destinationView.addSpanLabel(currentChunkStart, currentChunkEnd, cLabel, 1.0d)
    }

    ta.addView(destView, destinationView)
  }
}
