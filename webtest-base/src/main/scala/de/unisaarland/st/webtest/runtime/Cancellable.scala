package de.unisaarland.st.webtest.runtime

import java.util.concurrent.{Callable, FutureTask}

import de.unisaarland.st.webtest.base.UtilityTypes.TValidation

import scala.concurrent._
import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import scalaz.Failure

class Cancellable[T](executionContext: ExecutionContext, todo: => T) {
  private val promise = Promise[T]()

  def future = promise.future

  private val jf: FutureTask[T] = new FutureTask[T](
    new Callable[T] {
      override def call(): T = todo
    }
  ) {
    override def done() = promise.complete(Try(get()))
  }

  def cancel(): Boolean = {
    jf.cancel(true)
  }

  executionContext.execute(jf)
}

object Cancellable {
  private def apply[T](todo: => T)(implicit executionContext: ExecutionContext): Cancellable[T] =
    new Cancellable[T](executionContext, todo)

  def await[T](todo : => TValidation[T], duration: FiniteDuration)(implicit ex: ExecutionContext): TValidation[T] = {
    val cancellable = new Cancellable[TValidation[T]](ex, todo)

    try {
      Await.result(cancellable.future, duration)
    } catch {
      case t: TimeoutException =>
        if (!cancellable.cancel())
          {Failure(new RuntimeException("Could not cancel task"))}
        else {
          Failure(t)
        }
    }
  }
}