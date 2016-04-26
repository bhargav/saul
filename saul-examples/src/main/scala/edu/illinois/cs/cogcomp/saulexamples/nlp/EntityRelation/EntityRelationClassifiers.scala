package edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation

import edu.illinois.cs.cogcomp.lbjava.learn.{ SparsePerceptron, StochasticGradientDescent, SupportVectorMachine, SparseNetworkLearner }
import edu.illinois.cs.cogcomp.saul.classifier.Learnable
import edu.illinois.cs.cogcomp.saul.constraint.ConstraintTypeConversion._
import edu.illinois.cs.cogcomp.saul.datamodel.property.Property
import edu.illinois.cs.cogcomp.saulexamples.EntityMentionRelation.datastruct.{ ConllRawSentence, ConllRawToken, ConllRelation }
import edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation.EntityRelationDataModel._

object EntityRelationClassifiers {
  /** independent entity classifiers */
  object OrganizationClassifier extends Learnable[ConllRawToken](EntityRelationDataModel) {
    def label: Property[ConllRawToken] = entityType is "Org"
    override lazy val classifier = new SparseNetworkLBP
    override def feature = using(word, windowWithin[ConllRawSentence](-2, 2, List(pos)), phrase,
      containsSubPhraseMent, containsSubPhraseIng, wordLen)
    // The gazetteer properties are temporarily removed: containsInPersonList, containsInCityList
  }

  object PersonClassifier extends Learnable[ConllRawToken](EntityRelationDataModel) {
    def label: Property[ConllRawToken] = entityType is "Peop"
    override def feature = using(word, windowWithin[ConllRawSentence](-2, 2, List(pos)), phrase,
      containsSubPhraseMent, containsSubPhraseIng, wordLen)
    override lazy val classifier = new SparseNetworkLBP
    // The gazetteer properties are temporarily removed: containsInPersonList, containsInCityList
  }

  object LocationClassifier extends Learnable[ConllRawToken](EntityRelationDataModel) {
    def label: Property[ConllRawToken] = entityType is "Loc"
    override def feature = using(word, windowWithin[ConllRawSentence](-2, 2, List(pos)), phrase, containsSubPhraseMent,
      containsSubPhraseIng, wordLen)
    override lazy val classifier = new SparsePerceptron()
    // The gazetteer properties are temporarily removed: containsInPersonList, containsInCityList
  }

  /** independent relation classifiers */
  object WorksForClassifier extends Learnable[ConllRelation](EntityRelationDataModel) {
    def label: Property[ConllRelation] = relationType is "Work_For"
    override def feature = using(relFeature, relPos)
    override lazy val classifier = new SparseNetworkLBP
  }

  object LivesInClassifier extends Learnable[ConllRelation](EntityRelationDataModel) {
    def label: Property[ConllRelation] = relationType is "Live_In"
    override def feature = using(relFeature, relPos)
    override lazy val classifier = new SparseNetworkLBP
  }

  object OrgBasedInClassifier extends Learnable[ConllRelation](EntityRelationDataModel) {
    override def label: Property[ConllRelation] = relationType is "OrgBased_In"
    override lazy val classifier = new SparsePerceptron()
  }

  object LocatedInClassifier extends Learnable[ConllRelation](EntityRelationDataModel) {
    override def label: Property[ConllRelation] = relationType is "Located_In"
    override lazy val classifier = new SparsePerceptron()
  }

  /** relation pipeline classifiers */
  object WorksForClassifierPipeline extends Learnable[ConllRelation](EntityRelationDataModel) {
    override def label: Property[ConllRelation] = relationType is "Work_For"
    override def feature = using(relFeature, relPos, entityPrediction)
    override lazy val classifier = new SparseNetworkLBP
  }

  object LivesInClassifierPipeline extends Learnable[ConllRelation](EntityRelationDataModel) {
    override def label: Property[ConllRelation] = relationType is "Live_In"
    override def feature = using(relFeature, relPos, entityPrediction)
    override lazy val classifier = new SparseNetworkLBP
  }