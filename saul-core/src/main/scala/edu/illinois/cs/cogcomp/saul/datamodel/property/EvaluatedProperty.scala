/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saul.datamodel.property

import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector
import edu.illinois.cs.cogcomp.saul.datamodel.property.features.discrete.BooleanProperty

import scala.reflect.ClassTag

class EvaluatedProperty[T <: AnyRef, U](val property: TypedProperty[T, U], val value: U)(implicit val tag: ClassTag[T]) extends TypedProperty[T, String] {

  val name = property.name + "_is_" + value

  val boolMapping: (T) => Boolean = {
    t: T => property.sensor(t).equals(this.value)
  }

  val typedProperties = new BooleanProperty[T](name, this.boolMapping)

  override def featureVectorImpl(instance: T): FeatureVector = typedProperties.featureVector(instance)

  override val sensor: (T) => String = { t: T => "" }
}
