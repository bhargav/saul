package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent
import edu.illinois.cs.cogcomp.lbjava.learn.SupportVectorMachine
import edu.illinois.cs.cogcomp.saul.classifier.Learnable
import edu.illinois.cs.cogcomp.saul.constraint.ConstraintTypeConversion._

import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.REDataModel._

/**
  * Created by Bhargav Mangipudi on 1/28/16.
  */
object REClassifiers {
  object mentionTypeClassifier extends Learnable[Constituent](REDataModel) {
    def label = mentionFineLabel
    override def feature = using(
      NoPrepFeature, OnePrepFeature, TwoPrepFeature, MoreThanTwoPrepFeature, NoVerbFeature, NoCommaFeature,
      InOrgPoliticalListFeature, InOrgTerroristListFeature,
      InVehListFeature,
      InWeaListFeature,
      InFacBarrierListFeature, InFacBuildingListFeature, InFacConduitListFeature, InFacPathListFeature, InFacPlantListFeature,
      InFacBuildingSubAreaListFeature, InFacGenericListFeature,
      //      SurroundingWordsFeature,
      WordSequenceFeature, POSSequenceFeature, WordAndPOSSequenceFeature,
      InPersonListFeature,
      //      InGPEListFeature,
      InEthnicGroupOrNationalityListFeature,
      InGPECityListFeature, InGPECountryListFeature, InGPECountyListFeature, InGPEStateListFeature, InGPECommonNounListFeature,
      InGPEMajorAreaListFeature,
      InOrgGovtListFeature, InOrgCommercialListFeature, InOrgEducationalListFeature,
      //      InPersonTitleListFeature, InPersonNameListFeature, InPersonPronounListFeature, InPersonDBpediaListFeature,
      //      SynOfAllNounFeature,
      //      mentionTypeFeatures,
      POSIndexBagFeature, WordIndexBagFeature, POSWordIndexBagFeature, POSEndWordIndexBagFeature,
      WordBCIndexBagFeature, POSWordBCIndexBagFeature, POSEndWordBCIndexBagFeature,
      ParseExactFeature, ParseCoverFeature,
      ContextLeftWordFeature, ContextLeftPOSFeature, ContextRightWordFeature, ContextRightPOSFeature,
      //      WikiAttributesFeature,
      NERLabelsFeature)
    override lazy val classifier = new SupportVectorMachine(1, 0.1, -1, "L2LOSS_SVM_DUAL")
  }
}
