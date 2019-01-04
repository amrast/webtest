package de.unisaarland.st.webtest.nlp.mallet

import java.util.regex.Pattern

import cc.mallet.pipe._
import cc.mallet.pipe.iterator.StringArrayIterator
import cc.mallet.topics.ParallelTopicModel
import cc.mallet.types.{FeatureSequence, Instance, InstanceList}
import de.unisaarland.st.webtest.api.{Topic, TopicDescription, TopicModel}
import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.nlp.mallet.pipe.{LineBreakRemover, TokenSequenceEnglishLemma}
import de.unisaarland.st.webtest.api.Topic.topicOrdering
import de.unisaarland.st.webtest.api.default.StopWords

import scala.collection.JavaConverters.{asScalaIteratorConverter, seqAsJavaListConverter}
import scala.tools.nsc.io.File

class TopicModelExtractor(pipeList: Seq[Pipe], val stopwords: Set[String]) extends Logging {
  System.setProperty("java.util.logging.config.class", Logging.getClass.getName)


  def createTopicModel(str: Seq[String], numTopics: Int, numWords: Int, alpha_t: Double = 0.01, beta_w: Double = 0.01, numIterations: Int = 50, numThreads: Int = 1): Seq[TopicModel] = {

    val documents = new InstanceList(new SerialPipes(pipeList.asJava))
    documents.addThruPipe(new StringArrayIterator(str.toArray))

    // Create a model with numTopics topics, alpha_t = 0.01, beta_w = 0.01
    //  Note that the first parameter is passed as the sum over topics, while
    //  the second is the parameter for a single dimension of the Dirichlet prior.
    val model = new ParallelTopicModel(numTopics, numTopics * alpha_t, beta_w)
    model.setNumThreads(numThreads)
    model.setNumIterations(numIterations)
    model.addInstances(documents)
    model.estimate()

    // The data alphabet maps word IDs to strings
    val dataAlphabet = documents.getDataAlphabet

    for ((_, index) <- str.zipWithIndex) yield {

      logger.info(s"Working on $index document")

//      val tokens = model.getData.get(index).instance.getData.asInstanceOf[FeatureSequence]
//      val topics = model.getData.get(index).topicSequence

//      logger.debug(s"Tokens ${tokens.getLength}, topics ${topics.size()}")

//      for (position <- (0 until tokens.getLength).view) {
//        print("%s-%d".format(dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
//          topics.getIndexAtPosition(position)))
//      }
//      println(" ")
//
//      model.getData.get(index).topicSequence.iterator().asScala(x => x.)
//      println(.topicSequence)

      // Estimate the topic distribution of the instance, given the current Gibbs state.
      val topicDistribution = model.getTopicProbabilities(index)

      // Get an array of sorted sets of word ID/count pairs
      val topicSortedWords = model.getSortedWords

      //Show top numwords words in topics with proportions
      val topics = for (i <- 0 until numTopics) yield {

        val topicDescription = topicSortedWords.get(i).iterator().asScala.take(numWords).map( idCountPair =>
          TopicDescription(dataAlphabet.lookupObject(idCountPair.getID).toString, idCountPair.getWeight)
        )

        Topic(topicDescription.toSeq, topicDistribution(i))
      }

      new TopicModel(topics)


    }
  }

}

object TopicModelExtractor {


  def apply(stopwords: Seq[String], loadDefaultStopWords: Boolean): TopicModelExtractor = {
    val pipeList = scala.collection.mutable.ListBuffer.empty[Pipe]
    pipeList.append( new LineBreakRemover())
    pipeList.append( new CharSequenceLowercase())
    pipeList.append( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")))

    val removeStopWordsPipe = if (loadDefaultStopWords) new TokenSequenceRemoveStopwords(false, false)
    else {
      val tempFile = java.io.File.createTempFile("temp-file-name", ".tmp")
      tempFile.deleteOnExit()
      new TokenSequenceRemoveStopwords(tempFile, "UTF-8", false, false, false)
    }

    removeStopWordsPipe.addStopWords(stopwords.toArray)
    pipeList.append(removeStopWordsPipe)
    pipeList.append( new TokenSequenceEnglishLemma())
    pipeList.append( new TokenSequence2FeatureSequence())

    val sws = if (loadDefaultStopWords) StopWords.DEFAULT_STOPWORDS ++ stopwords.toSet else stopwords.toSet

    new TopicModelExtractor(pipeList, sws)

  }

  def apply(stopwordFile: File): TopicModelExtractor = {
    val pipeList = scala.collection.mutable.ListBuffer.empty[Pipe]
    pipeList.append( new LineBreakRemover())
    pipeList.append( new CharSequenceLowercase())
    pipeList.append( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")))
    pipeList.append( new TokenSequenceRemoveStopwords(stopwordFile.jfile, "UTF-8", false, false, false))
    pipeList.append( new TokenSequenceEnglishLemma())
    pipeList.append( new TokenSequence2FeatureSequence())
    new TopicModelExtractor(pipeList, stopwordFile.lines().toSet)
  }



}
