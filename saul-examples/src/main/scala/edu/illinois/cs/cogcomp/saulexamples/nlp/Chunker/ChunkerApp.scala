/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation._
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester
import edu.illinois.cs.cogcomp.core.experiments.evaluators.ConstituentLabelingEvaluator
import edu.illinois.cs.cogcomp.saul.classifier.ClassifierUtils

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source

object ChunkerConstants {
  val SHALLOW_PARSE_GOLD_SPAN_VIEW = "SHALLOW_PARSE_GOLD"
  val SHALLOW_PARSE_GOLD_BIO_VIEW = "SHALLOW_PARSE_GOLD_BIO"

  val SHALLOW_PARSE_ANNOTATED_SPAN_VIEW = "SHALLOW_PARSE_ANNOTATED"
  val SHALLOW_PARSE_ANNOTATED_BIO_VIEW = "SHALLOW_PARSE_ANNOTATED_BIO"
}

object ChunkerApp extends App {
  import ChunkerConstants._

  val trainFile = "../data/conll2000chunking/train.txt"
  val testFile = "../data/conll2000chunking/test.txt"

  def parseData(fileName: String): Seq[TextAnnotation] = {
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

    println("Number of sentences = " + numSentences)

    arrayBuffer
  }

  lazy val trainData = parseData(trainFile)
  lazy val testData = parseData(testFile)

  val jarModelPath = ""

  trainData.foreach({ textAnnotation: TextAnnotation =>
    val numberOfSentences = textAnnotation.getNumberOfSentences
    val sentences = (0 until numberOfSentences).map(textAnnotation.getSentence)
    ChunkerDataModel.sentence.populate(sentences, train = true)
  })

  testData.foreach({ textAnnotation: TextAnnotation =>
    val numberOfSentences = textAnnotation.getNumberOfSentences
    val sentences = (0 until numberOfSentences).map(textAnnotation.getSentence)
    ChunkerDataModel.sentence.populate(sentences, train = false)
  })

  ChunkerClassifiers.ChunkerClassifier.learn(10)
  println(ChunkerClassifiers.ChunkerClassifier.test())

  val evaluator = new ConstituentLabelingEvaluator()
  val tester = new ClassificationTester()

  val chunkerAnnotator = new ChunkerAnnotator()
  testData.foreach({ textAnnotation: TextAnnotation =>
    // Remove POS View before evaluation.
    textAnnotation.removeView(ViewNames.POS)

    chunkerAnnotator.addView(textAnnotation)

    val goldView = textAnnotation.getView(SHALLOW_PARSE_GOLD_SPAN_VIEW)
    val annotatedView = textAnnotation.getView(SHALLOW_PARSE_ANNOTATED_SPAN_VIEW)

    // Workaround for incorrect ConstituentLabelingEvaluator behaviour.
    val predictedView = new SpanLabelView(SHALLOW_PARSE_GOLD_SPAN_VIEW, textAnnotation)
    annotatedView.getConstituents.foreach({ cons: Constituent =>
      predictedView.addConstituent(cons.cloneForNewView(SHALLOW_PARSE_GOLD_SPAN_VIEW))
    })

    evaluator.evaluate(tester, goldView, predictedView)
  })

  println(tester.getPerformanceTable.toOrgTable)
}
