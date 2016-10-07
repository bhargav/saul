/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saulexamples.nlp.Chunker

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, Sentence }
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory
import edu.illinois.cs.cogcomp.edison.features.lrec.{ Affixes, POSWindow, WordTypeInformation }
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel

import scala.collection.JavaConversions._

object ChunkerDataModel extends DataModel {
  val sentence = node[Sentence]
  val tokens = node[Constituent]

  val sentenceToTokens = edge(sentence, tokens)
  sentenceToTokens.addSensor(ChunkerSensors.getTokensInSentence _)

  // Label
  val chunkLabel = property(tokens, "ChunkLabel") { token: Constituent => token.getLabel }

  // Affixes feature
  private val affixFeatureExtractor = new Affixes(ViewNames.TOKENS)
  val affixes = property(tokens, "Affixes") { token: Constituent =>
    affixFeatureExtractor.getFeatures(token)
      .map(_.getName)
      .toList
  }

  // WordTypeInformation feature
  private val wordTypeInformationExtractor = new WordTypeInformation(ViewNames.TOKENS)
  val wordTypeInformation = property(tokens, "WordTypeInformation") { token: Constituent =>
    wordTypeInformationExtractor.getFeatures(token)
      .map(_.getName)
      .toList
  }

  // POS Window features
  private val posWindowExtractor = new POSWindow(ViewNames.POS)
  val posWindow = property(tokens, "POSWindow") { token: Constituent =>
    posWindowExtractor.getFeatures(token)
      .map(_.getName)
      .toList
  }

  // Capitalization features
  private val capitalizationExtractor = new ContextFeatureExtractor(2, true, true,
    WordFeatureExtractorFactory.capitalization)
  val capitalizationWindowProperty = property(tokens, "Capitalization") { token: Constituent =>
    capitalizationExtractor.getFeatures(token)
      .map(_.getName)
      .toList
  }

  // Filter to restrict window to current sentence's tokens only.
  val previousTagsFilter = Seq({ token: Constituent => tokens(token) ~> -sentenceToTokens })
  val previousTags = property(tokens, "PreviousTags", cache = true) { token: Constituent =>
    tokens.getWithWindow(token, -2, -1, previousTagsFilter)
      .flatten
      .map({ previousCons: Constituent =>
        // Use Label while training and prediction while testing.
        if (ChunkerClassifiers.ChunkerClassifier.isTraining) {
          chunkLabel(previousCons)
        } else {
          ChunkerClassifiers.ChunkerClassifier(previousCons)
        }
      })
  }
}
