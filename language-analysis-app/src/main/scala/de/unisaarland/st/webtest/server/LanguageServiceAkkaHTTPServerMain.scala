package de.unisaarland.st.webtest.server

import java.io.FileNotFoundException

import akka.actor.ActorSystem
import de.unisaarland.st.webtest.base.{ConfigLoader, Logging}
import de.unisaarland.st.webtest.nlp.deep4j.Deep4jLanguageAnalyzer

import scalaz.{Failure, Success}

object LanguageServiceAkkaHTTPServerMain extends App with Logging {


  val config = args.length match {
    case 0 =>
      logger.info("No external configuration given. Falling back to default")
      Success(ConfigLoader.getConfig("languageanalysis"))
    case 1 =>
      val configFile = scala.tools.nsc.io.File(args.head)
      if (configFile.exists) {
        ConfigLoader.loadConfig(configFile)
      } else {
        Failure(new FileNotFoundException(s"Config-file ${args.head} does not exist"))
      }
    case _ =>
      Failure(new RuntimeException("Illegal startup command. Usage: java -jar $jarname [ConfigFile]"))

  }

  config.map { c =>

    implicit val actorSystem = ActorSystem("webmate-languageanalysis-app")

    val appConfig = c.getObject("languageanalysis-app").toConfig

    val analyzer = logRuntime(() => Deep4jLanguageAnalyzer(ConfigLoader.loadFile(appConfig.getString("language.word2vec.googleVector"))), "Loading word space")

    if (appConfig.hasPath("interface")){

      new LanguageServiceServer(1234, analyzer, appConfig.getString("interface"))
    } else {
      new LanguageServiceServer(1234, analyzer)
    }
  }


}
