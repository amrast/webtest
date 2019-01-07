package de.unisaarland.st.webtest.monitor

import com.google.common.util.concurrent.AtomicDouble
import de.unisaarland.st.webtest.base.math.MathUtils
import org.slf4j.Logger


class TaskSizeBasedConsoleBasedProgressMonitor(taskSize: Int, desc: Option[String] = None, minDelta: Double = 0.01)(logger: Logger) {

  private val tasks = new AtomicDouble(if (taskSize < 1) 1 else taskSize)
  private val completed = new AtomicDouble(0)

  private val taskDescription = desc.map(s => s" '$s'").getOrElse("")

  private val lastReportedProgress = new AtomicDouble(0)

  def changeTotalTaskSize(delta: Int): Double = tasks.addAndGet(delta)

  /**
    * Notifies progress on executed tasks.
    */
  def notifyTaskStart(): Unit = {
    val current = completed.addAndGet(1) / tasks.get()
    val lastProgress = lastReportedProgress.get
    val progress = math.abs(lastProgress - current)
    if (progress > minDelta && lastReportedProgress.compareAndSet(lastProgress, current)) {
      val p = (MathUtils.round(current, 2) * 100).toInt
      logger.debug(s"Task $taskDescription reported $p% progress [${completed.get().toInt} / ${tasks.get().toInt}]")
    }
  }

}
