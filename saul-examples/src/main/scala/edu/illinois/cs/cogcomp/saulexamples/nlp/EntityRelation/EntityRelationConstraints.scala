/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation

import edu.illinois.cs.cogcomp.saul.classifier.Constraint
import edu.illinois.cs.cogcomp.saulexamples.EntityMentionRelation.datastruct.ConllRelation
import EntityRelationClassifiers._
import Constraint._

object EntityRelationConstraints {
  // if x is works-for relation, it shouldn't be lives-in relation.
  def relationArgumentConstraints = EntityRelationDataModel.pairs.ForAll { x: ConllRelation =>
    worksForConstraint(x) and livesInConstraint(x) and worksForImpliesNotLivesIn(x)
  }

  // if x is lives-in realtion, then its first argument should be person, and second argument should be location.
  def livesInConstraint(x: ConllRelation) = {
    ((LivesInClassifier on2 x) isTrue) ==> (((PersonClassifier on2 x.e1) isTrue) and ((LocationClassifier on2 x.e2) isTrue))
  }

  // if x is works-for relation, then its first argument should be person, and second argument should be organization.
  def worksForConstraint(x: ConllRelation) = {
    ((WorksForClassifier on2 x) isTrue) ==> (((PersonClassifier on2 x.e1) isTrue) and ((OrganizationClassifier on2 x.e2) isTrue))
  }

  // if x is works-for, it cannot be lives-in, and vice verca
  def worksForImpliesNotLivesIn(x: ConllRelation) = {
    ((WorksForClassifier on2 x isTrue) ==> (LivesInClassifier on2 x isFalse)) and
      ((LivesInClassifier on2 x isTrue) ==> (WorksForClassifier on2 x isFalse))
  }

  // TODO: create constrained classifiers for these constraints
  // if x is located-relation, its first argument must be a person or organization, while its second argument
  // must be a location
  def locatedInConstraint(x: ConllRelation) = {
    (LocatedInClassifier on2 x isTrue) ==>
      (((PersonClassifier on2 x.e1 isTrue) or (OrganizationClassifier on2 x.e1 isTrue)) and (LocationClassifier on2 x.e2 isTrue))
  }

  def orgBasedInConstraint(x: ConllRelation) = {
    (OrgBasedInClassifier on2 x isTrue) ==> ((OrganizationClassifier on2 x isTrue) and (LocationClassifier on2 x isTrue))
  }
}
