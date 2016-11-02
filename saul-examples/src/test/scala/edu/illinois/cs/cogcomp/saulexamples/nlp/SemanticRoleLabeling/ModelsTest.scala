/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling

import edu.illinois.cs.cogcomp.saul.classifier.ClassifierUtils
import edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling.SRLClassifiers._
import org.scalatest.{ FlatSpec, Matchers }

class ModelsTest extends FlatSpec with Matchers {
/*
  "argument type classifier (aTr)" should "work." in {
    ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_aTr/", argumentTypeLearner)
    val results = argumentTypeLearner.test(exclude = "candidate")
    results.perLabel
      .filter(!_.f1.isNaN)
      .foreach {
        result =>
          result.label match {
            case "A0" => result.f1 should be(0.95 +- 0.03)
            case "A1" => result.f1 should be(0.95 +- 0.03)
            case "A2" => result.f1 should be(0.85 +- 0.03)
            case _ => (result.f1 >= 0.0) should be(true)
          }
      }
  }

  "predicate identifier (dTr)" should "perform higher than 0.98." in {
    ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_dTr/", predicateClassifier)
    val results = predicateClassifier.test()
    results.perLabel.foreach {
      result =>
        result.label match { case "true" => result.f1 should be(0.99 +- 0.01) }
    }
  }
*/
  "L+I argument type classifier (aTr)" should "work." in {
    ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_fTr/", argumentTypeLearner)
    val scores = argTypeConstrainedClassifier.test(exclude = "candidate")
    scores.perLabel.foreach { resultPerLabel =>
      resultPerLabel.label match {
        case "A0" => resultPerLabel.f1 should be(0.98 +- 0.02)
        case "A1" => resultPerLabel.f1 should be(0.93 +- 0.02)
        case "A2" => resultPerLabel.f1 should be(0.80 +- 0.02)
        case _ => ""
      }
    }
  }

/*
  "argument identifier (bTr)" should "perform higher than 0.95." in {
    ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_bTr/", argumentXuIdentifierGivenApredicate)
    val results = argumentXuIdentifierGivenApredicate.test()
    results.perLabel.foreach {
      result =>
        result.label match { case "true" => (result.f1 >= 0.95) should be(true) }
    }
  }

  "argument identifier (cTr) trained with XuPalmer" should "perform higher than 0.9." in {
    ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_cTr/", argumentTypeLearner)
    val results = argumentTypeLearner.test()
    results.perLabel.foreach {
      result =>
        result.label match {
          case "A0" => result.f1 should be(0.95 +- 0.03)
          case "A1" => result.f1 should be(0.95 +- 0.03)
          case "A2" => result.f1 should be(0.85 +- 0.03)
          case _ => ""
        }
    }
  }

  "argument identifier (fTr) trained with XuPalmer and candidate predicates" should "work." in {
    ClassifierUtils.LoadClassifier(SRLConfigurator.SRL_JAR_MODEL_PATH.value + "/models_fTr/", argumentTypeLearner)
    val results = argumentTypeLearner.test(exclude = "candidate")
    results.perLabel.foreach {
      result =>
        result.label match {
          case "A0" => result.f1 should be(0.95 +- 0.03)
          case "A1" => result.f1 should be(0.95 +- 0.03)
          case "A2" => result.f1 should be(0.85 +- 0.03)
          case _ => ""
        }
    }
  }*/
}

