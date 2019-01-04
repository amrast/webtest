package de.unisaarland.st.webtest.api

import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import org.apache.commons.lang3.StringEscapeUtils

import scalaz.Success

trait LanguageAnalyzer {

  /**
    * Extracts the language of a given element structure and returns a certainty factor.
    */
  def getLanguageProbabilities(e: String): TValidation[LanguageProbability]

  /**
    * Compute the semantic text similiarity of two given elements and their substructures.
    */
  def computeSemanticSimilarity(e1: String, e2: String): TValidation[Double]

  /**
    * Extracts the list of main topics present in the text of the given element.
    * @param str text structure to be analyzed
    * @param numTopics number of topics extracted for the given text
    * @return Set of topics
    */
  def getTopicsForString(str: String, numTopics: Int = 5, numWords: Int = 5): TValidation[Seq[TopicModel]]

  def computeSemanticWordSimilarity(word1: String, word2: String): TValidation[Double]

  /**
    * Escapes html charactes replaces multiple spaces and trims the string
    */
  protected def prepareString(str: String): String = {
    StringEscapeUtils.unescapeHtml4(str).trim.replaceAll("\\s+", " ")
  }

  /**
    * Checks if the word exists. Typically checks if the corpus contains the word.
    */
  def validWord(str: String): TValidation[Boolean]

  def getCorpus: TValidation[Set[String]]

  def getWordVector(word: String): TValidation[Seq[Double]]

  def getWordVector(words: Seq[String]): TValidation[Seq[(String, TValidation[Seq[Double]])]] = {
    Success(words.map { w => (w, getWordVector(w))})
  }
}
