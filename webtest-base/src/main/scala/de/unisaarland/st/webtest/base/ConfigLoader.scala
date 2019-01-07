package de.unisaarland.st.webtest.base

import java.io.File
import java.net.{URI, URL}

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation

import scala.tools.nsc.io.Path
import scalaz.{Failure, Success}

/**
  * General utility class to load typesafe configs
  */
object ConfigLoader extends Logging {

  /**
    * Loads the configuration with the given name from the current class context of the calling class.
    * @param configName config to be loaded
    * @return a configuration
    */
  def loadConfig(configName: String): TValidation[Config] = try {
    val cl = Thread.currentThread().getContextClassLoader
    logger.info(s"Loading resources from ${cl.getResource(".").getPath}")
    val file = new File(cl.getResource(configName).getFile)
    logger.debug(s"Resource is $file")

    val config = ConfigFactory.parseFile(file)
    Success(config)
  } catch {
    case t: Throwable => Failure(t)
  }

  def loadConfig(path: Path): TValidation[Config] = {
    logger.debug(s"Loading config from ${path.toAbsolute}")
    loadConfig(path.jfile)
  }

  /**
    * Loads the configuration out of the given file
    *
    * @return a new configuration object
    */
  def loadConfig(f: java.io.File): TValidation[Config] = try {
    Success(ConfigFactory.parseFile(f))
  } catch {
    case t: Throwable => Failure(t)
  }

  def loadConfig(f: scala.tools.nsc.io.File): TValidation[Config] = loadConfig(f.jfile)

  /**
    * Loads the configuration which is referenced by the given url.
    * @return a new configuration object
    */
  def loadConfig(url: URL): TValidation[Config] = {
    logger.debug(s"Loading config $url")
    loadConfig(new File(url.toURI))
  }

  /**
    * Loads the configuration which is referenced by the given uri.
    * @return a new configuration object
    */
  def loadConfig(uri: URI): TValidation[Config] = {
    logger.debug(s"Loading config $uri")
    loadConfig(new File(uri))
  }

  def loadFile(path: String): scala.tools.nsc.io.File = {
    val escaped = if (path.startsWith("~")) path.replaceFirst("~", System.getProperty("user.home")) else path
    scala.tools.nsc.io.File(escaped)
  }

  def safeGetString(key: String, config: Config): Option[String] = try {
    Option(config.getString(key))
  } catch {
    case _: com.typesafe.config.ConfigException.Missing => None
  }

  def safeGetBoolean(key: String, config: Config): Option[Boolean] = try {
    Option(config.getBoolean(key))
  } catch {
    case _: com.typesafe.config.ConfigException.Missing => None
  }

  def safeGetInt(key: String, config: Config): Option[Int] = try {
    Option(config.getInt(key))
  } catch {
    case _: com.typesafe.config.ConfigException.Missing => None
  }

  def prettyPrint(c: Config): String = {
    c.root().render(ConfigRenderOptions.concise())
  }

  def getConfig(configBaseName: String) = {
    val DEFAULT_RUNTIME_ENVIRONMENT = "dev"

    val customConfPath = Option(System.getProperty(s"webmate.$configBaseName.confPath"))
    customConfPath foreach (d => logger.info(s"Using custom configuration path $d (via sys property)"))

    val customEnvvarConfPath = Option(System.getenv("WEBMATE_%s_CONFPATH".format(configBaseName.toUpperCase)))
    customEnvvarConfPath foreach (d => logger.info(s"Using custom configuration path $d (via env variable)"))

    val appHomeDir = Option(System.getProperty(s"webmate.$configBaseName.appHomeDir"))
    appHomeDir foreach (d => logger.info(s"Using app home directory $d."))

    val runtimeEnvironment = Option(System.getenv("WEBMATE_%s_ENV".format(configBaseName.toUpperCase))).getOrElse(DEFAULT_RUNTIME_ENVIRONMENT)
    logger.info(s"Using runtime environment $runtimeEnvironment.")

    val configPaths = List(
      customEnvvarConfPath,
      customConfPath,
      appHomeDir map ("%s/conf/%s_%s.conf".format(_, configBaseName, runtimeEnvironment)),
      appHomeDir map ("%s/conf/%s.conf".format(_, configBaseName))).flatten

    var config = ConfigFactory.empty()
    configPaths foreach { path =>
    {
      val lessSpecificConfig = ConfigFactory.parseFile(new File(path))
      config = config.withFallback(lessSpecificConfig)
    }
    }
    config.withFallback(ConfigFactory.load())
  }

}
