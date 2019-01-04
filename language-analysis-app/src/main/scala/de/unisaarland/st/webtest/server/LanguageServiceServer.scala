package de.unisaarland.st.webtest.server

import java.net.{InetSocketAddress, Socket, URI}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import de.unisaarland.st.webtest.api.LanguageAnalyzer
import de.unisaarland.st.webtest.api.commands.{CalculateTopicsCommand, TextSimiliarityCommand}
import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.base.UtilityTypes.{FutureTValidation, TValidation}
import play.api.libs.json._
import de.unisaarland.st.webtest.api.TopicModel.topicModelFormat

import scala.concurrent.Future
import scalaz.Validation.FlatMap.ValidationFlatMapRequested
import scalaz.{Failure, Success}

class LanguageServiceServer(port: Int, service: LanguageAnalyzer, interface: String = "localhost")(implicit actor: ActorSystem) extends Logging with AutoCloseable {

  implicit val materializer = try {
    ActorMaterializer()
  } catch {
    case e: java.lang.NoSuchMethodError if e.getMessage.contains("akka.util.Helpers") =>
      throw new RuntimeException(s"It seems as you have an old akka-actor version on your class path (maybe version 2.4.3?). You need >2.4.19", e)
  }

  implicit val executionContext = actor.dispatcher



  implicit def myExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: java.lang.IllegalArgumentException =>
      extractUri { uri =>
        logger.error(s"Request to $uri could not be handled normally: ${Option(e.getCause).map(_.getMessage).getOrElse(e.getMessage)}")
        complete(HttpResponse(StatusCodes.NotFound, entity = e.getMessage))
      }
    case a =>
      extractUri { uri =>
        logger.error(s"Error handling $uri", a)
        complete(HttpResponse(StatusCodes.InternalServerError, entity = a.getMessage))
      }
  }

  val route: Route =
    pathPrefix("word2vec") {
      get {
        path("corpus") {
          parameters('word) { word =>
            completeFuture(Future(service.validWord(word).map(a => Json.toJson(a).toString())))
          }
        } ~ {
          path("words") {
            completeFuture(Future(service.getCorpus.map(a => Json.toJson(a).toString())))
          }
        } ~ {
          path("wordsimilarity") {
            parameters('word1, 'word2) { (word1, word2) =>
              completeFuture(Future(service.computeSemanticWordSimilarity(word1, word2).map(_.toString)))
            }
          }
        } ~ {
          path("wordvector") {
            parameter('word) { word =>
              completeFuture(Future(service.getWordVector(word).map(a => Json.toJson(a).toString)))
            }
          }
        }
      } ~ post {
        path("language") {
          entity(as[String]) { text =>
            val future = Future(service.getLanguageProbabilities(text).map { r => Json.toJson(r).toString() })
            completeFuture(future)
          }
        } ~
          path("textsimilarity") {
            entity(as[String]) { body =>
              val future = Future(deserialize[TextSimiliarityCommand](Json.parse(body)).flatMap { cmd =>
                service.computeSemanticSimilarity(cmd.text1, cmd.text2).map(_.toString)
              })
              completeFuture(future)
            }
          } ~ {
          path("topics") {
            entity(as[String]) { body =>
              val future = Future(deserialize[CalculateTopicsCommand](Json.parse(body)).flatMap(cmd =>
                service.getTopicsForString(cmd.str, cmd.numTopics, cmd.numWords).map(r => Json.toJson(r).toString())
              ))

              completeFuture(future)
            }
          }
        }
      }
    }
  val bindingFuture = Http().bindAndHandle(route, interface, port)
  val uri = new URI(s"http://$interface:$port")

  /**
    * Checks if the given json value can be deserialized to the given type.
    */
  def deserialize[T](json: JsValue)(implicit reads: Reads[T]): TValidation[T] = {
    json.validate[T] match {
      case JsSuccess(value, _) => Success(value)
      case JsError(errors) => Failure(JsResultException(errors))
    }
  }

  logger.info(s"Server online at $uri${System.lineSeparator()}")


  //  StdIn.readLine() // let it run until user presses return
  //  bindingFuture.flatMap(_.unbind()) // trigger unbinding from the port
  //    .onComplete(_ => actor.terminate()) // and shutdown when done

  private def completeFuture[T](fut: FutureTValidation[T])(implicit marshal: ToEntityMarshaller[T]): Route = {
    onSuccess(fut) {
      case Success(t) => complete(t)
      case Failure(e) => failWith(e)
    }
  }

  override def close() = bindingFuture.flatMap(_.unbind()).onComplete(_ => actor.terminate())

}

object LanguageServiceServer {
  def apply(port: Int, analyzer: LanguageAnalyzer): LanguageServiceServer = {
    new LanguageServiceServer(port, analyzer)(ActorSystem("webmate-languageanalysis-app"))
  }
}
