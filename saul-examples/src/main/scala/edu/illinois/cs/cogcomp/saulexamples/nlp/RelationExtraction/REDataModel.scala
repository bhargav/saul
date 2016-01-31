package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.mention.MentionTypeFeatures

import scala.collection.JavaConversions._

/**
  * Created by Bhargav Mangipudi on 1/28/16.
  */
object REDataModel extends DataModel {
  val tokens = node[Constituent]

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

  val NoPrepFeature = property(tokens, "NoPrep") {
    c: Constituent => MentionTypeFeatures.NoPrep(c)
  }

  val OnePrepFeature = property(tokens, "OnePrep") {
    c: Constituent => MentionTypeFeatures.OnePrep(c)
  }

  val TwoPrepFeature = property(tokens, "TwoPrep") {
    c: Constituent => MentionTypeFeatures.TwoPrep(c)
  }

  val MoreThanTwoPrepFeature = property(tokens, "MoreThanTwoPrep") {
    c: Constituent => MentionTypeFeatures.MoreThanTwoPrep(c)
  }

  val NoVerbFeature = property(tokens, "NoVerb") {
    c: Constituent => MentionTypeFeatures.NoVerb(c)
  }

  val NoCommaFeature = property(tokens, "NoComma") {
    c: Constituent => MentionTypeFeatures.NoComma(c)
  }

  val POSIndexBagFeature = property(tokens, "POSIndexBag") {
    c: Constituent => MentionTypeFeatures.PosIndexBag(c).toList
  }

  val WordIndexBagFeature = property(tokens, "WordIndexBag") {
    c: Constituent => MentionTypeFeatures.WordIndexBag(c).toList
  }

  val POSWordIndexBagFeature = property(tokens, "POSWordIndexBag") {
    c: Constituent => MentionTypeFeatures.PosWordIndexBag(c).toList
  }

  val POSEndWordIndexBagFeature = property(tokens, "POSEndWordIndexBag") {
    c: Constituent => MentionTypeFeatures.PosEndWordIndexBag(c).toList
  }

  val WordBCIndexBagFeature = property(tokens, "WordBCIndexBag") {
    c: Constituent => MentionTypeFeatures.WordBCIndexBag(c).toList
  }

  val POSWordBCIndexBagFeature = property(tokens, "POSWordBCIndexBag") {
    c: Constituent => MentionTypeFeatures.PosWordBCIndexBag(c).toList
  }

  val POSEndWordBCIndexBagFeature = property(tokens, "POSEndWordBCIndexBag") {
    c: Constituent => MentionTypeFeatures.PosEndWordBCIndexBag(c).toList
  }

  val ParseExactFeature = property(tokens, "ParseExact") {
    c: Constituent => MentionTypeFeatures.ParseExact(c).toList
  }

  val ParseCoverFeature = property(tokens, "ParseCover") {
    c: Constituent => MentionTypeFeatures.ParseCover(c).toList
  }

  val ContextLeftWordFeature = property(tokens, "ContextLeftWord") {
    c: Constituent => MentionTypeFeatures.ContextLeftWord(c)
  }

  val ContextLeftPOSFeature = property(tokens, "ContextLeftPOS") {
    c: Constituent => MentionTypeFeatures.ContextLeftPos(c)
  }

  val ContextRightWordFeature = property(tokens, "ContextRightWord") {
    c: Constituent => MentionTypeFeatures.ContextRightWord(c)
  }

  val ContextRightPOSFeature = property(tokens, "ContextRightPOS") {
    c: Constituent => MentionTypeFeatures.ContextRightPos(c)
  }

  val NERLabelsFeature = property(tokens, "NERLabels") {
    c: Constituent => MentionTypeFeatures.NerLabels(c).toList
  }

  val WikiAttributesFeature = property(tokens, "WikiAttributes") {
    c: Constituent => MentionTypeFeatures.WikiAttributes(c).toList
  }

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
    c: Constituent => MentionTypeFeatures.InPersonList(c);
  }

  val InPersonTitleListFeature = property(tokens, "InPersonTitleList") {
    c: Constituent => MentionTypeFeatures.InPersonTitleList(c);
  }

  val InPersonNameListFeature = property(tokens, "InPersonNameList") {
    c: Constituent => MentionTypeFeatures.InPersonNameList(c);
  }

  val InPersonPronounListFeature = property(tokens, "InPersonPronounList") {
    c: Constituent => MentionTypeFeatures.InPersonPronounList(c);
  }

  val InPersonDBpediaListFeature = property(tokens, "InPersonDBpediaList") {
    c: Constituent => MentionTypeFeatures.InPersonDBpediaList(c);
  }

  val InGPEListFeature = property(tokens, "InGPEList") {
    c: Constituent => MentionTypeFeatures.InGPEList(c);
  }

  val InGPECityListFeature = property(tokens, "InGPECityList") {
    c: Constituent => MentionTypeFeatures.InGPECityList(c);
  }

  val InGPECountryListFeature = property(tokens, "InGPECountryList") {
    c: Constituent => MentionTypeFeatures.InGPECountryList(c);
  }

  val InGPECountyListFeature = property(tokens, "InGPECountyList") {
    c: Constituent => MentionTypeFeatures.InGPECountyList(c);
  }

  val InGPEStateListFeature = property(tokens, "InGPEStateList") {
    c: Constituent => MentionTypeFeatures.InGPEStateList(c);
  }

  val InGPECommonNounListFeature = property(tokens, "InGPECommonNounList") {
    c: Constituent => MentionTypeFeatures.InGPECommonNounList(c);
  }

  val InGPEMajorAreaListFeature = property(tokens, "InGPEMajorAreaList") {
    c: Constituent => MentionTypeFeatures.InGPEMajorAreaList(c);
  }

  val InEthnicGroupListFeature = property(tokens, "InEthnicGroupList") {
    c: Constituent => MentionTypeFeatures.InEthnicGroupList(c);
  }

  val InNationalityListFeature = property(tokens, "InNationalityList") {
    c: Constituent => MentionTypeFeatures.InNationalityList(c);
  }

  val InEthnicGroupOrNationalityListFeature = property(tokens, "InEthnicGroupOrNationalityList") {
    c: Constituent => MentionTypeFeatures.InEthnicGroupOrNationalityList(c);
  }

  val InOrgGovtListFeature = property(tokens, "InOrgGovtList") {
    c: Constituent => MentionTypeFeatures.InOrgGovtList(c);
  }

  val InOrgCommercialListFeature = property(tokens, "InOrgCommercialList") {
    c: Constituent => MentionTypeFeatures.InOrgCommercialList(c);
  }

  val InOrgEducationalListFeature = property(tokens, "InOrgEducationalList") {
    c: Constituent => MentionTypeFeatures.InOrgEducationalList(c);
  }

  val InFacBarrierListFeature = property(tokens, "InFacBarrierList") {
    c: Constituent => MentionTypeFeatures.InFacBarrierList(c);
  }

  val InFacBuildingListFeature = property(tokens, "InFacBuildingList") {
    c: Constituent => MentionTypeFeatures.InFacBuildingList(c);
  }

  val InFacConduitListFeature = property(tokens, "InFacConduitList") {
    c: Constituent => MentionTypeFeatures.InFacConduitList(c);
  }

  val InFacPathListFeature = property(tokens, "InFacPathList") {
    c: Constituent => MentionTypeFeatures.InFacPathList(c);
  }

  val InFacPlantListFeature = property(tokens, "InFacPlantList") {
    c: Constituent => MentionTypeFeatures.InFacPlantList(c);
  }

  val InFacBuildingSubAreaListFeature = property(tokens, "InFacBuildingSubAreaList") {
    c: Constituent => MentionTypeFeatures.InFacBuildingSubAreaList(c);
  }

  val InFacGenericListFeature = property(tokens, "InFacGenericList") {
    c: Constituent => MentionTypeFeatures.InFacGenericList(c);
  }

  val InWeaListFeature = property(tokens, "InWeaList") {
    c: Constituent => MentionTypeFeatures.InWeaList(c);
  }

  val InVehListFeature = property(tokens, "InVehList") {
    c: Constituent => MentionTypeFeatures.InVehList(c);
  }

  val InOrgPoliticalListFeature = property(tokens, "InOrgPoliticalList") {
    c: Constituent => MentionTypeFeatures.InOrgPoliticalList(c);
  }

  val InOrgTerroristListFeature = property(tokens, "InOrgTerroristList") {
    c: Constituent => MentionTypeFeatures.InOrgTerroristList(c);
  }
}
