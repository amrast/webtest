package de.unisaarland.st.webtest.base

import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

import com.google.common.base.Stopwatch.createStarted
import org.slf4j.{Logger, LoggerFactory}


/**
 * Add this trait to your class to facilitate using info, debug, etc.
 */
trait Logging { self: Logging =>

  val logger: Logger = LoggerFactory.getLogger(getClass)

  implicit val implicitLogger = Some(this)

  protected def logRuntime[T](act: () => T, processName: String = ""): T = {
//    if (logger.isDebugEnabled()) {
//      val s = createStarted()
//      try {
//        act()
//      } finally {
//        val stop = s.stop.elapsed(TimeUnit.MILLISECONDS)
//        TryThat.trythatSilent {
//          if (stop > 60000L) {
//            logger.debug(s"${if (processName.isEmpty) "Process" else s"'$processName'"} took ${stop / 60000L} m, ${stop % 60000L} ms")
//          } else {
//            logger.debug(s"${if (processName.isEmpty) "Process" else s"'$processName'"} took $stop ms")
//          }
//        }
//      }
//    } else {
      act()
//    }
  }

}

object Logging extends Logging {

  def getLogger(name: String) = LoggerFactory.getLogger(name)

  def getLogger(`class`: Class[_]) = LoggerFactory.getLogger(`class`)

  def OSsupportsCpuTimeCheck: Boolean = {
    ManagementFactory.getThreadMXBean.isCurrentThreadCpuTimeSupported
  }
}

