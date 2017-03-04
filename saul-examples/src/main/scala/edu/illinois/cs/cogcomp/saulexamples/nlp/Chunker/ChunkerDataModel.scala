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
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers
import edu.illinois.cs.cogcomp.edison.features.lrec.{ Affixes, POSandPositionWindowThree }
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.saulexamples.nlp.CommonSensors

import scala.collection.mutable
import scala.collection.JavaConversions._

object ChunkerDataModel extends DataModel {
  val sentence = node[Sentence]
  val tokens = node[Constituent]

  val sentenceToTokens = edge(sentence, tokens)
  sentenceToTokens.addSensor(CommonSensors.sentenceToTokens _)

  // GOLD BIO label for SHALLOW_PARSE
  val chunkLabel = property(tokens, "ChunkLabel") { token: Constituent =>
    token.getTextAnnotation
      .getView(ChunkerConstants.SHALLOW_PARSE_GOLD_BIO_VIEW)
      .getConstituentsCovering(token)
      .head
      .getLabel
  }

  /**
    * Get Tokens from the same sentence in the context.
    *
    * @param token
    * @param contextSize
    * @return
    */
  def getTokensInContext(token: Constituent, contextSize: Int): Seq[(Constituent, Int)] = {
    val currentSentence = (tokens(token) ~> -sentenceToTokens).head
    val sentenceTokens = (sentence(currentSentence) ~> sentenceToTokens).toList.sortBy(_.getStartSpan)
    val currentTokenPos = sentenceTokens.indexOf(token)

    // Get tokens in a context
    sentenceTokens.zipWithIndex
      .slice(currentTokenPos - contextSize, currentTokenPos + contextSize)
      .map({ case (cons: Constituent, index: Int) => (cons, index - currentTokenPos) })
  }

  // Affixes feature
  private val affixFeatureExtractor = new Affixes(ViewNames.TOKENS)
  val affixes = property(tokens, "Affixes") { token: Constituent =>
    affixFeatureExtractor.getFeatures(token)
      .map(_.getName)
      .toList
  }

  // WordTypeInformation feature
  val wordTypeInformation = property(tokens, "WordTypeInformation") { token: Constituent =>
    getTokensInContext(token, 2).flatMap({ case (cons: Constituent, contextPos: Int) =>
      val surfaceForm = cons.getSurfaceForm
      val featureBuffer = new mutable.ArrayBuffer[String](3)

      // All Capitalized
      if (surfaceForm.forall(Character.isUpperCase)) {
        featureBuffer.append(s"CAP_$contextPos")
      }

      // All Digits
      if (surfaceForm.forall(Character.isDigit)) {
        featureBuffer.append(s"digit_$contextPos")
      }

      // All Non-Digits
      if (surfaceForm.forall(x => !Character.isLetter(x))) {
        featureBuffer.append(s"nonLetter_$contextPos")
      }

      featureBuffer.toList
    }).toList
  }

  // POS Window features
  private val posWindowExtractor = new POSandPositionWindowThree(ViewNames.POS)
  val posWindow = property(tokens, "POSWindow") { token: Constituent =>
    posWindowExtractor.getFeatures(token)
      .map(_.getName)
      .toList
  }

  // Capitalization features
  val capitalizationWindowProperty = property(tokens, "Capitalization") { token: Constituent =>
    getTokensInContext(token, 2).flatMap({ case (cons: Constituent, contextPos: Int) =>
      if (WordHelpers.isCapitalized(cons.getTextAnnotation, cons.getStartSpan)) {
        Some(s"CAP_$contextPos")
      } else {
        None
      }
    }).toList
  }

  // Get Previous Chunk labels
  val previousTags = property(tokens, "PreviousTags", cache = true) { token: Constituent =>
    getTokensInContext(token, 2).takeWhile(_._2 < 0)
      .map({
        case (cons: Constituent, contextPos: Int) =>
          // Use Label while training and prediction while testing.
          if (ChunkerClassifiers.ChunkerClassifier.isTraining) {
            s"${contextPos}_${chunkLabel(cons)}"
          } else {
            s"${contextPos}_${ChunkerClassifiers.ChunkerClassifier(cons)}"
          }
      }).toList
  }

  // Get surface forms in context window
  private val formsExtractor = new ContextFeatureExtractor(2, true, true, WordFeatureExtractorFactory.wordCase)
  val forms = property(tokens, "Forms") { token: Constituent =>
    formsExtractor.getFeatures(token).map(_.getName).toList
  }

  // Filter to restrict window to current sentence's tokens only.
  val sameSentenceTokensFilter = Seq({ token: Constituent => tokens(token) ~> -sentenceToTokens })

  // Formpp Feature
  val formpp = property(tokens, "Formpp") { token: Constituent =>
    val window = 2
    val contextBuffer = new mutable.ArrayBuffer[String]()

    val surfaceForms: List[String] = tokens.getWithWindow(token, -window, window, sameSentenceTokensFilter)
      .flatten
      .map(_.getSurfaceForm)

    for {
      j <- 0 until window
      i <- surfaceForms.indices
    } {
      val contextStrings = for {
        context <- 0 until window
        if i + context < surfaceForms.length
      } contextBuffer.append(s"${i}_${j}:${surfaceForms(i + context)}")
    }

    contextBuffer.toList
  }

  // Mixed Feature
  private val mixedBefore = 2
  private val mixedAfter = 2
  private val mixedK = 2
  val mixed = property(tokens, "Mixed") { token: Constituent =>
    val tokenNeighborhood = tokens.getWithWindow(token, -mixedBefore, mixedAfter, sameSentenceTokensFilter).flatten

    val tags = new mutable.ArrayBuffer[String](mixedBefore + mixedAfter + 1)
    val forms = new mutable.ArrayBuffer[String](mixedBefore + mixedAfter + 1)

    tokenNeighborhood.foreach({ tokenNear: Constituent =>
      tags.append(CommonSensors.getPosTag(tokenNear))
      forms.append(tokenNear.getSurfaceForm)
    })

    val mixedFeatures = new mutable.ArrayBuffer[String]()

    for {
      j <- 1 to mixedK
      x <- 0 to 2
    } {
      var t: Boolean = true
      tags.zipWithIndex
        .foreach({ case (tag: String, i: Int) =>
            val stringBuffer = new StringBuffer()

            for {
              context <- 0 until j
              if i + context < tags.size
            } {
              if (context != 0) stringBuffer.append("_")

              if (t && x == 0) {
                stringBuffer.append(tags(i + context))
              } else {
                stringBuffer.append(forms(i + context))
              }

              t = !t
            }

            mixedFeatures.append(s"${i}_${j}:${stringBuffer.toString}")
        })
    }

    mixedFeatures.toList
  }

  // SO Previous Feature
  val SOPrevious = property(tokens, "SOPrevious", cache = true) { token: Constituent =>
    val tokenNeighborhood = tokens.getWithWindow(token, -2, -1, sameSentenceTokensFilter).flatten

    val tags = new mutable.ArrayBuffer[String](3)
    val labels = new mutable.ArrayBuffer[String](2)

    tokenNeighborhood.foreach({ tokenNear: Constituent =>
      tags.append(CommonSensors.getPosTag(tokenNear))

      // Use Label while training and prediction while testing.
      if (ChunkerClassifiers.ChunkerClassifier.isTraining) {
        labels.append(chunkLabel(tokenNear))
      } else {
        labels.append(ChunkerClassifiers.ChunkerClassifier(tokenNear))
      }
    })

    tags.append(CommonSensors.getPosTag(token))

    val features = new mutable.ArrayBuffer[String]()

    if (labels.size >= 2) {
      features.append(s"ll:${labels(0)}_${labels(1)}")
    }

    if (labels.size >= 2 && tags.size >= 3) {
      features.append(s"lt2:${labels(1)}_${tags(2)}")
    }

    if (tags.size >= 2) {
      features.append(s"lt1:${labels(0)}_${tags(1)}")
    }

    features.toList
  }
}
