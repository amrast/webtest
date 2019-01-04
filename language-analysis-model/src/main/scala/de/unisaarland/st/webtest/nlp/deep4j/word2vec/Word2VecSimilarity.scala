package de.unisaarland.st.webtest.nlp.deep4j.word2vec

import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.nlp.word2vec.util.Util
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.util.SetUtils
import org.nd4j.linalg.api.ndarray.{BaseNDArray, INDArray}
import org.nd4j.linalg.api.ops.impl.accum.distances.CosineSimilarity

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._



/**
  * Semantic similarity score
  * on whether 2 articles are the same.
  * The cut off number is going to be low.
  * Experimentation is usually going to be required
  * to find the right cut off point for whether an article is "similar" or not
  * The more data you train on for word2vec the more accurate the classifier is.
  * for whether 2 articles are similar.
  *
  * This is based on a few factors: one is
  * the neural word embeddings from word2vec
  *
  * The other is the percent of words that intersect in the article.
  *
  * This comes out with a similarity score measured by the distance in the
  * word2vec vector space are from each other alongside content overlapping in
  * the articles.
  *
  * @author Adam Gibson
  *
  */
class Word2VecSimilarity(words1: String, words2: String, vec: Word2Vec) extends Logging {

  private var distance: Option[Double] = Option.empty[Double]

  private def shorten(str: String): String = str.substring(0, math.min(100, str.length))

  private def calc: Double = {

    logger.trace(s"Checking '${shorten(words1)}' vs. '${shorten(words2)}'")

    //calculate word frequencies
    val d1: WordMetaData = new WordMetaData(vec, words1)
    val d2: WordMetaData = new WordMetaData(vec, words2)
    d1.calc()
    d2.calc()
    //all the words occurring in each article
    val vocab = SetUtils.union(d1.getWordCounts.keySet, d2.getWordCounts.keySet)
    //remove stop words

    val words = vec.getStopWords.asScala

    val w = scala.collection.mutable.ListBuffer.empty[String]
    for (x <- words) w.append(x)



    val remove = vocab.toSet.filter(word => Util.matchesAnyStopWord(w.asJava, word))

    vocab.removeAll(remove)

    val inter = SetUtils.intersection(d1.getWordCounts.keySet, d2.getWordCounts.keySet)
    inter.removeAll(remove)
    //words to be processed: need indexing

    val wordList = vocab.toSeq

    /*the word embeddings (each row is a word)*/
    val a1Matrix: BaseNDArray = new ExtendedNDArray(wordList.size, 300)
    val a2Matrix: BaseNDArray = new ExtendedNDArray(wordList.size, 300)


    for((word, i) <- wordList.zipWithIndex) {
      val matrix = vec.getWordVectorMatrix(word)
      if (d1.getWordCounts.getCount(word) > 0) a1Matrix.putRow(i, matrix)
        else a1Matrix.putRow(i, ZeroNDArray(300))
      if (d2.getWordCounts.getCount(word) > 0) a2Matrix.putRow(i, matrix)
        else a2Matrix.putRow(i, ZeroNDArray(300))
    }


    //percent of words that overlap
    val wordSim: Double = inter.size.toDouble / wordList.size.toDouble
    //cosine similarity of the word embeddings * percent of words that overlap (this is a weight to add a decision boundary)
    val finalScore: Double = Word2VecSimilarity.cosineSim(a1Matrix, a2Matrix, wordSim)
    //threshold is >= 0.05 for any articles that are similar
    distance = Option(finalScore)
    finalScore
  }

  def getDistance: Double = distance.getOrElse(calc)
}

object Word2VecSimilarity {

  def cosineSim(matrix1: INDArray, matrix2: INDArray, wordSim: Double): Double = {
    val similarity = new CosineSimilarity(matrix1, matrix2)
    similarity.exec()
    similarity.getAndSetFinalResult(wordSim)
  }

}


class ExtendedNDArray(newRows: Int, newColumns: Int) extends BaseNDArray(newRows, newColumns) {
  override def unsafeDuplication(): INDArray = ???
}

case class ZeroNDArray(newRows: Int) extends BaseNDArray(Array.ofDim[Double](newRows, 1)) {
    override def unsafeDuplication(): INDArray = ???
}