package edu.illinois.cs.cogcomp.saulexamples.nlp.EmailSpam

import edu.illinois.cs.cogcomp.saul.datamodel.property.Property
import edu.illinois.cs.cogcomp.saulexamples.data.Document
import edu.illinois.cs.cogcomp.saulexamples.nlp.EmailSpam.spamDataModel._
import edu.illinois.cs.cogcomp.saul.classifier.Learnable

object spamClassifers {
  object spamClassifier extends Learnable[Document](spamDataModel) {

    def label: Property[Document] = spamLable is "spam"
    override def algorithm = "SparseNetwork"
    //override def feature = using(
    //word,phrase,containsSubPhraseMent,containsSubPhraseIng,
    // containsInPersonList,wordLen,containsInCityList
    // )
  }
}