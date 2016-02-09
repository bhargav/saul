package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{Sentence, TextAnnotation, Constituent}
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.illinoisRE.mention.MentionTypeFeatures
import edu.illinois.cs.cogcomp.illinoisRE.relation.RelationFeatures
import edu.illinois.cs.cogcomp.saulexamples.nlp.commonSensors

import scala.collection.JavaConversions._

/**
  * Created by Bhargav Mangipudi on 1/28/16.
  */
object REDataModel extends DataModel {

  val documents = node[TextAnnotation]

  val sentences = node[Sentence]

  val tokens = node[Constituent]

  val pairedRelations = node[SemanticRelation]

  val documentToSentences = edge(documents, sentences)
  documentToSentences.addSensor(commonSensors.getSentences _)

  val sentenceToTokens = edge(sentences, tokens)
  sentenceToTokens.addSensor(RESensors.sentenceToTokens _)

  val sentenceToRelations = edge(sentences, pairedRelations)
  sentenceToRelations.addSensor(RESensors.sentenceToRelations _)

//  Mention Type Features

  val mentionBinaryLabel = property(tokens, "mentionBinaryLabel") {
    c: Constituent => {
      if (!c.getLabel.equalsIgnoreCase(REConstants.NONE_MENTION))
        REConstants.EXIST_MENTION
      else
        REConstants.NONE_MENTION
    }
  }

  val mentionCoarseLabel = property(tokens, "mentionCoarseLabel") {
    c: Constituent => {
      val fineLabel = c.getLabel
      val colonIndex = fineLabel.indexOf(":")
      if (colonIndex != -1)
        fineLabel.substring(0, colonIndex)
      else
        fineLabel
    }
  }

  val mentionFineLabel = property(tokens, "mentionFineLabel") {
    c: Constituent => c.getLabel
  }

  val mentionTypeFeatures = property(tokens, "mentionTypeFeatures") {
    c: Constituent => MentionTypeFeatures.generateFeatures(c).toList
  }

//  val NoPrepFeature = property(tokens, "NoPrep") {
//    c: Constituent => MentionTypeFeatures.NoPrep(c)
//  }
//
//  val OnePrepFeature = property(tokens, "OnePrep") {
//    c: Constituent => MentionTypeFeatures.OnePrep(c)
//  }
//
//  val TwoPrepFeature = property(tokens, "TwoPrep") {
//    c: Constituent => MentionTypeFeatures.TwoPrep(c)
//  }
//
//  val MoreThanTwoPrepFeature = property(tokens, "MoreThanTwoPrep") {
//    c: Constituent => MentionTypeFeatures.MoreThanTwoPrep(c)
//  }
//
//  val NoVerbFeature = property(tokens, "NoVerb") {
//    c: Constituent => MentionTypeFeatures.NoVerb(c)
//  }
//
//  val NoCommaFeature = property(tokens, "NoComma") {
//    c: Constituent => MentionTypeFeatures.NoComma(c)
//  }
//
//  val POSIndexBagFeature = property(tokens, "POSIndexBag") {
//    c: Constituent => MentionTypeFeatures.PosIndexBag(c).toList
//  }
//
//  val WordIndexBagFeature = property(tokens, "WordIndexBag") {
//    c: Constituent => MentionTypeFeatures.WordIndexBag(c).toList
//  }
//
//  val POSWordIndexBagFeature = property(tokens, "POSWordIndexBag") {
//    c: Constituent => MentionTypeFeatures.PosWordIndexBag(c).toList
//  }
//
//  val POSEndWordIndexBagFeature = property(tokens, "POSEndWordIndexBag") {
//    c: Constituent => MentionTypeFeatures.PosEndWordIndexBag(c).toList
//  }
//
//  val WordBCIndexBagFeature = property(tokens, "WordBCIndexBag") {
//    c: Constituent => MentionTypeFeatures.WordBCIndexBag(c).toList
//  }
//
//  val POSWordBCIndexBagFeature = property(tokens, "POSWordBCIndexBag") {
//    c: Constituent => MentionTypeFeatures.PosWordBCIndexBag(c).toList
//  }
//
//  val POSEndWordBCIndexBagFeature = property(tokens, "POSEndWordBCIndexBag") {
//    c: Constituent => MentionTypeFeatures.PosEndWordBCIndexBag(c).toList
//  }
//
//  val ParseExactFeature = property(tokens, "ParseExact") {
//    c: Constituent => MentionTypeFeatures.ParseExact(c).toList
//  }
//
//  val ParseCoverFeature = property(tokens, "ParseCover") {
//    c: Constituent => MentionTypeFeatures.ParseCover(c).toList
//  }
//
//  val ContextLeftWordFeature = property(tokens, "ContextLeftWord") {
//    c: Constituent => MentionTypeFeatures.ContextLeftWord(c)
//  }
//
//  val ContextLeftPOSFeature = property(tokens, "ContextLeftPOS") {
//    c: Constituent => MentionTypeFeatures.ContextLeftPos(c)
//  }
//
//  val ContextRightWordFeature = property(tokens, "ContextRightWord") {
//    c: Constituent => MentionTypeFeatures.ContextRightWord(c)
//  }
//
//  val ContextRightPOSFeature = property(tokens, "ContextRightPOS") {
//    c: Constituent => MentionTypeFeatures.ContextRightPos(c)
//  }
//
//  val NERLabelsFeature = property(tokens, "NERLabels") {
//    c: Constituent => MentionTypeFeatures.NerLabels(c).toList
//  }
//
//  val WikiAttributesFeature = property(tokens, "WikiAttributes") {
//    c: Constituent => MentionTypeFeatures.WikiAttributes(c).toList
//  }

  val SurroundingWordsFeature = property(tokens, "SurroundingWords") {
    c: Constituent => MentionTypeFeatures.SurroundingWords(c).toList
  }

  val BOWFeature = property(tokens, "BOW") {
    c: Constituent => MentionTypeFeatures.Bow(c).toList
  }

  val SynOfAllNounFeature = property(tokens, "SynOfAllNoun") {
    c: Constituent => MentionTypeFeatures.SynOfAllNoun(c).toList
  }

  val HeadWordFeature = property(tokens, "HeadWord") {
    c: Constituent => MentionTypeFeatures.Hw(c)
  }

  val WordSequenceFeature = property(tokens, "WordSequence") {
    c: Constituent => MentionTypeFeatures.WordSequence(c)
  }

  val POSSequenceFeature = property(tokens, "POSSequence") {
    c: Constituent => MentionTypeFeatures.PosSequence(c)
  }

  val WordAndPOSSequenceFeature = property(tokens, "WordAndPOSSequence") {
    c: Constituent => MentionTypeFeatures.WordAndPosSequence(c)
  }

  val InPersonListFeature = property(tokens, "InPersonList") {
    c: Constituent => MentionTypeFeatures.InPersonList(c)
  }

  val InPersonTitleListFeature = property(tokens, "InPersonTitleList") {
    c: Constituent => MentionTypeFeatures.InPersonTitleList(c)
  }

  val InPersonNameListFeature = property(tokens, "InPersonNameList") {
    c: Constituent => MentionTypeFeatures.InPersonNameList(c)
  }

  val InPersonPronounListFeature = property(tokens, "InPersonPronounList") {
    c: Constituent => MentionTypeFeatures.InPersonPronounList(c)
  }

  val InPersonDBpediaListFeature = property(tokens, "InPersonDBpediaList") {
    c: Constituent => MentionTypeFeatures.InPersonDBpediaList(c)
  }

  val InGPEListFeature = property(tokens, "InGPEList") {
    c: Constituent => MentionTypeFeatures.InGPEList(c)
  }

  val InGPECityListFeature = property(tokens, "InGPECityList") {
    c: Constituent => MentionTypeFeatures.InGPECityList(c)
  }

  val InGPECountryListFeature = property(tokens, "InGPECountryList") {
    c: Constituent => MentionTypeFeatures.InGPECountryList(c)
  }

  val InGPECountyListFeature = property(tokens, "InGPECountyList") {
    c: Constituent => MentionTypeFeatures.InGPECountyList(c)
  }

  val InGPEStateListFeature = property(tokens, "InGPEStateList") {
    c: Constituent => MentionTypeFeatures.InGPEStateList(c)
  }

  val InGPECommonNounListFeature = property(tokens, "InGPECommonNounList") {
    c: Constituent => MentionTypeFeatures.InGPECommonNounList(c)
  }

  val InGPEMajorAreaListFeature = property(tokens, "InGPEMajorAreaList") {
    c: Constituent => MentionTypeFeatures.InGPEMajorAreaList(c)
  }

  val InEthnicGroupListFeature = property(tokens, "InEthnicGroupList") {
    c: Constituent => MentionTypeFeatures.InEthnicGroupList(c)
  }

  val InNationalityListFeature = property(tokens, "InNationalityList") {
    c: Constituent => MentionTypeFeatures.InNationalityList(c)
  }

  val InEthnicGroupOrNationalityListFeature = property(tokens, "InEthnicGroupOrNationalityList") {
    c: Constituent => MentionTypeFeatures.InEthnicGroupOrNationalityList(c)
  }

  val InOrgGovtListFeature = property(tokens, "InOrgGovtList") {
    c: Constituent => MentionTypeFeatures.InOrgGovtList(c)
  }

  val InOrgCommercialListFeature = property(tokens, "InOrgCommercialList") {
    c: Constituent => MentionTypeFeatures.InOrgCommercialList(c)
  }

  val InOrgEducationalListFeature = property(tokens, "InOrgEducationalList") {
    c: Constituent => MentionTypeFeatures.InOrgEducationalList(c)
  }

  val InFacBarrierListFeature = property(tokens, "InFacBarrierList") {
    c: Constituent => MentionTypeFeatures.InFacBarrierList(c)
  }

  val InFacBuildingListFeature = property(tokens, "InFacBuildingList") {
    c: Constituent => MentionTypeFeatures.InFacBuildingList(c)
  }

  val InFacConduitListFeature = property(tokens, "InFacConduitList") {
    c: Constituent => MentionTypeFeatures.InFacConduitList(c)
  }

  val InFacPathListFeature = property(tokens, "InFacPathList") {
    c: Constituent => MentionTypeFeatures.InFacPathList(c)
  }

  val InFacPlantListFeature = property(tokens, "InFacPlantList") {
    c: Constituent => MentionTypeFeatures.InFacPlantList(c)
  }

  val InFacBuildingSubAreaListFeature = property(tokens, "InFacBuildingSubAreaList") {
    c: Constituent => MentionTypeFeatures.InFacBuildingSubAreaList(c)
  }

  val InFacGenericListFeature = property(tokens, "InFacGenericList") {
    c: Constituent => MentionTypeFeatures.InFacGenericList(c)
  }

  val InWeaListFeature = property(tokens, "InWeaList") {
    c: Constituent => MentionTypeFeatures.InWeaList(c)
  }

  val InVehListFeature = property(tokens, "InVehList") {
    c: Constituent => MentionTypeFeatures.InVehList(c)
  }

  val InOrgPoliticalListFeature = property(tokens, "InOrgPoliticalList") {
    c: Constituent => MentionTypeFeatures.InOrgPoliticalList(c)
  }

  val InOrgTerroristListFeature = property(tokens, "InOrgTerroristList") {
    c: Constituent => MentionTypeFeatures.InOrgTerroristList(c)
  }

  // Relation Type Features

  val relationBinaryLabel = property(pairedRelations, "RelationBinaryLabel") {
    s: SemanticRelation => RelationFeatures.BinaryLabel(s)
  }

  val relationCoarseLabelUndirected = property(pairedRelations, "RelationCoarseLabelUndirected") {
    s: SemanticRelation => RelationFeatures.CoarseLabelUndirected(s)
  }

  val relationCoarseLabel = property(pairedRelations, "RelationCoarseLabel") {
    s: SemanticRelation => RelationFeatures.CoarseLabel(s)
  }

  val relationFineLabel = property(pairedRelations, "RelationFineLabel") {
    s: SemanticRelation => RelationFeatures.FineLabel(s)
  }

  // ------------------

  val WordBetweenNullFeature = property(pairedRelations, "WordBetweenNull") {
    s: SemanticRelation => RelationFeatures.WordBetweenNull(s)
  }

  val M1IncludesM2Feature = property(pairedRelations, "M1IncludesM2") {
    s: SemanticRelation => RelationFeatures.M1IncludesM2(s)
  }

  val M2IncludesM1Feature = property(pairedRelations, "M2IncludesM1") {
    s: SemanticRelation => RelationFeatures.M2IncludesM1(s)
  }

  // ------------------

  val CpInBetweenNullFeature = property(pairedRelations, "CpInBetweenNull") {
    s: SemanticRelation => RelationFeatures.CpInBetweenNull(s)
  }

  val BagOfChunkTypesInBetweenFeature = property(pairedRelations, "BagOfChunkTypesInBetween") {
    s: SemanticRelation => RelationFeatures.BagOfChunkTypesInBetween(s).toList
  }

  // ------------------

  val BowM1Feature = property(pairedRelations, "BowM1") {
    s: SemanticRelation => RelationFeatures.BowM1(s).toList
  }

  val BowM2Feature = property(pairedRelations, "BowM2") {
    s: SemanticRelation => RelationFeatures.BowM2(s).toList
  }

  // ------------------

  val HwM1Feature = property(pairedRelations, "HwM1") {
    s: SemanticRelation => RelationFeatures.HwM1(s)
  }

  val HwM2Feature = property(pairedRelations, "HwM2") {
    s: SemanticRelation => RelationFeatures.HwM2(s)
  }

  // ------------------

  val LhwM1Feature = property(pairedRelations, "LhwM1") {
    s: SemanticRelation => RelationFeatures.LhwM1(s)
  }

  val HwM1RFeature = property(pairedRelations, "HwM1R") {
    s: SemanticRelation => RelationFeatures.HwM1R(s)
  }

  val LhwM2Feature = property(pairedRelations, "LhwM2") {
    s: SemanticRelation => RelationFeatures.LhwM2(s)
  }

  val HwM2RFeature = property(pairedRelations, "HwM2R") {
    s: SemanticRelation => RelationFeatures.HwM2R(s)
  }

  val LLhwM1Feature = property(pairedRelations, "LLhwM1") {
    s: SemanticRelation => RelationFeatures.LLhwM1(s)
  }

  val LhwM1RFeature = property(pairedRelations, "LhwM1R") {
    s: SemanticRelation => RelationFeatures.LhwM1R(s)
  }

  val HwM1RRFeature = property(pairedRelations, "HwM1RR") {
    s: SemanticRelation => RelationFeatures.HwM1RR(s)
  }

  val LLhwM2Feature = property(pairedRelations, "LLhwM2") {
    s: SemanticRelation => RelationFeatures.LLhwM2(s)
  }

  val LhwM2RFeature = property(pairedRelations, "LhwM2R") {
    s: SemanticRelation => RelationFeatures.LhwM2R(s)
  }

  val HwM2RRFeature = property(pairedRelations, "HwM2RR") {
    s: SemanticRelation => RelationFeatures.HwM2RR(s)
  }

  // ------------------

  val PM1aPM2Feature = property(pairedRelations, "PM1aPM2") {
    s: SemanticRelation => RelationFeatures.PM1aPM2(s)
  }

  val M1PaM2PFeature = property(pairedRelations, "M1PaM2P") {
    s: SemanticRelation => RelationFeatures.M1PaM2P(s)
  }

  val PPM1aPPM2Feature = property(pairedRelations, "PPM1aPPM2") {
    s: SemanticRelation => RelationFeatures.PPM1aPPM2(s)
  }

  val PM1PaPM2PFeature = property(pairedRelations, "PM1PaPM2P") {
    s: SemanticRelation => RelationFeatures.PM1PaPM2P(s)
  }

  val M1PPaM2PPFeature = property(pairedRelations, "M1PPaM2PP") {
    s: SemanticRelation => RelationFeatures.M1PPaM2PP(s)
  }

  // --------------------

  val PofM1HwFeature = property(pairedRelations, "PofM1Hw") {
    s: SemanticRelation => RelationFeatures.PofM1Hw(s)
  }

  val PofM2HwFeature = property(pairedRelations, "PofM2Hw") {
    s: SemanticRelation => RelationFeatures.PofM2Hw(s)
  }

  val PosBetweenSingleFeature = property(pairedRelations, "PosBetweenSingle") {
    s: SemanticRelation => RelationFeatures.PosBetweenSingle(s)
  }

  // --------------------

  val PbeforeM1HeadFeature = property(pairedRelations, "PbeforeM1Head") {
    s: SemanticRelation => RelationFeatures.PbeforeM1Head(s)
  }

  val PafterM1HeadFeature = property(pairedRelations, "PafterM1Head") {
    s: SemanticRelation => RelationFeatures.PafterM1Head(s)
  }

  val PbeforeM2HeadFeature = property(pairedRelations, "PbeforeM2Head") {
    s: SemanticRelation => RelationFeatures.PbeforeM2Head(s)
  }

  val PafterM2HeadFeature = property(pairedRelations, "PafterM2Head") {
    s: SemanticRelation => RelationFeatures.PafterM2Head(s)
  }

  // ------------------

  val HwM1M2Feature = property(pairedRelations, "HwM1M2") {
    s: SemanticRelation => RelationFeatures.HwM1M2(s)
  }

  val WordBetweenSingleFeature = property(pairedRelations, "WordBetweenSingle") {
    s: SemanticRelation => RelationFeatures.WordBetweenSingle(s)
  }

  val WordBetweenFirstFeature = property(pairedRelations, "WordBetweenFirst") {
    s: SemanticRelation => RelationFeatures.WordBetweenFirst(s)
  }

  val WordBetweenLastFeature = property(pairedRelations, "WordBetweenLast") {
    s: SemanticRelation => RelationFeatures.WordBetweenLast(s)
  }

  val WordBetweenBOWFeature = property(pairedRelations, "WordBetweenBOW") {
    s: SemanticRelation => {
      val wordList = RelationFeatures.WordBetweenBow(s)
      if (wordList == null) List.empty else wordList.toList
    }
  }

  // ------------------

  val BigramsInBetweenFeature = property(pairedRelations, "BigramsInBetween") {
    s: SemanticRelation => {
      val biGrams = RelationFeatures.BigramsInBetween(s)
      if (biGrams == null) List.empty else biGrams.toList
    }
  }

  // ------------------

  val M1MostConfidentMainTypeFeature = property(pairedRelations, "M1MostConfidentMainType") {
    s: SemanticRelation => RelationFeatures.M1MostConfidentMainType(s)
  }

  val M2MostConfidentMainTypeFeature = property(pairedRelations, "M2MostConfidentMainType") {
    s: SemanticRelation => RelationFeatures.M2MostConfidentMainType(s)
  }

  val M1M2MostConfidentMainTypeFeature = property(pairedRelations, "M1M2MostConfidentMainType") {
    s: SemanticRelation => RelationFeatures.M1M2MostConfidentMainType(s)
  }

  val M1MostConfidentSubTypeFeature = property(pairedRelations, "M1MostConfidentSubType") {
    s: SemanticRelation => RelationFeatures.M1MostConfidentSubType(s)
  }

  val M2MostConfidentSubTypeFeature = property(pairedRelations, "M2MostConfidentSubType") {
    s: SemanticRelation => RelationFeatures.M2MostConfidentSubType(s)
  }

  val M1M2MostConfidentSubTypeFeature = property(pairedRelations, "M1M2MostConfidentSubType") {
    s: SemanticRelation => RelationFeatures.M1M2MostConfidentSubType(s)
  }

  val M1M2MentionLevelFeature = property(pairedRelations, "M1M2MentionLevel") {
    s: SemanticRelation => RelationFeatures.M1M2MentionLevel(s)
  }

  val M1LevelMainTypeFeature = property(pairedRelations, "M1LevelMainType") {
    s: SemanticRelation => RelationFeatures.M1LevelMainType(s)
  }

  val M2LevelMainTypeFeature = property(pairedRelations, "M2LevelMainType") {
    s: SemanticRelation => RelationFeatures.M2LevelMainType(s)
  }

  val M1LevelMainTypeAndm2LevelMainTypeFeature = property(pairedRelations, "M1LevelMainTypeAndm2LevelMainType") {
    s: SemanticRelation => RelationFeatures.M1LevelMainTypeAndm2LevelMainType(s)
  }

  val M1LevelSubTypeFeature = property(pairedRelations, "M1LevelSubType") {
    s: SemanticRelation => RelationFeatures.M1LevelSubType(s)
  }

  val M2LevelSubTypeFeature = property(pairedRelations, "M2LevelSubType") {
    s: SemanticRelation => RelationFeatures.M2LevelSubType(s)
  }

  val M1LevelSubTypeAndm2LevelSubTypeFeature = property(pairedRelations, "M1LevelSubTypeAndm2LevelSubType") {
    s: SemanticRelation => RelationFeatures.M1LevelSubTypeAndm2LevelSubType(s)
  }

  // ------------------

  val M1MainType_m1IncludesM2Feature = property(pairedRelations, "M1MainType_m1IncludesM2") {
    s: SemanticRelation => RelationFeatures.M1MainType_m1IncludesM2(s)
  }

  val M2MainType_m1IncludesM2Feature = property(pairedRelations, "M2MainType_m1IncludesM2") {
    s: SemanticRelation => RelationFeatures.M2MainType_m1IncludesM2(s)
  }

  val M1m2MainType_m1IncludesM2Feature = property(pairedRelations, "M1m2MainType_m1IncludesM2") {
    s: SemanticRelation => RelationFeatures.M1m2MainType_m1IncludesM2(s)
  }

  val M1MainType_m2IncludesM1Feature = property(pairedRelations, "M1MainType_m2IncludesM1") {
    s: SemanticRelation => RelationFeatures.M1MainType_m2IncludesM1(s)
  }

  val M2MainType_m2IncludesM1Feature = property(pairedRelations, "M2MainType_m2IncludesM1") {
    s: SemanticRelation => RelationFeatures.M2MainType_m2IncludesM1(s)
  }

  val M1m2MainType_m2IncludesM1Feature = property(pairedRelations, "M1m2MainType_m2IncludesM1") {
    s: SemanticRelation => RelationFeatures.M1m2MainType_m2IncludesM1(s)
  }

  val M1SubType_m1IncludesM2Feature = property(pairedRelations, "M1SubType_m1IncludesM2") {
    s: SemanticRelation => RelationFeatures.M1SubType_m1IncludesM2(s)
  }

  val M2SubType_m1IncludesM2Feature = property(pairedRelations, "M2SubType_m1IncludesM2") {
    s: SemanticRelation => RelationFeatures.M2SubType_m1IncludesM2(s)
  }

  val M1m2SubType_m1IncludesM2Feature = property(pairedRelations, "M1m2SubType_m1IncludesM2") {
    s: SemanticRelation => RelationFeatures.M1m2SubType_m1IncludesM2(s)
  }

  val M1SubType_m2IncludesM1Feature = property(pairedRelations, "M1SubType_m2IncludesM1") {
    s: SemanticRelation => RelationFeatures.M1SubType_m2IncludesM1(s)
  }

  val M2SubType_m2IncludesM1Feature = property(pairedRelations, "M2SubType_m2IncludesM1") {
    s: SemanticRelation => RelationFeatures.M2SubType_m2IncludesM1(s)
  }

  val M1m2SubType_m2IncludesM1Feature = property(pairedRelations, "M1m2SubType_m2IncludesM1") {
    s: SemanticRelation => RelationFeatures.M1m2SubType_m2IncludesM1(s)
  }

  // ------------------

  val M1HeadWordAndDepParentWordFeature = property(pairedRelations, "M1HeadWordAndDepParentWord") {
    s: SemanticRelation => RelationFeatures.M1HeadWordAndDepParentWord(s)
  }

  val M2HeadWordAndDepParentWordFeature = property(pairedRelations, "M2HeadWordAndDepParentWord") {
    s: SemanticRelation => RelationFeatures.M2HeadWordAndDepParentWord(s)
  }

  // ------------------

  val M1DepLabelFeature = property(pairedRelations, "M1DepLabel") {
    s: SemanticRelation => RelationFeatures.M1DepLabel(s)
  }

  val M2DepLabelFeature = property(pairedRelations, "M2DepLabel") {
    s: SemanticRelation => RelationFeatures.M2DepLabel(s)
  }

  // ------------------

  val DepPathInBetweenFeature = property(pairedRelations, "DepPathInBetween") {
    s: SemanticRelation => RelationFeatures.DepPathInBetween(s)
  }

  val DepLabelsInBetweenFeature = property(pairedRelations, "DepLabelsInBetween") {
    s: SemanticRelation => {
      val depLabelList = RelationFeatures.DepLabelsInBetween(s)
      if (depLabelList == null) List.empty else depLabelList.toList
    }
  }

  val FirstDepLabelInBetweenFeature = property(pairedRelations, "FirstDepLabelInBetween") {
    s: SemanticRelation => RelationFeatures.FirstDepLabelInBetween(s)
  }

  val LastDepLabelInBetweenFeature = property(pairedRelations, "LastDepLabelInBetween") {
    s: SemanticRelation => RelationFeatures.LastDepLabelInBetween(s)
  }

  val FeaturesOfFirstPrep = property(pairedRelations, "FeaturesOfFirstPrep") {
    s: SemanticRelation => {
      val featuresList = RelationFeatures.FeaturesOfFirstPrep(s)
      if (featuresList == null) List.empty else featuresList.toList
    }
  }

  val FeaturesOfSecondPrep = property(pairedRelations, "FeaturesOfSecondPrep") {
    s: SemanticRelation => {
      val featuresList = RelationFeatures.FeaturesOfSecondPrep(s)
      if (featuresList == null) List.empty else featuresList.toList
    }
  }

  val FeaturesOfLastPrep = property(pairedRelations, "FeaturesOfLastPrep") {
    s: SemanticRelation => {
      val featuresList = RelationFeatures.FeaturesOfLastPrep(s)
      if (featuresList == null) List.empty else featuresList.toList
    }
  }

  val OnePrepInBetweenFeature = property(pairedRelations, "OnePrepInBetween") {
    s: SemanticRelation => RelationFeatures.OnePrepInBetween(s)
  }

  val TwoPrepInBetweenFeature = property(pairedRelations, "TwoPrepInBetween") {
    s: SemanticRelation => RelationFeatures.TwoPrepInBetween(s)
  }

  val MoreThanTwoPrepInBetweenFeature = property(pairedRelations, "MoreThanTwoPrepInBetween") {
    s: SemanticRelation => RelationFeatures.MoreThanTwoPrepInBetween(s)
  }

  val SinglePrepStringInBetweenFeature = property(pairedRelations, "SinglePrepStringInBetween") {
    s: SemanticRelation => RelationFeatures.SinglePrepStringInBetween(s)
  }

  val FirstPrepStringInBetweenFeature = property(pairedRelations, "FirstPrepStringInBetween") {
    s: SemanticRelation => RelationFeatures.FirstPrepStringInBetween(s)
  }

  val LastPrepStringInBetweenFeature = property(pairedRelations, "LastPrepStringInBetween") {
    s: SemanticRelation => RelationFeatures.LastPrepStringInBetween(s)
  }

  val M1IsNationalityFeature = property(pairedRelations, "M1IsNationality") {
    s: SemanticRelation => RelationFeatures.M1IsNationality(s)
  }

  val M2IsNationalityFeature = property(pairedRelations, "M2IsNationality") {
    s: SemanticRelation => RelationFeatures.M2IsNationality(s)
  }

  val PreModIsPartOfWikiTitleFeature = property(pairedRelations, "PreModIsPartOfWikiTitle") {
    s: SemanticRelation => RelationFeatures.PreModIsPartOfWikiTitle(s)
  }

  val PremodIsWordNetNounCollocationFeature = property(pairedRelations, "PremodIsWordNetNounCollocation") {
    s: SemanticRelation => RelationFeatures.PremodIsWordNetNounCollocation(s)
  }

  val HasCommonVerbSRLPredicateFeature = property(pairedRelations, "HasCommonVerbSRLPredicate") {
    s: SemanticRelation => RelationFeatures.HasCommonVerbSRLPredicate(s)
  }

  // ========= BROWN CLUSTER FEATURES ======

  val BowM1bc10Feature = property(pairedRelations, "BowM1bc10") {
    s: SemanticRelation => {
      val wordsList = RelationFeatures.BowM1bc(s, 10)
      if (wordsList == null) List.empty else wordsList.toList
    }
  }

  val BowM2bc10Feature = property(pairedRelations, "BowM2bc10") {
    s: SemanticRelation => {
      val wordsList = RelationFeatures.BowM2bc(s, 10)
      if (wordsList == null) List.empty else wordsList.toList
    }
  }

  val HwM1bc10Feature = property(pairedRelations, "HwM1bc10") {
    s: SemanticRelation => RelationFeatures.HwM1bc(s, 10)
  }

  val HwM2bc10Feature = property(pairedRelations, "HwM2bc10") {
    s: SemanticRelation => RelationFeatures.HwM2bc(s, 10)
  }

  val LhwM1bc10Feature = property(pairedRelations, "LhwM1bc10") {
    s: SemanticRelation => RelationFeatures.LhwM1bc(s, 10)
  }

  val HwM1Rbc10Feature = property(pairedRelations, "HwM1Rbc10") {
    s: SemanticRelation => RelationFeatures.HwM1Rbc(s, 10)
  }

  val LhwM2bc10Feature = property(pairedRelations, "LhwM2bc10") {
    s: SemanticRelation => RelationFeatures.LhwM2bc(s, 10)
  }

  val HwM2Rbc10Feature = property(pairedRelations, "HwM2Rbc10") {
    s: SemanticRelation => RelationFeatures.HwM2Rbc(s, 10)
  }

  val LLhwM1bc10Feature = property(pairedRelations, "LLhwM1bc10") {
    s: SemanticRelation => RelationFeatures.LLhwM1bc(s, 10)
  }

  val LhwM1Rbc10Feature = property(pairedRelations, "LhwM1Rbc10") {
    s: SemanticRelation => RelationFeatures.LhwM1Rbc(s, 10)
  }

  val HwM1RRbc10Feature = property(pairedRelations, "HwM1RRbc10") {
    s: SemanticRelation => RelationFeatures.HwM1RRbc(s, 10)
  }

  val LLhwM2bc10Feature = property(pairedRelations, "LLhwM2bc10") {
    s: SemanticRelation => RelationFeatures.LLhwM2bc(s, 10)
  }

  val LhwM2Rbc10Feature = property(pairedRelations, "LhwM2Rbc10") {
    s: SemanticRelation => RelationFeatures.LhwM2Rbc(s, 10)
  }

  val HwM2RRbc10Feature = property(pairedRelations, "HwM2RRbc10") {
    s: SemanticRelation => RelationFeatures.HwM2RRbc(s, 10)
  }

  val PM1aPM2bc10Feature = property(pairedRelations, "PM1aPM2bc10") {
    s: SemanticRelation => RelationFeatures.PM1aPM2bc(s, 10)
  }

  val M1PaM2Pbc10Feature = property(pairedRelations, "M1PaM2Pbc10") {
    s: SemanticRelation => RelationFeatures.M1PaM2Pbc(s, 10)
  }

  val PPM1aPPM2bc10Feature = property(pairedRelations, "PPM1aPPM2bc10") {
    s: SemanticRelation => RelationFeatures.PPM1aPPM2bc(s, 10)
  }

  val PM1PaPM2Pbc10Feature = property(pairedRelations, "PM1PaPM2Pbc10") {
    s: SemanticRelation => RelationFeatures.PM1PaPM2Pbc(s, 10)
  }

  val M1PPaM2PPbc10Feature = property(pairedRelations, "M1PPaM2PPbc10") {
    s: SemanticRelation => RelationFeatures.M1PPaM2PPbc(s, 10)
  }

  val HwM1M2bc10Feature = property(pairedRelations, "HwM1M2bc10") {
    s: SemanticRelation => RelationFeatures.HwM1M2bc(s, 10)
  }

  val WordBetweenSinglebc10Feature = property(pairedRelations, "WordBetweenSinglebc10") {
    s: SemanticRelation => RelationFeatures.WordBetweenSinglebc(s, 10)
  }

  val WordBetweenFirstbc10Feature = property(pairedRelations, "WordBetweenFirstbc10") {
    s: SemanticRelation => RelationFeatures.WordBetweenFirstbc(s, 10)
  }

  val WordBetweenLastbc10Feature = property(pairedRelations, "WordBetweenLastbc10") {
    s: SemanticRelation => RelationFeatures.WordBetweenLastbc(s, 10)
  }

  val M1HeadWordAndDepParentWordbc10Feature = property(pairedRelations, "M1HeadWordAndDepParentWordbc10") {
    s: SemanticRelation => RelationFeatures.M1HeadWordAndDepParentWordbc(s, 10)
  }

  val M2HeadWordAndDepParentWordbc10Feature = property(pairedRelations, "M2HeadWordAndDepParentWordbc10") {
    s: SemanticRelation => RelationFeatures.M2HeadWordAndDepParentWordbc(s, 10)
  }

  val SinglePrepStringInBetweenbc10Feature = property(pairedRelations, "SinglePrepStringInBetweenbc10") {
    s: SemanticRelation => RelationFeatures.SinglePrepStringInBetweenbc(s, 10)
  }

  val FirstPrepStringInBetweenbc10Feature = property(pairedRelations, "FirstPrepStringInBetweenbc10") {
    s: SemanticRelation => RelationFeatures.FirstPrepStringInBetweenbc(s, 10)
  }

  val LastPrepStringInBetweenbc10Feature = property(pairedRelations, "LastPrepStringInBetweenbc10") {
    s: SemanticRelation => RelationFeatures.LastPrepStringInBetweenbc(s, 10)
  }

//  val M1WikiAttributesFeature = property(pairedRelations, "M1WikiAttributes") {
//    s: SemanticRelation => {
//      val attributeList = RelationFeatures.M1WikiAttributes(s)
//      if (attributeList == null) List.empty else attributeList.toList
//    }
//  }
//
//  val M2WikiAttributesFeature = property(pairedRelations, "M2WikiAttributes") {
//    s: SemanticRelation => {
//      val attributeList = RelationFeatures.M2WikiAttributes(s)
//      if (attributeList == null) List.empty else attributeList.toList
//    }
//  }

  val HasCoveringMentionFeature = property(pairedRelations, "HasCoveringMention") {
    s: SemanticRelation => RelationFeatures.HasCoveringMention(s)
  }

  val FrontPosSequenceFeature = property(pairedRelations, "FrontPosSequence") {
    s: SemanticRelation => RelationFeatures.FrontPosSequence(s)
  }

  val BackPosSequenceFeature = property(pairedRelations, "BackPosSequence") {
    s: SemanticRelation => RelationFeatures.BackPosSequence(s)
  }

  val SmallerMentionIsPerTitleFeature = property(pairedRelations, "SmallerMentionIsPerTitle") {
    s: SemanticRelation => RelationFeatures.SmallerMentionIsPerTitle(s)
  }

  val PosAfterM1Feature = property(pairedRelations, "PosAfterM1") {
    s: SemanticRelation => RelationFeatures.PosAfterM1(s)
  }

  val PosBeforeM1Feature = property(pairedRelations, "PosBeforeM1") {
    s: SemanticRelation => RelationFeatures.PosBeforeM1(s)
  }

  val WordAfterM1Feature = property(pairedRelations, "WordAfterM1") {
    s: SemanticRelation => RelationFeatures.WordAfterM1(s)
  }

  val PosOfLastWordInM1Feature = property(pairedRelations, "PosOfLastWordInM1") {
    s: SemanticRelation => RelationFeatures.PosOfLastWordInM1(s)
  }

  val OnlyPrepInDepPathFeature = property(pairedRelations, "OnlyPrepInDepPath") {
    s: SemanticRelation => RelationFeatures.OnlyPrepInDepPath(s)
  }

  val ApposInDepPathFeature = property(pairedRelations, "ApposInDepPath") {
    s: SemanticRelation => RelationFeatures.ApposInDepPath(s)
  }

  val PosOfSingleWordBetweenMentionsFeature = property(pairedRelations, "PosOfSingleWordBetweenMentions") {
    s: SemanticRelation => RelationFeatures.PosOfSingleWordBetweenMentions(s)
  }

  val SingleWordBetweenMentionsFeature = property(pairedRelations, "SingleWordBetweenMentions") {
    s: SemanticRelation => RelationFeatures.SingleWordBetweenMentions(s)
  }
}
