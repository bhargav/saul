/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saul.datamodel.property

import java.util

import edu.illinois.cs.cogcomp.lbjava.classify.{Classifier, FeatureVector}
import edu.illinois.cs.cogcomp.saul.datamodel.node.Node

import scala.collection.mutable
import scala.reflect.ClassTag

/** Base trait for representing attributes that can be defined on a
  * [[Node]] instance.
  *
  * @tparam T Type of instances stored in the corresponding Node.
  */
trait Property[T] {

  /** This field is used internally by LBJava. */
  private[property] val containingPackage = "LBP_Package"

  /** Name of the property */
  val name: String

  /** ClassTag of the corresponding Node's instance type. */
  private[saul] val tag: ClassTag[T]

  /** Type of the property's value */
  type S

  /** Generating sensor for extracting the feature from an Node instance */
  val sensor: T => S

  /** Method to extract the feature value for an input Node instance
    *
    * @param instance Instance to extract the feature from.
    * @return Extracted feature.
    */
  def apply(instance: T): S = sensor(instance)

  /** Boolean denoting if a property's value (feature) should be cached in-memory to prevent redundant evaluations */
  private[saul] val isCacheable: Boolean = false

  /** WeakHashMap instance to cache feature vectors */
  private[Property] final lazy val featureVectorCache = new mutable.WeakHashMap[T, FeatureVector]()

  /** Method to extract [[FeatureVector]] instance that is used the classifiers implemented using LBJava.
    *
    * Note: This method wraps the actual extractor method [[featureVectorImpl()]] to support feature caching.
    *
    * @param instance Instance to extract the feature vector from.
    * @return Extracted Feature Vector.
    */
  final def featureVector(instance: T): FeatureVector = {
    if (isCacheable) {
      featureVectorCache.getOrElseUpdate(instance, featureVectorImpl(instance))
    } else {
      featureVectorImpl(instance)
    }
  }

  /** Method to extract [[FeatureVector]] instance that is used by the classifiers implemented using LBJava.
    *
    * Note: This is an abstract method that should be implemented by specialized Property subclasses.
    *
    * @param instance Instance to extract the feature vector from.
    * @return  Extracted Feature Vector.
    */
  protected def featureVectorImpl(instance: T): FeatureVector

  /** Type of the property's feature. This is used by LBJava's classifiers internally. */
  def outputType: String = "discrete"

  /** List of allowable values for this Property. This is used by LBJava's classifiers internally. */
  private[saul] def allowableValues: Array[String] = Array.empty[String]

  /**
    * This is used by LBJava internally. This is used to wrap multiple [[Classifier]] instances.
    *
    * @return List of child classifiers.
    */
  private[saul] def compositeChildren: Option[util.LinkedList[Classifier]] = None

  /** Method to clear cached property (if caching is enabled) */
  private[saul] def clearCache(): Unit = {
    if (isCacheable) {
      featureVectorCache.clear()
    }
  }
}

object Property {

  /** Transfer a properties to a lbj classifier. */
  def convertToClassifier[T](property: Property[T]): Classifier = new LBPClassifier[T](property)
}
