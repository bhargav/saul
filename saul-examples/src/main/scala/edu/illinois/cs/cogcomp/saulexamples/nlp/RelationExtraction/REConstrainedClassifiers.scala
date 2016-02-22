package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.saul.classifier.ConstrainedClassifier
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.REClassifiers._

/** Created by Bhargav Mangipudi on 2/21/16.
  */
object REConstrainedClassifiers {
  object relationTypeFineHierarchyConstained extends ConstrainedClassifier[SemanticRelation, SemanticRelation](REDataModel, relationTypeFineClassifier.classifier) {
    def subjectTo = REConstraints.relationHierarchyConstraint
  }
}
