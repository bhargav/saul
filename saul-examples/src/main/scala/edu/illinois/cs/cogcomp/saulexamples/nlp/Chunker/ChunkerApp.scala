/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import java.util.Properties

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation._
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester
import edu.illinois.cs.cogcomp.core.experiments.evaluators.ConstituentLabelingEvaluator
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder
import edu.illinois.cs.cogcomp.saul.classifier.ClassifierUtils
import edu.illinois.cs.cogcomp.saul.util.Logging

import scala.collection.JavaConversions._
import scala.io.StdIn


object ChunkerApp extends Logging {
  import ChunkerConstants._

  val trainFile = "../data/conll2000chunking/train.txt"
  val testFile = "../data/conll2000chunking/test.txt"

  val jarModelPath = ""

  object ChunkerExperimentType extends Enumeration {
    val TrainAndTest, TestFromModel, Interactive = Value

    def withNameOpt(s: String): Option[Value] = values.find(_.toString == s)
  }

  def main(args: Array[String]): Unit = {
    /** Try to parse the experiment type as input argument or use default */
    val testType = args.headOption
      .flatMap(ChunkerExperimentType.withNameOpt)
      .getOrElse(ChunkerExperimentType.Interactive)

    testType match {
      case ChunkerExperimentType.TrainAndTest => trainAndTest()
      case ChunkerExperimentType.TestFromModel => testWithPretrainedModels()
      case ChunkerExperimentType.Interactive => interactiveWithPretrainedModels()
    }
  }

  private def loadModelFromJarPath(): Unit = {
    // Load model from jar path
//    ClassifierUtils.LoadClassifier(
//      jarModelPath,
//      ChunkerClassifiers.ChunkerClassifier)
    ChunkerClassifiers.ChunkerClassifier.load()
  }

  private def getSentencesInTextAnnotation(taSeq: Seq[TextAnnotation]) = {
    taSeq.flatMap({ textAnnotation: TextAnnotation =>
      (0 until textAnnotation.getNumberOfSentences).map(textAnnotation.getSentence)
    })
  }

  lazy val trainData = ChunkerDataReader.parseData(trainFile)
  lazy val testData = ChunkerDataReader.parseData(testFile)

  lazy val preTrainedAnnotator: ChunkerAnnotator = {
    loadModelFromJarPath()

    val annotatorInstance = new ChunkerAnnotator()
    annotatorInstance.initialize(new ResourceManager(new Properties()))
    annotatorInstance
  }

  /** Note: This function does NOT populate testing instances.
    * Also does not use GOLD POS tags. Instead a trained POSAnnotater is used. */
  private def testModelImpl(): Unit = {
    ClassifierUtils.TestClassifiers(ChunkerClassifiers.ChunkerClassifier)

    val evaluator = new ConstituentLabelingEvaluator()
    val tester = new ClassificationTester()

    testData.foreach({ textAnnotation: TextAnnotation =>
      // Remove POS View before evaluation.
      textAnnotation.removeView(ViewNames.POS)

      preTrainedAnnotator.addView(textAnnotation)

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

  def trainAndTest(): Unit = {
    ChunkerDataModel.sentence.populate(getSentencesInTextAnnotation(trainData), train = true)
    ChunkerDataModel.sentence.populate(getSentencesInTextAnnotation(testData), train = false)

    ChunkerClassifiers.ChunkerClassifier.learn(50)
    ClassifierUtils.SaveClassifiers(ChunkerClassifiers.ChunkerClassifier)

    testModelImpl()
  }

  def testWithPretrainedModels(): Unit = {
    loadModelFromJarPath()

    ChunkerDataModel.sentence.populate(getSentencesInTextAnnotation(testData), train = false)

    testModelImpl()
  }

  def interactiveWithPretrainedModels(): Unit = {
    val taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer())

    while (true) {
      println("Enter a sentence to annotate (or Press Enter to exit)")
      val input = StdIn.readLine()

      input match {
        case sentence: String if sentence.trim.nonEmpty =>
          // Create a Text Annotation with the current input sentence.
          val ta = taBuilder.createTextAnnotation(sentence.trim)
          preTrainedAnnotator.addView(ta)
          println("POS View            : " + ta.getView(ViewNames.POS).toString)
          println("Annotated BIO View  : " + ta.getView(SHALLOW_PARSE_ANNOTATED_BIO_VIEW))
          println("Annotated Span View : " + ta.getView(SHALLOW_PARSE_ANNOTATED_SPAN_VIEW))
        case _ => return
      }
    }
  }
}
