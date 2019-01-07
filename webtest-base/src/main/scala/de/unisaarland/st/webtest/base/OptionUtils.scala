package de.unisaarland.st.webtest.base

import java.util.Optional

import de.unisaarland.st.webtest.base.UtilityTypes.{FutureTValidation, TValidation}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scalaz.{Failure, Success}

/**
 * Conversions between Scala Option and Java 8 Optional.
 */
object JavaOptionals {
  implicit def toRichOption[T](opt: Option[T]): RichOption[T] = new RichOption[T](opt)
  implicit def toRichOptional[T](optional: Optional[T]): RichOptional[T] = new RichOptional[T](optional)
}

class RichOption[T] (opt: Option[T]) {

  /**
   * Transform this Option to an equivalent Java Optional
   */
  def toOptional: Optional[T] = Optional.ofNullable(opt.getOrElse(null).asInstanceOf[T])
}

class RichOptional[T] (opt: Optional[T]) {

  /**
   * Transform this Optional to an equivalent Scala Option
   */
  def toOption: Option[T] = if (opt.isPresent) Some(opt.get()) else None
}

object TryThat {

  def trythat[T](body: => T)(implicit optLogger: Option[Logging] = None): TValidation[T] = {
    try {
      Success(body)
    } catch {
      case t: Throwable =>
        optLogger foreach (_.logger.error("An exception has been thrown in a trythat code block: %s".format(t.getMessage), t))
        Failure(t)
    }
  }

  def trythatSilent[T](body: => T)(implicit optLogger: Option[Logging] = None): TValidation[T] = {
    try {
      Success(body)
    } catch {
      case t: Throwable =>
        Failure(t)
    }
  }

  def protect[T](body: => TValidation[T])(implicit optLogger: Option[Logging] = None): TValidation[T] = {
    try {
      val result = body
      result
    } catch {
      case t: Throwable =>
        optLogger foreach (_.logger.error("An exception has been thrown in a protected code block: %s".format(t.getMessage), t))
        Failure(t)
    }
  }

  def protectShort[T](body: => TValidation[T])(implicit optLogger: Option[Logging] = None): TValidation[T] = {
    try {
      val result = body
      result
    } catch {
      case t: Throwable =>
        optLogger foreach (_.logger.error(s"Exception occured in protected code block: ${t.getClass.getSimpleName} - '${t.getMessage}'"))
        Failure(t)
    }
  }

  /**
    * This method executes the given body and re-executes it, if an error occurs.
    *
    * HANDLE WITH CARE: There is no rollback for partially executed previous bodies.
    */
  def protectRetry[T](body: => TValidation[T], retries: Int = 1)(implicit optLogger: Option[Logging] = None): TValidation[T] = {
    if (retries > 0) {
      body match {
        case Success(x) => Success(x)
        case Failure(e) if retries > 0 =>
          optLogger foreach (_.logger.error(s"An exception has been thrown in a protected code block. Repeating ${retries - 1} times: ${e.getMessage}", e))
          protectRetry(body, retries - 1)
        case Failure(e) =>
          optLogger foreach (_.logger.error(s"An exception has been thrown in a protected code block. Giving up: ${e.getMessage}", e))
          Failure(e)
      }
    } else {
      Failure(new RuntimeException("Failed to execute method after retrying"))
    }
  }

  def protectSilent[T](body: => TValidation[T])(implicit optLogger: Option[Logging] = None): TValidation[T] = {
    try {
      body
    } catch {
      case t: Throwable =>
        Failure(t)
    }
  }

  /**
    * Recovers all Future failures and transforms them to Validation failures.
    */
  def wrapFutureErrors[T](action: => FutureTValidation[T])(implicit ec: ExecutionContext): FutureTValidation[T] = {
    action recoverWith {
      case e: Throwable => Future.successful(Failure(new RuntimeException(s"Execution failed: ${e.getMessage}", e)))
    }
  }
}

class TryWithResource [A <: AutoCloseable](resource: A) {

  def to[B](block: A => B) = {
    var t: Throwable = null
    try {
      block(resource)
    } catch {
      case x: Throwable => t = x; throw x
    } finally {
      if (resource != null) {
        if (t != null) {
          try {
            resource.close()
          } catch {
            case y: Throwable => t.addSuppressed(y)
          }
        } else {
          resource.close()
        }
      }
    }
  }
}

object TryWithResource {
  def tryWithResource[A <: AutoCloseable](resource: A) = new TryWithResource(resource)

  def apply[A <: AutoCloseable](r: A) = new TryWithResource(r)
}