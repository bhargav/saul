/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saul.classifier

import edu.illinois.cs.cogcomp.lbjava.learn.{ LinearThresholdUnit, SparseNetworkLearner }

/** Created by Parisa on 5/24/15.
  */
class SparseNetworkLBP() extends SparseNetworkLearner {
  def net = getNetwork
  var iConjuctiveLables = conjunctiveLabels

  override def getLTU(i: Int): LinearThresholdUnit = {
    getNetwork.get(i).asInstanceOf[LinearThresholdUnit]
  }
  def getnumExamples: Int = {
    getNumExamples
  }
  def getnumFeatures: Int = {
    getNumFeatures
  }
  def getbaseLTU: LinearThresholdUnit = {
    getBaseLTU
  }

}
