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
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder
import edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger.POSTaggerApp

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.{ Source, StdIn }

object ChunkerApp extends App {
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
          val chunkLabelView = new SpanLabelView(ViewNames.SHALLOW_PARSE, textAnnotation)

          textAnnotation.getView(ViewNames.TOKENS)
            .getConstituents
            .zipWithIndex
            .foreach({
              case (constituent: Constituent, idx: Int) =>
                val posCons = constituent.cloneForNewViewWithDestinationLabel(ViewNames.POS, posLabels(idx))
                posView.addConstituent(posCons)

                val chunkCons = constituent.cloneForNewViewWithDestinationLabel(ViewNames.SHALLOW_PARSE, chunkLabels(idx))
                chunkLabelView.addConstituent(chunkCons)
            })

          textAnnotation.addView(ViewNames.POS, posView)
          textAnnotation.addView(ViewNames.SHALLOW_PARSE, chunkLabelView)

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

  /** Interactive model to annotate input sentences with Pre-trained models
    */
  def interactiveWithPretrainedModels(): Unit = {
    val posAnnotator = POSTaggerApp.getPretrainedAnnotator(ViewNames.POS)
    val taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer())

    while (true) {
      println("Enter a sentence to annotate (or Press Enter to exit)")
      val input = StdIn.readLine()

      input match {
        case sentence: String if sentence.trim.nonEmpty =>
          // Create a Text Annotation with the current input sentence.
          val ta = taBuilder.createTextAnnotation(sentence.trim)
          posAnnotator.addView(ta)

          val tokens = ta.getView(ViewNames.TOKENS).getConstituents
          ChunkerDataModel.tokens.populate(tokens)

          println("Tokens: " + ta.getView(ViewNames.TOKENS))
        case _ => return
      }
    }
  }
}
