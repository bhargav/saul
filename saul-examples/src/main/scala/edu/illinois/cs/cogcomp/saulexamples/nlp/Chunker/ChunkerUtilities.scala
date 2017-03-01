/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{SpanLabelView, TextAnnotation, TokenLabelView}
import edu.illinois.cs.cogcomp.saul.util.Logging

import scala.collection.JavaConversions._

object ChunkerUtilities extends Logging {

  /** Convert the gold BIO labelling to Span Label View
    * Note: Use this method only for the GOLD view as this does not perform error handling. */
  def addGoldSpanLabelView(ta: TextAnnotation, sourceBIOView: String, destView: String): Unit = {
    assert(ta.hasView(sourceBIOView))
    assert(!ta.hasView(destView))

    val destinationView = new SpanLabelView(destView, ta)

    var currentSpanStart = -1
    var currentSpanEnd = -1
    var currentTag = ""

    ta.getView(sourceBIOView).getConstituents.foreach({ constituent =>
      val inASpan = currentSpanStart != -1

      if (inASpan) {
        if (constituent.getLabel.startsWith("O") || constituent.getLabel.startsWith("B-")) {
          destinationView.addSpanLabel(currentSpanStart, currentSpanEnd, currentTag, 1.0d)
          currentSpanStart = -1
          currentSpanEnd = -1
          currentTag = ""
        } else {
          // Label Starts with I-
          if (constituent.getLabel.endsWith(currentTag)) {
            currentSpanEnd = constituent.getEndSpan
          } else {
            destinationView.addSpanLabel(currentSpanStart, currentSpanEnd, currentTag, 1.0d)
            logger.info("Dangling I-label")

            currentSpanStart = -1
            currentSpanEnd = -1
            currentTag = ""
          }
        }
      }

      if (constituent.getLabel.startsWith("B-")) {
        currentSpanStart = constituent.getStartSpan
        currentSpanEnd = constituent.getEndSpan
        currentTag = constituent.getLabel.substring(2)
      }
      else if (!inASpan && constituent.getLabel.startsWith("I-")) {
        logger.info(s"Dangling I- label for constituent - $constituent")
      }
    })

    ta.addView(destView, destinationView)
  }
}
