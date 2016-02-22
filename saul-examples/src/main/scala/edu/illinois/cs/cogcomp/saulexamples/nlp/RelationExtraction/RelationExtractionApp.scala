package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.io._

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{Constituent, TextAnnotation, SpanLabelView}
import edu.illinois.cs.cogcomp.illinoisRE.common.{Util, Document, ResourceManager, Constants}
import edu.illinois.cs.cogcomp.illinoisRE.data.{SemanticRelation, DataLoader}
import edu.illinois.cs.cogcomp.illinoisRE.mention.{MentionTyper, MentionDetector}
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax

import scala.collection.JavaConversions._
import scala.io.Source

/**
  * Created by Bhargav Mangipudi on 1/28/16.
  */
object RelationExtractionApp {
  object REExperimentType extends Enumeration {
    val RunMentionCV, RunRelationCV = Value
  }

  def main(args: Array[String]): Unit = {
    val experimentType = REExperimentType.RunRelationCV

    val docs = loadDataFromCache.toList

    val numSentences = docs.map(_.getNumberOfSentences).sum
    println(s"Total number of sentences = $numSentences")

    docs.foreach(originalTA => {
      val tempDoc: Document = new Document(originalTA)
      MentionDetector.labelDocMentionCandidates(tempDoc)
      assert(originalTA.equals(tempDoc.getTextAnnotation))

      createTypedCandidateMentions(originalTA, originalTA.getView(Constants.GOLD_MENTION_VIEW).asInstanceOf[SpanLabelView])
    })

    val numFolds = 5

    def setupDataModelForFold(fold: Int) = {
      println(s"Running fold $fold")

      val zipDocs = docs.zipWithIndex
      val trainDocs = zipDocs.filterNot({ case (doc, index) => index % numFolds == fold }).map(_._1)
      val testDocs = zipDocs.filter({ case (doc, index) => index % numFolds == fold }).map(_._1)

      REDataModel.clearInstances
      REDataModel.documents.populate(trainDocs)
      REDataModel.documents.populate(testDocs, false)

      (trainDocs, testDocs)
    }

    if (experimentType == REExperimentType.RunMentionCV) {
      (0 until numFolds).map({ fold =>
        val (_, testDocs) = setupDataModelForFold(fold)

        println(s"Total number of mentions = ${REDataModel.tokens.getTrainingInstances.size}" +
          s" / ${REDataModel.tokens.getTestingInstances.size}")

        REClassifiers.mentionTypeFineClassifier.forget
        REClassifiers.mentionTypeFineClassifier.learn(1)

        testMentionTypeClassifier(testDocs, Constants.PRED_MENTION_VIEW)

        val evalOnFold = evaluateMentionTypeClassifier(testDocs, Constants.GOLD_MENTION_VIEW, Constants.PRED_MENTION_VIEW)

        (evalOnFold, fold)
      }).toList
        .foreach({ case ((untyped, typed), index) =>
          println(s"Fold number $index")
          untyped.printPerformance(System.out)
          typed.printPerformance(System.out)
        })

    } else if (experimentType == REExperimentType.RunRelationCV) {
      (0 until numFolds).map({ fold =>
        setupDataModelForFold(fold)

        println(s"Total number of relations = ${REDataModel.pairedRelations.getTrainingInstances.size}" +
          s" / ${REDataModel.pairedRelations.getTestingInstances.size}")

        REClassifiers.relationTypeFineClassifier.forget
        REClassifiers.relationTypeFineClassifier.learn(1)

        REClassifiers.relationTypeCoarseClassifier.forget
        REClassifiers.relationTypeCoarseClassifier.learn(1)

        (evaluationRelationTypeClassifier, fold)
      })
        .toList
        .foreach({ case (eval, fold) =>
          println(s"Fold number $fold")
          eval.zip(List("Relation Fine", "Relation Coarse", "Relation Constrained")).foreach({ case ((recall, predicted, correct), clf) =>
              println(s"Classifier - $clf - ${(recall, predicted, correct)}")
              println(s"Accuracy - ${correct.toDouble/predicted*100.0} // Recall - ${correct.toDouble/recall*100.0} // F1 - ${Util.calculateF1(correct, recall, predicted)}")
          })
        })
    }
  }

  def testMentionTypeClassifier(testDocs: Iterable[TextAnnotation], predictionViewName: String) : Unit = {
    for (doc <- testDocs) {
      // Predictions are added as a new view to the TA
      val typedView = new SpanLabelView(predictionViewName, "predict", doc, 1.0, true)
      val softmax = new Softmax()

      doc.getView(Constants.CANDIDATE_MENTION_VIEW).getConstituents.foreach({
        c: Constituent =>
          val label = REClassifiers.mentionTypeFineClassifier(c)
          val scoreSet = softmax.normalize(REClassifiers.mentionTypeFineClassifier.classifier.scores(c))
          typedView.addSpanLabel(c.getStartSpan, c.getEndSpan, label, scoreSet.get(label))
      })

      doc.addView(predictionViewName, typedView)
    }
  }

  def evaluateMentionTypeClassifier(testDocs: Iterable[TextAnnotation], goldViewName: String, predictionViewName: String) : (TestDiscrete, TestDiscrete) = {
    val mentionDetectionPerformance = new TestDiscrete()
    val mentionTypePerformance = new TestDiscrete()

    val goldMentionList = testDocs.map(_.getView(goldViewName))
    val predictedMentionList = testDocs.map(_.getView(predictionViewName))

    assert(goldMentionList.size == predictedMentionList.size)

    for ((goldView, predictView) <- goldMentionList.zip(predictedMentionList)) {
      predictView.getConstituents.filterNot(_.getLabel == MentionTyper.NONE_MENTION).foreach({ cc: Constituent =>
        val goldMatch = goldView.getConstituents.filter(gc => cc.getStartSpan == gc.getStartSpan && cc.getEndSpan == gc.getEndSpan).toList
        assert(goldMatch.length <= 1)

        if (goldMatch.length == 1) {
          mentionDetectionPerformance.reportPrediction("MENTION", "MENTION")
          mentionTypePerformance.reportPrediction(cc.getLabel, goldMatch(0).getLabel)
        } else {
          mentionDetectionPerformance.reportPrediction("MENTION", "NO_MENTION")
          mentionTypePerformance.reportPrediction(cc.getLabel, "NO_MENTION")
        }
      })

      goldView.getConstituents.foreach({ gc: Constituent =>
        val predMatch = predictView.getConstituents.filterNot(_.getLabel == MentionTyper.NONE_MENTION).filter(cc => cc.getStartSpan == gc.getStartSpan && cc.getEndSpan == gc.getEndSpan).toList
        assert(predMatch.length <= 1)

        if (predMatch.length == 0)
          mentionDetectionPerformance.reportPrediction("NO_MENTION", "MENTION")
        else {
          if (!gc.getLabel.equals(predMatch(0).getLabel))
            mentionTypePerformance.reportPrediction(predMatch(0).getLabel, gc.getLabel)
        }
      })
    }

    (mentionDetectionPerformance, mentionTypePerformance)
  }

  def evaluationRelationTypeClassifier : List[(Int, Int, Int)] = {
    def evaluate(predf: SemanticRelation => String, goldf: SemanticRelation => String) : (Int, Int, Int) = {
      REDataModel.pairedRelations.getTestingInstances.foldLeft((0, 0, 0))({ case ((r, p, c), rel) =>
        val goldLabel = goldf(rel)
        val predictedLabel = predf(rel)
        val rInc = if (goldLabel != Constants.NO_RELATION) 1 else 0
        val pInc = if (predictedLabel != Constants.NO_RELATION) 1 else 0
        val cInc = if (pInc == 1 && goldLabel.equalsIgnoreCase(predictedLabel)) 1 else 0

        (r + rInc, p + pInc, c + cInc)
      })
    }

    evaluate(REClassifiers.relationTypeFineClassifier(_), _.getFineLabel) ::
    evaluate(REClassifiers.relationTypeCoarseClassifier(_), _.getCoarseLabel) ::
    evaluate(REConstrainedClassifiers.relationTypeFineHierarchyConstained.classifier.discreteValue(_), _.getFineLabel) :: Nil
  }

  def createTypedCandidateMentions(ta: TextAnnotation, goldTypedView: SpanLabelView) {
    val mentionView: SpanLabelView = ta.getView(Constants.CANDIDATE_MENTION_VIEW).asInstanceOf[SpanLabelView]
    val typedView: SpanLabelView = new SpanLabelView(Constants.TYPED_CANDIDATE_MENTION_VIEW, "alignFromGold", ta, 1.0, true)
    import scala.collection.JavaConversions._

    for (c <- mentionView.getConstituents) {
      var label: String = MentionTyper.NONE_MENTION

      for (tc <- goldTypedView.getConstituents) {
        if (c.getStartSpan == tc.getStartSpan && c.getEndSpan == tc.getEndSpan) {
          label = tc.getLabel
        }
      }

      typedView.addSpanLabel(c.getStartSpan, c.getEndSpan, label, 1.0)
    }
    ta.addView(Constants.TYPED_CANDIDATE_MENTION_VIEW, typedView)
  }

  def loadDataFromCache : Iterator[TextAnnotation] = {
    val masterFileList = ResourceManager.getProjectRoot + "/../data/ace2004/allfiles"
    val cacheBasePath = ResourceManager.getProjectRoot + "/../data_cache/"

    Source.fromFile(masterFileList).getLines().map(fileName => {
      val outputFile = new File(cacheBasePath + fileName + ".ta")
      if (outputFile.exists()) {
        val e = new ObjectInputStream(new FileInputStream(outputFile.getPath))
        val ta = e.readObject.asInstanceOf[TextAnnotation]
        e.close

        ta
      } else {
        val ta = DataLoader.getACEDocument(fileName).getTextAnnotation

        try {
          new File(cacheBasePath).mkdirs
          val f = new FileOutputStream(outputFile)
          val e = new ObjectOutputStream(f)
          e.writeObject(ta)
          e.flush
        } catch {
          case ex: Exception => ex.printStackTrace; outputFile.getAbsolutePath
        }

        ta
      }
    })
  }
}
