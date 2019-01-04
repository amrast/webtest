package de.unisaarland.st.webtest.nlp.deep4j.word2vec

import java.util.StringTokenizer

import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.berkeley.Counter
import org.nd4j.linalg.api.ndarray.INDArray

import scala.collection.mutable.ListBuffer


class WordMetaData(vec: Word2Vec, words: String) {

    val wordCounts = new Counter[String]
    val wordList = new ListBuffer[String]

    def getVectorForWord(word: String): INDArray = vec.getWordVectorMatrix(word).mul(wordCounts.getCount(word))

    private def addWords(words: String) {
      val t1: StringTokenizer = new StringTokenizer(words)
      while (t1.hasMoreTokens) {
        val next: String = t1.nextToken
        if (!wordList.contains(next) && vec.hasWord(next)) wordList.append(next)
        if (vec.hasWord(next)) wordCounts.incrementCount(next, 1.0)
      }
    }

    def calc() {
      addWords(words)
    }

    def getWords: String = words

    def getWordCounts: Counter[String] = wordCounts

    def getWordList: Seq[String] = wordList

    def getVec: Word2Vec = vec

}
