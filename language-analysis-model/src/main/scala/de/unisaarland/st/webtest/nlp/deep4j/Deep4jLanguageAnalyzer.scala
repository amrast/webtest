package de.unisaarland.st.webtest.nlp.deep4j

import de.unisaarland.st.webtest.api.{LanguageAnalyzer, LanguageProbability, TopicModel}
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import de.unisaarland.st.webtest.base.utils.Tokenizer
import de.unisaarland.st.webtest.base.{Logging, TryThat}
import de.unisaarland.st.webtest.nlp.SimilarityMatrix
import de.unisaarland.st.webtest.nlp.deep4j.word2vec.Paragraph2VecSimilarity
import de.unisaarland.st.webtest.nlp.languagedetect.LanguageDetector
import de.unisaarland.st.webtest.nlp.mallet.TopicModelExtractor
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.slf4j.LoggerFactory

import scala.tools.nsc.io.File
import scalaz.{Failure, Success}
import scalaz.Validation.FlatMap.ValidationFlatMapRequested

class Deep4jLanguageAnalyzer(word2Vec: Word2Vec, topicAnalzyer: TopicModelExtractor) extends LanguageAnalyzer with Logging {

  private val detector: LanguageDetector = new LanguageDetector
  /**
    * Extracts the language of a given element structure and returns a certainty factor.
    */
  override def getLanguageProbabilities(e: String): TValidation[LanguageProbability] = TryThat.protect {
    detector.detectLanguageProbabilities(e).map(_.head).map{ result => LanguageProbability(result._1, result._2)}
  }

  /**
    * Compute the semantic text similiarity of two given elements and their substructures.
    */
  override def computeSemanticSimilarity(str1: String, str2: String): TValidation[Double] = TryThat.protectShort {
    val first = prepareString(str1)
    val snd = prepareString(str2)
    if (first.isEmpty || snd.isEmpty) {
      Success(0.0)
    } else {
      val s1 = Tokenizer.tokenize(str1).map(toLowerCaseIfPossible)
      val s2 = Tokenizer.tokenize(str2).map(toLowerCaseIfPossible)
      val split1 = s1.filter(validWord(_).toOption.getOrElse(false))
      val split2 = s2.filter(validWord(_).toOption.getOrElse(false))

      if (logger.isTraceEnabled && s1.lengthCompare(split1.length) > 0) logger.trace(s"Removed ${s1.length - split1.length} unknown words: ${(s1 diff split1).mkString(", ")}")
      if (logger.isTraceEnabled && s2.lengthCompare(split2.length) > 0) logger.trace(s"Removed ${s2.length - split2.length} unknown words: ${(s2 diff split2).mkString(", ")}")

      val matrix = SimilarityMatrix(split1, split2, this)
      if (matrix.height == 0 || matrix.width == 0) {
        Success(0.0)
      } else {
        val path = matrix.getMaxPath
        val calc = matrix.getSum(path) / math.min(matrix.width, matrix.height)
        logger.trace(s"Semantic similarity is $calc between '${first.substring(0, math.min(first.length, 100))}' and '${snd.substring(math.min(snd.length, 100))}'")
        Success(calc)
      }
    }
  }

  override def getTopicsForString(str: String, numTopics: Int, numWords: Int): TValidation[Seq[TopicModel]] = TryThat.protect {
    Success(topicAnalzyer.createTopicModel(Seq(prepareString(str)), numTopics, numWords))
  }

  def computeSemanticWordSimilarity(word1: String, word2: String): TValidation[Double] = TryThat.protect {
    val similarity = word2Vec.similarity(prepareString(word1), prepareString(word2))
    logger.trace(s"Similarity between $word1 and $word2 is $similarity")
    Success(similarity)
  }

  def generateParagraph2Vec: TValidation[Paragraph2VecSimilarity] = {
    scalaz.Success(Paragraph2VecSimilarity(word2Vec))
  }


  /**
    * Checks that the given word-string is contained in the corpus.
    */
  def validWord(str: String): TValidation[Boolean] = TryThat.protectShort {
    Success(word2Vec.getVocab.containsWord(str))
  }

  /**
    * Transforms the given string to lowercase, if the lower-case word is in the corpus.
    */
  private def toLowerCaseIfPossible(str: String): String = {
    val lc = str.toLowerCase
    if (validWord(lc).getOrElse(false)) lc else str
  }

  import scala.collection.JavaConverters.collectionAsScalaIterableConverter

  def getCorpus: TValidation[Set[String]] = Success(word2Vec.getVocab.words().asScala.toSet)

  def getWordVector(word: String): TValidation[Seq[Double]] = {
    validWord(word).flatMap {
      case true => Success(word2Vec.getWordVector(word))
      case false => Failure(new IllegalArgumentException(s"Word '$word' is not in corpus"))
    }
  }

}

object Deep4jLanguageAnalyzer {

  private val logger = LoggerFactory.getLogger(Deep4jLanguageAnalyzer.getClass)

  /**
    * Loads the given pre-evaluated wordvector file and an empty topic analysis feature
    */
  def apply(googleNewsFile: File): Deep4jLanguageAnalyzer = {

    logger.debug(s"Loading google language news data vector from ${googleNewsFile.path}")
    //noinspection ScalaDeprecation
    val word2Vec = WordVectorSerializer.loadGoogleModel(googleNewsFile.jfile, true)
    logger.debug(s"Loading complete")
    new Deep4jLanguageAnalyzer(word2Vec.asInstanceOf[Word2Vec], TopicModelExtractor(Seq(), loadDefaultStopWords = true))
  }

  def apply(file: File, useGoogle: Boolean): Deep4jLanguageAnalyzer = {
    //noinspection ScalaDeprecation
    val word2Vec = WordVectorSerializer.readWord2Vec(file.jfile)
    new Deep4jLanguageAnalyzer(word2Vec, TopicModelExtractor(Seq(), loadDefaultStopWords = true))
  }
}
