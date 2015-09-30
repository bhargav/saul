package edu.illinois.cs.cogcomp.lfs.data_model.attribute.features.real

import edu.illinois.cs.cogcomp.lbjava.classify.{Classifier, FeatureVector, RealPrimitiveStringFeature}
import edu.illinois.cs.cogcomp.lfs.data_model.attribute.TypedAttribute
import edu.illinois.cs.cogcomp.lfs.data_model.attribute.features.ClassifierContainsInLBP

import scala.reflect.ClassTag

/**
  * Created by haowu on 2/5/15.
  */
case class RealGenAttribute[T <: AnyRef](
                                     val name : String,
                                     val mapping: T => List[Double]
                                     )(implicit val tag : ClassTag[T]) extends TypedAttribute[T,List[Double]]{

  val ra = this.name

  override def makeClassifierWithName(n: String): Classifier  =  {
    new ClassifierContainsInLBP(){
   this.containingPackage = "LBP_Package"

   this.name = n
   // this.name = name // Parisa: I am not sure why this was the ra.name that made nullpoiterExceptions.
   def classify(__example: AnyRef): FeatureVector = {
      val d: T = __example.asInstanceOf[T]
      val values = mapping(d)

      var __result: FeatureVector = null
      __result = new FeatureVector


      values.zipWithIndex.foreach{
        case(value,__id)=> __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id+"",value))
      }

     __result
    }
  }
  }

  override def addToFeatureVector(t: T, fv: FeatureVector): FeatureVector = {
    fv.addFeatures(this.classifier.classify(t))
    fv
  }


  def addToFeatureVector(t: T, fv: FeatureVector, nameOfClassifier : String): FeatureVector = {
    fv.addFeatures(makeClassifierWithName(nameOfClassifier).classify(t))
    fv
  }


}