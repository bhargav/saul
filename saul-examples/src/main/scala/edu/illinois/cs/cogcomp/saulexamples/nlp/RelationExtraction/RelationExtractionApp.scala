package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.io._

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, TextAnnotation, SpanLabelView }
import edu.illinois.cs.cogcomp.illinoisRE.common.{ Document, ResourceManager, Constants }
import edu.illinois.cs.cogcomp.illinoisRE.data.{ SemanticRelation, DataLoader }
import edu.illinois.cs.cogcomp.illinoisRE.mention.{ MentionTyper, MentionDetector }
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax

import scala.collection.JavaConversions._
import scala.io.Source

/** Created by Bhargav Mangipudi on 1/28/16.
 */
object RelationExtractionApp {

  case class EvaluationResult(classifierName: String, foldIndex: Int, goldCount: Int, predictedCount: Int, correctCount: Int) {
    def getPrecision = correctCount.toDouble / predictedCount
    def getRecall = correctCount.toDouble / goldCount
    def getF1: Double = {
      val recall = getRecall
      val precision = getPrecision
      2.0 * recall * precision / (precision + recall)
    }

    override def toString: String = {
      s"Classifier: $classifierName Fold $foldIndex - Precision: $getPrecision // Recall: $getRecall // F1: $getF1"
    }
  }

  /** Enumerates Experiment Type */
  object REExperimentType extends Enumeration {
    val RunMentionCV, RunRelationCV, RunRelationCVWithBrownFeatures = Value
  }

  /** Main method */
  def main(args: Array[String]): Unit = {
    val experimentType = REExperimentType.values.find(_.toString == args.headOption.getOrElse()).getOrElse(REExperimentType.RunRelationCVWithBrownFeatures)

    if (experimentType == REExperimentType.RunRelationCVWithBrownFeatures)
      REClassifiers.useRelationBrownFeatures = true

    val docs = loadDataFromCache.toIterable
    docs.foreach(preProcessDocument)

    val numSentences = docs.map(_.getNumberOfSentences).sum
    println(s"Total number of sentences = $numSentences")

    val numFolds = 5

    def setupDataModelForFold(fold: Int) =
      {
        val groupedDocs = docs.zipWithIndex.groupBy({ case (doc, index) => index % numFolds == fold })
        val testDocs = groupedDocs(true).map(_._1)
        val trainDocs = groupedDocs(false).map(_._1)

        REDataModel.clearInstances
        REDataModel.documents.populate(trainDocs)
        REDataModel.documents.populate(testDocs, train = false)

        (trainDocs, testDocs)
      }

    val experimentResult: List[EvaluationResult] = {
      if (experimentType == REExperimentType.RunMentionCV) {
        (0 until numFolds).flatMap({ fold =>
          setupDataModelForFold(fold)

          println(s"Total number of mentions = ${REDataModel.tokens.getTrainingInstances.size}" +
            s" / ${REDataModel.tokens.getTestingInstances.size}")

          REClassifiers.mentionTypeFineClassifier.forget()
          REClassifiers.mentionTypeCoarseClassifier.forget()

          REClassifiers.mentionTypeFineClassifier.learn(5)
          REClassifiers.mentionTypeCoarseClassifier.learn(5)

          //        addMentionPredictionView(REDataModel.documents.getTestingInstances, Constants.PRED_MENTION_VIEW)

          evaluateMentionTypeClassifier(fold)
        }).toList
      } else {
        (0 until numFolds).flatMap({ fold =>
          setupDataModelForFold(fold)

          println(s"Total number of relations = ${REDataModel.pairedRelations.getTrainingInstances.size}" +
            s" / ${REDataModel.pairedRelations.getTestingInstances.size}")

          REClassifiers.relationTypeFineClassifier.forget()
          REClassifiers.relationTypeCoarseClassifier.forget()

          REClassifiers.relationTypeFineClassifier.learn(5)
          REClassifiers.relationTypeCoarseClassifier.learn(5)

          evaluationRelationTypeClassifier(fold)
        }).toList
      }
    }

    // Evaluation
    experimentResult.groupBy(_.classifierName).foreach({
      case (clfName, evalList) =>
        evalList.foreach(println)
        println(s"Average Precision: : ${evalList.map(_.getPrecision).sum * 100 / evalList.length}")
        println(s"Average Recall: : ${evalList.map(_.getRecall).sum * 100 / evalList.length}")
        println(s"Average F1: : ${evalList.map(_.getF1).sum * 100 / evalList.length}")
    })
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

  def evaluateMentionTypeClassifier(fold: Int): List[EvaluationResult] = {
    val testInstances = REDataModel.tokens.getTestingInstances
    val excludeList = MentionTyper.NONE_MENTION :: Nil

    import REClassifiers._

    evaluate[Constituent](testInstances, "Mention Fine", fold, mentionTypeFineClassifier(_), _.getLabel, excludeList) ::
      evaluate[Constituent](testInstances, "Mention Coarse", fold, mentionTypeCoarseClassifier(_), _.getLabel, excludeList) :: Nil
  }

  def evaluationRelationTypeClassifier(fold: Int): List[EvaluationResult] = {
    val testInstances = REDataModel.pairedRelations.getTestingInstances
    val excludeList = Constants.NO_RELATION :: Nil

    import REClassifiers._
    import REConstrainedClassifiers._

    evaluate[SemanticRelation](testInstances, "Relation Fine", fold, relationTypeFineClassifier(_), _.getFineLabel, excludeList) ::
      evaluate[SemanticRelation](testInstances, "Relation Coarse", fold, relationTypeCoarseClassifier(_), _.getCoarseLabel, excludeList) ::
      evaluate[SemanticRelation](testInstances, "Hierarchy Constrained", fold, relationHierarchyConstrainedClassifier.classifier.discreteValue, _.getFineLabel, excludeList) :: Nil
  }

  private def evaluate[T](
    testInstances: Iterable[T],
    clfName: String,
    fold: Int,
    predictedLabeler: T => String,
    goldLabeler: T => String,
    exclude: List[String] = List.empty
  ): EvaluationResult = {
    // TODO@bhargav - Replace with TestDiscrete or some such
    val (gold, predicted, correct) = testInstances.foldLeft((0, 0, 0))({
      case ((gold, predicted, correct), rel) =>
        val goldLabel = goldLabeler(rel)
        val predictedLabel = predictedLabeler(rel)

        val goldInc = if (exclude.exists(_.equals(goldLabel))) 0 else 1
        val predictedInc = if (exclude.exists(_.equals(predictedLabel))) 0 else 1
        val correctInc = if (predictedInc == 1 && goldLabel.equals(predictedLabel)) 1 else 0

        (gold + goldInc, predicted + predictedInc, correct + correctInc)
    })

    EvaluationResult(clfName, fold, gold, predicted, correct)
  }

  /** Add candidate mentions and typed-candidate mentions to the ACE Document */
  def preProcessDocument(document: TextAnnotation): Unit = {
    val tempDoc: Document = new Document(document)

    //      Method adds candidates to CANDIDATE_MENTION_VIEW View to the TextAnnotation instance
    //      Also adds a CHUNK_PARSE and a SHALLOW_PARSE
    MentionDetector.labelDocMentionCandidates(tempDoc)

    val goldTypedView = document.getView(Constants.GOLD_MENTION_VIEW)
    val mentionView = document.getView(Constants.CANDIDATE_MENTION_VIEW)

    val typedView: SpanLabelView = new SpanLabelView(
      Constants.TYPED_CANDIDATE_MENTION_VIEW,
      "alignFromGold",
      document,
      1.0,
      true
    )

    mentionView.getConstituents.foreach({ c: Constituent =>
      val goldOverlap = goldTypedView.getConstituents.filter(tc => c.getStartSpan == tc.getStartSpan && c.getEndSpan == tc.getEndSpan)
      val label = if (goldOverlap.isEmpty) MentionTyper.NONE_MENTION else goldOverlap.head.getLabel
      typedView.addSpanLabel(c.getStartSpan, c.getEndSpan, label, 1.0)
    })

    document.addView(Constants.TYPED_CANDIDATE_MENTION_VIEW, typedView)
  }

  /** Method to load ACE Documents
   *  Attempts to fetch the serialized TA instances directly and updates cache if a particular
   *  document is not present in the cache directory.
   *
   *  @return List of TextAnnotation items each of them representing a single document
   */
  def loadDataFromCache: Iterator[TextAnnotation] = {
    val masterFileList = ResourceManager.getProjectRoot + "/../data/ace2004/allfiles"
    val cacheBasePath = ResourceManager.getProjectRoot + "/../data_cache/"

    Source.fromFile(masterFileList).getLines().map(fileName => {
      val outputFile = new File(cacheBasePath + fileName + ".ta")
      if (outputFile.exists()) {
        val e = new ObjectInputStream(new FileInputStream(outputFile.getPath))
        val ta = e.readObject.asInstanceOf[TextAnnotation]
        e.close()

        ta
      } else {
        val ta = DataLoader.getACEDocument(fileName).getTextAnnotation

        try {
          new File(cacheBasePath).mkdirs
          val f = new FileOutputStream(outputFile)
          val e = new ObjectOutputStream(f)
          e.writeObject(ta)
          e.flush()
        } catch {
          case ex: Exception => ex.printStackTrace(); outputFile.getAbsolutePath
        }

        ta
      }
    })
  }
}
