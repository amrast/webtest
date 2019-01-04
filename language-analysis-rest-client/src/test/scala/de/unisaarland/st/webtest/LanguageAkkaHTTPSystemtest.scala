package de.unisaarland.st.webtest

import java.net.URI
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.base.UtilityTypes.{FutureTValidation, TValidation}
import de.unisaarland.st.webtest.client.LanguageAnalysisClient
import de.unisaarland.st.webtest.test.TestUtils
import scalaz.Validation.FlatMap.ValidationFlatMapRequested
import spray.client.pipelining._
import spray.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scalaz.{Failure, Success}

class LanguageAkkaHTTPSystemtest extends TestUtils with Logging {

  implicit private val actorSystem = ActorSystem("WordTest")

  private val pipeline = sendReceive

  private val remoteHost = new LanguageAnalysisClient(new URI("http://localhost:1234"))

  private implicit def futValToResult[T](testResult: FutureTValidation[T]): org.specs2.execute.Result = {
    val result = Await.result(testResult, FiniteDuration(10, TimeUnit.SECONDS))
    validationToResult(result)
  }

  "The server" >> {
    "is checking words" >> {
      val result = remoteHost.validWord("test")

      result.flatMap {
        case true => Success(())
        case false => Failure(new RuntimeException("'test' is supposed to be a valid word"))
      }
    }

    "is computing word similarities" >> {
        remoteHost.computeSemanticSimilarity("some", "word")
    }
  }


}
