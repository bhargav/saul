/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation
import edu.illinois.cs.cogcomp.core.io.caches.TextAnnotationMapDBHandler
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.hashing.MurmurHash3

object SRLUtilities {
  private lazy val mapDBCacheHandler = new TextAnnotationMapDBHandler("dataset-cache")

   def getDatasetName(startSection: String, endSection: String, resourceManager: ResourceManager): String = {
     val requiredThings = mutable.ArrayBuffer[String]()
     requiredThings.append(SRLscalaConfigurator.TREEBANK_HOME)
     requiredThings.append(SRLscalaConfigurator.PROPBANK_HOME)
     requiredThings.append(startSection, endSection)
     requiredThings.append(resourceManager.getBoolean(SRLConfigurator.USE_CURATOR).toString)
     requiredThings.append(resourceManager.getString(SRLConfigurator.SRL_PARSE_VIEW))

     val hash: Int = MurmurHash3.orderedHash(requiredThings)
     s"${hash.toHexString}"
  }

  def getCachedTextAnnotation(datasetName: String): Seq[TextAnnotation] = {
    mapDBCacheHandler.getDataset(datasetName).toSeq
  }

  def putTextAnnotationInCache(datasetName: String, dataset: Seq[TextAnnotation]): Unit = {
    dataset.foreach(data => mapDBCacheHandler.addTextAnnotation(datasetName, data))
  }
}
