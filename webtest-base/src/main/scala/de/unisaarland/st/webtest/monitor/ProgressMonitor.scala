package de.unisaarland.st.webtest.monitor

trait ProgressMonitor {

  def reportProgress(percent: Double, taskIdentifier: String = ""): Unit

  def reportProgressTotal(taskSolved: Int, totalTasks: Int, taskIdentifier: String = ""): Unit

  def reportDone(taskIdentifier: String = ""): Unit

}

trait ProgressMonitorFactory {


  def getProgressMonitor: ProgressMonitor = {
    val id = Thread.currentThread().getId
    getProgressMonitorForThread(id)
  }

  protected def getProgressMonitorForThread(id: Long): ProgressMonitor

}


