package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import java.io._

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation
import edu.illinois.cs.cogcomp.core.io.IOUtils
import edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction.data.DataLoader

import scala.collection.JavaConversions._
import scala.io.Source

/**
  * Created by Bhargav Mangipudi on 1/28/16.
  */
object relationExtractionApp {

  def main(args: Array[String]): Unit = {
    val docs = loadDataFromCache.toList
    val trainData = docs.flatMap(ta => ta.getView(Constants.GOLD_MENTION_VIEW).getConstituents)

    println(s"Total Tokens = ${trainData.length}")

    REDataModel.tokens populate trainData

    REClassifiers.mentionTypeClassifier.crossValidation(5);
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
