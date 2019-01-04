package de.unisaarland.st.webtest.client

import java.net.URI
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import de.unisaarland.st.webtest.api.commands.{CalculateTopicsCommand, TextSimiliarityCommand}
import de.unisaarland.st.webtest.api.{LanguageAnalyzer, LanguageProbability, TopicModel}
import de.unisaarland.st.webtest.base.UtilityTypes.{FutureTValidation, TValidation}
import de.unisaarland.st.webtest.base.{ConfigLoader, TryThat}
import play.api.libs.json.{Json, Reads}
import spray.client.pipelining._
import spray.http._

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scalaz.{Failure, Success}


class LanguageAnalysisClient(remoteHost: URI) extends LanguageAnalyzer with AutoCloseable {

  private val c = ConfigLoader.getConfig("application.conf")//.getOrElse(throw new RuntimeException("Could not load config"))

  implicit private val actorSystem = ActorSystem("Word2VecHttp", c)
  import actorSystem.dispatcher

  private val pipeline = sendReceive

  /**
    * Extracts the language of a given element structure and returns a certainty factor.
    */
  def getLanguageProbabilities(e: String): TValidation[LanguageProbability] = {
    defaultHandleJsonResponse(Post(remote("/word2vec/language"), e))
  }

  /**
    * Compute the semantic text similiarity of two given elements and their substructures.
    */
  def computeSemanticSimilarity(e1: String, e2: String): TValidation[Double] = {
    defaultHandleJsonResponse(Post(remote("/word2vec/textsimilarity"), TextSimiliarityCommand(e1, e2).toJson.toString()))
  }

  /**
    * Extracts the list of main topics present in the text of the given element.
    * @param str text structure to be analyzed
    * @param numTopics number of topics extracted for the given text
    * @return Set of topics
    */
  def getTopicsForString(str: String, numTopics: Int = 5, numWords: Int = 5): TValidation[Seq[TopicModel]] = {
    defaultHandleJsonResponse(Post(remote("/word2vec/topics"), CalculateTopicsCommand(str, numTopics, numWords).toJson.toString()))
  }

  def computeSemanticWordSimilarity(word1: String, word2: String): TValidation[Double] = {
    defaultHandleJsonResponse(Get(remote("/word2vec/wordsimilarity").withQuery("word1" -> word1 ,"word2" -> word2)))
  }

  /**
    * Checks if the word exists. Typically checks if the corpus contains the word.
    */
  def validWord(str: String): TValidation[Boolean] = TryThat.protect {
    defaultHandleJsonResponse(Get(Uri(s"$remoteHost/word2vec/corpus").withQuery("word" -> str)))
  }

  def getCorpus: TValidation[Set[String]] = TryThat.protect {
    defaultHandleJsonResponse[Set[String]](Get(remote("/word2vec/words")))
  }

  def getWordVector(word: String): TValidation[Seq[Double]] = TryThat.protect {
    defaultHandleJsonResponse(Get(remote("/word2vec/wordvector").withQuery("word" -> word)))
  }


  private def defaultHandleJsonResponse[T](request: HttpRequest, duration: FiniteDuration = FiniteDuration(120, TimeUnit.SECONDS))(implicit jsonReads: Reads[T]): TValidation[T] = TryThat.protect {
    Await.result(execute(request){ response =>
      Success(Json.parse(response.entity.asString).as[T])
    }, duration)
  }

  private def execute[T](request: HttpRequest)(handle: (HttpResponse => TValidation[T])): FutureTValidation[T] = {
    pipeline(request).map { response =>
      if (response.status.isFailure) {
        Failure(new RuntimeException(s"Received failure status ${response.status} : ${response.entity.asString(HttpCharsets.`UTF-8`)}"))
      } else {
        handle(response)
      }
    }
  }

  private def remote(path: String): Uri = Uri(s"$remoteHost$path")

  override def close() = actorSystem.terminate()
}
