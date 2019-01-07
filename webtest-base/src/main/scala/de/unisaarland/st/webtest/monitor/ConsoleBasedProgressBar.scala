package de.unisaarland.st.webtest.monitor

import com.google.common.util.concurrent.AtomicDouble
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.parallel.mutable


class ConsoleBasedProgressBar(logging: Logger, minimalProgressLimit: Double = 0.01) extends ProgressMonitor {

  private val old = new AtomicDouble(0.0)

  override def reportProgress(percent: Double, taskIdentifier: String = ""): Unit = {

    if (percent - old.get() > minimalProgressLimit) {
      val p = BigDecimal(percent).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      old.set(percent)
      logging.info(s"Task${if (taskIdentifier.nonEmpty) s" '$taskIdentifier'" else ""} reported ${(p*100).toInt}% progress")
    }
  }

  override def reportDone(taskIdentifier: String): Unit = reportProgress(1)

  override def reportProgressTotal(taskSolved: Int, totalTasks: Int, taskIdentifier: String = ""): Unit = {
    val percent = BigDecimal(taskSolved.toDouble / totalTasks.toDouble).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

    if (percent - old.get() > minimalProgressLimit) {
      old.set(percent)
      logging.info(s"Task${if (taskIdentifier.nonEmpty) s" '$taskIdentifier" else ""} reported ${(percent*100).toInt}% progress. [$taskSolved/$totalTasks]")

    }
  }
}

object ConsoleBasedProgressBarFactory extends ProgressMonitorFactory {

  private val bars = new mutable.ParHashMap[Long, ConsoleBasedProgressBar]()

  override protected def getProgressMonitorForThread(id: Long): ProgressMonitor = {
    bars.get(id) match {
      case Some(x) => x
      case None =>
        val logger = LoggerFactory.getLogger("ConsoleMonitor")
        val bar = new ConsoleBasedProgressBar(logger)
        bars.put(id, bar)
        bar
    }
  }

}
