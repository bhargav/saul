package edu.illinois.cs.cogcomp.saul.classifier

import edu.illinois.cs.cogcomp.lbjava.learn.{ LinearThresholdUnit, SparseNetworkLearner }

/** Created by Parisa on 5/24/15.
  */
class SparseNetworkLBP extends SparseNetworkLearner {

  var net = network
  var iConjuctiveLables = conjunctiveLabels
  override def getLTU(i: Int): LinearThresholdUnit = {
    var a: LinearThresholdUnit = net.get(i).asInstanceOf[LinearThresholdUnit]
    a
  }
  def getnumExamples: Int = {
    numExamples
  }
  def getnumFeatures: Int = {
    numFeatures
  }
  def getbaseLTU: LinearThresholdUnit = {
    baseLTU
  }
}
