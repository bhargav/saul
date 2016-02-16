package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{Sentence, TextAnnotation, Constituent}
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.illinoisRE.mention.MentionTypeFeatures
import edu.illinois.cs.cogcomp.illinoisRE.relation.RelationFeatures
import edu.illinois.cs.cogcomp.saulexamples.nlp.CommonSensors

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
  documentToSentences.addSensor(CommonSensors.getSentences _)

  val sentenceToTokens = edge(sentences, tokens)
  sentenceToTokens.addSensor(RESensors.sentenceToTokens _)

  val sentenceToRelations = edge(sentences, pairedRelations)
  sentenceToRelations.addSensor(RESensors.sentenceToRelations _)


  def sanitizeFeature(f: String) = if (f != null) f else ""

  def sanitizeFeature(f: Array[String]) : List[String] = if (f != null) f.toList else List.empty[String]

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
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BinaryLabel(s))
  }

  val relationCoarseLabelUndirected = property(pairedRelations, "RelationCoarseLabelUndirected") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.CoarseLabelUndirected(s))
  }

  val relationCoarseLabel = property(pairedRelations, "RelationCoarseLabel") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.CoarseLabel(s))
  }

  val relationFineLabel = property(pairedRelations, "RelationFineLabel") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FineLabel(s))
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
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BagOfChunkTypesInBetween(s))
  }

  // ------------------

  val BowM1Feature = property(pairedRelations, "BowM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BowM1(s))
  }

  val BowM2Feature = property(pairedRelations, "BowM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BowM2(s))
  }

  // ------------------

  val HwM1Feature = property(pairedRelations, "HwM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1(s))
  }

  val HwM2Feature = property(pairedRelations, "HwM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM2(s))
  }

  // ------------------

  val LhwM1Feature = property(pairedRelations, "LhwM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM1(s))
  }

  val HwM1RFeature = property(pairedRelations, "HwM1R") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1R(s))
  }

  val LhwM2Feature = property(pairedRelations, "LhwM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM2(s))
  }

  val HwM2RFeature = property(pairedRelations, "HwM2R") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM2R(s))
  }

  val LLhwM1Feature = property(pairedRelations, "LLhwM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LLhwM1(s))
  }

  val LhwM1RFeature = property(pairedRelations, "LhwM1R") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM1R(s))
  }

  val HwM1RRFeature = property(pairedRelations, "HwM1RR") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1RR(s))
  }

  val LLhwM2Feature = property(pairedRelations, "LLhwM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LLhwM2(s))
  }

  val LhwM2RFeature = property(pairedRelations, "LhwM2R") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM2R(s))
  }

  val HwM2RRFeature = property(pairedRelations, "HwM2RR") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM2RR(s))
  }

  // ------------------

  val PM1aPM2Feature = property(pairedRelations, "PM1aPM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PM1aPM2(s))
  }

  val M1PaM2PFeature = property(pairedRelations, "M1PaM2P") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1PaM2P(s))
  }

  val PPM1aPPM2Feature = property(pairedRelations, "PPM1aPPM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PPM1aPPM2(s))
  }

  val PM1PaPM2PFeature = property(pairedRelations, "PM1PaPM2P") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PM1PaPM2P(s))
  }

  val M1PPaM2PPFeature = property(pairedRelations, "M1PPaM2PP") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1PPaM2PP(s))
  }

  // --------------------

  val PofM1HwFeature = property(pairedRelations, "PofM1Hw") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PofM1Hw(s))
  }

  val PofM2HwFeature = property(pairedRelations, "PofM2Hw") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PofM2Hw(s))
  }

  val PosBetweenSingleFeature = property(pairedRelations, "PosBetweenSingle") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PosBetweenSingle(s))
  }

  // --------------------

  val PbeforeM1HeadFeature = property(pairedRelations, "PbeforeM1Head") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PbeforeM1Head(s))
  }

  val PafterM1HeadFeature = property(pairedRelations, "PafterM1Head") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PafterM1Head(s))
  }

  val PbeforeM2HeadFeature = property(pairedRelations, "PbeforeM2Head") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PbeforeM2Head(s))
  }

  val PafterM2HeadFeature = property(pairedRelations, "PafterM2Head") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PafterM2Head(s))
  }

  // ------------------

  val HwM1M2Feature = property(pairedRelations, "HwM1M2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1M2(s))
  }

  val WordBetweenSingleFeature = property(pairedRelations, "WordBetweenSingle") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordBetweenSingle(s))
  }

  val WordBetweenFirstFeature = property(pairedRelations, "WordBetweenFirst") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordBetweenFirst(s))
  }

  val WordBetweenLastFeature = property(pairedRelations, "WordBetweenLast") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordBetweenLast(s))
  }

  val WordBetweenBOWFeature = property(pairedRelations, "WordBetweenBOW") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordBetweenBow(s))
  }

  // ------------------

  val BigramsInBetweenFeature = property(pairedRelations, "BigramsInBetween") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BigramsInBetween(s))
  }

  // ------------------

  val M1MostConfidentMainTypeFeature = property(pairedRelations, "M1MostConfidentMainType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1MostConfidentMainType(s))
  }

  val M2MostConfidentMainTypeFeature = property(pairedRelations, "M2MostConfidentMainType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2MostConfidentMainType(s))
  }

  val M1M2MostConfidentMainTypeFeature = property(pairedRelations, "M1M2MostConfidentMainType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1M2MostConfidentMainType(s))
  }

  val M1MostConfidentSubTypeFeature = property(pairedRelations, "M1MostConfidentSubType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1MostConfidentSubType(s))
  }

  val M2MostConfidentSubTypeFeature = property(pairedRelations, "M2MostConfidentSubType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2MostConfidentSubType(s))
  }

  val M1M2MostConfidentSubTypeFeature = property(pairedRelations, "M1M2MostConfidentSubType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1M2MostConfidentSubType(s))
  }

  val M1M2MentionLevelFeature = property(pairedRelations, "M1M2MentionLevel") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1M2MentionLevel(s))
  }

  val M1LevelMainTypeFeature = property(pairedRelations, "M1LevelMainType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1LevelMainType(s))
  }

  val M2LevelMainTypeFeature = property(pairedRelations, "M2LevelMainType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2LevelMainType(s))
  }

  val M1LevelMainTypeAndm2LevelMainTypeFeature = property(pairedRelations, "M1LevelMainTypeAndm2LevelMainType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1LevelMainTypeAndm2LevelMainType(s))
  }

  val M1LevelSubTypeFeature = property(pairedRelations, "M1LevelSubType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1LevelSubType(s))
  }

  val M2LevelSubTypeFeature = property(pairedRelations, "M2LevelSubType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2LevelSubType(s))
  }

  val M1LevelSubTypeAndm2LevelSubTypeFeature = property(pairedRelations, "M1LevelSubTypeAndm2LevelSubType") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1LevelSubTypeAndm2LevelSubType(s))
  }

  // ------------------

  val M1MainType_m1IncludesM2Feature = property(pairedRelations, "M1MainType_m1IncludesM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1MainType_m1IncludesM2(s))
  }

  val M2MainType_m1IncludesM2Feature = property(pairedRelations, "M2MainType_m1IncludesM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2MainType_m1IncludesM2(s))
  }

  val M1m2MainType_m1IncludesM2Feature = property(pairedRelations, "M1m2MainType_m1IncludesM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1m2MainType_m1IncludesM2(s))
  }

  val M1MainType_m2IncludesM1Feature = property(pairedRelations, "M1MainType_m2IncludesM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1MainType_m2IncludesM1(s))
  }

  val M2MainType_m2IncludesM1Feature = property(pairedRelations, "M2MainType_m2IncludesM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2MainType_m2IncludesM1(s))
  }

  val M1m2MainType_m2IncludesM1Feature = property(pairedRelations, "M1m2MainType_m2IncludesM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1m2MainType_m2IncludesM1(s))
  }

  val M1SubType_m1IncludesM2Feature = property(pairedRelations, "M1SubType_m1IncludesM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1SubType_m1IncludesM2(s))
  }

  val M2SubType_m1IncludesM2Feature = property(pairedRelations, "M2SubType_m1IncludesM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2SubType_m1IncludesM2(s))
  }

  val M1m2SubType_m1IncludesM2Feature = property(pairedRelations, "M1m2SubType_m1IncludesM2") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1m2SubType_m1IncludesM2(s))
  }

  val M1SubType_m2IncludesM1Feature = property(pairedRelations, "M1SubType_m2IncludesM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1SubType_m2IncludesM1(s))
  }

  val M2SubType_m2IncludesM1Feature = property(pairedRelations, "M2SubType_m2IncludesM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2SubType_m2IncludesM1(s))
  }

  val M1m2SubType_m2IncludesM1Feature = property(pairedRelations, "M1m2SubType_m2IncludesM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1m2SubType_m2IncludesM1(s))
  }

  // ------------------

  val M1HeadWordAndDepParentWordFeature = property(pairedRelations, "M1HeadWordAndDepParentWord") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1HeadWordAndDepParentWord(s))
  }

  val M2HeadWordAndDepParentWordFeature = property(pairedRelations, "M2HeadWordAndDepParentWord") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2HeadWordAndDepParentWord(s))
  }

  // ------------------

  val M1DepLabelFeature = property(pairedRelations, "M1DepLabel") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1DepLabel(s))
  }

  val M2DepLabelFeature = property(pairedRelations, "M2DepLabel") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2DepLabel(s))
  }

  // ------------------

  val DepPathInBetweenFeature = property(pairedRelations, "DepPathInBetween") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.DepPathInBetween(s))
  }

  val DepLabelsInBetweenFeature = property(pairedRelations, "DepLabelsInBetween") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.DepLabelsInBetween(s))
  }

  val FirstDepLabelInBetweenFeature = property(pairedRelations, "FirstDepLabelInBetween") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FirstDepLabelInBetween(s))
  }

  val LastDepLabelInBetweenFeature = property(pairedRelations, "LastDepLabelInBetween") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LastDepLabelInBetween(s))
  }

  val FeaturesOfFirstPrep = property(pairedRelations, "FeaturesOfFirstPrep") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FeaturesOfFirstPrep(s))
  }

  val FeaturesOfSecondPrep = property(pairedRelations, "FeaturesOfSecondPrep") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FeaturesOfSecondPrep(s))
  }

  val FeaturesOfLastPrep = property(pairedRelations, "FeaturesOfLastPrep") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FeaturesOfLastPrep(s))
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
    s: SemanticRelation => sanitizeFeature(RelationFeatures.SinglePrepStringInBetween(s))
  }

  val FirstPrepStringInBetweenFeature = property(pairedRelations, "FirstPrepStringInBetween") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FirstPrepStringInBetween(s))
  }

  val LastPrepStringInBetweenFeature = property(pairedRelations, "LastPrepStringInBetween") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LastPrepStringInBetween(s))
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
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BowM1bc(s, 10))
  }

  val BowM2bc10Feature = property(pairedRelations, "BowM2bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BowM2bc(s, 10))
  }

  val HwM1bc10Feature = property(pairedRelations, "HwM1bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1bc(s, 10))
  }

  val HwM2bc10Feature = property(pairedRelations, "HwM2bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM2bc(s, 10))
  }

  val LhwM1bc10Feature = property(pairedRelations, "LhwM1bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM1bc(s, 10))
  }

  val HwM1Rbc10Feature = property(pairedRelations, "HwM1Rbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1Rbc(s, 10))
  }

  val LhwM2bc10Feature = property(pairedRelations, "LhwM2bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM2bc(s, 10))
  }

  val HwM2Rbc10Feature = property(pairedRelations, "HwM2Rbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM2Rbc(s, 10))
  }

  val LLhwM1bc10Feature = property(pairedRelations, "LLhwM1bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LLhwM1bc(s, 10))
  }

  val LhwM1Rbc10Feature = property(pairedRelations, "LhwM1Rbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM1Rbc(s, 10))
  }

  val HwM1RRbc10Feature = property(pairedRelations, "HwM1RRbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1RRbc(s, 10))
  }

  val LLhwM2bc10Feature = property(pairedRelations, "LLhwM2bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LLhwM2bc(s, 10))
  }

  val LhwM2Rbc10Feature = property(pairedRelations, "LhwM2Rbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LhwM2Rbc(s, 10))
  }

  val HwM2RRbc10Feature = property(pairedRelations, "HwM2RRbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM2RRbc(s, 10))
  }

  val PM1aPM2bc10Feature = property(pairedRelations, "PM1aPM2bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PM1aPM2bc(s, 10))
  }

  val M1PaM2Pbc10Feature = property(pairedRelations, "M1PaM2Pbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1PaM2Pbc(s, 10))
  }

  val PPM1aPPM2bc10Feature = property(pairedRelations, "PPM1aPPM2bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PPM1aPPM2bc(s, 10))
  }

  val PM1PaPM2Pbc10Feature = property(pairedRelations, "PM1PaPM2Pbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PM1PaPM2Pbc(s, 10))
  }

  val M1PPaM2PPbc10Feature = property(pairedRelations, "M1PPaM2PPbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1PPaM2PPbc(s, 10))
  }

  val HwM1M2bc10Feature = property(pairedRelations, "HwM1M2bc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.HwM1M2bc(s, 10))
  }

  val WordBetweenSinglebc10Feature = property(pairedRelations, "WordBetweenSinglebc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordBetweenSinglebc(s, 10))
  }

  val WordBetweenFirstbc10Feature = property(pairedRelations, "WordBetweenFirstbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordBetweenFirstbc(s, 10))
  }

  val WordBetweenLastbc10Feature = property(pairedRelations, "WordBetweenLastbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordBetweenLastbc(s, 10))
  }

  val M1HeadWordAndDepParentWordbc10Feature = property(pairedRelations, "M1HeadWordAndDepParentWordbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M1HeadWordAndDepParentWordbc(s, 10))
  }

  val M2HeadWordAndDepParentWordbc10Feature = property(pairedRelations, "M2HeadWordAndDepParentWordbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.M2HeadWordAndDepParentWordbc(s, 10))
  }

  val SinglePrepStringInBetweenbc10Feature = property(pairedRelations, "SinglePrepStringInBetweenbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.SinglePrepStringInBetweenbc(s, 10))
  }

  val FirstPrepStringInBetweenbc10Feature = property(pairedRelations, "FirstPrepStringInBetweenbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FirstPrepStringInBetweenbc(s, 10))
  }

  val LastPrepStringInBetweenbc10Feature = property(pairedRelations, "LastPrepStringInBetweenbc10") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.LastPrepStringInBetweenbc(s, 10))
  }

//  val M1WikiAttributesFeature = property(pairedRelations, "M1WikiAttributes") {
//    s: SemanticRelation =>
//      val attributeList = RelationFeatures.M1WikiAttributes(s))
//      if (attributeList == null) List.empty else attributeList.toList
//  }
//
//  val M2WikiAttributesFeature = property(pairedRelations, "M2WikiAttributes") {
//    s: SemanticRelation =>
//      val attributeList = RelationFeatures.M2WikiAttributes(s))
//      if (attributeList == null) List.empty else attributeList.toList
//  }

  val HasCoveringMentionFeature = property(pairedRelations, "HasCoveringMention") {
    s: SemanticRelation => RelationFeatures.HasCoveringMention(s)
  }

  val FrontPosSequenceFeature = property(pairedRelations, "FrontPosSequence") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.FrontPosSequence(s))
  }

  val BackPosSequenceFeature = property(pairedRelations, "BackPosSequence") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.BackPosSequence(s))
  }

  val SmallerMentionIsPerTitleFeature = property(pairedRelations, "SmallerMentionIsPerTitle") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.SmallerMentionIsPerTitle(s))
  }

  val PosAfterM1Feature = property(pairedRelations, "PosAfterM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PosAfterM1(s))
  }

  val PosBeforeM1Feature = property(pairedRelations, "PosBeforeM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PosBeforeM1(s))
  }

  val WordAfterM1Feature = property(pairedRelations, "WordAfterM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.WordAfterM1(s))
  }

  val PosOfLastWordInM1Feature = property(pairedRelations, "PosOfLastWordInM1") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PosOfLastWordInM1(s))
  }

  val OnlyPrepInDepPathFeature = property(pairedRelations, "OnlyPrepInDepPath") {
    s: SemanticRelation => RelationFeatures.OnlyPrepInDepPath(s)
  }

  val ApposInDepPathFeature = property(pairedRelations, "ApposInDepPath") {
    s: SemanticRelation => RelationFeatures.ApposInDepPath(s)
  }

  val PosOfSingleWordBetweenMentionsFeature = property(pairedRelations, "PosOfSingleWordBetweenMentions") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.PosOfSingleWordBetweenMentions(s))
  }

  val SingleWordBetweenMentionsFeature = property(pairedRelations, "SingleWordBetweenMentions") {
    s: SemanticRelation => sanitizeFeature(RelationFeatures.SingleWordBetweenMentions(s))
  }
}
