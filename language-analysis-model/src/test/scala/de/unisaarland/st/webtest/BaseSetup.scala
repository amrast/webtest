package de.unisaarland.st.webtest

import com.typesafe.config.Config
import de.unisaarland.st.webtest.base.{ConfigLoader, Logging}
import de.unisaarland.st.webtest.nlp.deep4j.Deep4jLanguageAnalyzer
import scalaz.Validation.FlatMap.ValidationFlatMapRequested

case class BaseSetup() extends Logging {
  val analyzer = ConfigLoader.loadConfig(getClass.getResource("language-test.conf").toURI).flatMap { config: Config =>
    val file = ConfigLoader.loadFile(config.getString("language.word2vec.googleVector"))
    val stopWords = ConfigLoader.loadFile("")

    logger.debug(s"Loading google language news database from ${file.path}")
    scalaz.Success(Deep4jLanguageAnalyzer(file))

  }
}