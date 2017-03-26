/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{Constituent, Relation, TextAnnotation, TokenLabelView}
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier
import edu.illinois.cs.cogcomp.lbjava.infer.{FirstOrderConstant, FirstOrderConstraint}
import edu.illinois.cs.cogcomp.saul.classifier.{ClassifierUtils, ConstrainedClassifier}
import edu.illinois.cs.cogcomp.saul.constraint.ConstraintTypeConversion._
import edu.illinois.cs.cogcomp.saul.constraint.LfsConstraint
import edu.illinois.cs.cogcomp.saulexamples.data.XuPalmerCandidateGenerator
import edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling.SRLClassifiers.{argumentTypeLearner, argumentXuIdentifierGivenApredicate}

import scala.collection.JavaConversions._
import scala.collection.mutable

object SRLConstraintsTest extends App {
  class SRLExplicitConstraints(val dataModel: SRLMultiGraphDataModel,
                               argumentTypeLearner: Classifier,
                               argumentXuIdentifierGivenApredicate: Classifier) {
    import dataModel._

    val noOverlap = ConstrainedClassifier.constraint[TextAnnotation] {
      {
        var a: FirstOrderConstraint = null
        x: TextAnnotation => {
          a = new FirstOrderConstant(true)
          (sentences(x) ~> sentencesToRelations ~> relationsToPredicates).foreach {
            y =>
            {
              val argCandList = XuPalmerCandidateGenerator.generateCandidates(y, (sentences(y.getTextAnnotation) ~> sentencesToStringTree).head)
                .map(y => new Relation("candidate", y.cloneForNewView(y.getViewName), y.cloneForNewView(y.getViewName), 0.0))

              x.getView(ViewNames.TOKENS).asInstanceOf[TokenLabelView].getConstituents.toList.foreach {
                t: Constituent =>
                {
                  val contains = argCandList.filter(z => z.getTarget.doesConstituentCover(t))
                  a = a and contains.toList._atmost(1)({ p: Relation => new FirstOrderConstant(argumentTypeLearner.discreteValue(p).equals("candidate")) })
                }
              }
            }
          }
        }
          a
      }
    } //end of NoOverlap constraint

    val arg_IdentifierClassifier_Constraint = ConstrainedClassifier.constraint[Relation] {
      x: Relation =>
      {
        new FirstOrderConstant(argumentXuIdentifierGivenApredicate.discreteValue(x).equals("false")) ==>
          new FirstOrderConstant(argumentTypeLearner.discreteValue(x).equals("candidate"))
      }
    }

//    val predArg_IdentifierClassifier_Constraint = ConstrainedClassifier.constraint[Relation] {
//      x: Relation =>
//      {
//        new FirstOrderConstant(predicateClassifier.discreteValue(x.getSource).equals("true")) and new FirstOrderConstant(argumentXuIdentifierGivenApredicate.discreteValue(x).equals("true")) ==>
//          new FirstOrderConstant(!argumentTypeLearner.discreteValue(x).equals("candidate"))
//      }
//    }

    val r_arg_Constraint = ConstrainedClassifier.constraint[TextAnnotation] {
      var a: FirstOrderConstraint = null
      x: TextAnnotation => {
        a = new FirstOrderConstant(true)
        val values = Array("R-A1", "R-A2", "R-A3", "R-A4", "R-A5", "R-AA", "R-AM-ADV", "R-AM-CAU", "R-AM-EXT", "R-AM-LOC", "R-AM-MNR", "R-AM-PNC")
        (sentences(x) ~> sentencesToRelations ~> relationsToPredicates).foreach {
          y =>
          {
            val argCandList = (predicates(y) ~> -relationsToPredicates).toList
            argCandList.foreach {
              t: Relation =>
              {
                for (i <- 0 until values.length)
                  a = a and new FirstOrderConstant(argumentTypeLearner.discreteValue(t).equals(values(i))) ==>
                    argCandList.filterNot(x => x.equals(t))._exists {
                      k: Relation => new FirstOrderConstant(argumentTypeLearner.discreteValue(k).equals(values(i).substring(2)))
                    }
              }
                a
            }
          }
        }
      }
        a
    } // end r-arg constraint

    val c_arg_Constraint = ConstrainedClassifier.constraint[TextAnnotation] {
      var a: FirstOrderConstraint = null
      x: TextAnnotation => {
        a = new FirstOrderConstant(true)
        val values = Array("C-A1", "C-A2", "C-A3", "C-A4", "C-A5", "C-AA", "C-AM-DIR", "C-AM-LOC", "C-AM-MNR", "C-AM-NEG", "C-AM-PNC")
        (sentences(x) ~> sentencesToRelations ~> relationsToPredicates).foreach {
          y =>
          {
            val argCandList = (predicates(y) ~> -relationsToPredicates).toList
            val sortedCandidates = argCandList.sortBy(x => x.getTarget.getStartSpan)
            sortedCandidates.zipWithIndex.foreach {
              case (t, ind) =>
              {
                if (ind > 0)
                  for (i <- 0 until values.length)
                    a = a and new FirstOrderConstant(argumentTypeLearner.discreteValue(t).equals(values(i))) ==>
                      sortedCandidates.subList(0, ind)._exists {
                        k: Relation => new FirstOrderConstant(argumentTypeLearner.discreteValue(k).equals(values(i).substring(2)))
                      }
              }
            }
          }
        }
      }
        a
    }

    val legal_arguments_Constraint = ConstrainedClassifier.constraint[TextAnnotation] { x: TextAnnotation =>
      val constraints = for {
        y <- sentences(x) ~> sentencesToRelations ~> relationsToPredicates
        argCandList = (predicates(y) ~> -relationsToPredicates).toList
        argLegalList = legalArguments(y)
        z <- argCandList
      } yield argLegalList._exists { t: String => new FirstOrderConstant(argumentTypeLearner.discreteValue(z).equals(t)) } or
        new FirstOrderConstant(argumentTypeLearner.discreteValue(z).equals("candidate"))
      constraints.toSeq._forall(a => a)
    }

    val noDuplicate = ConstrainedClassifier.constraint[TextAnnotation] {
      // Predicates have at most one argument of each type i.e. there shouldn't be any two arguments with the same type for each predicate
      val values = Array("A0", "A1", "A2", "A3", "A4", "A5", "AA")
      var a: FirstOrderConstraint = null
      x: TextAnnotation => {
        a = new FirstOrderConstant(true)
        (sentences(x) ~> sentencesToRelations ~> relationsToPredicates).foreach {
          y =>
          {
            val argCandList = (predicates(y) ~> -relationsToPredicates).toList
            for (t1 <- 0 until argCandList.size - 1) {
              for (t2 <- t1 + 1 until argCandList.size) {
                a = a and (new FirstOrderConstant(values contains argumentTypeLearner.discreteValue(argCandList.get(t1))) ==>
                  new FirstOrderConstant(!argumentTypeLearner.discreteValue(argCandList.get(t1)).equals(argumentTypeLearner.discreteValue(argCandList.get(t2)))))
              }
            }
          }
        }
        a
      }
    }

    val r_and_c_args = ConstrainedClassifier.constraint[TextAnnotation] {
      x =>
        r_arg_Constraint(x) and c_arg_Constraint(x) and legal_arguments_Constraint(x) and noDuplicate(x)
    }
  }

  val graph = SRLApps.srlDataModelObject
  ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_bTr/", argumentXuIdentifierGivenApredicate)
  ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_aTr/", argumentTypeLearner)

  val constraints = new SRLExplicitConstraints(graph, SRLClassifiers.argumentTypeLearner.getLabeler, SRLClassifiers.argumentXuIdentifierGivenApredicate.getLabeler)
  import constraints._

  val constraintsToEvaluate = List((r_and_c_args, "r_and_c_args"),
    (r_arg_Constraint, "r_arg_Constraint"),
    (c_arg_Constraint, "c_arg_Constraint"),
    (legal_arguments_Constraint, "legal_arguments_Constraint"),
    (noDuplicate, "noDuplicate"))

  val counters = new mutable.HashMap[(LfsConstraint[TextAnnotation], String), Int]()

  println("Number of sentences = " + graph.sentences.getTestingInstances.size)
  graph.sentences.getAllInstances.foreach({ sentence: TextAnnotation =>
    constraintsToEvaluate.foreach({ case (constraint: LfsConstraint[TextAnnotation], friendlyName: String) =>
      val currentVal = counters.getOrElse((constraint, friendlyName), 0)
      if (!constraint.makeConstrainDef(sentence).evaluate()) {
        counters.put((constraint, friendlyName), currentVal + 1)
      }
    })
  })

  println(counters)
}
