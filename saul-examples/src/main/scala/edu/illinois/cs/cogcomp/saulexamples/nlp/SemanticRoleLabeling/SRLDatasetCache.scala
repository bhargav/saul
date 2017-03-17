/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling

import java.util.concurrent.ConcurrentMap

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation
import edu.illinois.cs.cogcomp.core.io.IOUtils
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager
import edu.illinois.cs.cogcomp.core.utilities.protobuf.ProtobufSerializer
import org.mapdb.{ DBMaker, Serializer }

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.hashing.MurmurHash3


case class SRLDatasetCache(startSection: String, endSection: String, resourceManager: ResourceManager) {
  import SRLDatasetCache.mapDBDatabase

  private def getMap(datasetName: String): ConcurrentMap[Integer, Array[Byte]]  = {
    mapDBDatabase.hashMap(datasetName, Serializer.INTEGER, Serializer.BYTE_ARRAY).createOrOpen()
  }

  private lazy val datasetName: String = {
    val requiredThings = mutable.ArrayBuffer[String]()
    requiredThings.append(SRLscalaConfigurator.TREEBANK_HOME)
    requiredThings.append(SRLscalaConfigurator.PROPBANK_HOME)
    requiredThings.append(startSection, endSection)
    requiredThings.append(resourceManager.getBoolean(SRLConfigurator.USE_CURATOR).toString)
    requiredThings.append(resourceManager.getString(SRLConfigurator.SRL_PARSE_VIEW))

    val hash: Int = MurmurHash3.orderedHash(requiredThings)
    s"${hash.toHexString}"
  }

  def cacheExists(): Boolean = {
    IOUtils.exists(filePath) && getMap(datasetName).size() > 0
  }

  def getDataset(): Seq[TextAnnotation] = {
    val map = getMap(datasetName)
    map.values().map(ProtobufSerializer.parseFrom).toSeq
  }

  def putDataset(dataset: Seq[TextAnnotation]): Unit = {
    val map = getMap(datasetName)
    dataset.foreach({ ta: TextAnnotation =>
      val key: Int = MurmurHash3.stringHash(ta.getTokenizedText)
      map.put(key, ProtobufSerializer.writeAsBytes(ta))
    })
    mapDBDatabase.commit()
  }

  def clearDataset(): Unit = {
    val map = getMap(datasetName)
    map.clear()
    mapDBDatabase.commit()
  }
}

object SRLDatasetCache {
  private val cacheBasePath = ""

  private val mapDBDatabase = DBMaker.fileDB(cacheBasePath + "dataset-cache")
    .transactionEnable()
    .fileMmapEnableIfSupported()
    .fileMmapPreclearDisable()
    .cleanerHackEnable()
    .closeOnJvmShutdown()
    .make()
}
