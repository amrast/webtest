package de.unisaarland.st.webtest.monitor

import java.util.{Timer, TimerTask}
import java.util.concurrent.TimeUnit

import de.unisaarland.st.webtest.base.{Logging, TryThat}

import scala.collection.parallel.immutable.ParMap
import scala.collection.parallel.mutable.ParHashSet
import scala.concurrent.duration.FiniteDuration
import scalaz.Success

class KeepAliveMonitor[T, S](results: ParHashSet[T], isError: T => Boolean, groupBy: T => S) {

  private val timer = new Timer()

  private var oldStatistics = ParMap.empty[S, Int]

  private val periodLength = FiniteDuration(10, TimeUnit.SECONDS)

  private var oldSuccess = 0
  private var oldFailures = 0



  private def getUpdates: ParMap[S, Int] = {
    val newResults = results.filter(isError).groupBy(groupBy).mapValues(_.size)
    val updates = newResults.map{ case (filter, value) => filter -> (value - oldStatistics.getOrElse(filter, 0))}.filterNot(_._2 == 0)

    oldStatistics = newResults.toMap
    updates.toMap
  }

  private def getSuccessFailureNumbers: (Int, Int) = {
    val total = results.size
    val errors = results.count(isError)

    (total - errors, errors)
  }

  private def getSFUpdate: (Int, Int) = {
    val numbers = getSuccessFailureNumbers

    val result = (numbers._1 - oldSuccess, numbers._2 - oldFailures)

    oldSuccess = numbers._1
    oldFailures = numbers._2

    result

  }

  class AliveNotifier extends TimerTask with Logging {

    override def run(): Unit = TryThat.protectShort {
      if (!Thread.currentThread().isInterrupted) {
        val update = getSFUpdate
        val updates = Seq(s"Success: ${update._1.toDouble / periodLength.toSeconds.toDouble}/s", s"Failures: ${update._2.toDouble / periodLength.toSeconds.toDouble}/s") ++
          getUpdates.map { case (filter, value) => s"$filter ${value.toDouble / periodLength.toSeconds.toDouble}/s" }
        logger.info(updates.mkString(", "))
        Success(())
      } else {
        Success(())
      }
    }
  }


  def cancel(): Unit = timer.cancel()


  timer.schedule(new AliveNotifier, 5000, periodLength.toMillis)


}
