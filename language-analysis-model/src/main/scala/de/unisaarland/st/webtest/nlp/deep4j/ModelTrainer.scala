package de.unisaarland.st.webtest.nlp.deep4j

import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.documentiterator.DocumentIterator
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}

import scala.tools.nsc.io.File

object ModelTrainer extends Logging {

  def generateModel(sentenceFile: File): Word2Vec = {
    logger.debug(s"Loading Word2Vec from ${sentenceFile.path}")
    val iter = new LineSentenceIterator(sentenceFile.jfile)
    val vec: Word2Vec = DefaultWordVector().build(iter)
    logger.debug("Training model")
    vec.fit()
    vec
  }

  def saveModel(w2v: Word2Vec, output: File): TValidation[Unit] = scalaz.Success(WordVectorSerializer.writeFullModel(w2v, output.path))

  def readModel(modelFile: File): TValidation[Word2Vec] = scalaz.Success(WordVectorSerializer.loadFullModel(modelFile.path))

}

import org.deeplearning4j.text.sentenceiterator.SentenceIterator

case class DefaultWordVector(minWordFrequency: Int = 5, iterations: Int = 1, layersize: Int = 100, seed: Option[Long] = Some(42L), windowSize: Int = 5, tokenizerFactoryOpt: Option[TokenizerFactory] = None) {

  private val tokenizerFactory = tokenizerFactoryOpt.getOrElse {
    val t = new DefaultTokenizerFactory()
    t.setTokenPreProcessor(new CommonPreprocessor())
    t
  }

  private def build: Word2Vec.Builder = {
    val builder = new Word2Vec.Builder().minWordFrequency(minWordFrequency).iterations(iterations).layerSize(layersize).windowSize(windowSize).tokenizerFactory(tokenizerFactory)
    seed.foreach(builder.seed)
    builder
  }

  def build(iter: SentenceIterator): Word2Vec = build.iterate(iter).build()

  def build(iter: DocumentIterator): Word2Vec = build.iterate(iter).build()

}