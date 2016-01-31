package edu.illinois.cs.cogcomp.saulexamples.nlp.RelationExtraction

import scala.collection.mutable
import scala.io.Source
import scala.util.matching.Regex

/**
  * Created by Bhargav Mangipudi on 1/28/16.
  */
class wordClusterManager {
  val brownClusterInputStream = getClass.getClassLoader.getResourceAsStream("edu/illinois/cs/cogcomp/illinoisRE/common/brown_clusters")

  println("Reading Brown Clusters data...")
  val pattern = new Regex("\\s+")
  val pairs = Source.fromInputStream(brownClusterInputStream).getLines.filter(s => s != null && !s.isEmpty).map(pattern.split(_))

  val map = pairs.map(item => (item(1), item(0))).toMap
  val bit10 = new mutable.HashMap[String, String]()

  println("Processing Brown Clusters data...")
  pairs.foreach(item => {
    if (item(0).length >= 10) bit10.put(item(1), item(0).substring(0, 10))
  })

  pairs.foreach(item => {
    if (!bit10.contains(item(1).toLowerCase) && item(0).length >= 10)
      bit10.put(item(1).toLowerCase, item(0).substring(0, 10))
  })

  println("Done...")

  def getCluster(word: String) : String = {
    map.getOrElse(word, "**********-NONE-CLUSTER--")
  }

  def getCluster(word: String, bitNum: Int) : String = {
    if (bitNum != 10) return null
    bit10.getOrElse(word.toLowerCase, "**********-NONE-CLUSTER--")
  }
}
