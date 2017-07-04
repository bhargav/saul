/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, PredicateArgumentView, Relation, TextAnnotation }
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree
import edu.illinois.cs.cogcomp.edison.features.factory._
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AbstractSRLAnnotationReader
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.saul.datamodel.property.PairwiseConjunction
import edu.illinois.cs.cogcomp.saulexamples.data.SRLFrameManager
import edu.illinois.cs.cogcomp.saulexamples.nlp.CommonSensors
import edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling.SRLSensors._
import edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling.SRLClassifiers._
import edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling.SRLConstrainedClassifiers._

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/** Data Model graph for the Semantic Role Labeling task. Represents the data model and feature definitions.
  *
  * @param parseViewName View name for the gold SRL annotation.
  * @param frameManager Instance of the SRL Frame Manager.
  * @param cacheStaticFeatures Boolean to decide if static features should be cached during experimentation. Caching
  *                            feature vectors trades off RAM usage with feature extraction time.
  */
class SRLMultiGraphDataModel(
  parseViewName: String = SRLscalaConfigurator.SRL_PARSE_VIEW,
  frameManager: SRLFrameManager = SRLscalaConfigurator.SRL_FRAME_MANAGER,
  cacheStaticFeatures: Boolean = false
) extends DataModel {
  // Nodes

  val predicates = node[Constituent]((x: Constituent) => x.getTextAnnotation.getCorpusId + ":" + x.getTextAnnotation.getId + ":" + x.getSpan)

  val arguments = node[Constituent]((x: Constituent) => x.getTextAnnotation.getCorpusId + ":" + x.getTextAnnotation.getId + ":" + x.getSpan)

  val relations = node[Relation]((x: Relation) => "S" + x.getSource.getTextAnnotation.getCorpusId + ":" + x.getSource.getTextAnnotation.getId + ":" + x.getSource.getSpan +
    "D" + x.getTarget.getTextAnnotation.getCorpusId + ":" + x.getTarget.getTextAnnotation.getId + ":" + x.getTarget.getSpan)

  val sentences = node[TextAnnotation]((x: TextAnnotation) => x.getCorpusId + ":" + x.getId)

  val stringTree = node[Tree[String]]

  val tokens = node[Constituent]((x: Constituent) => x.getTextAnnotation.getCorpusId + ":" + x.getTextAnnotation.getId + ":" + x.getSpan)

  // Edges

  val sentencesToStringTree = edge(sentences, stringTree)
  val sentencesToTokens = edge(sentences, tokens)
  val sentencesToRelations = edge(sentences, relations)
  val relationsToPredicates = edge(relations, predicates)
  val relationsToArguments = edge(relations, arguments)

  // Sensors

  sentencesToTokens.addSensor(CommonSensors.textAnnotationToTokens _)

  // This sensor is disabled. Relations are populated manually.
  // sentencesToRelations.addSensor(textAnnotationToRelation _)

  sentencesToRelations.addSensor(textAnnotationToRelationMatch _)
  relationsToArguments.addSensor(relToArgument _)
  relationsToPredicates.addSensor(relToPredicate _)
  sentencesToStringTree.addSensor(textAnnotationToStringTree _)

  // Properties

  /** This can be applied to both predicates and arguments */
  val address = property(predicates, "add") {
    x: Constituent => x.getTextAnnotation.getCorpusId + ":" + x.getTextAnnotation.getId + ":" + x.getSpan
  }

  // Classification labels
  val isPredicateGold = property(predicates, "p", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent =>
      {
        val goldPredicates = x.getTextAnnotation
          .getView(ViewNames.SRL_VERB)
          .asInstanceOf[PredicateArgumentView]
          .getPredicates
          .asScala

        // Predicate is gold if it covers the same span as a gold predicate
        goldPredicates.exists(_.getSpan == x.getSpan)
      }
  }

  val predicateSenseGold = property(predicates, "s") {
    x: Constituent => x.getAttribute(AbstractSRLAnnotationReader.SenseIdentifier)
  }

  // Binary label denoting if the relation is a gold relation
  val isArgumentXuGold = property(relations, "aX", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => {
      val goldView = x.getSource
        .getTextAnnotation
        .getView(ViewNames.SRL_VERB)
        .asInstanceOf[PredicateArgumentView]

      if (!goldView.getPredicates.asScala.exists(_.getSpan == x.getSource.getSpan)) {
        false
      } else {
        goldView.getArguments(x.getSource)
          .asScala
          .exists(_.getTarget.getSpan == x.getTarget.getSpan)
      }
    }
  }

  val argumentLabelGold = property(relations, "l", cacheFeatureVector = cacheStaticFeatures) {
    r: Relation => {
      val goldView = r.getSource
        .getTextAnnotation
        .getView(ViewNames.SRL_VERB)
        .asInstanceOf[PredicateArgumentView]

      if (!goldView.getPredicates.asScala.exists(_.getSpan == r.getSource.getSpan)) {
        "candidate"
      } else {
        // Find if the relation exists in Gold view.
        val findRelationInGold = goldView.getArguments(r.getSource)
          .asScala
          .find(_.getTarget.getSpan == r.getTarget.getSpan)

        // If we find the relation, we label it the relation name or label it a candidate
        findRelationInGold.map(_.getRelationName).getOrElse("candidate")
      }
    }
  }

  // Features properties
  val posTag = property(predicates, "posC", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => getPOS(x)
  }

  val subcategorization = property(predicates, "subcatC", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexFeatureExtractor(x, new SubcategorizationFrame(parseViewName))
  }

  val phraseType = property(predicates, "phraseTypeC", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexFeatureExtractor(x, new ParsePhraseType(parseViewName))
  }

  val headword = property(predicates, "headC", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexFeatureExtractor(x, new ParseHeadWordPOS(parseViewName))
  }

  val syntacticFrame = property(predicates, "synFrameC", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexFeatureExtractor(x, new SyntacticFrame(parseViewName))
  }

  val path = property(predicates, "pathC", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexFeatureExtractor(x, new ParsePath(parseViewName))
  }

  //  val subcategorizationRelation = property(relations, "subcat") {
  //    x: Relation => fexFeatureExtractor(x.getTarget, new SubcategorizationFrame(parseViewName))
  //  }

  val phraseTypeRelation = property(relations, "phraseType", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => fexFeatureExtractor(x.getTarget, new ParsePhraseType(parseViewName))
  }

  val headwordRelation = property(relations, "head", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => fexFeatureExtractor(x.getTarget, new ParseHeadWordPOS(parseViewName))
  }

  val syntacticFrameRelation = property(relations, "synFrame", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => fexFeatureExtractor(x.getTarget, new SyntacticFrame(parseViewName))
  }

  val pathRelation = property(relations, "path", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => fexFeatureExtractor(x.getTarget, new ParsePath(parseViewName))
  }

  val predPosTag = property(relations, "pPos", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => getPOS(x.getSource)
  }

  val predLemmaR = property(relations, "pLem", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => getLemma(x.getSource)
  }

  val predLemmaP = property(predicates, "pLem", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => getLemma(x)
  }

  val linearPosition = property(relations, "position", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => fexFeatureExtractor(x.getTarget, new LinearPosition())
  }

  val voice = property(predicates, "voice", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexFeatureExtractor(x, new VerbVoiceIndicator(parseViewName))
  }

  val predWordWindow = property(predicates, "predWordWindow", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexContextFeats(x, WordFeatureExtractorFactory.word)
  }

  val predPOSWindow = property(predicates, "predPOSWindow", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => fexContextFeats(x, WordFeatureExtractorFactory.pos)
  }

  val argWordWindow = property(relations, "argWordWindow", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => fexContextFeats(rel.getTarget, WordFeatureExtractorFactory.word)
  }

  val argPOSWindow = property(relations, "argPOSWindow", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => fexContextFeats(rel.getTarget, WordFeatureExtractorFactory.pos)
  }

  val verbClass = property(predicates, "verbClass", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => frameManager.getAllClasses(getLemma(x)).toList
  }

  val constituentLength = property(relations, "constLength", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => rel.getTarget.getEndSpan - rel.getTarget.getStartSpan
  }

  val chunkLength = property(relations, "chunkLength", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => rel.getTarget.getTextAnnotation.getView(ViewNames.SHALLOW_PARSE).getConstituentsCovering(rel.getTarget).length
  }

  val chunkEmbedding = property(relations, "chunkEmbedding", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => fexFeatureExtractor(rel.getTarget, new ChunkEmbedding(ViewNames.SHALLOW_PARSE))
  }

  val chunkPathPattern = property(relations, "chunkPath", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => fexFeatureExtractor(rel.getTarget, new ChunkPathPattern(ViewNames.SHALLOW_PARSE))
  }

  /** Combines clause relative position and clause coverage */
  val clauseFeatures = property(relations, "clauseFeats", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation =>
      val clauseViewName = parseViewName match {
        case ViewNames.PARSE_GOLD => "CLAUSES_GOLD"
        case ViewNames.PARSE_STANFORD => ViewNames.CLAUSES_STANFORD
        case ViewNames.PARSE_CHARNIAK => ViewNames.CLAUSES_CHARNIAK
      }
      fexFeatureExtractor(rel.getTarget, new ClauseFeatureExtractor(parseViewName, clauseViewName))
  }

  val containsNEG = property(relations, "containsNEG", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => fexFeatureExtractor(rel.getTarget, ChunkPropertyFeatureFactory.isNegated)
  }

  val containsMOD = property(relations, "containsMOD", cacheFeatureVector = cacheStaticFeatures) {
    rel: Relation => fexFeatureExtractor(rel.getTarget, ChunkPropertyFeatureFactory.hasModalVerb)
  }

  // Frame properties
  val legalSenses = property(relations, "legalSens", cacheFeatureVector = cacheStaticFeatures) {
    x: Relation => frameManager.getLegalSenses(predLemmaR(x)).toList

  }

  val legalArguments = property(predicates, "legalArgs", cacheFeatureVector = cacheStaticFeatures) {
    x: Constituent => frameManager.getLegalArguments(predLemmaP(x)).toList
  }

  // Classifiers as properties
  val isPredicatePrediction = property(predicates, "isPredicatePrediction") {
    x: Constituent => predicateClassifier(x)
  }

  val isArgumentPrediction = property(relations, "isArgumentPrediction") {
    x: Relation => argumentXuIdentifierGivenApredicate(x)
  }

  val isArgumentPipePrediction = property(relations, "isArgumentPipePrediction") {
    x: Relation =>
      predicateClassifier(x.getSource) match {
        case "false" => "false"
        case _ => argumentXuIdentifierGivenApredicate(x)

      }
  }

  val typeArgumentPrediction = property(relations, "typeArgumentPrediction") {
    x: Relation =>
      argumentTypeLearner(x)
  }

  val typeArgumentPipePrediction = property(relations) {
    x: Relation =>
      val a: String = predicateClassifier(x.getSource) match {
        case "false" => "false"
        case _ => argumentXuIdentifierGivenApredicate(x)
      }
      val b = a match {
        case "false" => "false"
        case _ => argumentTypeLearner(x)
      }
      b
  }
  val typeArgumentPipeGivenGoldPredicate = property(relations) {
    x: Relation =>
      val a: String = argumentXuIdentifierGivenApredicate(x) match {
        case "false" => "candidate"
        case _ => argumentTypeLearner(x)
      }
      a
  }
  val typeArgumentPipeGivenGoldPredicateConstrained = property(relations) {
    x: Relation =>
      val a: String = argumentXuIdentifierGivenApredicate(x) match {
        case "false" => "candidate"
        case _ => argTypeConstraintClassifier(x)
      }
      a
  }
  val propertyConjunction = property(relations, cacheFeatureVector = cacheStaticFeatures) {
    x: Relation =>
      PairwiseConjunction(List(containsMOD, containsNEG), x)
  }
}
