/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent
import edu.illinois.cs.cogcomp.lbjava.learn.{ SparseAveragedPerceptron, SparseNetworkLearner }
import edu.illinois.cs.cogcomp.saul.classifier.Learnable

object ChunkerClassifiers {
  import ChunkerDataModel._

  object ChunkerClassifier extends Learnable[Constituent](tokens) {

    override lazy val classifier = {
      // Parameters
      val params = new SparseAveragedPerceptron.Parameters()
      params.learningRate = 0.1
      params.thickness = 0.2
      val baseLTU = new SparseAveragedPerceptron(params)

      new SparseNetworkLearner(baseLTU)
    }

    /** Label property for users classifier */
    override def label = chunkLabel

    override def feature = using(wordTypeInformation, affixes, posWindow)
  }
}
