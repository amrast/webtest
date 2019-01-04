package de.unisaarland.st.webtest.nlp.deep4j.word2vec

import de.unisaarland.st.webtest.base.TryThat
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import org.deeplearning4j.models.embeddings.learning.impl.sequence.DM
import org.deeplearning4j.models.embeddings.loader.{VectorsConfiguration, WordVectorSerializer}
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors
import org.deeplearning4j.models.word2vec.{VocabWord, Word2Vec}
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.nd4j.linalg.ops.transforms.Transforms

import scala.tools.nsc.io.File
import scalaz.Success

/**
  * https://github.com/deeplearning4j/deeplearning4j/blob/38a721afcd31578395ed6acebbe47a3592a41236/deeplearning4j-nlp-parent/deeplearning4j-nlp/src/test/java/org/deeplearning4j/models/paragraphvectors/ParagraphVectorsTest.java
  * @param d2v
  */
class Paragraph2VecSimilarity(d2v: ParagraphVectors) {

  def avg_feature_vector(words: Seq[String]): Seq[String] = ???


  def computeSentenceSimilarity(str1: String, str2: String): TValidation[Double] = TryThat.protect {
    val arrayA = d2v.inferVector(str1)
    val arrayB = d2v.inferVector(str2)

    Success(Transforms.cosineSim(arrayA, arrayB))
  }

  def saveToFile(output: File): TValidation[Unit] = TryThat.protect {
    WordVectorSerializer.writeParagraphVectors(d2v, output.jfile)
    Success(())
  }

}

object Paragraph2VecSimilarity {


  def apply(paragraphVectorsFile: File): Paragraph2VecSimilarity = {
    val d2v = WordVectorSerializer.readParagraphVectors(paragraphVectorsFile.jfile)

    new Paragraph2VecSimilarity(d2v)
  }


  def apply(w2v: Word2Vec): Paragraph2VecSimilarity = {

    val configuration = new VectorsConfiguration()

    configuration.setIterations(5)
    configuration.setLearningRate(0.01)
    configuration.setUseHierarchicSoftmax(true)
    configuration.setNegative(0)

    val tokenizerFactory = new DefaultTokenizerFactory()
    tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor())

    val d2v = new ParagraphVectors.Builder(configuration)
      .useExistingWordVectors(w2v)
      .sequenceLearningAlgorithm(new DM[VocabWord]())
      .tokenizerFactory(tokenizerFactory)
      .resetModel(false)
      .build()

    new Paragraph2VecSimilarity(d2v)
  }

}
