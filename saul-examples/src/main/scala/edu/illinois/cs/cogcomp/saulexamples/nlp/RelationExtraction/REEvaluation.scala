/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.io.{ FileOutputStream, PrintStream }

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete

/** Helper class for evaluation across experiments */
case class EvaluationResult(classifierName: String, foldIndex: Int, performance: TestDiscrete) {
  override def toString: String = {
    val overallStats = performance.getOverallStats
    s"Classifier: $classifierName Fold $foldIndex - Precision: ${overallStats(0)} // Recall: ${overallStats(1)} // F1: ${overallStats(2)}"
  }

  def writeToDisk(path: String): Unit = {
    val outputStream = new PrintStream(new FileOutputStream(path, true))
    outputStream.println(toString)
    performance.printPerformance(outputStream)
    outputStream.close()
  }
}

object REEvaluation {

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

  def evaluationRelationConstrainedClassifier(fold: Int): List[EvaluationResult] = {
    val testInstances = REDataModel.pairedRelations.getTestingInstances
    val excludeList = REConstants.NO_RELATION :: Nil

    import REConstrainedClassifiers._

    evaluate[SemanticRelation](
      testInstances,
      "Relation Hierarchy Constraint",
      fold,
      relationHierarchyConstrainedClassifier.classifier.discreteValue,
      _.getFineLabel,
      excludeList
    ) :: Nil
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
}
