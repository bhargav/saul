package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.io._

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{TextAnnotation, SpanLabelView}
import edu.illinois.cs.cogcomp.illinoisRE.common.{Document, ResourceManager, Constants}
import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader
import edu.illinois.cs.cogcomp.illinoisRE.mention.{MentionTyper, MentionDetector}

import scala.io.Source

/**
  * Created by Bhargav Mangipudi on 1/28/16.
  */
object relationExtractionApp {

  def main(args: Array[String]): Unit = {
    val docs = loadDataFromCache.toList

    val numSentences = docs.map(_.getNumberOfSentences).sum
    println(s"Total number of sentences = $numSentences")

    docs.foreach(originalTA => {
      val tempDoc: Document = new Document(originalTA)
      MentionDetector.labelDocMentionCandidates(tempDoc)
      assert(originalTA.equals(tempDoc.getTextAnnotation))

      createTypedCandidateMentions(originalTA, originalTA.getView(Constants.GOLD_MENTION_VIEW).asInstanceOf[SpanLabelView])
    })

    val numFolds = 5
    for (fold <- 0 until numFolds) {
      println(s"Running fold $fold")

      val zipDocs = docs.zipWithIndex
      val trainDocs = zipDocs.filter({case (doc, index) => index % numFolds == fold}).map(_._1)
      val testDocs = zipDocs.filterNot({case (doc, index) => index % numFolds == fold}).map(_._1)

      println(trainDocs.length)
      println(testDocs.length)

      REDataModel.clearInstances
      REDataModel.documents.populate(trainDocs)
      REDataModel.documents.populate(testDocs, false)

      REClassifiers.mentionTypeClassifier.forget
      REClassifiers.mentionTypeClassifier.learn(1)
      REClassifiers.mentionTypeClassifier.test
    }
  }

  def createTypedCandidateMentions(ta: TextAnnotation, gold_typed_view: SpanLabelView) {
    val mentionView: SpanLabelView = ta.getView(Constants.CANDIDATE_MENTION_VIEW).asInstanceOf[SpanLabelView]
    val typedView: SpanLabelView = new SpanLabelView(Constants.TYPED_CANDIDATE_MENTION_VIEW, "alignFromGold", ta, 1.0, true)
    import scala.collection.JavaConversions._

    for (c <- mentionView.getConstituents) {
      var label: String = MentionTyper.NONE_MENTION

      for (tc <- gold_typed_view.getConstituents) {
        if (c.getStartSpan == tc.getStartSpan && c.getEndSpan == tc.getEndSpan) {
          label = tc.getLabel
        }
      }

      typedView.addSpanLabel(c.getStartSpan, c.getEndSpan, label, 1.0)
    }
    ta.addView(Constants.TYPED_CANDIDATE_MENTION_VIEW, typedView)
  }

  def loadDataFromCache : Iterator[TextAnnotation] = {
    val masterFileList = ResourceManager.getProjectRoot + "/data/ace2004/allfiles"
    val cacheBasePath = ResourceManager.getProjectRoot + "/data_cache/"

    Source.fromFile(masterFileList).getLines().map(fileName => {
      val outputFile = new File(cacheBasePath + fileName + ".ta")
      if (outputFile.exists()) {
        val e = new ObjectInputStream(new FileInputStream(outputFile.getPath))
        val ta = e.readObject.asInstanceOf[TextAnnotation]
        e.close

        ta
      } else {
        val ta = DataLoader.getACEDocument(fileName).getTextAnnotation

        try {
          new File(cacheBasePath).mkdirs
          val f = new FileOutputStream(outputFile)
          val e = new ObjectOutputStream(f)
          e.writeObject(ta)
          e.flush
        } catch {
          case ex: Exception => ex.printStackTrace; outputFile.getAbsolutePath
        }

        ta
      }
    })
  }
}
