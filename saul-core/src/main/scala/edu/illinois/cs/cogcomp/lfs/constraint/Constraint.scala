package edu.illinois.cs.cogcomp.lfs.constraint

import edu.illinois.cs.cogcomp.lbjava.infer._
import edu.illinois.cs.cogcomp.lbjava.learn.Learner
import edu.illinois.cs.cogcomp.lfs.data_model.attribute.Attribute
import edu.illinois.cs.cogcomp.lfs.lbj_related.LBJLearnerEquivalent

//import edu.illinois.cs.cogcomp.lbjava.infer.{FirstOrderConstant, PropositionalConstraint, Inference, FirstOrderConstraint}

/**
 * Created by kordjam on 11/11/14.
 * We need to define the langauge of constraints here  to work with the first order constraints that are programmed in
 * our main LBP script. The wrapper just gives us a java firstorderconstraint object in the shell of an scala object.
 * in this way our language works on scala objects.
 */

object ConstraintTypeConversion{

  implicit def singleAttributeToList[T <: AnyRef](att : Attribute[T]) : List[Attribute[T]] = {
    att :: Nil
  }

  implicit def learnerToLFS(l : Learner) : LBJLearnerEquivalent = {
    new LBJLearnerEquivalent {
      override val classifier = l
    }
  }

  implicit def LfsToLearner(l : LBJLearnerEquivalent) : Learner = {
      l.classifier
  }

  implicit def constraintWrapper( p : FirstOrderConstraint ) : My1stOrderCons = {
    new My1stOrderCons(p)
  }

  implicit def javaCollToMyQuantifierWrapper[T]( coll : java.util.Collection[T] ) : MyQuantifierWrapper[T] =  {
    import scala.collection.JavaConversions._
    new MyQuantifierWrapper[T](coll.toSeq)
  }

  implicit def scalaCollToMyQuantifierWrapper[T]( coll : Seq[T] ): MyQuantifierWrapper[T] ={
    new MyQuantifierWrapper[T](coll)
  }

}

class MyQuantifierWrapper[T]( val coll : Seq[T] ){

  def _exists ( p : T => FirstOrderConstraint ) : FirstOrderConstraint = {
    val __result: FirstOrderConstraint = new FirstOrderConstant(false)
    def makeDisjunction( c1 : FirstOrderConstraint,c2 : FirstOrderConstraint ) : FirstOrderConstraint = {
        new FirstOrderDisjunction(c1,c2)
    }
    coll.map(p).foldLeft[FirstOrderConstraint](__result)(makeDisjunction)
  }


  def _forAll ( p : T => FirstOrderConstraint ) : FirstOrderConstraint = {
    val __result: FirstOrderConstraint = new FirstOrderConstant(true)
    def makeConjunction( c1 : FirstOrderConstraint,c2 : FirstOrderConstraint ) : FirstOrderConstraint = {
      new FirstOrderConjunction(c1,c2)
    }
    coll.map(p).foldLeft[FirstOrderConstraint](__result)(makeConjunction)

  }

  /**
   * transfer the constraint to a constant,
   * I'm worried about ths performance, because otherwise(at most 10 will be O(n^10) thing to evaluate)
   * One reason is we use conjunction and disjunction in forall and exist
   * @param n
   * @param p
   * @return
   */
  def _atMost(n : Int) (p : T => FirstOrderConstraint) : FirstOrderConstraint = {
    if(coll.count(p.andThen(_.evaluate())) <= n){
      new FirstOrderConstant(true)
    }else{
      new FirstOrderConstant(false)
    }
  }

  /**
   * transfer the constraint to a constant,
   * I'm worried about ths performance, because otherwise(at most 10 will be O(n^10) thing to evaluate)
   * One reason is we use conjunction and disjunction in forall and exist
   * @param n
   * @param p
   * @return
   */
  def _atLeast(n : Int) (p : T => FirstOrderConstraint) : FirstOrderConstraint = {
    if(coll.count(p.andThen(_.evaluate())) >= n){
      new FirstOrderConstant(true)
    }else{
      new FirstOrderConstant(false)
    }
  }

}


class My1stOrderCons(val r : FirstOrderConstraint){

  def ==>(other : FirstOrderConstraint) = implies(other)

  def implies(other : FirstOrderConstraint) = new FirstOrderImplication(this.r,other)

  def <==>(other : FirstOrderConstraint) = new FirstOrderDoubleImplication(this.r,other)

  def unary_! = new FirstOrderNegation(this.r)

  def &&&(other : FirstOrderConstraint) = and(other)

  def and(other : FirstOrderConstraint) = new FirstOrderConjunction(this.r,other)

  def |||(other : FirstOrderConstraint) = or(other)

  def or(other : FirstOrderConstraint) = new FirstOrderDisjunction(this.r,other)


}

class LHSFirstOrderEqualityWithValueLBP(cls : Learner, t : AnyRef) {

// probably we need to write here
// LHSFirstOrderEqualityWithValueLBP(cls : Learner, t : AnyRef) extends ConstraintTrait

  val lbjRepr = new FirstOrderVariable(cls, t)

  def is(v : String) :  FirstOrderConstraint = {
    new FirstOrderEqualityWithValue(true, lbjRepr, v)
  }
  def is(v: LHSFirstOrderEqualityWithValueLBP):FirstOrderConstraint={ //not sure if this works correctly
    new FirstOrderEqualityWithVariable(true,lbjRepr,v.lbjRepr)
  }

  def isTrue :  FirstOrderConstraint = is("true")

  def isNotTrue :  FirstOrderConstraint = is("false")


}