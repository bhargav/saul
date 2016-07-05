package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.io._
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, SpanLabelView, TextAnnotation }
import edu.illinois.cs.cogcomp.curator.CuratorFactory
import edu.illinois.cs.cogcomp.illinoisRE.common.Document
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.illinoisRE.mention.MentionDetector
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser.SplitPolicy
import edu.illinois.cs.cogcomp.saul.classifier.ClassifierUtils
import edu.illinois.cs.cogcomp.saul.parser.{ IterableToLBJavaParser, LBJavaParserToIterable }
import edu.illinois.cs.cogcomp.saul.util.Logging
import org.joda.time.DateTime
import scala.collection.JavaConversions._
import scala.collection.mutable

/** Relation Extraction
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

  private val DatasetTypeACE04 = "ace04"
  private val DatasetTypeACE05 = "ace05"

  /** Main method */
  def main(args: Array[String]): Unit = {
    val experimentType = REExperimentType
      .values
      .find(_.toString == args.headOption.getOrElse())
      .getOrElse(REExperimentType.RunRelationCV)

    val docs = loadDataset(DatasetTypeACE05)
    docs.foreach(preProcessDocument)

    val numSentences = docs.map(_.getNumberOfSentences).sum
    println(s"Total number of sentences = $numSentences")

    val numFolds = 5
    val dataReader = new IterableToLBJavaParser(docs)
    val foldParser = new FoldParser(dataReader, numFolds, SplitPolicy.sequential, 0, false, docs.size)

    val experimentResult = experimentType match {
      case REExperimentType.RunMentionCV => runMentionClassifierCVExperiment(foldParser, numFolds)
      case REExperimentType.RunRelationCV => runRelationClassifierCVExperiment(foldParser, numFolds, useBrownFeatures = false)
      case REExperimentType.RunRelationCVWithBrownFeatures => runRelationClassifierCVExperiment(foldParser, numFolds, useBrownFeatures = true)
    }

    val outputStream = new PrintStream(new FileOutputStream(s"${experimentType}_${DateTime.now()}.txt"))

    // Evaluation
    experimentResult.groupBy(_.classifierName).foreach({
      case (clfName, evalList) =>
        evalList.foreach(outputStream.println(_))
        evalList.foreach(_.performance.printPerformance(outputStream))
    })
  }

  def runMentionClassifierCVExperiment(docs: FoldParser, numFolds: Int): Iterable[EvaluationResult] = {
    (0 until numFolds).flatMap({ fold =>
      setupDataModelForFold(docs, fold, populateRelations = false)

      println(s"Total number of mentions = ${REDataModel.tokens.getTrainingInstances.size}" +
        s" / ${REDataModel.tokens.getTestingInstances.size}")

      val classifiers = List(REClassifiers.mentionTypeFineClassifier, REClassifiers.mentionTypeCoarseClassifier)

      classifiers.foreach(clf => clf.forget())
      ClassifierUtils.TrainClassifiers(5, classifiers)

      //        addMentionPredictionView(REDataModel.documents.getTestingInstances, Constants.PRED_MENTION_VIEW)

      evaluateMentionTypeClassifier(fold)
    })
  }

  def runRelationClassifierCVExperiment(docs: FoldParser, numFolds: Int, useBrownFeatures: Boolean): Iterable[EvaluationResult] = {
    REClassifiers.useRelationBrownFeatures = useBrownFeatures

    (0 until numFolds).flatMap({ fold =>
      setupDataModelForFold(docs, fold, populateRelations = true)

      println(s"Total number of relations = ${REDataModel.pairedRelations.getTrainingInstances.size}" +
        s" / ${REDataModel.pairedRelations.getTestingInstances.size}")

      val classifiers = List(REClassifiers.relationTypeFineClassifier, REClassifiers.relationTypeCoarseClassifier)

      classifiers.foreach(clf => clf.forget())
      ClassifierUtils.TrainClassifiers(5, classifiers)

      evaluationRelationTypeClassifier(fold)
    })
  }

  private def setupDataModelForFold(
    foldParser: FoldParser,
    fold: Int,
    populateRelations: Boolean
  ): Unit = {

    // Get the training docs.
    foldParser.setPivot(fold)
    foldParser.setFromPivot(false)
    val trainDocs = new LBJavaParserToIterable(foldParser).toList

    // Get the testing docs.
    foldParser.reset()
    foldParser.setFromPivot(true)
    val testDocs = new LBJavaParserToIterable(foldParser).toList

    REDataModel.clearInstances

    REDataModel.documents.populate(trainDocs)
    REDataModel.documents.populate(testDocs, train = false)

    if (populateRelations) {
      val trainRelations = trainDocs.flatMap(textAnnotation => RESensors.populateRelations(
        textAnnotation,
        REConstants.TYPED_CANDIDATE_MENTION_VIEW,
        ViewNames.RELATION_ACE_FINE_HEAD,
        ViewNames.RELATION_ACE_COARSE_HEAD
      ))

      val testRelations = testDocs.flatMap(textAnnotation => RESensors.populateRelations(
        textAnnotation,
        REConstants.TYPED_CANDIDATE_MENTION_VIEW,
        ViewNames.RELATION_ACE_FINE_HEAD,
        ViewNames.RELATION_ACE_COARSE_HEAD
      ))

      REDataModel.pairedRelations.populate(trainRelations)
      REDataModel.pairedRelations.populate(testRelations, train = false)
    }
  }

  def evaluateMentionTypeClassifier(fold: Int): List[EvaluationResult] = {
    val testInstances = REDataModel.tokens.getTestingInstances
    val excludeList = REConstants.NONE_MENTION :: Nil

    import REClassifiers._

    evaluate[Constituent](testInstances, "Mention Fine", fold, mentionTypeFineClassifier(_), _.getLabel, excludeList) ::
      evaluate[Constituent](testInstances, "Mention Coarse", fold, mentionTypeCoarseClassifier(_), REDataModel.mentionCoarseLabel(_), excludeList) :: Nil
  }

  def evaluationRelationTypeClassifier(fold: Int): List[EvaluationResult] = {
    val testInstances = REDataModel.pairedRelations.getTestingInstances
    val excludeList = REConstants.NO_RELATION :: Nil

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

    //    Method adds candidates to CANDIDATE_MENTION_VIEW View to the TextAnnotation instance.
    //    Also adds a CHUNK_PARSE and a SHALLOW_PARSE
    MentionDetector.labelDocMentionCandidates(tempDoc)

    val goldTypedView = document.getView(ViewNames.NER_ACE_FINE_HEAD)
    val mentionView = document.getView(REConstants.CANDIDATE_MENTION_VIEW)

    val typedView: SpanLabelView = new SpanLabelView(
      REConstants.TYPED_CANDIDATE_MENTION_VIEW,
      "alignFromGold",
      document,
      1.0,
      true
    )

    val allConstituents = new mutable.HashSet[Constituent]()
    goldTypedView.getConstituents.foreach(c => allConstituents.add(c))

    mentionView.getConstituents.foreach({ c: Constituent =>
      val goldOverlap = goldTypedView.getConstituents
        .filter(tc => c.getStartSpan == tc.getStartSpan && c.getEndSpan == tc.getEndSpan)
      val label = if (goldOverlap.isEmpty) REConstants.NONE_MENTION else goldOverlap.head.getLabel
      val goldConstituent = goldOverlap.headOption

      // Clone attributes as well if it is a valid label
      goldConstituent.map(_.getAttributeKeys)
        .foreach(_.foreach({
          case key: String =>
            c.addAttribute(key, goldConstituent.get.getAttribute(key))
        }))

      typedView.addSpanLabel(c.getStartSpan, c.getEndSpan, label, 1.0)

      if (goldOverlap.nonEmpty) {
        if (!allConstituents.contains(goldConstituent.get)) {
          logger.warn("Found multiple candidates for the same gold constituent.")
        } else {
          allConstituents.remove(goldOverlap.head)
        }
      }
    })

    document.addView(REConstants.TYPED_CANDIDATE_MENTION_VIEW, typedView)

    if (allConstituents.nonEmpty) {
      logger.warn(s"${allConstituents.size} entities not accounted for !!")
    }
  }

  def addMentionPredictionView(testDocs: Iterable[TextAnnotation], predictionViewName: String): Unit = {
    val softMax = new Softmax()

    testDocs.foreach({ doc =>
      // Predictions are added as a new view to the TA
      val typedView = new SpanLabelView(predictionViewName, "predict", doc, 1.0, true)

      doc.getView(REConstants.CANDIDATE_MENTION_VIEW).getConstituents.foreach({ c: Constituent =>
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
  def loadDataset(dataset: String): Iterable[TextAnnotation] = {
    val sections = Array("nw")

    val datasetRootPath = s"../data/$dataset/data/English"
    val is2004Dataset = dataset.equals("ace04")

    val cacheFilePath = s"../data/{$dataset}_${sections.reduce(_ + _)}.index"
    val cacheFile = new File(cacheFilePath)

    val annotatorService = CuratorFactory.buildCuratorClient
    val requiredViews = List(
      ViewNames.POS,
      ViewNames.SHALLOW_PARSE,
      ViewNames.NER_CONLL,
      ViewNames.PARSE_STANFORD,
      ViewNames.DEPENDENCY_STANFORD
    )

    if (cacheFile.exists()) {
      try {
        val inputStream = new ObjectInputStream(new FileInputStream(cacheFile.getPath))
        val taItems = inputStream.readObject.asInstanceOf[List[TextAnnotation]]
        inputStream.close()

        taItems
      } catch {
        case ex: Exception => logger.error("Failure while reading cache file!", ex); Iterable.empty
      }
    } else {
      val aceReader: Iterable[TextAnnotation] = new ACEReader(datasetRootPath, sections, is2004Dataset)
      val items = aceReader.flatMap({ ta =>
        try {
          // Add required views to the TextAnnotation
          requiredViews.foreach(view => annotatorService.addView(ta, view))
          Option(ta)
        } catch {
          case e: Exception => logger.error("Annotator error!", e); None
        }
      })

      if (items.nonEmpty) {
        // Cache annotated TAs for faster processing
        try {
          val f = new FileOutputStream(cacheFile)
          val e = new ObjectOutputStream(f)
          e.writeObject(items)
          e.flush()
        } catch {
          case ex: Exception => logger.error("Error while writing cache file!", ex)
        }
      }

      items
    }
  }
}
