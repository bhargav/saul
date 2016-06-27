package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.io._

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, TextAnnotation, SpanLabelView }
import edu.illinois.cs.cogcomp.curator.CuratorFactory
import edu.illinois.cs.cogcomp.illinoisRE.common.{ Document, Constants }
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.illinoisRE.mention.{ MentionTyper, MentionDetector }
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax
import edu.illinois.cs.cogcomp.saul.util.Logging

import scala.collection.JavaConversions._
import scala.collection.mutable

/** Created by Bhargav Mangipudi on 1/28/16.
  */
object RelationExtractionApp extends Logging {

  /** Helper class for evaluation across experiments */
  case class EvaluationResult(classifierName: String, foldIndex: Int, performance: TestDiscrete) {
    override def toString: String = {
      val overallStats = performance.getOverallStats
      s"Classifier: $classifierName Fold $foldIndex - Precision: ${overallStats(0)} // Recall: ${overallStats(1)} // F1: ${overallStats(2)}"
    }
  }

  /** Enumerates Experiment Type */
  object REExperimentType extends Enumeration {
    val RunMentionCV, RunRelationCV, RunRelationCVWithBrownFeatures = Value
  }

  /** Main method */
  def main(args: Array[String]): Unit = {
    val experimentType = REExperimentType
      .values
      .find(_.toString == args.headOption.getOrElse())
      .getOrElse(REExperimentType.RunMentionCV)

    val docs = loadDataFromCache.toIterable
    docs.foreach(preProcessDocument)

    val numSentences = docs.map(_.getNumberOfSentences).sum
    println(s"Total number of sentences = $numSentences")

    val numFolds = 5

    val experimentResult: List[EvaluationResult] = {
      experimentType match {
        case REExperimentType.RunMentionCV => runMentionClassifierCVExperiment(docs, numFolds)
        case REExperimentType.RunRelationCV => runRelationClassifierCVExperiment(docs, numFolds, useBrownFeatures = false)
        case REExperimentType.RunRelationCVWithBrownFeatures => runRelationClassifierCVExperiment(docs, numFolds, useBrownFeatures = true)
      }
    }

    // Evaluation
    experimentResult.groupBy(_.classifierName).foreach({
      case (clfName, evalList) =>
        evalList.foreach(println)
        evalList.foreach(_.performance.printPerformance(System.out))
    })
  }

  def runMentionClassifierCVExperiment(docs: Iterable[TextAnnotation], numFolds: Int) = {
    (0 until numFolds).flatMap({ fold =>
      setupDataModelForFold(docs, fold, numFolds)

      println(s"Total number of mentions = ${REDataModel.tokens.getTrainingInstances.size}" +
        s" / ${REDataModel.tokens.getTestingInstances.size}")

      REClassifiers.mentionTypeFineClassifier.forget()
      REClassifiers.mentionTypeCoarseClassifier.forget()

      REClassifiers.mentionTypeFineClassifier.learn(5)
      REClassifiers.mentionTypeCoarseClassifier.learn(5)

      //        addMentionPredictionView(REDataModel.documents.getTestingInstances, Constants.PRED_MENTION_VIEW)

      evaluateMentionTypeClassifier(fold)
    }).toList
  }

  def runRelationClassifierCVExperiment(docs: Iterable[TextAnnotation], numFolds: Int, useBrownFeatures: Boolean) = {
    REClassifiers.useRelationBrownFeatures = useBrownFeatures

    (0 until numFolds).flatMap({ fold =>
      setupDataModelForFold(docs, fold, numFolds)

      println(s"Total number of relations = ${REDataModel.pairedRelations.getTrainingInstances.size}" +
        s" / ${REDataModel.pairedRelations.getTestingInstances.size}")

      REClassifiers.relationTypeFineClassifier.forget()
      REClassifiers.relationTypeCoarseClassifier.forget()

      REClassifiers.relationTypeFineClassifier.learn(5)
      REClassifiers.relationTypeCoarseClassifier.learn(5)

      evaluationRelationTypeClassifier(fold)
    }).toList
  }

  private def setupDataModelForFold(docs: Iterable[TextAnnotation], fold: Int, numFolds: Int): Unit = {
    val groupedDocs = docs.zipWithIndex.groupBy({ case (doc, index) => index % numFolds == fold })
    val testDocs = groupedDocs(true).map(_._1)
    val trainDocs = groupedDocs(false).map(_._1)

    REDataModel.clearInstances
    REDataModel.documents.populate(trainDocs)
    REDataModel.documents.populate(testDocs, train = false)
  }

  def evaluateMentionTypeClassifier(fold: Int): List[EvaluationResult] = {
    val testInstances = REDataModel.tokens.getTestingInstances
    val excludeList = MentionTyper.NONE_MENTION :: Nil

    import REClassifiers._

    evaluate[Constituent](testInstances, "Mention Fine", fold, mentionTypeFineClassifier(_), _.getLabel, excludeList) ::
      evaluate[Constituent](testInstances, "Mention Coarse", fold, mentionTypeCoarseClassifier(_), REDataModel.mentionCoarseLabel(_), excludeList) :: Nil
  }

  def evaluationRelationTypeClassifier(fold: Int): List[EvaluationResult] = {
    val testInstances = REDataModel.pairedRelations.getTestingInstances
    val excludeList = Constants.NO_RELATION :: Nil

    import REClassifiers._
    import REConstrainedClassifiers._

    evaluate[SemanticRelation](testInstances, "Relation Fine", fold, relationTypeFineClassifier(_), _.getFineLabel, excludeList) ::
      evaluate[SemanticRelation](testInstances, "Relation Coarse", fold, relationTypeCoarseClassifier(_), _.getCoarseLabel, excludeList) ::
      evaluate[SemanticRelation](testInstances, "Relation Hierarchy Constraint", fold, relationHierarchyConstrainedClassifier.classifier.discreteValue, _.getFineLabel, excludeList) :: Nil
  }

  private def evaluate[T](
    testInstances: Iterable[T],
    clfName: String,
    fold: Int,
    predictedLabeler: T => String,
    goldLabeler: T => String,
    exclude: List[String] = List.empty
  ): EvaluationResult = {
    val performance = new TestDiscrete()
    exclude.filterNot(_.isEmpty).foreach(performance.addNull)

    testInstances.foreach({ rel =>
      val goldLabel = goldLabeler(rel)
      val predictedLabel = predictedLabeler(rel)
      performance.reportPrediction(predictedLabel, goldLabel)
    })

    EvaluationResult(clfName, fold, performance)
  }

  /** Add candidate mentions and typed-candidate mentions to the ACE Document */
  def preProcessDocument(document: TextAnnotation): Unit = {
    val tempDoc: Document = new Document(document)

    val requiredView = List(ViewNames.POS, ViewNames.SHALLOW_PARSE, ViewNames.NER_CONLL, ViewNames.PARSE_STANFORD)

    val annotatorService = CuratorFactory.buildCuratorClient
    requiredView.foreach(viewName => annotatorService.addView(document, viewName))

    //      Method adds candidates to CANDIDATE_MENTION_VIEW View to the TextAnnotation instance
    //      Also adds a CHUNK_PARSE and a SHALLOW_PARSE
    MentionDetector.labelDocMentionCandidates(tempDoc)

    val goldTypedView = document.getView(ViewNames.NER_ACE_FINE_HEAD)
    val mentionView = document.getView(Constants.CANDIDATE_MENTION_VIEW)

    val typedView: SpanLabelView = new SpanLabelView(
      Constants.TYPED_CANDIDATE_MENTION_VIEW,
      "alignFromGold",
      document,
      1.0,
      true
    )

    val allConstituents = new mutable.HashSet[Constituent]()
    goldTypedView.getConstituents.foreach(c => allConstituents.add(c))

    logger.info(s"Number of constituents = ${goldTypedView.getNumberOfConstituents}")
    logger.info(s"Number of constituents in HashSet = ${allConstituents.size}")

    mentionView.getConstituents.foreach({ c: Constituent =>
      val goldOverlap = goldTypedView.getConstituents.filter(tc => c.getStartSpan == tc.getStartSpan && c.getEndSpan == tc.getEndSpan)
      val label = if (goldOverlap.isEmpty) MentionTyper.NONE_MENTION else goldOverlap.head.getLabel
      typedView.addSpanLabel(c.getStartSpan, c.getEndSpan, label, 1.0)

      if (goldOverlap.nonEmpty) {
        if (!allConstituents.contains(goldOverlap.head)) {
          logger.warn("Multiple Gold entities present")
        } else {
          allConstituents.remove(goldOverlap.head)
        }
      }
    })

    document.addView(Constants.TYPED_CANDIDATE_MENTION_VIEW, typedView)

    logger.info(s"Number of candidates generated = ${typedView.getNumberOfConstituents}")
    if (allConstituents.nonEmpty) {
      logger.warn(s"${allConstituents.size} entities not accounted for !!")
    }
  }

  def addMentionPredictionView(testDocs: Iterable[TextAnnotation], predictionViewName: String): Unit = {
    val softMax = new Softmax()

    testDocs.foreach({ doc =>
      // Predictions are added as a new view to the TA
      val typedView = new SpanLabelView(predictionViewName, "predict", doc, 1.0, true)

      doc.getView(Constants.CANDIDATE_MENTION_VIEW).getConstituents.foreach({ c: Constituent =>
        val label = REClassifiers.mentionTypeFineClassifier(c)
        val scoreSet = softMax.normalize(REClassifiers.mentionTypeFineClassifier.classifier.scores(c))
        typedView.addSpanLabel(c.getStartSpan, c.getEndSpan, label, scoreSet.get(label))
      })

      doc.addView(predictionViewName, typedView)
    })
  }

  /** Method to load ACE Documents
    * Attempts to fetch the serialized TA instances directly and updates cache if a particular
    * document is not present in the cache directory.
    *
    * @return List of TextAnnotation items each of them representing a single document
    */
  def loadDataFromCache: Iterator[TextAnnotation] = {
    val datasetRootPath = "data/ace04/data/English"
    val aceReader: Iterable[TextAnnotation] = new ACEReader(datasetRootPath, Array("nw"), true)

    aceReader.iterator
  }
}
