package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.lbjava.infer.OJalgoHook
import edu.illinois.cs.cogcomp.saul.classifier.ConstrainedClassifier
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.REClassifiers._
import edu.illinois.cs.cogcomp.saul.constraint.ConstraintTypeConversion._

/** Created by Bhargav Mangipudi on 2/21/16.
  */
object REConstrainedClassifiers {

  object relationHierarchyConstrainedClassifier extends ConstrainedClassifier[SemanticRelation, SemanticRelation](
    relationTypeFineClassifier
  ) {
    def subjectTo = REConstraints.relationHierarchyConstraint
    override def solver = new OJalgoHook()
  }
}
