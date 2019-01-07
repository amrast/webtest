package de.unisaarland.st.webtest.test

import java.net.{ServerSocket, SocketException, URL}

import de.unisaarland.st.webtest.base.{Logging, TryWithResource}
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import org.openqa.selenium.server.{RemoteControlConfiguration, SeleniumServer}

import scalaz.{Failure, Success}

trait SeleniumEnvironment extends org.specs2.mutable.After with Logging {

  private var started = false
  private val MIN_PORT = RemoteControlConfiguration.DEFAULT_PORT
  private val MAX_PORT = 60000

  private val r = scala.util.Random

  val MAX_TRIES: Int = 10

  private val portOpt = (for (_ <- (0 until MAX_TRIES).view) yield { MIN_PORT + r.nextInt(MAX_PORT -  MIN_PORT + 1)}).find(isPortFree)


  private def isPortFree(port: Int): Boolean = try {
    TryWithResource(new ServerSocket(port)).to { _ =>
      true
    }
  } catch {
    case _: SocketException => false
  }


  val seleniumServer: TValidation[SeleniumServer] = try {
    portOpt match {
      case Some(port) =>
        val rcc = new RemoteControlConfiguration
        rcc.setPort(port)
        val server = new SeleniumServer(false, rcc)

        server.boot()
        server.start()
        logger.info(s"Server started at port $port")
        started = true
        Success(server)
      case None => Failure(new RuntimeException(s"Could not find unused server port in $MAX_TRIES retries"))
    }
  } catch {
    case e: Exception =>
      System.err.println("Could not create Selenium Server because of: " + e.getMessage + e.getClass)
      Failure(e)
  }

  def isRunning: Boolean = started


  override def after: Any = {
    logger.info("Stopping Selenium Server")
    seleniumServer.map(_.stop())
    logger.info("Selenium server stopped")
  }

  def getURL = seleniumServer.map(s => new URL(s"http://localhost:${s.getPort}/wd/hub"))

}
