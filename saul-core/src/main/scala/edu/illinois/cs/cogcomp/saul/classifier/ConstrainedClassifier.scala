package edu.illinois.cs.cogcomp.saul.classifier

import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete
import edu.illinois.cs.cogcomp.lbjava.infer._
import edu.illinois.cs.cogcomp.lbjava.learn.Learner
import edu.illinois.cs.cogcomp.lbjava.parse.Parser
import edu.illinois.cs.cogcomp.saul.classifier.SL_model.LossAugmentedNormalizer
import edu.illinois.cs.cogcomp.saul.TestWithStorage
import edu.illinois.cs.cogcomp.saul.classifier.infer.InferenceCondition
import edu.illinois.cs.cogcomp.saul.constraint.LfsConstraint
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.saul.datamodel.edge.Edge
import edu.illinois.cs.cogcomp.saul.lbjrelated.LBJClassifierEquivalent
import edu.illinois.cs.cogcomp.saul.parser.LBJIteratorParserScala

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/** The input to a ConstrainedClassifier is of type `T`. However given an input, the inference is based upon the
  * head object of type `HEAD` corresponding to the input (of type `T`).
  * @tparam T the object type given to the classifier as input
  * @tparam HEAD the object type inference is based upon
  */
abstract class ConstrainedClassifier[T <: AnyRef, HEAD <: AnyRef](val dm: DataModel, val onClassifier: Learner)(
  implicit
  val tType: ClassTag[T],
  implicit val headType: ClassTag[HEAD]
) extends LBJClassifierEquivalent {

  type LEFT = T
  type RIGHT = HEAD

  def className: String = this.getClass.getName

  def getClassSimpleNameForClassifier = this.getClass.getSimpleName

  def __allowableValues: List[String] = "*" :: "*" :: Nil

  def subjectTo: LfsConstraint[HEAD]

  def solver: ILPSolver = new GurobiHook()

  /** The function is used to filter the generated candidates from the head object; remember that the inference starts
    * from the head object. This function finds the objects of type `T` which are connected to the target object of
    * type `HEAD`. If we don't define `filter`, by default it returns all objects connected to `HEAD`.
    * The filter is useful for the `JointTraining` when we go over all global objects and generate all contained object
    * that serve as examples for the basic classifiers involved in the `JoinTraining`. It is possible that we do not
    * want to use all possible candidates but some of them, for example when we have a way to filter the negative
    * candidates, this can come in the filter.
    */
  def filter(t: T, head: HEAD): Boolean = true

  val logger = false

  /** The `pathToHead` returns only one object of type HEAD, if there are many of them i.e. `Iterable[HEAD]` then it
    * simply returns the `head` of the `Iterable`
    */
  val pathToHead: Option[Edge[T, HEAD]] = None

  /** syntactic suger to create simple calls to the function */
  def apply(example: AnyRef): String = classifier.discreteValue(example: AnyRef)

  override val classifier = lbjClassifier.classifier

  def findHead(x: T): Option[HEAD] = {
    if (tType.equals(headType)) {
      Some(x.asInstanceOf[HEAD])
    } else {
      val lst = pathToHead match {
        case Some(e) =>
          //          println(s"Searching via ${s}")
          e.forward.neighborsOf(x)
        case _ => dm.getFromRelation[T, HEAD](x)
      }

      val l = lst.toSet.toList

      if (l.isEmpty) {
        if (logger)
          println("Warning: Failed to find head")
        None
      } else if (l.size != 1) {
        if (logger)
          println("Find too many heads")
        Some(l.head)
      } else {
        if (logger)
          println(s"Found head ${l.head} for child $x")
        Some(l.head)
      }
    }
  }

  def getCandidates(head: HEAD): Seq[T] = {

    if (tType.equals(headType)) {
      head.asInstanceOf[T] :: Nil
    } else {
      val l = pathToHead match {
        case Some(e) => e.backward.neighborsOf(head)
        case _ => dm.getFromRelation[HEAD, T](head)
      }

      if (l.isEmpty) {
        if (logger)
          println("Failed to find part")
        l.toSeq
      } else {
        l.filter(filter(_, head)).toSeq
      }
    }
  }

  def buildWithConstraint(infer: InferenceCondition[T, HEAD], cls: Learner, lexFlag: Boolean = true)(t: T): String = {

    val lex = cls.getLabelLexicon
    var flag = false
    if (lexFlag)
      for (i <- 0 until lex.size()) {
        if (lex.lookupKey(i).valueEquals(cls.getLabeler().discreteValue(t)))
          flag = true
      }
    else
      flag = true

    findHead(t) match {
      case Some(head) =>
        val name = String.valueOf(infer.subjectTo.hashCode())
        var inference = InferenceManager.get(name, head)

        if (inference == null) {
          inference = infer(head)
          if (logger)
            println("Inference is NULL " + name)
          InferenceManager.put(name, inference)
        }
        if (!flag) {
          print("The models have not been trained for this label!")
          ""
        } else
          inference.valueOf(cls, t)

      case None =>
        val name = String.valueOf(infer.subjectTo.hashCode())

        //        var inference = InferenceManager.get(name, head)
        //
        //        if (inference == null) {
        //
        //          inference = infer(head)
        //          //      println(inference)
        //          //      println("Inference NULL" + name)
        //
        //          InferenceManager.put(name, inference)
        //        }
        //
        //
        //        val result: String = inference.valueOf(cls, t)
        //        result

        //        "false"
        cls.discreteValue(t)
    }
  }

  def lossAugmentedInfer(h: HEAD, offset: Int): ListBuffer[String] = {
    var v = ListBuffer[String]()
    getCandidates(h).foreach {
      (example) =>
        // val g1 = onClassifier.scores(example)
        v += buildWithConstraint(subjectTo.createInferenceCondition[T](this.dm, getSolverInstance(), new LossAugmentedNormalizer(offset, onClassifier, example)).convertToType[T], onClassifier)(example)
    }
    v
  }

  def buildWithConstraint(inferenceCondition: InferenceCondition[T, HEAD])(t: T): String = {
    buildWithConstraint(inferenceCondition, onClassifier)(t)
  }

  private def getSolverInstance = solver match {
    case _: OJalgoHook => () => new OJalgoHook()
    case _: GurobiHook => () => new GurobiHook()
  }

  def lbjClassifier = dm.property[T](dm.getNodeWithType[T], className)("*", "*") {
    x: T => buildWithConstraint(subjectTo.createInferenceCondition[T](this.dm, getSolverInstance()).convertToType[T], onClassifier)(x)
  }

  def learn(it: Int): Unit = {
    val ds = dm.getNodeWithType[T].getTrainingInstances
    this.learn(it, ds)
  }

  def learn(iteration: Int, data: Iterable[T]): Unit = {
    //    featureExtractor.setDMforAll(this.datamodel)

    val crTokenTest = new LBJIteratorParserScala[T](data)
    crTokenTest.reset()

    def learnAll(crTokenTest: Parser, remainingIteration: Int): Unit = {
      //      println(remainingIteration)
      val v = crTokenTest.next
      if (v == null) {

        if (remainingIteration > 0) {
          crTokenTest.reset()
          learnAll(crTokenTest, remainingIteration - 1)
        }
      } else {
        //        println("Learning with example " + v)
        this.onClassifier.learn(v)
        learnAll(crTokenTest, remainingIteration)
      }
    }

    learnAll(crTokenTest, iteration)
  }

  /** Test with given data, use internally
    * @return List of (label, (f1, precision, recall))
    */
  def test(): List[(String, (Double, Double, Double))] = {
    val allHeads = this.dm.getNodeWithType[HEAD].getTestingInstances
    val data: List[T] = if (tType.equals(headType)) {
      allHeads.map(_.asInstanceOf[T]).toList
    } else {
      this.pathToHead match {
        case Some(path) => allHeads.map(h => path.backward.neighborsOf(h)).toList.flatten
        case _ => (allHeads map (h => this.dm.getFromRelation[HEAD, T](h))).toList.flatten
      }
    }

    test(data)
  }

  /** Test with given data, use internally
    * @param testData if the collection of data (which is and Iterable of type T) is not given it is derived from the data model based on its type
    * @param exclude it is the label that we want to exclude for evaluation, this is useful for evaluating the multi-class classifiers when we need to measure overall F1 instead of accuracy and we need to exclude the negative class
    * @param outFile The file to write the predictions (can be `null`)
    * @return List of (label, (f1,precision,recall))
    */

  def test(testData: Iterable[T], outFile: String = null, outputGranularity: Int = 0, exclude: String = ""): List[(String, (Double, Double, Double))] = {
    println()
    val testReader = new LBJIteratorParserScala[T](testData)
    testReader.reset()
    val tester: TestDiscrete = new TestDiscrete()
    TestWithStorage.test(tester, classifier, onClassifier.getLabeler, testReader, outFile, outputGranularity, exclude)
    val ret = tester.getLabels.map({
      label => (label, (tester.getF1(label), tester.getPrecision(label), tester.getRecall(label)))
    })
    ret toList
  }
}

object ConstrainedClassifier {
  val ConstraintManager = scala.collection.mutable.HashMap[Int, LfsConstraint[_]]()
  def constraint[HEAD <: AnyRef](f: HEAD => FirstOrderConstraint)(implicit headTag: ClassTag[HEAD]): LfsConstraint[HEAD] = {
    val hash = f.hashCode()
    ConstraintManager.getOrElseUpdate(hash, new LfsConstraint[HEAD] {
      override def makeConstrainDef(x: HEAD): FirstOrderConstraint = f(x)
    }).asInstanceOf[LfsConstraint[HEAD]]
  }
}