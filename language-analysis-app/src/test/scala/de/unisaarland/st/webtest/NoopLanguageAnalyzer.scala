package de.unisaarland.st.webtest

import com.google.common.annotations.VisibleForTesting
import de.unisaarland.st.webtest.api.{English_US, LanguageAnalyzer, LanguageProbability}

import scala.collection.mutable
import scalaz.{Failure, Success}

/**
  * This language analyzer is for testing only. Designed for checking akka connections
  */
@VisibleForTesting
class NoopLanguageAnalyzer extends LanguageAnalyzer {

  private val wordVectors = mutable.Map.empty[String, Seq[Double]]

  /**
    * Extracts the language of a given element structure and returns a certainty factor.
    */
  override def getLanguageProbabilities(e: String) = {
    e match {
      case "test" => Success(LanguageProbability(English_US, 1.0))
      case _ => Failure(new NotImplementedError(s"Does not support testing $e"))
    }
  }

  /**
    * Compute the semantic text similiarity of two given elements and their substructures.
    */
  override def computeSemanticSimilarity(e1: String, e2: String) = {
    Success(scala.math.random)
  }

  /**
    * Extracts the list of main topics present in the text of the given element.
    *
    * @param str       text structure to be analyzed
    * @param numTopics number of topics extracted for the given text
    * @return Set of topics
    */
  override def getTopicsForString(str: String, numTopics: Int, numWords: Int) = Success(Seq())

  override def computeSemanticWordSimilarity(word1: String, word2: String) = {
    Success(scala.math.random)
  }

  private val corpus = Set("test", "something", "math", "random")

  /**
    * Checks if the word exists. Typically checks if the corpus contains the word.
    */
  override def validWord(str: String) = {
    Success(corpus.contains(str))
  }

  override def getCorpus = Success(corpus)

  override def getWordVector(word: String) = {
    Success(wordVectors.getOrElseUpdate(word, for (_ <- 0 to 9) yield scala.math.random))
  }

}
